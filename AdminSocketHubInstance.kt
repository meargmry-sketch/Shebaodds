package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdminSocketHubInstance {

    // --- Existing Data Classes ---
    data class BroadcastBet(
        val id: String,
        val user: String,
        val match: String,
        val market: String,
        val stake: String,
        val possibleWin: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class BroadcastTransaction(
        val id: String,
        val user: String,
        val type: String,
        val amount: String,
        val method: String,
        val status: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class FinancialMetric(
        val totalBalance: String,
        val totalDeposits: String,
        val totalWithdrawals: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    // --- NEW: Casino Broadcast Data Class ---
    data class BroadcastCasinoPlay(
        val id: String,
        val user: String,
        val gameId: String,       // e.g., 'aviator', 'dice', 'slot'
        val gameName: String,     // e.g., 'Aviator'
        val stake: String,
        val profit: String,       // Positive for wins, negative for losses
        val outcome: String,      // 'win' or 'lose'
        val multiplier: String? = null, // Optional: for Crash/Aviator games
        val timestamp: Long = System.currentTimeMillis()
    )

    // --- Existing StateFlows ---
    private val _newBetBroadcasts = MutableStateFlow<List<BroadcastBet>>(emptyList())
    val newBetBroadcasts: StateFlow<List<BroadcastBet>> = _newBetBroadcasts.asStateFlow()

    private val _transactionBroadcasts = MutableStateFlow<List<BroadcastTransaction>>(emptyList())
    val transactionBroadcasts: StateFlow<List<BroadcastTransaction>> = _transactionBroadcasts.asStateFlow()

    private val _financialMetrics = MutableStateFlow(FinancialMetric("1257850.00", "523600.00", "186250.00"))
    val financialMetrics: StateFlow<FinancialMetric> = _financialMetrics.asStateFlow()

    // --- NEW: Casino Game Broadcast StateFlow ---
    private val _casinoGameBroadcasts = MutableStateFlow<List<BroadcastCasinoPlay>>(emptyList())
    val casinoGameBroadcasts: StateFlow<List<BroadcastCasinoPlay>> = _casinoGameBroadcasts.asStateFlow()

    // --- Existing Broadcast Functions ---
    fun broadcastNewBet(bet: BroadcastBet) {
        val current = _newBetBroadcasts.value.toMutableList()
        current.add(0, bet)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _newBetBroadcasts.value = current
        Log.d("AdminSocketHubInstance", "⚡ BROADCAST [NEW_BET] -> Client channel: ID=${bet.id}, User=${bet.user}, Match=${bet.match}, Stake=${bet.stake} ETB")
    }

    fun broadcastTransactionAlert(tx: BroadcastTransaction) {
        val current = _transactionBroadcasts.value.toMutableList()
        current.add(0, tx)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _transactionBroadcasts.value = current
        Log.d("AdminSocketHubInstance", "⚡ BROADCAST [TRANSACTION_ALERT] -> Client channel: ID=${tx.id}, User=${tx.user}, Type=${tx.type}, Amount=${tx.amount} ETB, Method=${tx.method}")
    }

    fun broadcastFinancialMetricUpdate(metric: FinancialMetric) {
        _financialMetrics.value = metric
        Log.d("AdminSocketHubInstance", "⚡ BROADCAST [FINANCIAL_METRICS] -> Stats updated: Balance=${metric.totalBalance}, Deposits=${metric.totalDeposits}, Withdrawals=${metric.totalWithdrawals}")
    }

    // --- NEW: Casino Play Broadcast Function ---
    fun broadcastCasinoPlay(gamePlay: BroadcastCasinoPlay) {
        val current = _casinoGameBroadcasts.value.toMutableList()
        current.add(0, gamePlay)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _casinoGameBroadcasts.value = current
        Log.d("AdminSocketHubInstance", "⚡ BROADCAST [CASINO_PLAY] -> Client channel: ID=${gamePlay.id}, User=${gamePlay.user}, Game=${gamePlay.gameName}, Stake=${gamePlay.stake} ETB, Profit=${gamePlay.profit}, Outcome=${gamePlay.outcome}")
    }

    fun clearBroadcasts() {
        _newBetBroadcasts.value = emptyList()
        _transactionBroadcasts.value = emptyList()
        _casinoGameBroadcasts.value = emptyList() // Clear casino list too
        _financialMetrics.value = FinancialMetric("1257850.00", "523600.00", "186250.00")
    }
}