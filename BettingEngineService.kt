package com.example.util

import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.Bet
import com.example.data.model.UserWallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlaceBetRequest(
    val userId: Int,
    val matchId: Int,
    val marketId: Int? = null,
    val selection: String,
    val expectedOdds: Double,
    val stake: Double,
    val maxSlippageAllowed: Double
)

data class BettingEngineResult(
    val success: Boolean,
    val message: String,
    val betId: Int? = null
)

class BettingEngineService(private val db: AppDatabase) {

    /**
     * Processes a new bet request atomically.
     * Uses isolation layers via Room's transaction to prevent race conditions on user wallet balances.
     */
    suspend fun placeSingleBet(request: PlaceBetRequest): BettingEngineResult = withContext(Dispatchers.IO) {
        val userId = request.userId
        val matchId = request.matchId
        val selection = request.selection
        val expectedOdds = request.expectedOdds
        val stake = request.stake
        val maxSlippageAllowed = request.maxSlippageAllowed

        // 1. Basic validation triage
        if (stake <= 0) {
            return@withContext BettingEngineResult(false, "Stake must be greater than 0 ETB.")
        }

        try {
            // Begin an isolated transaction
            return@withContext db.runInTransaction<BettingEngineResult> {
                val walletDao = db.walletDao()
                val matchDao = db.matchDao()
                val betDao = db.betDao()

                // 2. Lock the user's wallet for update
                val currentWallet = walletDao.getWalletByIdDirect(userId)
                    ?: return@runInTransaction BettingEngineResult(false, "Wallet not found.")

                if (currentWallet.balance < stake) {
                    return@runInTransaction BettingEngineResult(false, "Insufficient balance in wallet.")
                }

                // 3. Check live match status and specific odds line validity
                val matchEntity = matchDao.getMatchEntityByIdDirect(matchId)
                val sportMatch = matchDao.getMatchByIdDirect(matchId)

                if (matchEntity == null && sportMatch == null) {
                    return@runInTransaction BettingEngineResult(false, "The selected betting market no longer exists.")
                }

                val status = matchEntity?.status ?: (if (sportMatch?.status == "FINISHED") "Completed" else "Live")

                // Block bets on finished or suspended events
                if (status == "Completed" || status == "Cancelled") {
                    return@runInTransaction BettingEngineResult(false, "This match has already concluded.")
                }

                // Determine the market type matching the selection
                val marketType = when (selection) {
                    "1", "X", "2" -> "1X2"
                    "Over 2.5", "Under 2.5" -> "Over/Under"
                    "BTTS Yes", "BTTS No" -> "BTTS"
                    else -> "1X2"
                }

                val marketEntity = matchDao.getMarketByMatchAndTypeDirect(matchId, marketType)

                // Retrieve the latest current live odds
                val currentLiveOdds = if (marketEntity != null) {
                    when (marketType) {
                        "1X2" -> when (selection) {
                            "1" -> marketEntity.homeOdds
                            "X" -> marketEntity.drawOdds ?: 1.0
                            "2" -> marketEntity.awayOdds
                            else -> 1.0
                        }
                        "Over/Under" -> when (selection) {
                            "Over 2.5" -> marketEntity.homeOdds
                            "Under 2.5" -> marketEntity.awayOdds
                            else -> 1.0
                        }
                        "BTTS" -> when (selection) {
                            "BTTS Yes" -> marketEntity.homeOdds
                            "BTTS No" -> marketEntity.awayOdds
                            else -> 1.0
                        }
                        else -> 1.0
                    }
                } else {
                    sportMatch?.getOddsForSelection(selection) ?: 1.0
                }

                // Block bets on suspended markets
                val isSuspended = currentLiveOdds <= 1.01
                if (isSuspended) {
                    return@runInTransaction BettingEngineResult(false, "This market is currently suspended.")
                }

                // 4. Odds Slippage Guard Logic
                val oddsDifference = expectedOdds - currentLiveOdds
                if (oddsDifference > maxSlippageAllowed) {
                    return@runInTransaction BettingEngineResult(
                        false,
                        "Odds shifted from ${String.format("%.2f", expectedOdds)} to ${String.format("%.2f", currentLiveOdds)}. Please confirm and try again."
                    )
                }

                // 5. Atomic Execution: Deduct balance & log the active bet slip
                val updatedWallet = currentWallet.copy(
                    balance = currentWallet.balance - stake
                )
                walletDao.insertWalletDirect(updatedWallet)

                val bet = Bet(
                    userId = userId,
                    matchId = matchId,
                    marketType = marketType,
                    selection = selection,
                    odds = currentLiveOdds,
                    stake = stake,
                    potentialReturn = stake * currentLiveOdds,
                    status = "PENDING",
                    sport = sportMatch?.sport ?: matchEntity?.sport ?: "Football",
                    teamA = sportMatch?.teamA ?: matchEntity?.homeTeam ?: "Home Team",
                    teamB = sportMatch?.teamB ?: matchEntity?.awayTeam ?: "Away Team"
                )

                val generatedBetId = betDao.insertBetDirect(bet)

                // ⚡ WebSocket Event Broadcast - mirrors Node.js adminSocketHubInstance
                val teamAStr = sportMatch?.teamA ?: matchEntity?.homeTeam ?: "Home Team"
                val teamBStr = sportMatch?.teamB ?: matchEntity?.awayTeam ?: "Away Team"
                val socketBet = AdminSocketHubInstance.BroadcastBet(
                    id = "#$generatedBetId",
                    user = "User$userId",
                    match = "$matchId Context ($teamAStr vs $teamBStr)",
                    market = "$marketType - $selection",
                    stake = String.format(java.util.Locale.US, "%.2f", stake),
                    possibleWin = String.format(java.util.Locale.US, "%.2f", stake * currentLiveOdds)
                )
                AdminSocketHubInstance.broadcastNewBet(socketBet)

                Log.d("BettingEngineService", "[BET PLACED] Ticket #$generatedBetId approved for User $userId. Stake: $stake ETB")
                BettingEngineResult(
                    success = true,
                    message = "Bet successfully accepted.",
                    betId = generatedBetId.toInt()
                )
            }
        } catch (e: Exception) {
            Log.e("BettingEngineService", "[BET ENGINE CRITICAL ERROR]: ${e.message}", e)
            BettingEngineResult(false, "A technical error occurred while locking your slip.")
        }
    }

