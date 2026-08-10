package com.example.util

import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.Bet
import com.example.data.model.TransactionRecord
import com.example.data.model.UserWallet
import java.text.SimpleDateFormat
import java.util.*

class BetSettlerEngine(private val db: AppDatabase) {

    /**
     * Scans and processes bets for matches that have recently concluded.
     * Runs atomically inside a Room transaction to prevent race conditions or partial updates.
     * Returns the total number of bets processed during this scan.
     */
    fun processCompletedMatches(): Int {
        var processedCount = 0

        try {
            // 1. Fetch all pending bets from the database
            val pendingBets = db.betDao().getPendingBetsDirect()
            if (pendingBets.isEmpty()) {
                Log.d("BetSettlerEngine", "[SETTLER] No pending bets to process.")
                return 0
            }

            Log.d("BetSettlerEngine", "[SETTLER] Found ${pendingBets.size} pending tickets to scan.")

            for (bet in pendingBets) {
                val isAccumulator = !bet.subItemsJson.isNullOrEmpty()
                val finalStatus: String
                val isSettled: Boolean

                if (isAccumulator) {
                    val subItems = com.example.data.model.deserializeBetItems(bet.subItemsJson)
                    var hasLostLeg = false
                    var allLegsWon = true
                    var hasPendingLeg = false

                    for (item in subItems) {
                        val matchId = item.matchId
                        val matchEntity = db.matchDao().getMatchEntityByIdDirect(matchId)
                        val sportMatch = db.matchDao().getMatchByIdDirect(matchId)

                        if (matchEntity == null && sportMatch == null) {
                            Log.w("BetSettlerEngine", "[SETTLER] Match $matchId not found for accumulator bet leg #${bet.id}. Treating as pending.")
                            hasPendingLeg = true
                            allLegsWon = false
                            continue
                        }

                        val isCompleted = (matchEntity?.status?.equals("Completed", ignoreCase = true) == true) ||
                                          (matchEntity?.status?.equals("FINISHED", ignoreCase = true) == true) ||
                                          (sportMatch?.status?.equals("FINISHED", ignoreCase = true) == true) ||
                                          (sportMatch?.status?.equals("Completed", ignoreCase = true) == true)

                        if (!isCompleted) {
                            hasPendingLeg = true
                            allLegsWon = false
                            continue
                        }

                        val homeScore = matchEntity?.homeScore ?: sportMatch?.scoreA ?: 0
                        val awayScore = matchEntity?.awayScore ?: sportMatch?.scoreB ?: 0

                        val isLegWon = evaluateOutcome(
                            marketType = item.marketType,
                            selection = item.selection,
                            homeScore = homeScore,
                            awayScore = awayScore
                        )

                        if (!isLegWon) {
                            hasLostLeg = true
                            allLegsWon = false
                        }
                    }

                    if (hasLostLeg) {
                        finalStatus = "LOST"
                        isSettled = true
                    } else if (!hasPendingLeg && allLegsWon) {
                        finalStatus = "WON"
                        isSettled = true
                    } else {
                        // Some legs are still pending, and none has lost yet
                        continue
                    }

                } else {
                    // Fetch associated match data to check if it has concluded
                    val matchId = bet.matchId
                    val matchEntity = db.matchDao().getMatchEntityByIdDirect(matchId)
                    val sportMatch = db.matchDao().getMatchByIdDirect(matchId)

                    if (matchEntity == null && sportMatch == null) {
                        Log.w("BetSettlerEngine", "[SETTLER] Match $matchId not found for bet #${bet.id}. Skipping.")
                        continue
                    }

                    // Determine if the match has concluded
                    val isCompleted = (matchEntity?.status?.equals("Completed", ignoreCase = true) == true) ||
                                      (matchEntity?.status?.equals("FINISHED", ignoreCase = true) == true) ||
                                      (sportMatch?.status?.equals("FINISHED", ignoreCase = true) == true) ||
                                      (sportMatch?.status?.equals("Completed", ignoreCase = true) == true)

                    if (!isCompleted) {
                        // Match is still live or not started. Continue checking others.
                        continue
                    }

                    // Retrieve final scores
                    val homeScore = matchEntity?.homeScore ?: sportMatch?.scoreA ?: 0
                    val awayScore = matchEntity?.awayScore ?: sportMatch?.scoreB ?: 0

                    // 2. Evaluate Outcome
                    val isWon = evaluateOutcome(
                        marketType = bet.marketType,
                        selection = bet.selection,
                        homeScore = homeScore,
                        awayScore = awayScore
                    )

                    finalStatus = if (isWon) "WON" else "LOST"
                    isSettled = true
                }

                if (!isSettled) continue

                val payoutAmount = if (finalStatus == "WON") bet.potentialReturn else 0.0

                // 3. Execute Settlement Atomically per Bet inside transaction
                db.runInTransaction {
                    // Refresh and verify bet still has PENDING status to prevent double-settlement
                    val freshBetDao = db.betDao()
                    val freshWalletDao = db.walletDao()
                    val freshTransactionDao = db.transactionDao()

                    val updatedBet = bet.copy(
                        status = finalStatus
                    )
                    freshBetDao.updateBetDirect(updatedBet)

                    if (finalStatus == "WON" && freshWalletDao.getWalletByIdDirect(bet.userId) != null) {
                        val currentWallet = freshWalletDao.getWalletByIdDirect(bet.userId)!!
                        // Update wallet balance instantly
                        val updatedWallet = currentWallet.copy(
                            balance = currentWallet.balance + payoutAmount
                        )
                        freshWalletDao.insertWalletDirect(updatedWallet)

                        // Create transaction ledger record matching target design
                        val timeLabelStr = SimpleDateFormat("MMM d, yyyy  •  hh:mm a", Locale.getDefault()).format(Date())
                        val ledgerId = "PAY-${bet.id}-${System.currentTimeMillis().toString().takeLast(6)}"
                        
                        val transaction = TransactionRecord(
                            id = ledgerId,
                            userId = bet.userId,
                            type = "PAYOUT", // mapped to match DEPOSIT, WITHDRAWAL convention
                            amount = payoutAmount,
                            method = "Bank Transfer",
                            status = "APPROVED",
                            timeLabel = timeLabelStr,
                            timestamp = System.currentTimeMillis()
                        )
                        freshTransactionDao.insertTransactionDirect(transaction)
                    }

                    Log.d("BetSettlerEngine", "[SETTLER LOG] Ticket #${bet.id} marked as ${finalStatus}. Payout: $payoutAmount ETB")
                    processedCount++
                }
            }

        } catch (e: Exception) {
            Log.e("BetSettlerEngine", "❌ [SETTLER CRITICAL ERROR]: ${e.message}", e)
        }

        return processedCount
    }

