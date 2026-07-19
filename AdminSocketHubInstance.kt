package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdminSocketHubInstance {

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

    private val _newBetBroadcasts = MutableStateFlow<List<BroadcastBet>>(emptyList())
    val newBetBroadcasts: StateFlow<List<BroadcastBet>> = _newBetBroadcasts.asStateFlow()

    private val _transactionBroadcasts = MutableStateFlow<List<BroadcastTransaction>>(emptyList())
    val transactionBroadcasts: StateFlow<List<BroadcastTransaction>> = _transactionBroadcasts.asStateFlow()

    private val _financialMetrics = MutableStateFlow(FinancialMetric("1257850.00", "523600.00", "186250.00"))
    val financialMetrics: StateFlow<FinancialMetric> = _financialMetrics.asStateFlow()

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

    fun clearBroadcasts() {
        _newBetBroadcasts.value = emptyList()
        _transactionBroadcasts.value = emptyList()
        _financialMetrics.value = FinancialMetric("1257850.00", "523600.00", "186250.00")
    }
}
