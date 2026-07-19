package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.UserWallet
import com.example.data.model.MatchEntity
import com.example.data.model.MarketEntity
import com.example.data.model.SportMatch
import com.example.data.model.Bet
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import com.example.util.NormalizedOddsUpdate

class BetRepository(private val db: AppDatabase) {

    fun getDb(): AppDatabase = db

    val wallet: Flow<UserWallet?> = db.walletDao().getWallet()
    val allMatches: Flow<List<SportMatch>> = db.matchDao().getAllMatches()
    val liveMatches: Flow<List<SportMatch>> = db.matchDao().getLiveMatches()
    val allBets: Flow<List<Bet>> = db.betDao().getAllBets()
    val allTransactions: Flow<List<TransactionRecord>> = db.transactionDao().getAllTransactions()

    // Relational table direct flows for Admin or advanced sync inspections
    val matchEntities: Flow<List<MatchEntity>> = db.matchDao().getAllMatchEntities()
    val marketEntities: Flow<List<MarketEntity>> = db.matchDao().getAllMarketEntities()

    suspend fun updateMatch(match: SportMatch) = withContext(Dispatchers.IO) {
        db.matchDao().updateMatch(match)
        
        // Also update corresponding relational table row
        val currentEntity = db.matchDao().getMatchEntityById(match.id)
        if (currentEntity != null) {
            val updatedEntity = currentEntity.copy(
                homeScore = match.scoreA,
                awayScore = match.scoreB,
                status = match.status,
                timeString = match.timeString
            )
            db.matchDao().insertMatchEntities(listOf(updatedEntity))
        }
    }

    private val bettingEngine = com.example.util.BettingEngineService(db)
    private val settlerEngine = com.example.util.BetSettlerEngine(db)

    suspend fun settleCompletedMatches(): Int = withContext(Dispatchers.IO) {
        settlerEngine.processCompletedMatches()
    }

    suspend fun placeBet(
        matchId: Int,
        selection: String,
        odds: Double,
        stake: Double,
        maxSlippageAllowed: Double = 0.05
    ): PlaceBetResult = withContext(Dispatchers.IO) {
        val result = bettingEngine.placeSingleBet(
            com.example.util.PlaceBetRequest(
                userId = 1,
                matchId = matchId,
                selection = selection,
                expectedOdds = odds,
                stake = stake,
                maxSlippageAllowed = maxSlippageAllowed
            )
        )
        if (result.success && result.betId != null) {
            val match = db.matchDao().getMatchByIdDirect(matchId)
            val bet = Bet(
                id = result.betId,
                userId = 1,
                matchId = matchId,
                marketType = when (selection) {
                    "1", "X", "2" -> "1X2"
                    "Over 2.5", "Under 2.5" -> "Over/Under"
                    "BTTS Yes", "BTTS No" -> "BTTS"
                    else -> "1X2"
                },
                selection = selection,
                odds = odds,
                stake = stake,
                potentialReturn = stake * odds,
                status = "PENDING",
                sport = match?.sport ?: "Football",
                teamA = match?.teamA ?: "Home Team",
                teamB = match?.teamB ?: "Away Team"
            )
            PlaceBetResult.Success(bet)
        } else {
            PlaceBetResult.Failure(result.message)
        }
    }

    suspend fun placeMultiBet(
        items: List<com.example.data.model.BetItem>,
        stake: Double
    ): PlaceBetResult = withContext(Dispatchers.IO) {
        val result = bettingEngine.placeMultiBet(
            userId = 1,
            items = items,
            stake = stake
        )
        if (result.success) {
            PlaceBetResult.Success(null)
        } else {
            PlaceBetResult.Failure(result.message)
        }
    }

    suspend fun placeMultiLegBet(
        items: List<com.example.data.model.BetItem>,
        stake: Double
    ): PlaceBetResult = withContext(Dispatchers.IO) {
        val result = bettingEngine.placeMultiLegBet(
            userId = 1,
            items = items,
            stake = stake
        )
        if (result.success) {
            PlaceBetResult.Success(null)
        } else {
            PlaceBetResult.Failure(result.message)
        }
    }

    suspend fun cashoutBet(betId: Bet, cashoutAmount: Double) = withContext(Dispatchers.IO) {
        val updatedBet = betId.copy(status = "CASHOUT", potentialReturn = cashoutAmount)
        db.betDao().updateBet(updatedBet)
        
        // Refund cashout amount
        val walletDao = db.walletDao()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        val updatedWallet = currentWallet.copy(balance = currentWallet.balance + cashoutAmount)
        walletDao.insertWallet(updatedWallet)
    }