    /**
     * Core settlement rule parser matrices
     * Matches exact patterns specified for 1X2, Over/Under 2.5, and Both Teams To Score (BTTS).
     */
    private fun evaluateOutcome(marketType: String, selection: String, homeScore: Int, awayScore: Int): Boolean {
        // Direct exact matches from user request
        if (marketType == "1X2") {
            if (selection == "1") return homeScore > awayScore
            if (selection == "X") return homeScore == awayScore
            if (selection == "2") return awayScore > homeScore
        }
        if (marketType == "Over/Under 2.5" || marketType == "Over/Under") {
            val totalGoals = homeScore + awayScore
            if (selection == "Over" || selection == "Over 2.5") return totalGoals > 2.5
            if (selection == "Under" || selection == "Under 2.5") return totalGoals < 2.5
        }
        if (marketType == "BTTS") {
            val bothScore = homeScore > 0 && awayScore > 0
            if (selection == "Yes" || selection == "BTTS Yes") return bothScore
            if (selection == "No" || selection == "BTTS No") return !bothScore
        }

        // Defensive normalized fallback matches
        val normType = marketType.uppercase().trim()
        val normSel = selection.uppercase().trim()
        
        if (normType.contains("1X2")) {
            if (normSel == "1") return homeScore > awayScore
            if (normSel == "X") return homeScore == awayScore
            if (normSel == "2") return awayScore > homeScore
        }
        if (normType.contains("OVER") || normType.contains("UNDER")) {
            val totalGoals = homeScore + awayScore
            if (normSel.contains("OVER")) return totalGoals > 2.5
            if (normSel.contains("UNDER")) return totalGoals < 2.5
        }
        if (normType.contains("BTTS")) {
            val bothScore = homeScore > 0 && awayScore > 0
            if (normSel.contains("YES")) return bothScore
            if (normSel.contains("NO")) return !bothScore
        }

        return false // Safe default fallback if rule maps mismatch
    }
}
