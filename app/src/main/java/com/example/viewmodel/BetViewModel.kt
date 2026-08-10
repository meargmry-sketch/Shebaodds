package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Bet
import com.example.data.model.SportMatch
import com.example.data.model.TransactionRecord
import com.example.data.model.UserWallet
import com.example.data.model.leagueName
import com.example.data.repository.BetRepository
import com.example.data.repository.PlaceBetResult
import com.example.data.repository.RequestWithdrawResult
import com.example.data.api.GeminiSportsPredictor
import com.example.data.api.PredictionResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

class BetViewModel(application: Application, private val repository: BetRepository) : AndroidViewModel(application) {

    // Filter selectors
    private val _selectedSport = MutableStateFlow("All")
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    // Language Support Selector
    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun updateSelectedLanguage(lang: String) {
        _selectedLanguage.value = lang
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("selected_language", lang).apply()
        logConsole("🌐 [LANG CONFIG] Language updated to: $lang")
    }

    // Core Data flows from DB
    val wallet: StateFlow<UserWallet?> = repository.wallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBets: StateFlow<List<Bet>> = repository.allBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<SportMatch>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated API status StateFlows
    private val _isApiFetching = MutableStateFlow(false)
    val isApiFetching: StateFlow<Boolean> = _isApiFetching.asStateFlow()

