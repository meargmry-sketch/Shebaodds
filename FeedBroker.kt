package com.example.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Enterprise Odds Feed Broker & Normalization Engine
 * Translates raw broker feed events (IRawOddsPayload) to Normalized Schema (INormalizedOddsUpdate)
 * matches exact TypeScript requested schema structures in robust Kotlin format.
 */
object FeedBroker {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val rawPayloadAdapter = moshi.adapter(RawOddsPayload::class.java)

    /**
     * Parse raw JSON stream from broker.
     */
    fun parseRawPayload(json: String): RawOddsPayload? {
        return try {
            rawPayloadAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Serialize payload back for mock feeds.
     */
    fun serializeRawPayload(payload: RawOddsPayload): String {
        return rawPayloadAdapter.toJson(payload)
    }

    /**
     * Core Broker Normalization Algorithm:
     * Transforms raw feed model list into flat normalized database transaction ready structures.
     */
    fun normalize(raw: RawOddsPayload): List<NormalizedOddsUpdate> {
        // Extract numeric match ID from eventId (e.g., "sr:match:548912" -> 548912)
        val matchId = raw.eventId.split(":").last().toIntOrNull() ?: 1
        
        val homeScore = raw.score?.home ?: 0
        val awayScore = raw.score?.away ?: 0
        val elapsed = raw.score?.elapsed ?: "0'"

        return raw.markets.flatMap { market ->
            val isSuspended = market.status.lowercase() != "active"
            market.odds.map { item ->
                NormalizedOddsUpdate(
                    matchId = matchId,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    matchTime = elapsed,
                    marketType = market.marketId,
                    selectionName = item.outcome,
                    oddsValue = item.price,
                    isSuspended = isSuspended
                )
            }
        }
    }
}

// ============================================================================
// BROKER MODELS & SCHEMA MATCHING LITERAL TYPES
// ============================================================================

enum class ProviderMarketStatus {
    ACTIVE, SUSPENDED, DEACTIVATED;

    companion object {
        fun fromString(value: String): ProviderMarketStatus {
            return when (value.lowercase()) {
                "active" -> ACTIVE
                "suspended" -> SUSPENDED
                "deactivated" -> DEACTIVATED
                else -> SUSPENDED
            }
        }
    }
}

data class RawScore(
    val home: Int,
    val away: Int,
    val elapsed: String
)

data class RawOddsOutcome(
    val outcome: String, // e.g., "1", "X", "2", "Over 2.5", "Under 2.5"
    val price: Double    // Decimal raw price multiplier
)

data class RawMarket(
    val marketId: String,          // e.g., "1X2" or "total_goals"
    val status: String,            // "active", "suspended", "deactivated"
    val odds: List<RawOddsOutcome>
)

data class RawOddsPayload(
    val eventId: String,           // e.g., "sr:match:548912"
    val timestamp: Long,
    val sport: String,
    val status: String,            // "Live" | "PreMatch" | "Ended"
    val score: RawScore? = null,
    val markets: List<RawMarket>
)

data class NormalizedOddsUpdate(
    val matchId: Int,
    val homeScore: Int,
    val awayScore: Int,
    val matchTime: String,
    val marketType: String,
    val selectionName: String,
    val oddsValue: Double,
    val isSuspended: Boolean
)