    suspend fun resolveBetMock(bet: Bet, won: Boolean) = withContext(Dispatchers.IO) {
        val updatedBet = bet.copy(status = if (won) "WON" else "LOST")
        db.betDao().updateBet(updatedBet)
        
        if (won) {
            val walletDao = db.walletDao()
            val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
            val updatedWallet = currentWallet.copy(balance = currentWallet.balance + bet.potentialReturn)
            walletDao.insertWallet(updatedWallet)
        }
    }

    suspend fun approveBet(bet: Bet) = withContext(Dispatchers.IO) {
        val updatedBet = bet.copy(isApproved = true)
        db.betDao().updateBet(updatedBet)
    }

    suspend fun updateTransactionStatus(transaction: TransactionRecord, status: String) = withContext(Dispatchers.IO) {
        if (transaction.status == status) return@withContext
        val updatedTrx = transaction.copy(status = status)
        db.transactionDao().insertTransaction(updatedTrx)
        
        val walletDao = db.walletDao()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        // If we reject a withdrawal, we should refund the user's balance!
        if (status == "REJECTED" && transaction.type == "WITHDRAWAL") {
            val updatedWallet = currentWallet.copy(balance = currentWallet.balance + transaction.amount)
            walletDao.insertWallet(updatedWallet)
        } else if (status == "APPROVED" && transaction.type == "DEPOSIT") {
            val updatedWallet = currentWallet.copy(balance = currentWallet.balance + transaction.amount)
            walletDao.insertWallet(updatedWallet)
        }
    }

    suspend fun createPendingTelebirrDeposit(amount: Double): String = withContext(Dispatchers.IO) {
        val walletDao = db.walletDao()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        
        val trxId = "TX-" + (100000..999999).random().toString()
        val transaction = TransactionRecord(
            id = trxId,
            userId = currentWallet.id,
            type = "DEPOSIT",
            amount = amount,
            method = "TeleBirr",
            status = "PENDING",
            timeLabel = getCurrentTimeLabel(),
            timestamp = System.currentTimeMillis()
        )
        db.transactionDao().insertTransaction(transaction)
        trxId
    }

    suspend fun depositFunds(amount: Double, gateway: String): String = withContext(Dispatchers.IO) {
        val walletDao = db.walletDao()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        
        val updatedWallet = currentWallet.copy(
            balance = currentWallet.balance + amount
        )
        walletDao.insertWallet(updatedWallet)
        
        val trxId = "#TRX" + (10000..99999).random().toString()
        val transaction = TransactionRecord(
            id = trxId,
            userId = currentWallet.id,
            type = "DEPOSIT",
            amount = amount,
            method = gateway,
            status = "APPROVED",
            timeLabel = getCurrentTimeLabel()
        )
        db.transactionDao().insertTransaction(transaction)
        trxId
    }

    suspend fun withdrawFunds(amount: Double, gateway: String): RequestWithdrawResult = withContext(Dispatchers.IO) {
        val walletDao = db.walletDao()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        
        if (currentWallet.balance < amount) {
            return@withContext RequestWithdrawResult.Failure("Insufficient balance to request withdrawal.")
        }
        
        val updatedWallet = currentWallet.copy(
            balance = currentWallet.balance - amount
        )
        walletDao.insertWallet(updatedWallet)
        
        val trxId = "#TRX" + (10000..99999).random().toString()
        val transaction = TransactionRecord(
            id = trxId,
            userId = currentWallet.id,
            type = "WITHDRAWAL",
            amount = amount,
            method = gateway,
            status = "PENDING", // Withdrawals are pending approval initially
            timeLabel = getCurrentTimeLabel()
        )
        db.transactionDao().insertTransaction(transaction)
        RequestWithdrawResult.Success(trxId)
    }

    suspend fun updateWallet(wallet: UserWallet) = withContext(Dispatchers.IO) {
        db.walletDao().insertWallet(wallet)
    }

    suspend fun initializeDbIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-populate user / wallet matching provided Postgres SQL users schema
        val walletDao = db.walletDao()
        if (walletDao.getWalletDirect() == null) {
            walletDao.insertWallet(
                UserWallet(
                    id = 1,
                    username = "super_admin",
                    email = "mry3bcha@gmail.com",
                    passwordHash = "pbkdf2_sha256_admin_hash_9852",
                    balance = 523600.00,
                    role = "super_admin",
                    bonusBalance = 12500.00,
                    currency = "ETB"
                )
            )
        }
        
