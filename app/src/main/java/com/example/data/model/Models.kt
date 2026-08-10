package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.io.Serializable

// SQL Table: users
@Entity(tableName = "users")
data class UserWallet(
    @PrimaryKey val id: Int = 1,
    val username: String = "super_admin",
    val email: String = "mry3bcha@gmail.com",
    @ColumnInfo(name = "password_hash") val passwordHash: String = "pbkdf2_sha256_mock_hash",
    val balance: Double = 523600.00, // Managed in ETB
    val role: String = "super_admin", // 'user', 'admin', 'super_admin'
    @ColumnInfo(name = "bonus_balance") val bonusBalance: Double = 12500.00,
    val currency: String = "ETB",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) : Serializable

// SQL Table: matches (Relational core table)
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "home_team") val homeTeam: String,
    @ColumnInfo(name = "away_team") val awayTeam: String,
    @ColumnInfo(name = "start_time") val startTime: Long, // TIMESTAMP equivalent
    val status: String = "Not Started", // 'Live', 'Completed', 'Not Started'
    @ColumnInfo(name = "home_score") val homeScore: Int = 0,
    @ColumnInfo(name = "away_score") val awayScore: Int = 0,
    val sport: String = "Football", // Category classification
    @ColumnInfo(name = "time_string") val timeString: String = "" // match minute/status e.g. "65'"
) : Serializable

// SQL Table: markets (Relational core table referencing matches)
@Entity(
    tableName = "markets",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["match_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["match_id"])]
)
data class MarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "match_id") val matchId: Int,
    @ColumnInfo(name = "market_type") val marketType: String, // '1X2', 'Over/Under', 'BTTS'
    @ColumnInfo(name = "home_odds") val homeOdds: Double,
    @ColumnInfo(name = "draw_odds") val drawOdds: Double?,
    @ColumnInfo(name = "away_odds") val awayOdds: Double
) : Serializable

// Unified UI/View Model Model corresponding to a Match + combined loaded Markets
// Stored locally in "sport_matches" table to support backward compatibility and cache
@Entity(tableName = "sport_matches")
data class SportMatch(
    @PrimaryKey val id: Int,
    val sport: String, // Football, Basketball, Tennis, MMA, Esports
    val teamA: String,
    val teamB: String,
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val timeString: String = "", 
    val isLive: Boolean = false,
    val status: String = "NOT_STARTED", // LIVE, NOT_STARTED, FINISHED
    val dateTimeString: String, 
    
    // Joint Odds Markets cached denormalized in UI for rapid reactive rendering
    val odds1: Double,      // Team A win (e.g. 1.85)
    val oddsX: Double,      // Draw (e.g. 3.40)
    val odds2: Double,      // Team B win (e.g. 4.10)
    val oddsOver: Double,   // Over 2.5 goals (e.g. 1.90)
    val oddsUnder: Double,  // Under 2.5 goals (e.g. 2.05)
    val oddsBttsYes: Double, // Both Teams To Score (Yes) (e.g. 1.75)
    val oddsBttsNo: Double,  // Both Teams To Score (No) (e.g. 2.10)
    val isLocked: Boolean = false
) : Serializable {
    fun getOddsForSelection(selection: String): Double {
        return when (selection) {
            "1" -> odds1
            "X" -> oddsX
            "2" -> odds2
            "Over 2.5" -> oddsOver
            "Under 2.5" -> oddsUnder
            "BTTS Yes" -> oddsBttsYes
            "BTTS No" -> oddsBttsNo
            else -> 1.0
        }
    }
}