    suspend fun placeMultiBet(
        userId: Int = 1,
        items: List<com.example.data.model.BetItem>,
        stake: Double
    ): BettingEngineResult = withContext(Dispatchers.IO) {
        if (stake <= 0) {
            return@withContext BettingEngineResult(false, "Stake must be greater than 0 ETB.")
        }
        if (items.isEmpty()) {
            return@withContext BettingEngineResult(false, "Selection contains no matches.")
        }

        // 1. Conflict checking and Multi-match unique constraint check
        val conflictCheck = validateAccumulatorConflicts(items)
        if (conflictCheck != null) {
            return@withContext conflictCheck
        }

        // 2. Minimum Legs checking (An accumulator must contain at least 2 selections)
        if (items.size < 2) {
            return@withContext BettingEngineResult(
                false,
                "Accumulator bets require at least 2 distinct match selections."
            )
        }

        try {
            return@withContext db.runInTransaction<BettingEngineResult> {
                val walletDao = db.walletDao()
                val matchDao = db.matchDao()
                val betDao = db.betDao()

                // Lock wallet
                val currentWallet = walletDao.getWalletByIdDirect(userId)
                    ?: return@runInTransaction BettingEngineResult(false, "Wallet not found.")

                if (currentWallet.balance < stake) {
                    return@runInTransaction BettingEngineResult(false, "Insufficient balance in wallet.")
                }

                // Verify and compute final combined odds
                var accumulatedOdds = 1.0
                val processedItems = mutableListOf<com.example.data.model.BetItem>()

                for (item in items) {
                    val matchId = item.matchId
                    val selection = item.selection

                    val matchEntity = matchDao.getMatchEntityByIdDirect(matchId)
                    val sportMatch = matchDao.getMatchByIdDirect(matchId)

                    if (matchEntity == null && sportMatch == null) {
                        return@runInTransaction BettingEngineResult(false, "Selected match $matchId no longer exists.")
                    }

                    val status = matchEntity?.status ?: (if (sportMatch?.status == "FINISHED") "Completed" else "Live")
                    if (status == "Completed" || status == "Cancelled") {
                        return@runInTransaction BettingEngineResult(false, "Match ${item.teamA} vs ${item.teamB} has already concluded.")
                    }

                    val marketType = item.marketType
                    val marketEntity = matchDao.getMarketByMatchAndTypeDirect(matchId, marketType)

                    val currentLiveOdds = if (marketEntity != null) {
                        when (marketType) {
                            "1X2" -> when (selection) {
                                "1" -> marketEntity.homeOdds
                                "X" -> marketEntity.drawOdds ?: 1.0
                                "2" -> marketEntity.awayOdds
                                else -> 1.0
                            }
                            "Over/Under" -> when (selection) {
                                "Over 2.5" -> marketEntity.homeOdds
                                "Under 2.5" -> marketEntity.awayOdds
                                else -> 1.0
                            }
                            "BTTS" -> when (selection) {
                                "BTTS Yes" -> marketEntity.homeOdds
                                "BTTS No" -> marketEntity.awayOdds
                                else -> 1.0
                            }
                            else -> 1.0
                        }
                    } else {
                        sportMatch?.getOddsForSelection(selection) ?: 1.0
                    }

                    if (currentLiveOdds <= 1.01) {
                        return@runInTransaction BettingEngineResult(false, "The market for ${item.teamA} vs ${item.teamB} is suspended.")
                    }

                    accumulatedOdds *= currentLiveOdds
                    processedItems.add(item.copy(odds = currentLiveOdds))
                }

                // Deduct balance
                val updatedWallet = currentWallet.copy(
                    balance = currentWallet.balance - stake
                )
                walletDao.insertWalletDirect(updatedWallet)

                // Create combined Bet Slip (Combo/Parlay)
                val primaryItem = processedItems.first()
                val subItemsJson = com.example.data.model.serializeBetItems(processedItems)

                val bet = Bet(
                    userId = userId,
                    matchId = primaryItem.matchId,
                    marketType = if (processedItems.size > 1) "COMBO" else primaryItem.marketType,
                    selection = if (processedItems.size > 1) "${processedItems.size} Selections" else primaryItem.selection,
                    odds = accumulatedOdds,
                    stake = stake,
                    potentialReturn = stake * accumulatedOdds,
                    status = "PENDING",
                    sport = if (processedItems.size > 1) "Multibet" else primaryItem.sport,
                    teamA = if (processedItems.size > 1) "Multiple Matches (${processedItems.size})" else primaryItem.teamA,
                    teamB = if (processedItems.size > 1) "Accumulator Slips" else primaryItem.teamB,
                    subItemsJson = subItemsJson
                )

                val generatedBetId = betDao.insertBetDirect(bet)

                // WebSocket Update
                val socketBet = AdminSocketHubInstance.BroadcastBet(
                    id = "#$generatedBetId",
                    user = "User$userId",
                    match = bet.teamA + " - " + bet.teamB,
                    market = bet.marketType + " - " + bet.selection,
                    stake = String.format(java.util.Locale.US, "%.2f", stake),
                    possibleWin = String.format(java.util.Locale.US, "%.2f", stake * accumulatedOdds)
                )
                AdminSocketHubInstance.broadcastNewBet(socketBet)

                BettingEngineResult(
                    success = true,
                    message = "Accumulator wager accepted.",
                    betId = generatedBetId.toInt()
                )
            }
        } catch (e: Exception) {
            Log.e("BettingEngineService", "[BET ENGINE MULTI ERROR]: ${e.message}", e)
            BettingEngineResult(false, "A technical error occurred while placing accumulator slip.")
        }
    }

