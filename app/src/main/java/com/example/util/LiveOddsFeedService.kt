package com.example.util

import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import kotlin.math.pow

/**
 * LiveOddsFeedService provides high-fidelity, resilient stream client capabilities.
 * It is fully customized to ingest live broker feeds (IRawOddsPayload) and dispatch normalized
 * PG-Ready transaction entities (INormalizedOddsUpdate) after handling defensive checks, keeps-alives,
 * and automatic exponential back-off reconnection routines.
 */
class LiveOddsFeedService(
    private val feedUrl: String,
    private val apiKey: String,
    private val onValidatedUpdate: suspend (NormalizedOddsUpdate) -> Unit,
    private val onLogCallback: (String) -> Unit
) {
    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var heartbeatJob: Job? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10
    private val baseReconnectDelayMs = 2000L
    private var isClosedManually = false

    fun connect() {
        isClosedManually = false
        val authenticatedUrl = "$feedUrl?token=$apiKey"
        onLogCallback("[FEED ENGINE] Initializing secure channel to stream broker...")

        client = OkHttpClient.Builder()
            .build()

        val request = Request.Builder()
            .url(authenticatedUrl)
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onLogCallback("✅ [FEED ENGINE] Secure stream connection established with broker.")
                reconnectAttempts = 0
                startHeartbeatCheck()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    handleIncomingMessage(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                scope.launch {
                    handleIncomingMessage(bytes.utf8())
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                onLogCallback("⚠️ [FEED ENGINE] Connection closing (Code: $code). Reason: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onLogCallback("⚠️ [FEED ENGINE] Connection severed (Code: $code). Reason: $reason")
                if (!isClosedManually) {
                    cleanupAndReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onLogCallback("❌ [FEED ENGINE] WebSocket operational exception caught: ${t.message}")
                if (!isClosedManually) {
                    cleanupAndReconnect()
                }
            }
        })
    }

    private fun startHeartbeatCheck() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(30000) // 30 seconds interval checks
                try {
                    // Send standard WebSocket protocol keep-alive check or custom frame
                    webSocket?.send("{\"type\":\"ping\"}")
                } catch (e: Exception) {
                    onLogCallback("⚠️ [FEED ENGINE] Heartbeat failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleIncomingMessage(text: String) {
        try {
            // Check for heartbeat interceptor or provider confirmation
            if (text.contains("\"type\":\"ping\"") || text.contains("\"type\":\"alive\"") || text.contains("\"type\":\"pong\"")) {
                return
            }

            val payload = FeedBroker.parseRawPayload(text)
            if (payload == null || payload.eventId.isEmpty() || payload.markets.isEmpty()) {
                return
            }

            // Normalizes raw structural formats into flat ledger update metrics
            parseAndRoutePayload(payload)
        } catch (err: Exception) {
            onLogCallback("❌ [FEED ENGINE] Serialization parsing error on incoming frame packet: ${err.message}")
        }
    }

    private suspend fun parseAndRoutePayload(payload: RawOddsPayload) {
        // Map alphanumeric IDs (e.g., "sr:match:1234") to strict integer formats
        val cleanedMatchId = payload.eventId.replace(Regex("\\D+"), "").toIntOrNull() ?: return

        val currentHomeScore = payload.score?.home ?: 0
        val currentAwayScore = payload.score?.away ?: 0
        val elapsedMinutes = payload.score?.elapsed ?: "0'"

        for (market in payload.markets) {
            val isMarketSuspended = market.status.lowercase() != "active"

            for (selection in market.odds) {
                val normalizedUpdate = NormalizedOddsUpdate(
                    matchId = cleanedMatchId,
                    homeScore = currentHomeScore,
                    awayScore = currentAwayScore,
                    matchTime = elapsedMinutes,
                    marketType = market.marketId,
                    selectionName = selection.outcome,
                    oddsValue = selection.price,
                    isSuspended = isMarketSuspended
                )

                try {
                    onValidatedUpdate(normalizedUpdate)
                } catch (dbErr: Exception) {
                    onLogCallback("[DB ROUTE ERROR] Failed processing match target update $cleanedMatchId: ${dbErr.message}")
                }
            }
        }
    }

    private fun cleanupAndReconnect() {
        heartbeatJob?.cancel()
        if (reconnectAttempts >= maxReconnectAttempts) {
            onLogCallback("❌ [CRITICAL ALERT] Maximum odds feed reconnection attempts reached. Operational intervention required.")
            return
        }

        reconnectAttempts++
        // Exponential back-off calculation
        val delayTime = (baseReconnectDelayMs * (2.0.pow(reconnectAttempts.toDouble()))).toLong().coerceAtMost(30000L)
        onLogCallback("⏳ [FEED ENGINE] Attempting connection cycle ($reconnectAttempts/$maxReconnectAttempts) in ${delayTime}ms...")

        scope.launch {
            delay(delayTime)
            if (!isClosedManually) {
                connect()
            }
        }
    }

    fun disconnect() {
        isClosedManually = true
        heartbeatJob?.cancel()
        webSocket?.close(1000, "Clean service disconnect finalized")
        webSocket = null
        onLogCallback("[FEED ENGINE] Clean service disconnect finalized.")
    }
}