// SQL Table: bets
@Entity(
    tableName = "bets",
    foreignKeys = [
        ForeignKey(
            entity = UserWallet::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["match_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["match_id"])
    ]
)
data class Bet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int = 1,
    @ColumnInfo(name = "match_id") val matchId: Int,
    @ColumnInfo(name = "market_type") val marketType: String = "1X2",
    val selection: String, // "1", "X", "2", "Over 2.5", "Under 2.5", "BTTS Yes", etc.
    val odds: Double,
    val stake: Double,
    @ColumnInfo(name = "possible_win") val potentialReturn: Double, // SQL generated mapped locally
    val status: String,    // PENDING, WON, LOST, CASHOUT
    @ColumnInfo(name = "created_at") val timestamp: Long = System.currentTimeMillis(),
    val isApproved: Boolean = false,
    
    // Cached Denormalized helper properties for UI compatibility
    val sport: String = "Football",
    val teamA: String = "Home Team",
    val teamB: String = "Away Team",
    val subItemsJson: String? = null
) : Serializable {
    val ticketNumber: String
        get() {
            val idPart = (id * 313 + 7041) % 10000
            val timePart = timestamp % 100000000L
            return String.format("826%01d%08d", idPart % 10, timePart)
        }
}

// Sub-selection item to support multiple matches on a single ticket (Accumulator / Parlay)
data class BetItem(
    val matchId: Int,
    val teamA: String,
    val teamB: String,
    val selection: String,
    val odds: Double,
    val sport: String = "Football",
    val marketType: String = "1X2"
) : Serializable {
    fun toJsonObject(): org.json.JSONObject {
        val obj = org.json.JSONObject()
        obj.put("matchId", matchId)
        obj.put("teamA", teamA)
        obj.put("teamB", teamB)
        obj.put("selection", selection)
        obj.put("odds", odds)
        obj.put("sport", sport)
        obj.put("marketType", marketType)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: org.json.JSONObject): BetItem {
            return BetItem(
                matchId = obj.getInt("matchId"),
                teamA = obj.getString("teamA"),
                teamB = obj.getString("teamB"),
                selection = obj.getString("selection"),
                odds = obj.getDouble("odds"),
                sport = obj.optString("sport", "Football"),
                marketType = obj.optString("marketType", "1X2")
            )
        }
    }
}

fun serializeBetItems(items: List<BetItem>): String {
    val arr = org.json.JSONArray()
    for (item in items) {
        arr.put(item.toJsonObject())
    }
    return arr.toString()
}

fun deserializeBetItems(jsonStr: String?): List<BetItem> {
    if (jsonStr.isNullOrEmpty()) return emptyList()
    val list = mutableListOf<BetItem>()
    try {
        val arr = org.json.JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(BetItem.fromJsonObject(obj))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

// SQL Table: transactions
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserWallet::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"])
    ]
)
data class TransactionRecord(
    @PrimaryKey val id: String, // e.g., "#TRX9852"
    @ColumnInfo(name = "user_id") val userId: Int = 1,
    val type: String,          // DEPOSIT, WITHDRAWAL
    val amount: Double,
    val method: String,        // TeleBirr, CBE Birr
    val status: String,        // APPROVED, PENDING, REJECTED
    @ColumnInfo(name = "time_label") val timeLabel: String, // Localized timestamp representation
    @ColumnInfo(name = "created_at") val timestamp: Long = System.currentTimeMillis()
) : Serializable

val SportMatch.leagueName: String
    get() = when (id) {
        101 -> "Ethiopian Premier League"
        102 -> "English Premier League"
        103 -> "UEFA Champions League"
        104 -> "Egypt Premier League"
        105 -> "NBA"
        106 -> "ATP Wimbledon"
        107 -> "CS2 Major Championship"
        1 -> "Ethiopian Premier League"
        6 -> "NBA"
        11 -> "ATP Wimbledon"
        13 -> "CS2 Major Championship"
        else -> when (sport) {
            "Football" -> "World League Cup"
            "Basketball" -> "Pro Basketball Association"
            "Tennis" -> "Major Tournament Open"
            "Esports" -> "Cyber Championship Series"
            else -> "Super Tournament Cup"
        }
    }