        // Pre-populate sport matches if none exist
        val initialMatches = listOf(
            // Live matches
            SportMatch(
                id = 101, sport = "Football", teamA = "Ethiopia Bunna", teamB = "St. George",
                scoreA = 2, scoreB = 1, timeString = "64'", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.65, oddsX = 3.40, odds2 = 4.80,
                oddsOver = 1.85, oddsUnder = 1.95, oddsBttsYes = 1.65, oddsBttsNo = 2.15
            ),
            SportMatch(
                id = 102, sport = "Football", teamA = "Arsenal", teamB = "Chelsea",
                scoreA = 0, scoreB = 0, timeString = "22'", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.95, oddsX = 3.10, odds2 = 3.60,
                oddsOver = 1.90, oddsUnder = 1.80, oddsBttsYes = 1.70, oddsBttsNo = 2.05
            ),
            SportMatch(
                id = 103, sport = "Football", teamA = "Real Madrid", teamB = "Bayern Munich",
                scoreA = 1, scoreB = 1, timeString = "88'", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 2.10, oddsX = 1.85, odds2 = 5.20,
                oddsOver = 1.65, oddsUnder = 2.20, oddsBttsYes = 1.55, oddsBttsNo = 2.45
            ),
            // Upcoming matches
            SportMatch(
                id = 104, sport = "Football", teamA = "Al Ahly", teamB = "Zamalek",
                scoreA = 0, scoreB = 0, timeString = "Pre-Match", isLive = false, status = "NOT_STARTED",
                dateTimeString = "Pre-Match", odds1 = 1.50, oddsX = 3.90, odds2 = 6.00,
                oddsOver = 1.75, oddsUnder = 2.10, oddsBttsYes = 1.80, oddsBttsNo = 2.00
            ),
            SportMatch(
                id = 105, sport = "Basketball", teamA = "LA Lakers", teamB = "Boston Celtics",
                scoreA = 98, scoreB = 95, timeString = "Q4 3'", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.80, oddsX = 12.0, odds2 = 2.10,
                oddsOver = 1.90, oddsUnder = 1.90, oddsBttsYes = 1.0, oddsBttsNo = 1.0
            ),
            SportMatch(
                id = 106, sport = "Tennis", teamA = "N. Djokovic", teamB = "J. Sinner",
                scoreA = 2, scoreB = 1, timeString = "Set 4", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.65, oddsX = 1.0, odds2 = 2.25,
                oddsOver = 1.90, oddsUnder = 1.90, oddsBttsYes = 1.0, oddsBttsNo = 1.0
            ),
            SportMatch(
                id = 107, sport = "Esports", teamA = "Navi", teamB = "FaZe Clan",
                scoreA = 1, scoreB = 1, timeString = "Map 3", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.70, oddsX = 1.0, odds2 = 2.15,
                oddsOver = 1.85, oddsUnder = 1.85, oddsBttsYes = 1.0, oddsBttsNo = 1.0
            )
        )
        
        val matchDao = db.matchDao()
        if (matchDao.getMatchById(101) == null) {
            matchDao.clearAllSportMatches()
            matchDao.clearAllMatchEntities()
            matchDao.clearAllMarketEntities()

            matchDao.insertMatches(initialMatches)
            
            // Populate relational matches table
            val relationalMatches = initialMatches.map {
                MatchEntity(
                    id = it.id,
                    homeTeam = it.teamA,
                    awayTeam = it.teamB,
                    startTime = when (it.id) {
                        104 -> System.currentTimeMillis() + 10 * 3600 * 1000
                        else -> System.currentTimeMillis() - 2 * 3600 * 1000 // started earlier
                    },
                    status = if (it.isLive) "Live" else "Not Started",
                    homeScore = it.scoreA,
                    awayScore = it.scoreB,
                    sport = it.sport,
                    timeString = it.timeString
                )
            }
            matchDao.insertMatchEntities(relationalMatches)

            // Populate relational markets table
            val relationalMarkets = mutableListOf<MarketEntity>()
            initialMatches.forEach { match ->
                // Add 1X2 market
                relationalMarkets.add(
                    MarketEntity(
                        matchId = match.id,
                        marketType = "1X2",
                        homeOdds = match.odds1,
                        drawOdds = if (match.oddsX > 1.0) match.oddsX else null,
                        awayOdds = match.odds2
                    )
                )
                // Add Over/Under market
                relationalMarkets.add(
                    MarketEntity(
                        matchId = match.id,
                        marketType = "Over/Under",
                        homeOdds = match.oddsOver,
                        drawOdds = null,
                        awayOdds = match.oddsUnder
                    )
                )
                // Add BTTS market for football matches
                if (match.sport == "Football") {
                    relationalMarkets.add(
                        MarketEntity(
                            matchId = match.id,
                            marketType = "BTTS",
                            homeOdds = match.oddsBttsYes,
                            drawOdds = null,
                            awayOdds = match.oddsBttsNo
                        )
                    )
                }
            }
            matchDao.insertMarketEntities(relationalMarkets)
        }

