// ── User side: submit a pending deposit ─────────────────────────
fun submitPendingDeposit(amount: Double, method: String = "TeleBirr", onDone: (String) -> Unit) {
    viewModelScope.launch {
        val trxId = repository.createPendingTelebirrDeposit(amount)
        onDone(trxId)
    }
}

// ── Admin side: observe pending queue ───────────────────────────
val pendingTransactions: StateFlow<List<TransactionRecord>> =
    repository.allTransactions
        .map { list -> list.filter { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

val pendingDepositCount: StateFlow<Int> =
    pendingTransactions
        .map { list -> list.count { it.type == "DEPOSIT" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

// ── Admin side: approve / reject ────────────────────────────────
fun approveTransaction(tx: TransactionRecord) = viewModelScope.launch {
    repository.updateTransactionStatus(tx, "APPROVED")
}

fun rejectTransaction(tx: TransactionRecord) = viewModelScope.launch {
    repository.updateTransactionStatus(tx, "REJECTED")
}