    /**
     * Checks for conflicting markets within the same accumulator ticket.
     * E.g., cannot bet on both teams to win the same game, or both Over and Under on the same game, etc.
     */
    fun validateAccumulatorConflicts(items: List<com.example.data.model.BetItem>): BettingEngineResult? {
        val matchesGrouped = items.groupBy { it.matchId }
        for ((matchId, selections) in matchesGrouped) {
            if (selections.size > 1) {
                // Check for direct conflicting winners (different outcomes of 1X2 market)
                val winnerSelections = selections.filter { it.marketType == "1X2" }.map { it.selection }
                if (winnerSelections.size > 1) {
                    val names = winnerSelections.joinToString(" and ")
                    return BettingEngineResult(
                        false,
                        "Conflicting market validation failed: Cannot bet on multiple outcomes for the same Match (e.g., $names)."
                    )
                }

                // Check for Over/Under conflict
                val ouSelections = selections.filter { it.marketType == "Over/Under" }.map { it.selection }
                if (ouSelections.size > 1) {
                    return BettingEngineResult(
                        false,
                        "Conflicting market validation failed: Cannot select both Over and Under outcomes for the same game."
                    )
                }

                // Check for BTTS conflict
                val bttsSelections = selections.filter { it.marketType == "BTTS" }.map { it.selection }
                if (bttsSelections.size > 1) {
                    return BettingEngineResult(
                        false,
                        "Conflicting market validation failed: Cannot select both BTTS Yes and BTTS No for the same game."
                    )
                }

                // General fallback for any multiple selections under the same match (dependent events)
                val teams = "${selections.first().teamA} vs ${selections.first().teamB}"
                return BettingEngineResult(
                    false,
                    "Conflicting market check: Multiple selections found for $teams. Accumulators only permit one selection per match to prevent dependent/conflicting market combinations."
                )
            }
        }
        return null
    }

    /**
     * Alias for placeMultiBet to support explicit multi-leg/accumulator betting requests cleanly.
     */
    suspend fun placeMultiLegBet(
        userId: Int = 1,
        items: List<com.example.data.model.BetItem>,
        stake: Double
    ): BettingEngineResult {
        return placeMultiBet(userId, items, stake)
    }
}