        // Transactions
        val transactionDao = db.transactionDao()
        val existingTrx = transactionDao.getAllTransactions().firstOrNull() ?: emptyList()
        if (existingTrx.isEmpty()) {
            val transactions = listOf(
                TransactionRecord("#TRX9852", 1, "DEPOSIT", 5000.0, "TeleBirr", "APPROVED", "10:42 PM", System.currentTimeMillis() - 10 * 60 * 1000),
                TransactionRecord("#TRX9851", 1, "WITHDRAWAL", 2500.0, "CBE Birr", "PENDING", "10:30 PM", System.currentTimeMillis() - 22 * 60 * 1000),
                TransactionRecord("#TRX9850", 1, "DEPOSIT", 1000.0, "TeleBirr", "APPROVED", "10:28 PM", System.currentTimeMillis() - 24 * 60 * 1000),
                TransactionRecord("#TRX9849", 1, "WITHDRAWAL", 3000.0, "TeleBirr", "REJECTED", "10:15 PM", System.currentTimeMillis() - 37 * 60 * 1000),
                TransactionRecord("#TRX9848", 1, "DEPOSIT", 2000.0, "CBE Birr", "APPROVED", "10:05 PM", System.currentTimeMillis() - 47 * 60 * 1000)
            )
            for (trx in transactions) {
                transactionDao.insertTransaction(trx)
            }
        }
    }

    suspend fun processLiveDbUpdate(update: NormalizedOddsUpdate) = withContext(Dispatchers.IO) {
        db.runInTransaction {
            val matchDao = db.matchDao()

            // 1. Fetch or initialize SportMatch to get general information (like team names)
            val sportMatch = matchDao.getMatchByIdDirect(update.matchId)

            // 2. Update the live match scoreline state metrics (MatchEntity table)
            val matchEntity = matchDao.getMatchEntityByIdDirect(update.matchId)
            val updatedMatchEntity = if (matchEntity != null) {
                matchEntity.copy(
                    homeScore = update.homeScore,
                    awayScore = update.awayScore,
                    timeString = update.matchTime,
                    status = "Live"
                )
            } else {
                MatchEntity(
                    id = update.matchId,
                    homeTeam = sportMatch?.teamA ?: "Match H #${update.matchId}",
                    awayTeam = sportMatch?.teamB ?: "Match A #${update.matchId}",
                    startTime = System.currentTimeMillis(),
                    status = "Live",
                    homeScore = update.homeScore,
                    awayScore = update.awayScore,
                    sport = sportMatch?.sport ?: "Football",
                    timeString = update.matchTime
                )
            }
            matchDao.insertMatchEntitiesDirect(listOf(updatedMatchEntity))

            // 3. Update or Insert the correct market relation (MarketEntity table)
            // Normalize raw market types to our local market keys ('1X2', 'Over/Under', 'BTTS')
            val localMarketType = when (update.marketType.lowercase()) {
                "1x2" -> "1X2"
                "over/under", "total_goals" -> "Over/Under"
                "btts" -> "BTTS"
                else -> update.marketType
            }

            val existingMarket = matchDao.getMarketByMatchAndTypeDirect(update.matchId, localMarketType)
            val updatedMarket = if (existingMarket != null) {
                when (update.selectionName) {
                    "1" -> existingMarket.copy(homeOdds = update.oddsValue)
                    "X" -> existingMarket.copy(drawOdds = update.oddsValue)
                    "2" -> existingMarket.copy(awayOdds = update.oddsValue)
                    "Over 2.5" -> existingMarket.copy(homeOdds = update.oddsValue)
                    "Under 2.5" -> existingMarket.copy(awayOdds = update.oddsValue)
                    "BTTS Yes" -> existingMarket.copy(homeOdds = update.oddsValue)
                    "BTTS No" -> existingMarket.copy(awayOdds = update.oddsValue)
                    else -> existingMarket
                }
            } else {
                // Determine pre-filled odds from current SportMatch if available
                var hOdds = update.oddsValue
                var dOdds: Double? = if (localMarketType == "1X2") (sportMatch?.oddsX ?: 3.40) else null
                var aOdds = update.oddsValue

                when (localMarketType) {
                    "1X2" -> {
                        hOdds = if (update.selectionName == "1") update.oddsValue else (sportMatch?.odds1 ?: 1.85)
                        dOdds = if (update.selectionName == "X") update.oddsValue else (sportMatch?.oddsX ?: 3.20)
                        aOdds = if (update.selectionName == "2") update.oddsValue else (sportMatch?.odds2 ?: 4.20)
                    }
                    "Over/Under" -> {
                        hOdds = if (update.selectionName == "Over 2.5") update.oddsValue else (sportMatch?.oddsOver ?: 1.90)
                        aOdds = if (update.selectionName == "Under 2.5") update.oddsValue else (sportMatch?.oddsUnder ?: 1.80)
                    }
                    "BTTS" -> {
                        hOdds = if (update.selectionName == "BTTS Yes") update.oddsValue else (sportMatch?.oddsBttsYes ?: 1.75)
                        aOdds = if (update.selectionName == "BTTS No") update.oddsValue else (sportMatch?.oddsBttsNo ?: 2.05)
                    }
                }

                MarketEntity(
                    matchId = update.matchId,
                    marketType = localMarketType,
                    homeOdds = hOdds,
                    drawOdds = dOdds,
                    awayOdds = aOdds
                )
            }
            matchDao.insertMarketEntitiesDirect(listOf(updatedMarket))

            // 4. Update denormalized SportMatch UI model representation for real-time reactivity
            if (sportMatch != null) {
                var odds1 = sportMatch.odds1
                var oddsX = sportMatch.oddsX
                var odds2 = sportMatch.odds2
                var oddsOver = sportMatch.oddsOver
                var oddsUnder = sportMatch.oddsUnder
                var oddsYes = sportMatch.oddsBttsYes
                var oddsNo = sportMatch.oddsBttsNo

                when (update.selectionName) {
                    "1" -> odds1 = update.oddsValue
                    "X" -> oddsX = update.oddsValue
                    "2" -> odds2 = update.oddsValue
                    "Over 2.5" -> oddsOver = update.oddsValue
                    "Under 2.5" -> oddsUnder = update.oddsValue
                    "BTTS Yes" -> oddsYes = update.oddsValue
                    "BTTS No" -> oddsNo = update.oddsValue
                }

                val updatedSportMatch = sportMatch.copy(
                    scoreA = update.homeScore,
                    scoreB = update.awayScore,
                    timeString = update.matchTime,
                    isLive = true,
                    status = "LIVE",
                    odds1 = odds1,
                    oddsX = oddsX,
                    odds2 = odds2,
                    oddsOver = oddsOver,
                    oddsUnder = oddsUnder,
                    oddsBttsYes = oddsYes,
                    oddsBttsNo = oddsNo
                )
                matchDao.updateMatchDirect(updatedSportMatch)
            }
        }
    }

    suspend fun concludeMatchSimulated(matchId: Int, homeScore: Int, awayScore: Int) = withContext(Dispatchers.IO) {
        db.runInTransaction {
            val matchDao = db.matchDao()
            val sportMatch = matchDao.getMatchByIdDirect(matchId)
            val matchEntity = matchDao.getMatchEntityByIdDirect(matchId)

            if (sportMatch != null) {
                val updatedSportMatch = sportMatch.copy(
                    scoreA = homeScore,
                    scoreB = awayScore,
                    isLive = false,
                    status = "FINISHED",
                    timeString = "FT"
                )
                matchDao.updateMatchDirect(updatedSportMatch)
            }

            val updatedEntity = if (matchEntity != null) {
                matchEntity.copy(
                    homeScore = homeScore,
                    awayScore = awayScore,
                    status = "Completed",
                    timeString = "Finished"
                )
            } else {
                MatchEntity(
                    id = matchId,
                    homeTeam = sportMatch?.teamA ?: "Match H #$matchId",
                    awayTeam = sportMatch?.teamB ?: "Match A #$matchId",
                    startTime = System.currentTimeMillis() - 3600 * 1000,
                    status = "Completed",
                    homeScore = homeScore,
                    awayScore = awayScore,
                    sport = sportMatch?.sport ?: "Football",
                    timeString = "Finished"
                )
            }
            matchDao.insertMatchEntitiesDirect(listOf(updatedEntity))
        }
    }

    private fun getCurrentTimeLabel(): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

sealed class PlaceBetResult {
    data class Success(val bet: Bet? = null) : PlaceBetResult()
    data class Failure(val message: String) : PlaceBetResult()
}

sealed class RequestWithdrawResult {
    data class Success(val trxId: String) : RequestWithdrawResult()
    data class Failure(val message: String) : RequestWithdrawResult()
}