    // Trending matches flow (derived from repository matches, making it react inside real-time odds tick)
    val trendingMatches: StateFlow<List<SportMatch>> = repository.allMatches
        .map { matches ->
            matches.filter { it.id in listOf(1, 6, 11, 13) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered match state Flow
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Match Tracking and Odds fluctuation Alert system State
    private val _trackedMatchIds = MutableStateFlow<Set<Int>>(emptySet())
    val trackedMatchIds: StateFlow<Set<Int>> = _trackedMatchIds.asStateFlow()

    private val _oddsAlerts = MutableStateFlow<List<OddsAlert>>(emptyList())
    val oddsAlerts: StateFlow<List<OddsAlert>> = _oddsAlerts.asStateFlow()

    // CUSTOM TARGET ODDS PRICE ALERTS SYSTEM STATE
    private val _customPriceAlerts = MutableStateFlow<List<CustomPriceAlert>>(emptyList())
    val customPriceAlerts: StateFlow<List<CustomPriceAlert>> = _customPriceAlerts.asStateFlow()

    fun addCustomPriceAlert(matchId: Int, teamA: String, teamB: String, marketName: String, targetOdds: Double, condition: String) {
        val newAlert = CustomPriceAlert(
            id = java.util.UUID.randomUUID().toString(),
            matchId = matchId,
            teamA = teamA,
            teamB = teamB,
            marketName = marketName,
            targetOdds = targetOdds,
            condition = condition
        )
        _customPriceAlerts.value = _customPriceAlerts.value + newAlert
        logConsole("🔔 [ALERT CONFIG] Configured target price alert for $teamA vs $teamB on option $marketName at odds $targetOdds ($condition).")
    }

    fun removeCustomPriceAlert(alertId: String) {
        _customPriceAlerts.value = _customPriceAlerts.value.filter { it.id != alertId }
    }

    // RESPONSIBLE PLAY & HIGH-FREQUENCY CHECK ENGINE
    private val betPlacementTimestamps = mutableListOf<Long>()
    private val _showResponsiblePlayWarning = MutableStateFlow(false)
    val showResponsiblePlayWarning: StateFlow<Boolean> = _showResponsiblePlayWarning.asStateFlow()

    fun dismissResponsiblePlayWarning() {
        _showResponsiblePlayWarning.value = false
    }

    fun triggerResponsiblePlayWarning() {
        _showResponsiblePlayWarning.value = true
    }

    fun recordBetPlacementAndCheckFrequency(): Boolean {
        val now = System.currentTimeMillis()
        betPlacementTimestamps.add(now)
        // Keep placements only within the last 30 seconds
        betPlacementTimestamps.removeAll { now - it > 30000 }
        // If 3 or more bets in 30s, trigger responsible gaming modal
        val triggered = betPlacementTimestamps.size >= 3
        if (triggered) {
            _showResponsiblePlayWarning.value = true
            logConsole("🛡️ [RESPONSIBLE PLAY] High-frequency betting detected! Triggering limit advice overlay.")
        }
        return triggered
    }

    fun toggleTrackedMatch(matchId: Int) {
        val current = _trackedMatchIds.value.toMutableSet()
        if (current.contains(matchId)) {
            current.remove(matchId)
        } else {
            current.add(matchId)
        }
        _trackedMatchIds.value = current
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putStringSet("tracked_match_ids", current.map { it.toString() }.toSet()).apply()
    }

    fun clearOddsAlerts() {
        _oddsAlerts.value = emptyList()
        saveAlertsToPrefs(emptyList())
    }

    fun markAlertAsRead(alertId: String) {
        val updated = _oddsAlerts.value.map {
            if (it.id == alertId) it.copy(isRead = true) else it
        }
        _oddsAlerts.value = updated
        saveAlertsToPrefs(updated)
    }

    private fun saveAlertsToPrefs(alerts: List<OddsAlert>) {
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        val arr = org.json.JSONArray()
        for (alert in alerts) {
            val obj = org.json.JSONObject()
            obj.put("id", alert.id)
            obj.put("matchId", alert.matchId)
            obj.put("teamA", alert.teamA)
            obj.put("teamB", alert.teamB)
            obj.put("sport", alert.sport)
            obj.put("fieldChanged", alert.fieldChanged)
            obj.put("oldValue", alert.oldValue)
            obj.put("newValue", alert.newValue)
            obj.put("timestamp", alert.timestamp)
            obj.put("isRead", alert.isRead)
            arr.put(obj)
        }
        sp.edit().putString("odds_alerts_json", arr.toString()).apply()
    }

    private fun loadAlertsFromPrefs(): List<OddsAlert> {
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        val jsonStr = sp.getString("odds_alerts_json", "")
        if (jsonStr.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<OddsAlert>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OddsAlert(
                        id = obj.getString("id"),
                        matchId = obj.getInt("matchId"),
                        teamA = obj.getString("teamA"),
                        teamB = obj.getString("teamB"),
                        sport = obj.getString("sport"),
                        fieldChanged = obj.getString("fieldChanged"),
                        oldValue = obj.getDouble("oldValue"),
                        newValue = obj.getDouble("newValue"),
                        timestamp = obj.getLong("timestamp"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun addOddsAlert(match: SportMatch, field: String, oldVal: Double, newVal: Double) {
        val alertId = java.util.UUID.randomUUID().toString()
        val changeDir = if (newVal > oldVal) "📈 INCR" else "📉 DECR"
        val alert = OddsAlert(
            id = alertId,
            matchId = match.id,
            teamA = match.teamA,
            teamB = match.teamB,
            sport = match.sport,
            fieldChanged = "$field ($changeDir)",
            oldValue = oldVal,
            newValue = newVal,
            timestamp = System.currentTimeMillis()
        )
        // Post actual local system notification
        triggerPushNotification(alert)

        // Add to state and save
        val updated = listOf(alert) + _oddsAlerts.value
        val trimmed = updated.take(40)
        _oddsAlerts.value = trimmed
        saveAlertsToPrefs(trimmed)
    }

    private fun triggerPushNotification(alert: OddsAlert) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val context = getApplication<Application>()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "odds_fluctuation_alerts"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        channelId,
                        "Odds Alerts",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Alerts for tracked sports matches odds updates"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val title = "${alert.teamA} vs ${alert.teamB} odds updated"
                val body = "The [${alert.fieldChanged}] changed from ${String.format("%.2f", alert.oldValue)} to ${String.format("%.2f", alert.newValue)}."

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

                notificationManager.notify(alert.id.hashCode(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Biometric Quick Bet Feature State
    private val _biometricQuickBetEnabled = MutableStateFlow(true) // Default to true or false, let's default to true for immediate out of the box testing of the feature, or we can toggle it in UI
    val biometricQuickBetEnabled: StateFlow<Boolean> = _biometricQuickBetEnabled.asStateFlow()

    fun setBiometricQuickBetEnabled(enabled: Boolean) {
        _biometricQuickBetEnabled.value = enabled
    }

    // Biometric Login Feature State (Persistent in SharedPreferences)
    private val sharedPrefs by lazy {
        getApplication<Application>().getSharedPreferences("shebaodds_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val _biometricLoginEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometric_login_enabled", false))
    val biometricLoginEnabled: StateFlow<Boolean> = _biometricLoginEnabled.asStateFlow()

    fun setBiometricLoginEnabled(enabled: Boolean) {
        _biometricLoginEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_login_enabled", enabled).apply()
    }

    // Biometric Payments Feature State (Top-up & Withdraw)
    private val _biometricPaymentsEnabled = MutableStateFlow(true)
    val biometricPaymentsEnabled: StateFlow<Boolean> = _biometricPaymentsEnabled.asStateFlow()

    fun setBiometricPaymentsEnabled(enabled: Boolean) {
        _biometricPaymentsEnabled.value = enabled
    }

    // Responsible gaming limits states
    private val _dailyLimit = MutableStateFlow<Double?>(null)
    val dailyLimit: StateFlow<Double?> = _dailyLimit.asStateFlow()

    private val _weeklyLimit = MutableStateFlow<Double?>(null)
    val weeklyLimit: StateFlow<Double?> = _weeklyLimit.asStateFlow()

    private val _monthlyLimit = MutableStateFlow<Double?>(null)
    val monthlyLimit: StateFlow<Double?> = _monthlyLimit.asStateFlow()

    fun setDailyLimit(limit: Double?) {
        _dailyLimit.value = limit
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putFloat("limit_daily", limit?.toFloat() ?: -1f).apply()
    }

    fun setWeeklyLimit(limit: Double?) {
        _weeklyLimit.value = limit
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putFloat("limit_weekly", limit?.toFloat() ?: -1f).apply()
    }

    fun setMonthlyLimit(limit: Double?) {
        _monthlyLimit.value = limit
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putFloat("limit_monthly", limit?.toFloat() ?: -1f).apply()
    }

    // Responsible gaming deposit limits states
    private val _dailyDepositLimit = MutableStateFlow<Double?>(null)
    val dailyDepositLimit: StateFlow<Double?> = _dailyDepositLimit.asStateFlow()

    private val _weeklyDepositLimit = MutableStateFlow<Double?>(null)
    val weeklyDepositLimit: StateFlow<Double?> = _weeklyDepositLimit.asStateFlow()

    fun setDailyDepositLimit(limit: Double?) {
        _dailyDepositLimit.value = limit
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putFloat("limit_deposit_daily", limit?.toFloat() ?: -1f).apply()
    }

    fun setWeeklyDepositLimit(limit: Double?) {
        _weeklyDepositLimit.value = limit
        val sp = getApplication<Application>().getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        sp.edit().putFloat("limit_deposit_weekly", limit?.toFloat() ?: -1f).apply()
    }

    // Deposit totals matching current periods
    val dailyDepositTotal: StateFlow<Double> = allTransactions.map { transactions ->
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" && it.timestamp >= startOfDay }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weeklyDepositTotal: StateFlow<Double> = allTransactions.map { transactions ->
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" && it.timestamp >= startOfWeek }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun checkDepositLimits(amount: Double): String? {
        val dLimit = _dailyDepositLimit.value
        if (dLimit != null) {
            val totalDaily = dailyDepositTotal.value
            if (totalDaily + amount > dLimit) {
                return "This deposit exceeds your DAILY responsible gaming deposit limit of ${String.format(java.util.Locale.US, "%,.1f", dLimit)} ETB (Deposited today: ${String.format(java.util.Locale.US, "%,.1f", totalDaily)} ETB, remaining limit: ${String.format(java.util.Locale.US, "%,.1f", dLimit - totalDaily)} ETB)."
            }
        }
        val wLimit = _weeklyDepositLimit.value
        if (wLimit != null) {
            val totalWeekly = weeklyDepositTotal.value
            if (totalWeekly + amount > wLimit) {
                return "This deposit exceeds your WEEKLY responsible gaming deposit limit of ${String.format(java.util.Locale.US, "%,.1f", wLimit)} ETB (Deposited this week: ${String.format(java.util.Locale.US, "%,.1f", totalWeekly)} ETB, remaining limit: ${String.format(java.util.Locale.US, "%,.1f", wLimit - totalWeekly)} ETB)."
            }
        }
        return null
    }

    // Spending totals matching current periods
    val dailyWagerTotal: StateFlow<Double> = allBets.map { bets ->
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        bets.filter { it.timestamp >= startOfDay }.sumOf { it.stake }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weeklyWagerTotal: StateFlow<Double> = allBets.map { bets ->
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        bets.filter { it.timestamp >= startOfWeek }.sumOf { it.stake }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyWagerTotal: StateFlow<Double> = allBets.map { bets ->
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        bets.filter { it.timestamp >= startOfMonth }.sumOf { it.stake }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun checkBetLimits(stake: Double): String? {
        val dLimit = _dailyLimit.value
        if (dLimit != null) {
            val totalDaily = dailyWagerTotal.value
            if (totalDaily + stake > dLimit) {
                return "This bet exceeds your DAILY responsible gaming limit of ${String.format("%.1f", dLimit)} ETB (Spent today: ${String.format("%.1f", totalDaily)} ETB, remaining limit: ${String.format("%.1f", dLimit - totalDaily)} ETB)."
            }
        }
        val wLimit = _weeklyLimit.value
        if (wLimit != null) {
            val totalWeekly = weeklyWagerTotal.value
            if (totalWeekly + stake > wLimit) {
                return "This bet exceeds your WEEKLY responsible gaming limit of ${String.format("%.1f", wLimit)} ETB (Spent this week: ${String.format("%.1f", totalWeekly)} ETB, remaining limit: ${String.format("%.1f", wLimit - totalWeekly)} ETB)."
            }
        }
        val mLimit = _monthlyLimit.value
        if (mLimit != null) {
            val totalMonthly = monthlyWagerTotal.value
            if (totalMonthly + stake > mLimit) {
                return "This bet exceeds your MONTHLY responsible gaming limit of ${String.format("%.1f", mLimit)} ETB (Spent this month: ${String.format("%.1f", totalMonthly)} ETB, remaining limit: ${String.format("%.1f", mLimit - totalMonthly)} ETB)."
            }
        }
        return null
    }

    val filteredMatches: StateFlow<List<SportMatch>> = combine(
        repository.allMatches,
        _selectedSport,
        _searchQuery
    ) { matches, sport, query ->
        val bySport = if (sport == "All") {
            matches
        } else if (sport.equals("Soccer", ignoreCase = true) || sport.equals("Football", ignoreCase = true)) {
            matches.filter { it.sport.equals("Football", ignoreCase = true) || it.sport.equals("Soccer", ignoreCase = true) }
        } else {
            matches.filter { it.sport.equals(sport, ignoreCase = true) }
        }
        if (query.isBlank()) {
            bySport
        } else {
            bySport.filter {
                it.teamA.contains(query, ignoreCase = true) ||
                it.teamB.contains(query, ignoreCase = true) ||
                it.sport.contains(query, ignoreCase = true) ||
                it.leagueName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Prediction States
    private val _currentPrediction = MutableStateFlow<PredictionResult?>(null)
    val currentPrediction: StateFlow<PredictionResult?> = _currentPrediction.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analyzedMatchId = MutableStateFlow<Int?>(null)
    val analyzedMatchId: StateFlow<Int?> = _analyzedMatchId.asStateFlow()

    // Real-time Active slip selections cart
    private val _activeSlipSelectedItems = MutableStateFlow<List<com.example.data.model.BetItem>>(emptyList())
    val activeSlipSelectedItems: StateFlow<List<com.example.data.model.BetItem>> = _activeSlipSelectedItems.asStateFlow()

    // Real-time Chat message state variables
    private val _supportMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome-1",
                sender = "AGENT",
                senderName = "Sara (Compliance)",
                text = "Welcome to BetMaster Premium Support Desk! 🛡️ Ask us anything regarding wager betting rules or payment settlement guidelines.",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val supportMessages: StateFlow<List<ChatMessage>> = _supportMessages.asStateFlow()

    private val _isAgentTyping = MutableStateFlow(false)
    val isAgentTyping: StateFlow<Boolean> = _isAgentTyping.asStateFlow()

    fun sendSupportMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        val userMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            sender = "USER",
            senderName = "You",
            text = messageText,
            timestamp = System.currentTimeMillis()
        )
        
        _supportMessages.value = _supportMessages.value + userMsg

        viewModelScope.launch {
            _isAgentTyping.value = true
            delay(1200) // Realistic interactive delay
            _isAgentTyping.value = false

            val lower = messageText.lowercase()
            val isGeez = messageText.any { it in '\u1200'..'\u137F' }
            val isOromo = lower.contains("akkam") || lower.contains("wirdii") || lower.contains("galchi") || lower.contains("maallaqa") || lower.contains("tapha") || lower.contains("deeggarsa") || lower.contains("koo")
            val language = when {
                isGeez && (lower.contains("በሪሰ") || lower.contains("ክትስዕርዎ") || lower.contains("ቲኬትኩም")) -> "ti"
                isGeez -> "am"
                isOromo -> "om"
                else -> "en"
            }

            var replyText: String? = null
            var agentName = "Marcus (Rules Desk)"

            // 1. Core Rule Check: Balance / Ticket Status Queries
            if (lower.contains("ticket") || lower.contains("bet") || lower.contains("ውርርድ") || lower.contains("ቲኬት") || lower.contains("wirdii") || lower.contains("kaardii")) {
                val lastBet = allBets.value.maxByOrNull { it.timestamp }
                if (lastBet != null) {
                    replyText = when (language) {
                        "am" -> "የመጨረሻው ቲኬትዎ (ID: #${lastBet.id}) ሁኔታ: ${lastBet.status} ነው። መደብ: ${lastBet.stake} ETB | ሊያሸንፉ የሚችሉት: ${lastBet.potentialReturn} ETB።"
                        "ti" -> "ናይ መወዳእታ ቲኬትኩም (ID: #${lastBet.id}) ኩነታት: ${lastBet.status} እዩ። መጠን ውርርድ: ${lastBet.stake} ETB | ክትስዕርዎ እትኽእሉ: ${lastBet.potentialReturn} ETB።"
                        "om" -> "Wirdiin keessan inni dhumaa (ID: #${lastBet.id}) haala kana irra jira: ${lastBet.status}. Wirdii: ${lastBet.stake} ETB | Argannoo tilmaamamaa: ${lastBet.potentialReturn} ETB."
                        else -> "Your last bet ticket (ID: #${lastBet.id}) status is evaluated as: ${lastBet.status}. Stake: ${lastBet.stake} ETB | Potential Return: ${lastBet.potentialReturn} ETB."
                    }
                    agentName = "Sara (Compliance)"
                }
            }

            // 2. Core Rule Check: Welcome Bonus Inquiries
            if (replyText == null && (lower.contains("bonus") || lower.contains("ቦነስ") || lower.contains("በሪሰ") || lower.contains("kennaa"))) {
                replyText = when (language) {
                    "am" -> "አዲስ ተመዝጋቢ ስለሆኑ 100% የእንኳን ደህና መጣችሁ ቦነስ ያገኛሉ! ይህንን ቦነስ እውነተኛ ገንዘብ ለማድረግ ቢያንስ 1.40 ኦድስ ባላቸው ጨዋታዎች ላይ 5 ጊዜ ማጫወት (Rollover) ይኖርብዎታል።"
                    "ti" -> "እንኳዕ ብደሓን መጻእኩም! 100% ቦነስ ናይ መጀመሪያ ውህብቶ ክትረኽቡ ኢኹም። ነዚ ቦነስ ናብ ናይ ሓቂ ገንዘብ ንምቕያር እንተወሓደ 1.40 ኦድስ ዘለዎም ጸወታታት 5 ግዘ ክትጻወቱ ይግባእ።"
                    "om" -> "Baga nagaan dhuftan! Kennaa simannaa 100% argachuuf mirga qabdu. Kennaa kana gara maallaqa qabatamaatti jijjiiruuf, taphoota odds 1.40 fi isaa ol qaban irratti si'a 5 taphachuu qabdu."
                    else -> "Welcome Newcomer! You are eligible for a 100% First Deposit Match up to 10,000 ETB. To convert bonus funds to cash, meet the 5x accumulator rollover requirement with selection odds >= 1.40."
                }
                agentName = "Marcus (Rules Desk)"
            }

            // 3. Fallback to other legacy matches or Escalation
            if (replyText == null) {
                if (lower.contains("rule") || lower.contains("odds") || lower.contains("slip") || lower.contains("accum") || lower.contains("coefficient") || lower.contains("multi") || lower.contains("btts") || lower.contains("safety") || lower.contains("seer") || lower.contains("qabxii")) {
                    replyText = when (language) {
                        "am" -> "ጨዋታዎች እንደተጀመሩ ውርርዶች ይቆለፋሉ። ለተከማቹ ብዙ ጨዋታዎች (Multi-Bet) ኦድሶች እርስ በእርስ ይባዛሉ። የደህንነት ውጤቶች የስፖርት ስታቲስቲክስን ያንጸባርቃሉ።"
                        "ti" -> "ጸወታታት ምስ ጀመሩ ውርርዳት ይዕጸዉ። ንዝተዋህለሉ ብዙሓት ጸወታታት (Multi-Bet) ኦድሳት ንሓድሕዶም ይባዝሑ።"
                        "om" -> "Taphni akkuma jalqabe wirdiin ni cufama. Wirdii baay'eef odds waliin baay'atama. Qabxiin nageenyaa seenaa istaatistiksii taphaa irratti hunda'a."
                        else -> "Wager items lock immediately upon placement. For Multi-Bet accumulators, odds compound by multiplying separate lines together. Active safety score coefficients reflect statistical history metrics."
                    }
                    agentName = "Marcus (Rules Desk)"
                } else if (lower.contains("pay") || lower.contains("telebirr") || lower.contains("deposit") || lower.contains("withdraw") || lower.contains("money") || lower.contains("callback") || lower.contains("wallet") || lower.contains("cash") || lower.contains("bank") || lower.contains("financial") || lower.contains("ref") || lower.contains("receipt") || lower.contains("kaffal") || lower.contains("maallaq") || lower.contains("herreg")) {
                    replyText = when (language) {
                        "am" -> "ክፍያዎች በአስተማማኝ ሁኔታ በፊርማ ማረጋገጫ ይከናወናሉ። የቴሌቢር ክፍያ ማረጋገጫ በ30 ሰከንዶች ውስጥ ይጠናቀቃል። በ'አስተማማኝ ባንክ' ገጽ ላይ የክፍያ ሙከራ ማድረግ ይችላሉ።"
                        "ti" -> "ክፍሊታት ብውሑስ መንገዲ ይፍጸሙ። ናይ ቴሌቢር ክፍሊት ውጽኢት ኣብ ውሽጢ 30 ሰከንድ ይውዳእ።"
                        "om" -> "Kafaltiin haala amansiisaa ta'een raawwatama. Telebirr merchant callback sekondii 30 gadi keessatti xumurama. Gara panel 'Baanki Amansiisaa' deemuun herrega keessan gochuu dandeessu."
                        else -> "Payment transactions execute securely with signature verification. Telebirr merchant callback takes less than 30 seconds to settle. You can trigger simulated webhooks inside the 'Secure Bank' panel for instant credit."
                    }
                    agentName = "Sara (Finance Desk)"
                }
            }

            // 4. Fallback routing signature to pass directly to a live customer service manager
            if (replyText == null) {
                replyText = when (language) {
                    "am" -> "ጥያቄዎትን ሙሉ በሙሉ ለመመለስ ወደ ቀጥታ የደንበኞች አገልግሎት ባለሙያ እያገናኘሁዎት ነው። እባክዎ አንድ ሰከንድ ይጠብቁ..."
                    "ti" -> "ሕቶኹም ንምምላስ ናብ ናይ ቀጥታ ሓገዝ ዓማዊል ሰራሕተኛ ነመሓላልፈኩም ኣለና። እባክኹም ቁሩብ ጸበዩ..."
                    "om" -> "Gaaffii keessan guutuutti deebisuuf gara tajaajila maamiltootaa kallattiitti isin dabarsaa jirra. Maaloo daqiiqaa tokko eegaa..."
                    else -> "Transferring your session parameters directly to a senior live support desk officer. Please stand by..."
                }
                agentName = "Senior Support Officer"
            }

            val botResponse = ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                sender = "AGENT",
                senderName = agentName,
                text = replyText,
                timestamp = System.currentTimeMillis()
            )
            _supportMessages.value = _supportMessages.value + botResponse
        }
    }

    fun toggleSlipSelection(match: com.example.data.model.SportMatch, selection: String, odds: Double) {
        val currentList = _activeSlipSelectedItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.matchId == match.id }
        if (existingIndex >= 0) {
            val existingItem = currentList[existingIndex]
            if (existingItem.selection == selection) {
                // Clicking same selection on same match removes it
                currentList.removeAt(existingIndex)
            } else {
                // Replace selection if on same match (one wager line per game item)
                currentList[existingIndex] = com.example.data.model.BetItem(
                    matchId = match.id,
                    teamA = match.teamA,
                    teamB = match.teamB,
                    selection = selection,
                    odds = odds,
                    sport = match.sport,
                    marketType = when (selection) {
                        "1", "X", "2" -> "1X2"
                        "Over 2.5", "Under 2.5" -> "Over/Under"
                        "BTTS Yes", "BTTS No" -> "BTTS"
                        else -> "1X2"
                    }
                )
            }
        } else {
            // Append new selection leg
            currentList.add(
                com.example.data.model.BetItem(
                    matchId = match.id,
                    teamA = match.teamA,
                    teamB = match.teamB,
                    selection = selection,
                    odds = odds,
                    sport = match.sport,
                    marketType = when (selection) {
                        "1", "X", "2" -> "1X2"
                        "Over 2.5", "Under 2.5" -> "Over/Under"
                        "BTTS Yes", "BTTS No" -> "BTTS"
                        else -> "1X2"
                    }
                )
            )
        }
        _activeSlipSelectedItems.value = currentList
    }

    fun removeSelectionFromSlip(matchId: Int) {
        _activeSlipSelectedItems.value = _activeSlipSelectedItems.value.filter { it.matchId != matchId }
    }

    fun clearSlip() {
        _activeSlipSelectedItems.value = emptyList()
    }

    fun placeMultiBet(
        stake: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val limitError = checkBetLimits(stake)
        if (limitError != null) {
            onError(limitError)
            return
        }
        viewModelScope.launch {
            val result = repository.placeMultiBet(_activeSlipSelectedItems.value, stake)
            if (result is PlaceBetResult.Success) {
                clearSlip()
                recordBetPlacementAndCheckFrequency()
                onSuccess()
            } else if (result is PlaceBetResult.Failure) {
                onError(result.message)
            }
        }
    }

    fun placeMultiLegBet(
        stake: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val limitError = checkBetLimits(stake)
        if (limitError != null) {
            onError(limitError)
            return
        }
        viewModelScope.launch {
            val result = repository.placeMultiLegBet(_activeSlipSelectedItems.value, stake)
            if (result is PlaceBetResult.Success) {
                clearSlip()
                onSuccess()
            } else if (result is PlaceBetResult.Failure) {
                onError(result.message)
            }
        }
    }

    private val alertedTxIds = mutableSetOf<String>()

    init {
        // Load stored responsible gaming limits
        val sp = application.getSharedPreferences("shebaodds_prefs", Context.MODE_PRIVATE)
        
        // Load stored language
        val savedLang = sp.getString("selected_language", "en") ?: "en"
        _selectedLanguage.value = savedLang

        val dLimit = sp.getFloat("limit_daily", -1f)
        _dailyLimit.value = if (dLimit >= 0f) dLimit.toDouble() else null

        val wLimit = sp.getFloat("limit_weekly", -1f)
        _weeklyLimit.value = if (wLimit >= 0f) wLimit.toDouble() else null

        val mLimit = sp.getFloat("limit_monthly", -1f)
        _monthlyLimit.value = if (mLimit >= 0f) mLimit.toDouble() else null

        val dDepLimit = sp.getFloat("limit_deposit_daily", -1f)
        _dailyDepositLimit.value = if (dDepLimit >= 0f) dDepLimit.toDouble() else null

        val wDepLimit = sp.getFloat("limit_deposit_weekly", -1f)
        _weeklyDepositLimit.value = if (wDepLimit >= 0f) wDepLimit.toDouble() else null

        // Load tracked matches and alerts
        val trackedSet = sp.getStringSet("tracked_match_ids", emptySet())
        _trackedMatchIds.value = trackedSet?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        _oddsAlerts.value = loadAlertsFromPrefs()

        viewModelScope.launch {
            // Pairwise collection to capture changes in matches
            var previousMatchesMap = emptyMap<Int, SportMatch>()
            repository.allMatches.collect { currentMatches ->
                val currentTracked = _trackedMatchIds.value
                val activeSlipMatchIds = _activeSlipSelectedItems.value.map { it.matchId }.toSet()
                if (previousMatchesMap.isNotEmpty()) {
                    for (match in currentMatches) {
                        val isTracked = currentTracked.contains(match.id)
                        val isInActiveSlip = activeSlipMatchIds.contains(match.id)
                        if (isTracked || isInActiveSlip) {
                            val prev = previousMatchesMap[match.id]
                            if (prev != null) {
                                // Detect odds fluctuation trigger (>= 0.05 absolute difference)
                                if (Math.abs(match.odds1 - prev.odds1) >= 0.045) {
                                    addOddsAlert(match, "Home Win Odds", prev.odds1, match.odds1)
                                }
                                if (Math.abs(match.oddsX - prev.oddsX) >= 0.045) {
                                    addOddsAlert(match, "Draw Odds", prev.oddsX, match.oddsX)
                                }
                                if (Math.abs(match.odds2 - prev.odds2) >= 0.045) {
                                    addOddsAlert(match, "Away Win Odds", prev.odds2, match.odds2)
                                }

                                // Detect Score Update Alerts
                                if (match.scoreA != prev.scoreA || match.scoreB != prev.scoreB) {
                                    triggerMatchScoreUpdateNotification(
                                        match = match,
                                        oldScore = "${prev.scoreA}-${prev.scoreB}",
                                        newScore = "${match.scoreA}-${match.scoreB}"
                                    )
                                }
                                
                                // Detect Match Start Alerts (isLive changes from false to true or status from "UPCOMING" to "LIVE")
                                if ((match.isLive && !prev.isLive) || (match.status == "LIVE" && prev.status == "UPCOMING")) {
                                    triggerMatchStartNotification(match)
                                }
                            }
                        }
                    }
                }
                previousMatchesMap = currentMatches.associateBy { it.id }
            }
        }

        viewModelScope.launch {
            // First install check and mock population
            repository.initializeDbIfEmpty()
            // Start our real-time odds and matcher simulator
            startRealtimeOddsSimulator()
            
            // Auto-connect to local WebSocket server to stream real-time match and odds updates
            delay(1000)
            startLiveFeedClient("ws://127.0.0.1:9090", "shebaodds_api_demo")
        }

        // Periodic background RabbitMQ wager simulator
        viewModelScope.launch {
            delay(4000)
            while (true) {
                delay(7000)
                if (_isQueueConsumerActive.value) {
                    injectRapidWagers(1)
                }
            }
        }

        // Live broad-caster reactive loop
        viewModelScope.launch {
            // Seed base transactions so existing approvals are registered as ready but doesn't spam alerts right away
            val existingTx = repository.allTransactions.firstOrNull() ?: emptyList()
            existingTx.forEach { tx ->
                if (tx.status == "APPROVED") {
                    alertedTxIds.add(tx.id)
                }
            }

            combine(repository.allTransactions, repository.wallet) { transactions, walletState ->
                Pair(transactions, walletState)
            }.collect { (transactions, walletState) ->
                // Calculate display metrics matching Node.js structure
                val currentBalanceChange = if (walletState != null) walletState.balance - 523600.00 else 0.0
                val displayBalance = 1257850.00 + currentBalanceChange
                
                val dynamicDeposits = transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
                val displayDeposits = 523600.00 + dynamicDeposits

                val dynamicWithdrawals = transactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }.sumOf { it.amount }
                val displayWithdrawals = 186250.00 + dynamicWithdrawals

                com.example.util.AdminSocketHubInstance.broadcastFinancialMetricUpdate(
                    com.example.util.AdminSocketHubInstance.FinancialMetric(
                        totalBalance = String.format(java.util.Locale.US, "%.2f", displayBalance),
                        totalDeposits = String.format(java.util.Locale.US, "%.2f", displayDeposits),
                        totalWithdrawals = String.format(java.util.Locale.US, "%.2f", displayWithdrawals)
                    )
                )

                // Transaction alerts broadcast triggers on status transition to APPROVED
                transactions.forEach { tx ->
                    if (tx.status == "APPROVED" && !alertedTxIds.contains(tx.id)) {
                        alertedTxIds.add(tx.id)
                        val formattedType = if (tx.type.equals("DEPOSIT", ignoreCase = true)) "Deposit" else "Withdrawal"
                        com.example.util.AdminSocketHubInstance.broadcastTransactionAlert(
                            com.example.util.AdminSocketHubInstance.BroadcastTransaction(
                                id = tx.id,
                                user = "User${tx.userId}",
                                type = formattedType,
                                amount = String.format(java.util.Locale.US, "%.2f", tx.amount),
                                method = tx.method,
                                status = "Approved"
                            )
                        )
                    }
                }
            }
        }
    }

    fun selectSport(sport: String) {
        _selectedSport.value = sport
    }

    // Simulated API fetch / refresh action
    fun refreshDashboardData() {
        viewModelScope.launch {
            _isApiFetching.value = true
            delay(1200) // simulated API network latency
            _isApiFetching.value = false
        }
    }

    // Place bet action
    fun placeBet(
        matchId: Int,
        selection: String,
        odds: Double,
        stake: Double,
        maxSlippageAllowed: Double = 0.05,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val limitError = checkBetLimits(stake)
        if (limitError != null) {
            onError(limitError)
            return
        }
        viewModelScope.launch {
            when (val result = repository.placeBet(matchId, selection, odds, stake, maxSlippageAllowed)) {
                is PlaceBetResult.Success -> {
                    recordBetPlacementAndCheckFrequency()
                    onSuccess()
                }
                is PlaceBetResult.Failure -> {
                    onError(result.message)
                }
            }
        }
    }

    // Cashout bet action
    fun cashoutBet(bet: Bet) {
        viewModelScope.launch {
            // Calculate a fair cashout amount (usually stake * odds * ratio)
            val cashoutRatio = if (bet.sport == "Football") 0.85 else 0.90
            val cashoutAmount = bet.potentialReturn * cashoutRatio * (0.8 + Random.nextDouble(0.3))
            val roundedAmount = Math.round(cashoutAmount * 100.0) / 100.0
            repository.cashoutBet(bet, roundedAmount)
        }
    }

    // Seed realistic betting and transaction logs over the past fortnight for visual analytics dashboards!
    fun seedDemoHistory() {
        viewModelScope.launch {
            val db = repository.getDb()
            val rightNow = System.currentTimeMillis()
            val dayMs = 24L * 3600L * 1000L
            
            val initialDemoBets = listOf(
                Bet(matchId = 1, selection = "1", odds = 2.05, stake = 1200.0, potentialReturn = 2460.0, status = "WON", timestamp = rightNow - 14 * dayMs, sport = "Football", teamA = "Arsenal", teamB = "Man United"),
                Bet(matchId = 2, selection = "2", odds = 1.95, stake = 800.0, potentialReturn = 1560.0, status = "WON", timestamp = rightNow - 13 * dayMs, sport = "Basketball", teamA = "Lakers", teamB = "Warriors"),
                Bet(matchId = 3, selection = "1", odds = 1.85, stake = 1500.0, potentialReturn = 2775.0, status = "LOST", timestamp = rightNow - 11 * dayMs, sport = "Tennis", teamA = "Alcaraz", teamB = "Djokovic"),
                Bet(matchId = 4, selection = "1", odds = 1.45, stake = 3000.0, potentialReturn = 4350.0, status = "WON", timestamp = rightNow - 9 * dayMs, sport = "Esports", teamA = "T1 esports", teamB = "G2 Esports"),
                Bet(matchId = 5, selection = "X", odds = 3.65, stake = 500.0, potentialReturn = 1825.0, status = "LOST", timestamp = rightNow - 8 * dayMs, sport = "Football", teamA = "Chelsea", teamB = "Liverpool"),
                Bet(matchId = 6, selection = "2", odds = 2.45, stake = 1200.0, potentialReturn = 2940.0, status = "WON", timestamp = rightNow - 6 * dayMs, sport = "Tennis", teamA = "Sinner", teamB = "Medvedev"),
                Bet(matchId = 7, selection = "1", odds = 2.25, stake = 1000.0, potentialReturn = 2250.0, status = "LOST", timestamp = rightNow - 5 * dayMs, sport = "Basketball", teamA = "Miami Heat", teamB = "Boston Celtics"),
                Bet(matchId = 8, selection = "1", odds = 1.90, stake = 2000.0, potentialReturn = 3800.0, status = "CASHOUT", timestamp = rightNow - 4 * dayMs, sport = "Esports", teamA = "Faker", teamB = "Chovy"),
                Bet(matchId = 9, selection = "2", odds = 2.20, stake = 1600.0, potentialReturn = 3520.0, status = "WON", timestamp = rightNow - 3 * dayMs, sport = "Football", teamA = "Barcelona", teamB = "Real Madrid"),
                Bet(matchId = 10, selection = "2", odds = 1.80, stake = 1500.0, potentialReturn = 2700.0, status = "LOST", timestamp = rightNow - 2 * dayMs, sport = "Basketball", teamA = "Milwaukee Bucks", teamB = "Denver Nuggets"),
                Bet(matchId = 11, selection = "1", odds = 2.15, stake = 1100.0, potentialReturn = 2365.0, status = "WON", timestamp = rightNow - 1 * dayMs, sport = "Tennis", teamA = "Sabalenka", teamB = "Swiatek"),
                Bet(matchId = 12, selection = "1", odds = 1.35, stake = 2400.0, potentialReturn = 3240.0, status = "PENDING", timestamp = rightNow - 4 * 3600 * 1000, sport = "Football", teamA = "Man City", teamB = "Tottenham")
            )
            
            // Insert into Database
            for (bet in initialDemoBets) {
                db.betDao().insertBet(bet)
            }
            
            // Register a security audit log showing history was seeded
            val securityLogSdf = java.text.SimpleDateFormat("HH:mm:ss.S", java.util.Locale.getDefault())
            val logMsg = "Legacy database synchronization triggered: Seeded 12 matches spanning 14-day history for statistical charts"
            // Since UserProfileDialog uses a local state for logs, seeding the bets into Dao is clean.
        }
    }

    // Resolve a pending bet (Manual win / loss mock triggers for testing and showcase!)
    fun resolveBet(bet: Bet, won: Boolean) {
        viewModelScope.launch {
            repository.resolveBetMock(bet, won)
        }
    }

    // Approve a betting ticket
    fun approveBet(bet: Bet) {
        viewModelScope.launch {
            repository.approveBet(bet)
        }
    }

    // Run programmatic BetSettlerEngine for completed match resolution
    fun runSettlementEngine(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.settleCompletedMatches()
            onComplete(count)
        }
    }

    // Force complete/finish a live match to trigger the settlement engine test scenarios
    fun concludeMatchSimulated(matchId: Int, homeScore: Int, awayScore: Int) {
        viewModelScope.launch {
            repository.concludeMatchSimulated(matchId, homeScore, awayScore)
        }
    }

    // Emulates a Node.js Express router handler for POST /api/v1/bets/place
    // Taking the raw request payload, applying thread-safe transaction isolation, and returning the exact JSON payload.
    fun emulateRestPost(
        reqBodyJson: String,
        onResponse: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = extractIntFromJson(reqBodyJson, "userId") ?: 1
                val matchId = extractIntFromJson(reqBodyJson, "matchId") ?: 1
                val selection = extractStringFromJson(reqBodyJson, "selection") ?: "1"
                val expectedOdds = extractDoubleFromJson(reqBodyJson, "expectedOdds") ?: 2.10
                val stake = extractDoubleFromJson(reqBodyJson, "stake") ?: 100.0
                val maxSlippageAllowed = extractDoubleFromJson(reqBodyJson, "maxSlippageAllowed") ?: 0.05

                val reqObj = com.example.util.PlaceBetRequest(
                    userId = userId,
                    matchId = matchId,
                    selection = selection,
                    expectedOdds = expectedOdds,
                    stake = stake,
                    maxSlippageAllowed = maxSlippageAllowed
                )

                // Call the actual transaction-isolated Betting Engine!
                val service = com.example.util.BettingEngineService(repository.getDb())
                val result = service.placeSingleBet(reqObj)

                val df = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US)
                df.timeZone = java.util.TimeZone.getTimeZone("GMT")
                val dateLabel = df.format(java.util.Date())

                if (result.success) {
                    val responseJson = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Date: $dateLabel
                    Connection: close

                    {
                      "success": true,
                      "betId": ${result.betId},
                      "message": "${result.message}"
                    }
                    """.trimIndent()
                    onResponse(responseJson)
                } else {
                    val responseJson = """
                    HTTP/1.1 400 Bad Request
                    Content-Type: application/json
                    Date: $dateLabel
                    Connection: close

                    {
                      "success": false,
                      "message": "${result.message}"
                    }
                    """.trimIndent()
                    onResponse(responseJson)
                }
            } catch (e: Exception) {
                val responseJson = """
                HTTP/1.1 500 Internal Server Error
                Content-Type: application/json
                Connection: close

                {
                  "success": false,
                  "message": "A technical error occurred while locking your slip."
                }
                """.trimIndent()
                onResponse(responseJson)
            }
        }
    }

    private var lastActiveSheetsCacheTime: Long = 0
    private var cachedActiveSheetsJson: String? = null

    fun emulateRestGetActiveSheets(onResponse: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val rightNow = System.currentTimeMillis()
                val cacheTtlMs = 5000L // 5 seconds cache TTL
                val isCacheHit = cachedActiveSheetsJson != null && (rightNow - lastActiveSheetsCacheTime < cacheTtlMs)

                val df = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US)
                df.timeZone = java.util.TimeZone.getTimeZone("GMT")
                val dateLabel = df.format(java.util.Date())

                val jsonBody: String
                val cacheHeader: String

                if (isCacheHit) {
                    jsonBody = cachedActiveSheetsJson ?: "[]"
                    cacheHeader = "HIT"
                } else {
                    val currentMatches = allMatches.value
                    val activeMatches = currentMatches.filter { it.status != "FINISHED" }

                    val jsonArr = org.json.JSONArray()
                    for (match in activeMatches) {
                        // 1X2 market
                        val row1X2 = org.json.JSONObject().apply {
                            put("match_id", match.id)
                            put("home_team", match.teamA)
                            put("away_team", match.teamB)
                            put("start_time", match.dateTimeString)
                            put("status", if (match.isLive) "Live" else "Not Started")
                            put("market_type", "1X2")
                            
                            val optionsArr = org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply { put("selection", "1"); put("odds", match.odds1) })
                                if (match.oddsX > 1.0) {
                                    put(org.json.JSONObject().apply { put("selection", "X"); put("odds", match.oddsX) })
                                }
                                put(org.json.JSONObject().apply { put("selection", "2"); put("odds", match.odds2) })
                            }
                            put("options", optionsArr)
                        }
                        jsonArr.put(row1X2)

                        // Over/Under market
                        val rowOU = org.json.JSONObject().apply {
                            put("match_id", match.id)
                            put("home_team", match.teamA)
                            put("away_team", match.teamB)
                            put("start_time", match.dateTimeString)
                            put("status", if (match.isLive) "Live" else "Not Started")
                            put("market_type", "Over/Under")
                            
                            val optionsArr = org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply { put("selection", "Over 2.5"); put("odds", match.oddsOver) })
                                put(org.json.JSONObject().apply { put("selection", "Under 2.5"); put("odds", match.oddsUnder) })
                            }
                            put("options", optionsArr)
                        }
                        jsonArr.put(rowOU)

                        // BTTS market for Football
                        if (match.sport.equals("Football", ignoreCase = true)) {
                            val rowBtts = org.json.JSONObject().apply {
                                put("match_id", match.id)
                                put("home_team", match.teamA)
                                put("away_team", match.teamB)
                                put("start_time", match.dateTimeString)
                                put("status", if (match.isLive) "Live" else "Not Started")
                                put("market_type", "BTTS")
                                
                                val optionsArr = org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply { put("selection", "BTTS Yes"); put("odds", match.oddsBttsYes) })
                                    put(org.json.JSONObject().apply { put("selection", "BTTS No"); put("odds", match.oddsBttsNo) })
                                }
                                put("options", optionsArr)
                            }
                            jsonArr.put(rowBtts)
                        }
                    }
                    
                    jsonBody = jsonArr.toString(2)
                    cachedActiveSheetsJson = jsonBody
                    lastActiveSheetsCacheTime = rightNow
                    cacheHeader = "MISS"
                }

                val responseJson = """
                HTTP/1.1 200 OK
                Content-Type: application/json
                Date: $dateLabel
                X-Cache: $cacheHeader
                X-Cache-TTL: ${cacheTtlMs / 1000}s
                Connection: keep-alive

                $jsonBody
                """.trimIndent()
                onResponse(responseJson)
            } catch (e: Exception) {
                val responseJson = """
                HTTP/1.1 500 Internal Server Error
                Content-Type: application/json
                Connection: close

                {
                  "error": "Failed to load current betting market sheets."
                }
                """.trimIndent()
                onResponse(responseJson)
            }
        }
    }

    private fun extractIntFromJson(json: String, key: String): Int? {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractDoubleFromJson(json: String, key: String): Double? {
        val pattern = "\"$key\"\\s*:\\s*([0-9.]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractStringFromJson(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    // Update static transaction record status (approved/rejected from admin panel)
    fun updateTransactionStatus(transaction: TransactionRecord, status: String) {
        viewModelScope.launch {
            repository.updateTransactionStatus(transaction, status)
        }
    }

    // Deposit with secure payment simulator
    fun depositFunds(amount: Double, gateway: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val trxId = repository.depositFunds(amount, gateway)
            onSuccess(trxId)
        }
    }

    // Create a pending Telebirr transaction
    fun depositFundsPending(amount: Double, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val trxId = repository.createPendingTelebirrDeposit(amount)
            onSuccess(trxId)
        }
    }

    // Withdraw funds selection
    fun withdrawFunds(
        amount: Double,
        gateway: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.withdrawFunds(amount, gateway)) {
                is RequestWithdrawResult.Success -> {
                    onSuccess(result.trxId)
                }
                is RequestWithdrawResult.Failure -> {
                    onError(result.message)
                }
            }
        }
    }

    // Request AI Prediction for a specific match
    fun fetchMatchPrediction(match: SportMatch) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analyzedMatchId.value = match.id
            _currentPrediction.value = null
            
            // Artificial analytics delay to show high-fidelity analytical loader
            delay(1500)
            
            val result = GeminiSportsPredictor.predictMatch(
                teamA = match.teamA,
                teamB = match.teamB,
                sport = match.sport,
                currentScore = "${match.scoreA}-${match.scoreB}",
                isLive = match.isLive
            )
            _currentPrediction.value = result
            _isAnalyzing.value = false
        }
    }

    fun clearPrediction() {
        _currentPrediction.value = null
        _analyzedMatchId.value = null
    }

    fun processSportsWebhook(jsonPayload: String, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            try {
                logBroker("📥 [WEBHOOK ROUTER] Received POST /api/v1/webhooks/sports-stream/match")
                delay(200)
                
                // Parse JSON payload
                val fixtureId = extractIntFromJson(jsonPayload, "fixture_id")
                val status = extractStringFromJson(jsonPayload, "status")
                val homeScore = extractIntFromJson(jsonPayload, "home_score")
                val awayScore = extractIntFromJson(jsonPayload, "away_score")
                val elapsedTime = extractIntFromJson(jsonPayload, "elapsed_time")
                
                if (fixtureId == null || status == null || homeScore == null || awayScore == null || elapsedTime == null) {
                    logBroker("❌ [WEBHOOK ERROR] Invalid payload fields or types.")
                    onResponse("""
                        {
                          "success": false,
                          "error": "Invalid ingestion payload format."
                        }
                    """.trimIndent())
                    return@launch
                }
                
                logBroker("⚙️ [WEBHOOK PROCESSOR] Ingesting telemetry update: fixture_id=$fixtureId, status=$status, home_score=$homeScore, away_score=$awayScore, elapsed_time=$elapsedTime")
                delay(300)
                
                val matchedId = when (fixtureId) {
                    849201 -> 101 // Map our first main football match (Ethiopia Bunna vs St. George)
                    else -> (fixtureId % 7) + 101
                }
                
                val currentMatch = repository.getDb().matchDao().getMatchById(matchedId)
                if (currentMatch != null) {
                    val updated = currentMatch.copy(
                        scoreA = homeScore,
                        scoreB = awayScore,
                        timeString = "${elapsedTime}'",
                        isLive = status.equals("Live", ignoreCase = true) || status.equals("LIVE", ignoreCase = true)
                    )
                    repository.updateMatch(updated)
                    logBroker("⚽ [WEBHOOK SUCCESS] Match #${matchedId} (${updated.teamA} vs ${updated.teamB}) state updated in DB: ${updated.scoreA}-${updated.scoreB} at ${updated.timeString}")
                    
                    triggerMatchScoreUpdateNotification(updated, "${currentMatch.scoreA}-${currentMatch.scoreB}", "${updated.scoreA}-${updated.scoreB}")
                } else {
                    logBroker("⚠️ [WEBHOOK WARNING] Unknown fixture_id $fixtureId (no matching local match mapped).")
                }
                
                onResponse("""
                    {
                      "success": true,
                      "message": "Match telemetry data stream accepted and processed successfully."
                    }
                """.trimIndent())
                
            } catch (e: Exception) {
                logBroker("❌ [WEBHOOK EXCEPTION] Failed to process webhook payload: ${e.message}")
                onResponse("""
                    {
                      "success": false,
                      "error": "Internal ingestion node error: ${e.message}"
                    }
                """.trimIndent())
            }
        }
    }

    fun processOddsWebhook(jsonPayload: String, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            try {
                logBroker("📥 [WEBHOOK ROUTER] Received POST /api/v1/webhooks/sports-stream/odds")
                delay(200)

                val jsonObject = org.json.JSONObject(jsonPayload)
                val fixtureId = jsonObject.optInt("fixture_id", -1)
                val marketType = jsonObject.optString("market_type", "")
                val oddsBook = jsonObject.optJSONArray("odds_book")

                if (fixtureId == -1 || marketType.isEmpty() || oddsBook == null) {
                    logBroker("❌ [WEBHOOK ERROR] Invalid payload fields or types.")
                    onResponse("""
                        {
                          "success": false,
                          "error": "Invalid ingestion payload format."
                        }
                    """.trimIndent())
                    return@launch
                }

                logBroker("⚙️ [WEBHOOK PROCESSOR] Ingesting odds matrix update: fixture_id=$fixtureId, market_type=$marketType, lines_count=${oddsBook.length()}")
                delay(300)

                val matchedId = when (fixtureId) {
                    849201 -> 101
                    else -> (fixtureId % 7) + 101
                }

                val currentMatch = repository.getDb().matchDao().getMatchById(matchedId)
                if (currentMatch != null) {
                    var updated: com.example.data.model.SportMatch = currentMatch
                    var anySuspended = false

                    for (i in 0 until oddsBook.length()) {
                        val line = oddsBook.getJSONObject(i)
                        val selection = line.optString("selection", "")
                        val price = line.optDouble("price", -1.0)
                        val isSuspended = line.optBoolean("is_suspended", false)

                        if (price > 0.0) {
                            updated = when (selection) {
                                "1" -> updated.copy(odds1 = price)
                                "X" -> updated.copy(oddsX = price)
                                "2" -> updated.copy(odds2 = price)
                                "Over 2.5" -> updated.copy(oddsOver = price)
                                "Under 2.5" -> updated.copy(oddsUnder = price)
                                "BTTS Yes" -> updated.copy(oddsBttsYes = price)
                                "BTTS No" -> updated.copy(oddsBttsNo = price)
                                else -> updated
                            }
                        }
                        if (isSuspended) {
                            anySuspended = true
                        }
                    }

                    // Update match lock state if any selection is suspended
                    updated = updated.copy(isLocked = anySuspended)

                    repository.updateMatch(updated)
                    logBroker("📈 [WEBHOOK SUCCESS] Match #${matchedId} odds matrix successfully updated and locked=${updated.isLocked}")
                } else {
                    logBroker("⚠️ [WEBHOOK WARNING] Unknown fixture_id $fixtureId (no matching local match mapped).")
                }

                onResponse("""
                    {
                      "success": true,
                      "message": "Odds matrix updates stream accepted and processed successfully."
                    }
                """.trimIndent())

            } catch (e: Exception) {
                logBroker("❌ [WEBHOOK EXCEPTION] Failed to process odds webhook payload: ${e.message}")
                onResponse("""
                    {
                      "success": false,
                      "error": "Internal ingestion node error: ${e.message}"
                    }
                """.trimIndent())
            }
        }
    }


    private val _telebirrConsoleLogs = MutableStateFlow<List<String>>(listOf("[SYSTEM] Telebirr Sandbox Engine active."))
    val telebirrConsoleLogs: StateFlow<List<String>> = _telebirrConsoleLogs.asStateFlow()

    fun logConsole(message: String) {
        val currentLogs = _telebirrConsoleLogs.value.toMutableList()
        currentLogs.add(message)
        if (currentLogs.size > 100) currentLogs.removeAt(0)
        _telebirrConsoleLogs.value = currentLogs
    }

    fun clearTelebirrLogs() {
        _telebirrConsoleLogs.value = listOf("[SYSTEM] Console cleared.")
    }

    private val _feedBrokerConsoleLogs = MutableStateFlow<List<String>>(listOf("[BROKER] Sports odds feed broker active."))
    val feedBrokerConsoleLogs: StateFlow<List<String>> = _feedBrokerConsoleLogs.asStateFlow()

    fun logBroker(message: String) {
        val currentLogs = _feedBrokerConsoleLogs.value.toMutableList()
        currentLogs.add(message)
        if (currentLogs.size > 100) currentLogs.removeAt(0)
        _feedBrokerConsoleLogs.value = currentLogs
    }

    fun clearBrokerLogs() {
        _feedBrokerConsoleLogs.value = listOf("[BROKER] Console cleared.")
    }

    private var liveOddsFeedService: com.example.util.LiveOddsFeedService? = null

    private val _liveFeedStatus = MutableStateFlow("Disconnected")
    val liveFeedStatus: StateFlow<String> = _liveFeedStatus.asStateFlow()

    fun startLiveFeedClient(feedUrl: String, apiKey: String) {
        viewModelScope.launch {
            logBroker("📡 [CLIENT START] Requesting WebSocket feed client initialization for URL: $feedUrl...")
            _liveFeedStatus.value = "Connecting"
            
            liveOddsFeedService?.disconnect()
            
            liveOddsFeedService = com.example.util.LiveOddsFeedService(
                feedUrl = feedUrl,
                apiKey = apiKey,
                onValidatedUpdate = { norm ->
                    logBroker("🔥 [WEB_STREAM UPDATE] Match #${norm.matchId} odds updated on ${norm.marketType}: ${norm.selectionName} = ${norm.oddsValue}")
                    repository.processLiveDbUpdate(norm)
                },
                onLogCallback = { log ->
                    logBroker(log)
                    if (log.contains("✅")) {
                        _liveFeedStatus.value = "Connected"
                    } else if (log.contains("⏳") || log.contains("Connecting")) {
                        _liveFeedStatus.value = "Reconnecting"
                    } else if (log.contains("❌") || log.contains("severed") || log.contains("exception")) {
                        _liveFeedStatus.value = "Error/Reconnecting"
                    } else if (log.contains("Clean service disconnect")) {
                        _liveFeedStatus.value = "Disconnected"
                    }
                }
            )
            
            liveOddsFeedService?.connect()
        }
    }

    fun stopLiveFeedClient() {
        logBroker("🔌 [CLIENT STOP] Manually disconnected from the live streamer engine.")
        liveOddsFeedService?.disconnect()
        liveOddsFeedService = null
        _liveFeedStatus.value = "Disconnected"
    }

    fun processRawProviderFeed(jsonPayload: String) {
        viewModelScope.launch {
            logBroker("📡 [INCOMING FEED] Bytes received from WebSocket broker feed channel...")
            delay(310)
            
            val raw = com.example.util.FeedBroker.parseRawPayload(jsonPayload)
            if (raw == null) {
                logBroker("❌ [PARSING ERROR] Failed to parse raw broker feed payload json! Invalid structure.")
                return@launch
            }
            
            logBroker("✅ [PARSING SUCCESS] Parsed event: ${raw.eventId} | Sport: ${raw.sport} | Type: ${raw.status}")
            delay(430)
            
            logBroker("🌀 [NORMALIZING] Invoking normalizer transform maps on ${raw.markets.size} active betting markets...")
            val normalizedList = com.example.util.FeedBroker.normalize(raw)
            delay(520)
            
            logBroker("📝 [NORMALIZATION COMPLETE] Generated ${normalizedList.size} flat SQL/PG-Ready records:")
            normalizedList.forEach { norm ->
                logBroker("   👉 Match: #${norm.matchId} | Market: ${norm.marketType} | Line: ${norm.selectionName} @ ${norm.oddsValue} | Susp: ${norm.isSuspended}")
            }
            delay(400)
            
            logBroker("💼 [DATABASE APPLY] Processing normalized transactions sequentially into Room SQL DB...")
            normalizedList.forEach { norm ->
                repository.processLiveDbUpdate(norm)
            }
            logBroker("🎉 [DB SYNCED] Sports match states, historical indices and relational tables fully updated!")
        }
    }

    fun executeTelebirrCallback(outTradeNo: String, baseAmount: Double, simulateTamper: Boolean) {
        viewModelScope.launch {
            logConsole("📥 [WEBHOOK CALLBACK] Received payment callback invoke from Telebirr APIs...")
            delay(400)
            
            val tradeNo = "TRADE_" + (100000000..999999999).random().toString()
            val totalAmountStr = if (simulateTamper) {
                logConsole("⚠️ [SANDBOX MODE] Tampering payload baseAmount: $baseAmount -> ${(baseAmount * 2.5)}")
                String.format("%.2f", baseAmount * 2.5)
            } else {
                String.format("%.2f", baseAmount)
            }
            
            val transactionStatusCheck = if (simulateTamper) {
                "SUCCESS_SPOOFED_UNAUTHORIZED"
            } else {
                "SUCCESS"
            }

            // 1. Reconstruct alphabetical sign-string from incoming parameters
            val callbackPayload = mutableMapOf<String, Any?>(
                "outTradeNo" to outTradeNo,
                "tradeNo" to tradeNo,
                "transactionStatus" to transactionStatusCheck,
                "totalAmount" to totalAmountStr
            )

            val signString = com.example.util.TelebirrUtil.createSignString(callbackPayload)
            logConsole("🔄 [1/4 ORDERING] Reconstructed alphabetic sign-string payload:\n    $signString")
            delay(500)

            // Let's generate a signature using the Private Key (simulating the Telebirr backend responding)
            val incomingSignature = if (simulateTamper) {
                // Return a bad/spoofed signature or some random keys
                logConsole("🔑 [2/4 EXPLOIT] Injecting spoofed fake signature key...")
                "INVALID_SPOOFED_SIGNATURE_KEY_ATTEMPT_MD5_BLOWFISH"
            } else {
                logConsole("🔑 [2/4 SIGNING] Generated authentic signature using private RSA keys...")
                com.example.util.TelebirrUtil.signPayload(signString, com.example.BuildConfig.MERCHANT_PRIVATE_KEY)
            }
            
            logConsole("✨ Incoming verification Signature: ${incomingSignature.take(kotlin.math.min(incomingSignature.length, 30))}...")
            delay(600)

            // 2. Cryptographically verify signature using Telebirr's Public Key
            logConsole("🔍 [3/4 CRYPTO VERIFY] Invoking SHA256withRSA verifier with Telebirr Public Key...")
            val isVerified = if (simulateTamper) {
                false
            } else {
                com.example.util.TelebirrUtil.verifySignature(
                    signString = signString,
                    incomingSignature = incomingSignature,
                    publicKeyPem = com.example.BuildConfig.TELEBIRR_PUBLIC_KEY
                )
            }
            delay(700)

            if (!isVerified) {
                logConsole("❌ [CRITICAL SECURITY ALERT] Telebirr Callback verification failed!")
                logConsole("⛔ [ACTION REJECTED] Mismatch detected. System integrity preserved. Wallet balance UNCHANGED.")
                return@launch
            }

            logConsole("✅ [4/4 VERIFIED] Cryptographic signature is authentic! Status: $transactionStatusCheck")
            
            // 3. Process business wallet logic
            // Check in local transactions for this pending order
            val allTx = repository.allTransactions.firstOrNull() ?: emptyList()
            val targetTx = allTx.find { it.id == outTradeNo }
            
            if (targetTx == null) {
                logConsole("❌ [IDEMPOTENCY MATCH FAILED] No matching pending transaction found for reference: $outTradeNo")
                return@launch
            }

            if (targetTx.status == "APPROVED") {
                logConsole("⚠️ [IDEMPOTENCY SAFETY] Transaction $outTradeNo has already been processed! Skipping double-crediting.")
                return@launch
            }

            logConsole("💼 [DB SUCCESS] Idempotency checks passed. Resolving transaction: $outTradeNo as APPROVED.")
            repository.updateTransactionStatus(targetTx, "APPROVED")
            logConsole("🎉 [WALLET ADDED] Balance adjusted with +$totalAmountStr ETB. Integration complete and verified.")
        }
    }

    /**
     * Simulation Engine: ticks every 6 seconds to update match scores,
     * time displays, and fluctuates odds to make the App incredibly real time!
     */
    private fun startRealtimeOddsSimulator() {
        viewModelScope.launch {
            while (true) {
                delay(6000) // update every 6 seconds
                val currentMatches = repository.allMatches.firstOrNull() ?: continue
                val liveMatches = currentMatches.filter { it.isLive && it.status == "LIVE" }
                
                if (liveMatches.isNotEmpty()) {
                    // Choose 1 random live match to update
                    val targetMatch = liveMatches.random()
                    
                    // 1. Tick game time string
                    val updatedTime = when {
                        targetMatch.timeString.contains("Set") -> {
                            // Tennis sets / scores
                            val currentSet = targetMatch.timeString.filter { it.isDigit() }.toIntOrNull() ?: 4
                            if (Random.nextDouble() < 0.1) "Set ${if (currentSet < 5) currentSet + 1 else 5}" else targetMatch.timeString
                        }
                        targetMatch.timeString.contains("Map") -> {
                            // Esports maps
                            targetMatch.timeString
                        }
                        targetMatch.timeString.contains("Q") -> {
                            // Basketball quarter ticking
                            val parts = targetMatch.timeString.split(" ")
                            if (parts.size == 2) {
                                val quarter = parts[0]
                                val time = parts[1].replace("'", "").toDoubleOrNull() ?: 3.0
                                val remaining = if (time > 0.5) time - 0.5 else 12.0
                                val nextQuarter = if (time <= 0.5) {
                                    val qNum = quarter.replace("Q", "").toIntOrNull() ?: 4
                                    "Q${if (qNum < 4) qNum + 1 else 4}"
                                } else quarter
                                "$nextQuarter ${Math.round(remaining * 10.0) / 10.0}'"
                            } else targetMatch.timeString
                        }
                        else -> {
                            // Football minute ticking
                            val rawMin = targetMatch.timeString.replace("'", "").toIntOrNull() ?: 65
                            val nextMin = if (rawMin < 90) rawMin + 1 else 90
                            if (rawMin == 90) "90+FT" else "$nextMin'"
                        }
                    }

                    // 2. Chance of a goal/point!
                    var scoreA = targetMatch.scoreA
                    var scoreB = targetMatch.scoreB
                    val goalProbability = when (targetMatch.sport) {
                        "Basketball" -> 0.45 // High scoring frequency
                        "Tennis" -> 0.25      // Point scoring
                        else -> 0.04        // Football goals are rare (4% chance every 6s)
                    }

                    if (Random.nextDouble() < goalProbability) {
                        if (Random.nextBoolean()) {
                            scoreA += if (targetMatch.sport == "Basketball") (2..3).random() else 1
                        } else {
                            scoreB += if (targetMatch.sport == "Basketball") (2..3).random() else 1
                        }
                    }

                    // 3. Fluctuate Odds slightly
                    val factor = if (Random.nextBoolean()) 1.02 else 0.98
                    val newOdds1 = Math.round(targetMatch.odds1 * factor * 100.0) / 100.0
                    val newOdds2 = Math.round(targetMatch.odds2 * (2.0 - factor) * 100.0) / 100.0
                    val newOddsX = if (targetMatch.oddsX > 1.0) {
                        Math.round(targetMatch.oddsX * (if (Random.nextBoolean()) 1.01 else 0.99) * 100.0) / 100.0
                    } else 1.0

                    // Clamp minimum odds to 1.01
                    val finalOdds1 = Math.max(1.01, newOdds1)
                    val finalOdds2 = Math.max(1.01, newOdds2)
                    val finalOddsX = Math.max(1.0, newOddsX)

                    // Implement "match locks until we change odd":
                    // If currently locked, we are changing the odds, so it becomes unlocked!
                    // If unlocked, there's a 15% chance to lock it (and during lock, odds are frozen until they change again).
                    val setLocked: Boolean
                    val actualOdds1: Double
                    val actualOddsX: Double
                    val actualOdds2: Double

                    if (targetMatch.isLocked) {
                        setLocked = false
                        actualOdds1 = finalOdds1
                        actualOddsX = finalOddsX
                        actualOdds2 = finalOdds2
                    } else {
                        if (Random.nextDouble() < 0.15) {
                            setLocked = true
                            actualOdds1 = targetMatch.odds1
                            actualOddsX = targetMatch.oddsX
                            actualOdds2 = targetMatch.odds2
                        } else {
                            setLocked = false
                            actualOdds1 = finalOdds1
                            actualOddsX = finalOddsX
                            actualOdds2 = finalOdds2
                        }
                    }

                    val updatedMatch = targetMatch.copy(
                        timeString = updatedTime,
                        scoreA = scoreA,
                        scoreB = scoreB,
                        odds1 = actualOdds1,
                        oddsX = actualOddsX,
                        odds2 = actualOdds2,
                        isLocked = setLocked
                    )
                    
                    repository.updateMatch(updatedMatch)

                    // EVALUATE ACTIVE CUSTOM PRICE ALERTS
                    val activeAlerts = _customPriceAlerts.value
                    if (activeAlerts.isNotEmpty()) {
                        val alertsForMatch = activeAlerts.filter { it.matchId == updatedMatch.id && !it.isTriggered }
                        for (alert in alertsForMatch) {
                            val currentOdds = when (alert.marketName) {
                                "1" -> updatedMatch.odds1
                                "X" -> updatedMatch.oddsX
                                "2" -> updatedMatch.odds2
                                else -> 0.0
                            }
                            val isConditionMet = if (alert.condition == "ABOVE") {
                                currentOdds >= alert.targetOdds
                            } else {
                                currentOdds <= alert.targetOdds
                            }
                            if (isConditionMet) {
                                addOddsAlert(updatedMatch, "Target Odds Alert on option ${alert.marketName}", alert.targetOdds, currentOdds)
                                _customPriceAlerts.value = _customPriceAlerts.value.filter { it.id != alert.id }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateMatchDirectly(match: SportMatch) {
        viewModelScope.launch {
            repository.updateMatch(match)
        }
    }

    fun registerUser(username: String, email: String, passwordHash: String, role: String = "user") {
        viewModelScope.launch {
            val newUser = UserWallet(
                id = 1,
                username = username,
                email = email,
                passwordHash = passwordHash,
                balance = 523600.00,
                role = role,
                bonusBalance = 12500.00,
                currency = "ETB",
                createdAt = System.currentTimeMillis()
            )
            repository.updateWallet(newUser)
        }
    }

    private fun triggerMatchScoreUpdateNotification(match: SportMatch, oldScore: String, newScore: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val context = getApplication<Application>()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "odds_fluctuation_alerts"
                
                val title = "Score Update: ${match.teamA} vs ${match.teamB}"
                val body = "The score updated to $newScore (was $oldScore). Live time: ${match.timeString}."

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((match.id * 31 + 17).hashCode(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerMatchStartNotification(match: SportMatch) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val context = getApplication<Application>()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "odds_fluctuation_alerts"
                
                val title = "Match Started: ${match.teamA} vs ${match.teamB}"
                val body = "Your selected / tracked match has kicked off! Track livescores and fluctuate odds now."

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((match.id * 31 + 43).hashCode(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- RabbitMQ AMQP Rapid Wager Queue Simulation ---
    private val _isQueueConsumerActive = MutableStateFlow(true)
    val isQueueConsumerActive = _isQueueConsumerActive.asStateFlow()

    private val _queueLogs = MutableStateFlow<List<QueueWagerLog>>(emptyList())
    val queueLogs = _queueLogs.asStateFlow()

    private val _simulatedDbDeadlock = MutableStateFlow(false)
    val simulatedDbDeadlock = _simulatedDbDeadlock.asStateFlow()

    private val _queuePrefetchLimit = MutableStateFlow(100)
    val queuePrefetchLimit = _queuePrefetchLimit.asStateFlow()

    private val _activeQueueSize = MutableStateFlow(0)
    val activeQueueSize = _activeQueueSize.asStateFlow()

    private val _totalBetsQueuedCount = MutableStateFlow(0)
    val totalBetsQueuedCount = _totalBetsQueuedCount.asStateFlow()

    fun toggleQueueConsumer() {
        _isQueueConsumerActive.value = !_isQueueConsumerActive.value
        logBroker("🏁 [MESSAGE QUEUE] RabbitMQ consumer connection " + 
            if (_isQueueConsumerActive.value) "RESUMED. Cluster processing bets." else "PAUSED. Messages buffered in sportsbook_bets_queue.")
    }

    fun toggleSimulatedDbDeadlock() {
        _simulatedDbDeadlock.value = !_simulatedDbDeadlock.value
        logBroker("⚠️ [DATABASE MATRIX] Simulated database lock contention (deadlocks) " + 
            if (_simulatedDbDeadlock.value) "ENABLED. Active wagers will trigger transaction rollback and NACK (Requeue)." else "DISABLED. Atomic transactions committing normally.")
    }

    fun updatePrefetchLimit(limit: Int) {
        _queuePrefetchLimit.value = limit
        logBroker("⚙️ [RABBITMQ CONFIG] Prefetch concurrency limit adjusted: process $limit bets simultaneously per worker node.")
    }

    fun injectRapidWagers(count: Int) {
        viewModelScope.launch {
            logBroker("📥 [API GATEWAY] Pushing $count rapid bets into sportsman queue buffer sportsbook_bets_queue.")
            _activeQueueSize.value += count
            _totalBetsQueuedCount.value += count

            for (i in 0 until count) {
                // Simulating concurrency and network delays
                delay((50..200).random().toLong())

                val matchId = listOf(101, 102, 103, 104, 105).random()
                val matchObj = repository.getDb().matchDao().getMatchById(matchId)
                val teams = if (matchObj != null) "${matchObj.teamA} vs ${matchObj.teamB}" else "Arsenal vs Man United"
                val selections = listOf("1", "X", "2", "Over 2.5", "Under 2.5")
                val selection = selections.random()
                val odds = (1.40 + Random.nextDouble(4.50))
                val roundedOdds = Math.round(odds * 100.0) / 100.0
                val stake = (100..4500).random().toDouble()
                val userId = (1001..9999).random()

                val timeFormat = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.US)
                val timestampStr = timeFormat.format(java.util.Date())

                if (!_isQueueConsumerActive.value) {
                    // Consumer is paused, messages reside on queue disk buffers
                    val logEntry = QueueWagerLog(
                        id = System.currentTimeMillis() + i,
                        timestamp = timestampStr,
                        userId = userId,
                        matchId = matchId,
                        teams = teams,
                        selection = selection,
                        odds = roundedOdds,
                        stake = stake,
                        riskStatus = "PENDING (Buffered)",
                        dbStatus = "N/A (In Queue)",
                        rabbitMqAction = "BUFFERED",
                        latency = 0
                    )
                    _queueLogs.value = (listOf(logEntry) + _queueLogs.value).take(40)
                    continue
                }

                // 1. Run quick background risk checks
                val isApprovedByRisk = if (stake > 3500.0) {
                    Random.nextFloat() > 0.40 // 60% chance to approve extremely high stakes
                } else {
                    true
                }

                val riskStatus = if (isApprovedByRisk) "APPROVED" else "REJECTED (High Exposure Risk)"
                
                var dbStatus = "PENDING"
                var amqpAction = "ACK"
                val latency = (20..110).random()

                if (isApprovedByRisk) {
                    // 2. Perform atomic balance updates and ticket issuance (or simulate)
                    if (_simulatedDbDeadlock.value && Random.nextFloat() > 0.4) {
                        dbStatus = "ROLLED_BACK (Deadlock)"
                        amqpAction = "NACK (Requeued)"
                    } else {
                        try {
                            // Insert actual background bet if random chance permits to keep bets history lively!
                            if (Random.nextFloat() > 0.7 && matchObj != null) {
                                val betItem = Bet(
                                    id = 0, // Auto-increment
                                    userId = 1,
                                    matchId = matchId,
                                    marketType = "1X2",
                                    selection = selection,
                                    odds = roundedOdds,
                                    stake = stake,
                                    potentialReturn = stake * roundedOdds,
                                    status = "PENDING",
                                    sport = matchObj.sport,
                                    teamA = matchObj.teamA,
                                    teamB = matchObj.teamB,
                                    timestamp = System.currentTimeMillis()
                                )
                                repository.getDb().betDao().insertBet(betItem)
                            }
                            dbStatus = "COMMITTED"
                            amqpAction = "ACK"
                        } catch (e: Exception) {
                            dbStatus = "ROLLED_BACK"
                            amqpAction = "NACK (Requeued)"
                        }
                    }
                } else {
                    dbStatus = "N/A"
                    amqpAction = "ACK"
                }

                if (amqpAction.startsWith("ACK")) {
                    _activeQueueSize.value = maxOf(0, _activeQueueSize.value - 1)
                } else {
                    // Nacked bets are requeued and processed again shortly
                    delay(300)
                    logBroker("🔄 [RABBITMQ RECOVERY] Re-processing deadlocked wager message frame from sportsbook_bets_queue.")
                    _activeQueueSize.value = maxOf(0, _activeQueueSize.value - 1)
                    dbStatus = "COMMITTED (Retry)"
                    amqpAction = "ACK"
                }

                val logEntry = QueueWagerLog(
                    id = System.currentTimeMillis() + i,
                    timestamp = timestampStr,
                    userId = userId,
                    matchId = matchId,
                    teams = teams,
                    selection = selection,
                    odds = roundedOdds,
                    stake = stake,
                    riskStatus = riskStatus,
                    dbStatus = dbStatus,
                    rabbitMqAction = amqpAction,
                    latency = latency
                )

                _queueLogs.value = (listOf(logEntry) + _queueLogs.value).take(40)
            }
        }
    }

    // --- Casino Aggregator Handshake Bridge Simulation ---
    private val _casinoSecretKey = MutableStateFlow("SEC_39a2fb8c8d88fa72")
    val casinoSecretKey = _casinoSecretKey.asStateFlow()

    private val _casinoAggregatorOffline = MutableStateFlow(false)
    val casinoAggregatorOffline = _casinoAggregatorOffline.asStateFlow()

    private val _casinoHandshakeResult = MutableStateFlow<CasinoHandshakeResult?>(null)
    val casinoHandshakeResult = _casinoHandshakeResult.asStateFlow()

    private val _aviatorMultiplier = MutableStateFlow(1.0)
    val aviatorMultiplier = _aviatorMultiplier.asStateFlow()

    private var aviatorJob: kotlinx.coroutines.Job? = null

    fun updateCasinoSecretKey(newKey: String) {
        _casinoSecretKey.value = newKey
        logBroker("🔐 [CASINO SECURITY] Configured new Casino HMAC secret key: $newKey")
    }

    fun toggleCasinoAggregatorOffline() {
        _casinoAggregatorOffline.value = !_casinoAggregatorOffline.value
        logBroker("🔌 [CASINO AGGREGATOR] Switched aggregator online status to: " + 
            if (_casinoAggregatorOffline.value) "OFFLINE (Simulated outages/timeouts)" else "ONLINE")
    }

    fun executeCasinoHandshake(userId: Int, userIp: String, gameSlug: String) {
        viewModelScope.launch {
            aviatorJob?.cancel()
            _aviatorMultiplier.value = 1.0

            val timestamp = System.currentTimeMillis() / 1000
            val payload = "user_id=$userId&game=$gameSlug&ip=$userIp&timestamp=$timestamp"
            
            val logs = mutableListOf<String>()
            logs.add("🕒 [Handshake Initiated] Timestamp: $timestamp")
            logs.add("📦 [Crypto Payload] Raw plain parameters string:\n  \"$payload\"")
            
            val secret = _casinoSecretKey.value
            val signature = try {
                val keySpec = javax.crypto.spec.SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
                val mac = javax.crypto.Mac.getInstance("HmacSHA256")
                mac.init(keySpec)
                val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
                bytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                "error_generating_hmac"
            }
            
            logs.add("🔑 [HMAC-SHA256 Sign] Signature hex output:\n  \"$signature\"")

            val requestUrl = "https://api.casino-aggregator.com/v1/games/launch"
            val requestBody = """
                {
                  "user_id": $userId,
                  "game_slug": "$gameSlug",
                  "player_ip": "$userIp",
                  "auth_sig": "$signature",
                  "timestamp": $timestamp
                }
            """.trimIndent()

            logs.add("📡 [HTTP POST] Sending secure authorize handshake to casino servers...")
            logs.add("  POST $requestUrl\n  Body: $requestBody")

            delay(800) // Simulating round-trip network handshake latency

            if (_casinoAggregatorOffline.value) {
                logs.add("❌ [AXIOS ERROR] connect ECONNREFUSED 104.22.4.98:443 - Connection timed out after 5000ms.")
                logs.add("⚠️ [FALLBACK CONTROLLER] CasinoAggregatorBridge connection failed, launching fallback game loop.")
                
                val fallbackUrl = "/games/fallback/$gameSlug"
                logs.add("🎮 [FALLBACK LAUNCH] Successfully loaded internal virtual client engine. Launch URL: $fallbackUrl")
                
                _casinoHandshakeResult.value = CasinoHandshakeResult(
                    timestamp = timestamp,
                    payload = payload,
                    signature = signature,
                    requestUrl = requestUrl,
                    requestBody = requestBody,
                    responseStatus = 503,
                    responseBody = "{\"error\": \"Service Unavailable\", \"code\": \"GATEWAY_TIMEOUT\", \"message\": \"Aggregator backend servers did not respond within safety window.\"}",
                    finalLaunchUrl = fallbackUrl,
                    isFallback = true,
                    logs = logs
                )

                startAviatorTicker()

            } else {
                val mockUuid = java.util.UUID.randomUUID().toString()
                val finalUrl = "https://iframe.casino-aggregator.com/launch/$mockUuid?player=$userId&game=$gameSlug&sig=$signature"
                
                logs.add("✅ [HANDSHAKE OK] Remote casino verified signature parameters and authorized player access.")
                logs.add("🌐 [IFRAME READY] Embed launch URL generated: $finalUrl")

                _casinoHandshakeResult.value = CasinoHandshakeResult(
                    timestamp = timestamp,
                    payload = payload,
                    signature = signature,
                    requestUrl = requestUrl,
                    requestBody = requestBody,
                    responseStatus = 200,
                    responseBody = """
                        {
                          "status": "success",
                          "game_slug": "$gameSlug",
                          "authorized": true,
                          "iframe_launch_url": "$finalUrl",
                          "handshake_latency_ms": 112
                        }
                    """.trimIndent(),
                    finalLaunchUrl = finalUrl,
                    isFallback = false,
                    logs = logs
                )
            }
        }
    }

    private fun startAviatorTicker() {
        aviatorJob = viewModelScope.launch {
            var mult = 1.0
            val maxMult = 2.0 + Random.nextDouble(12.5)
            while (mult < maxMult) {
                delay(120)
                mult += if (mult < 3.0) 0.05 else if (mult < 8.0) 0.15 else 0.45
                _aviatorMultiplier.value = Math.round(mult * 100.0) / 100.0
            }
            logBroker("💥 [AVIATOR CRASHED] Fallback virtual plane crashed at ${_aviatorMultiplier.value}x multiplier!")
        }
    }

    // --- Anti-Fraud Device Fingerprint Engine Simulation ---
    private val _deviceMappings = MutableStateFlow<List<DeviceUserMapping>>(
        listOf(
            DeviceUserMapping(101, "Abebe_BonusSeeker", "HW_HASH_SHA256_A7B9", "197.156.12.5", 2, isFlagged = false),
            DeviceUserMapping(102, "Mulugeta_Syndicate", "HW_HASH_SHA256_A7B9", "197.156.12.18", 4, isFlagged = false),
            DeviceUserMapping(103, "Selam_Duplicator", "HW_HASH_SHA256_A7B9", "197.156.12.44", 8, isFlagged = false),
            DeviceUserMapping(201, "Tariku_CleanUser", "HW_HASH_SHA256_E8C3", "197.156.24.99", 5, isFlagged = false),
            DeviceUserMapping(202, "Lydia_Asefa", "HW_HASH_SHA256_D2F1", "197.156.88.102", 15, isFlagged = false)
        )
    )
    val deviceMappings = _deviceMappings.asStateFlow()

    private val _fraudAuditResult = MutableStateFlow<AntiFraudAuditResult?>(null)
    val fraudAuditResult = _fraudAuditResult.asStateFlow()

    fun resetFraudDatabase() {
        _deviceMappings.value = listOf(
            DeviceUserMapping(101, "Abebe_BonusSeeker", "HW_HASH_SHA256_A7B9", "197.156.12.5", 2, isFlagged = false),
            DeviceUserMapping(102, "Mulugeta_Syndicate", "HW_HASH_SHA256_A7B9", "197.156.12.18", 4, isFlagged = false),
            DeviceUserMapping(103, "Selam_Duplicator", "HW_HASH_SHA256_A7B9", "197.156.12.44", 8, isFlagged = false),
            DeviceUserMapping(201, "Tariku_CleanUser", "HW_HASH_SHA256_E8C3", "197.156.24.99", 5, isFlagged = false),
            DeviceUserMapping(202, "Lydia_Asefa", "HW_HASH_SHA256_D2F1", "197.156.88.102", 15, isFlagged = false)
        )
        _fraudAuditResult.value = null
        logBroker("🛡️ [ANTI-FRAUD RESET] Rolled back device fingerprints database to factory default test benchmarks.")
    }

    fun addManualFingerprintRecord(userId: Int, username: String, fingerprint: String, ipAddress: String, daysAgo: Int) {
        val newList = _deviceMappings.value.toMutableList()
        newList.removeAll { it.userId == userId }
        newList.add(DeviceUserMapping(userId, username, fingerprint, ipAddress, daysAgo, isFlagged = false))
        _deviceMappings.value = newList
        logBroker("🛡️ [ANTI-FRAUD REGISTRY] Manually registered custom device signature: User #$userId ($username) with hardware hash: $fingerprint")
    }

    fun verifyDeviceIntegrity(userId: Int, deviceFingerprint: String, ipAddress: String, username: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val logs = mutableListOf<String>()
            logs.add("🕒 [AntiFraud Core] Connection requested from node-pg.Pool client...")
            delay(300)
            logs.add("✅ [pg.Pool] Connection established. Executing duplicate hardware query...")
            
            val sqlQuery = """
                SELECT id, role, is_flagged FROM users 
                WHERE id != $userId AND device_hardware_hash = '$deviceFingerprint' AND created_at > NOW() - INTERVAL '30 days'
            """.trimIndent()
            
            logs.add("🔍 [POSTGRES RAW SQL]:\n$sqlQuery")
            delay(400)

            // Calculate duplicates
            val otherDevices = _deviceMappings.value.filter { 
                it.userId != userId && it.deviceFingerprint == deviceFingerprint && it.createdAtDaysAgo <= 30 
            }
            
            logs.add("📊 [SQL Output] Query matched ${otherDevices.size} pre-registered account profiles utilizing the identical hardware hash.")

            val clean: Boolean
            val reason: String?
            
            if (otherDevices.size >= 3) {
                clean = false
                reason = "Security Rejection: Hardware environment signature matched multiple registered profiles. Account flagged for review."
                
                logs.add("🚨 [SYNDICATE PATTERN DETECTED] Multi-accounting violation threshold triggered (Count: ${otherDevices.size} >= 3 other accounts).")
                logs.add("🔒 [DB UPDATE] Flagging violator account in relational table: UPDATE users SET is_flagged = TRUE WHERE id = $userId")
                
                // Flag the user in our local DB simulation
                val updatedList = _deviceMappings.value.map {
                    if (it.userId == userId) it.copy(isFlagged = true) else it
                }.toMutableList()
                
                // If the checking user wasn't registered yet, we add them flagged
                if (updatedList.none { it.userId == userId }) {
                    updatedList.add(DeviceUserMapping(userId, username, deviceFingerprint, ipAddress, 0, isFlagged = true))
                }
                _deviceMappings.value = updatedList
                
                logBroker("🚨 [ANTI-FRAUD ALERT] Flagged User #$userId ($username) for multi-accounting syndicate abuse on fingerprint $deviceFingerprint!")
            } else {
                clean = true
                reason = null
                logs.add("🟢 [INTEGRITY OK] Hardware configuration is under the safety threshold (< 3 duplicate accounts).")
                logs.add("💼 [REGISTRATION] Inserting registration mapping to DB...")
                
                val updatedList = _deviceMappings.value.map {
                    if (it.userId == userId) it.copy(deviceFingerprint = deviceFingerprint, ipAddress = ipAddress, isFlagged = false) else it
                }.toMutableList()
                
                if (updatedList.none { it.userId == userId }) {
                    updatedList.add(DeviceUserMapping(userId, username, deviceFingerprint, ipAddress, 0, isFlagged = false))
                }
                _deviceMappings.value = updatedList
                
                logBroker("✅ [ANTI-FRAUD OK] User #$userId device signature registered successfully. Clean state verified.")
            }
            
            logs.add("🔌 [pg.Pool] Releasing active database client back to the connection pool.")

            _fraudAuditResult.value = AntiFraudAuditResult(
                clean = clean,
                reason = reason,
                timestamp = timestamp,
                logs = logs,
                queryTrace = sqlQuery,
                matchedDuplicateUsers = otherDevices
            )
        }
    }
}

data class DeviceUserMapping(
    val userId: Int,
    val username: String,
    val deviceFingerprint: String,
    val ipAddress: String,
    val createdAtDaysAgo: Int,
    val isFlagged: Boolean = false
)

data class AntiFraudAuditResult(
    val clean: Boolean,
    val reason: String?,
    val timestamp: Long,
    val logs: List<String>,
    val queryTrace: String,
    val matchedDuplicateUsers: List<DeviceUserMapping>
)

data class CasinoHandshakeResult(
    val timestamp: Long,
    val payload: String,
    val signature: String,
    val requestUrl: String,
    val requestBody: String,
    val responseStatus: Int,
    val responseBody: String,
    val finalLaunchUrl: String,
    val isFallback: Boolean,
    val logs: List<String>
)

data class QueueWagerLog(
    val id: Long,
    val timestamp: String,
    val userId: Int,
    val matchId: Int,
    val teams: String,
    val selection: String,
    val odds: Double,
    val stake: Double,
    val riskStatus: String,
    val dbStatus: String,
    val rabbitMqAction: String,
    val latency: Int
)

class BetViewModelFactory(
    private val application: Application,
    private val repository: BetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BetViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class ChatMessage(
    val id: String,
    val sender: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
)

data class OddsAlert(
    val id: String,
    val matchId: Int,
    val teamA: String,
    val teamB: String,
    val sport: String,
    val fieldChanged: String,
    val oldValue: Double,
    val newValue: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class CustomPriceAlert(
    val id: String,
    val matchId: Int,
    val teamA: String,
    val teamB: String,
    val marketName: String, // "1", "X", "2"
    val targetOdds: Double,
    val condition: String, // "ABOVE", "BELOW"
    val isTriggered: Boolean = false
)
