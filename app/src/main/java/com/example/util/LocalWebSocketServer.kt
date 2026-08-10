package com.example.util

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.CopyOnWriteArrayList

object LocalWebSocketServer {
    private const val TAG = "LocalWebSocketServer"
    private const val PORT = 9090
    private var serverSocket: ServerSocket? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeClients = CopyOnWriteArrayList<ClientSession>()
    
    fun start() {
        if (job != null && job?.isActive == true) {
            Log.d(TAG, "Local WebSocket Server is already running")
            return
        }
        
        job = scope.launch {
            try {
                Log.d(TAG, "Starting Local WebSocket Server on port $PORT...")
                serverSocket = ServerSocket(PORT)
                
                // Start a background broadcast loop to generate scores and odds shifts
                launch {
                    runBroadcastLoop()
                }
                
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    Log.d(TAG, "New socket connection from: ${socket.remoteSocketAddress}")
                    val session = ClientSession(socket)
                    activeClients.add(session)
                    session.start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in Local WebSocket Server: ${e.message}")
            } finally {
                stop()
            }
        }
    }
    
    fun stop() {
        Log.d(TAG, "Stopping Local WebSocket Server...")
        job?.cancel()
        job = null
        try {
            serverSocket?.close()
        } catch (e: Exception) {}
        serverSocket = null
        
        for (client in activeClients) {
            client.close()
        }
        activeClients.clear()
    }
    
    private suspend fun CoroutineScope.runBroadcastLoop() {
        // Map of matchId -> (homeScore, awayScore)
        val gameScores = mutableMapOf(
            101 to Pair(2, 1),
            102 to Pair(0, 0),
            103 to Pair(1, 1),
            105 to Pair(98, 95),
            106 to Pair(2, 1),
            107 to Pair(1, 1)
        )
        // Map of matchId -> elapsed minutes or set text
        val elapsedMinutes = mutableMapOf(
            101 to 64,
            102 to 22,
            103 to 88,
            105 to 40, // basketball Q4 minutes representation
            106 to 4,  // set 4 tennis
            107 to 3   // esport map 3
        )
        // Map of matchId -> Sport
        val matchSports = mapOf(
            101 to "Football",
            102 to "Football",
            103 to "Football",
            105 to "Basketball",
            106 to "Tennis",
            107 to "Esports"
        )
        
        while (isActive) {
            delay(4000) // update every 4 seconds
            if (activeClients.isEmpty()) continue
            
            // Randomly select and update one match
            val matchId = gameScores.keys.random()
            val sport = matchSports[matchId] ?: "Football"
            val currentScore = gameScores[matchId] ?: Pair(0, 0)
            
            // Randomly advance scores (25% chance of goal or point)
            var newScore = currentScore
            if ((1..100).random() < 25) {
                newScore = if (sport == "Basketball") {
                    // basketball increments by 2 or 3
                    val delta = if ((1..100).random() > 50) 2 else 3
                    if ((1..100).random() > 50) {
                        Pair(currentScore.first + delta, currentScore.second)
                    } else {
                        Pair(currentScore.first, currentScore.second + delta)
                    }
                } else if (sport == "Tennis" || sport == "Esports") {
                    // minor tennis point increments check, skip score changes for simplicity, or just increase tennis set score or games
                    if ((1..100).random() > 50) {
                        Pair(currentScore.first + 1, currentScore.second)
                    } else {
                        Pair(currentScore.first, currentScore.second + 1)
                    }
                } else {
                    // football increments by 1
                    if ((1..100).random() > 50) {
                        Pair(currentScore.first + 1, currentScore.second)
                    } else {
                        Pair(currentScore.first, currentScore.second + 1)
                    }
                }
                gameScores[matchId] = newScore
            }
            
            // Increment clock / minutes representation
            val nextMinutes = (elapsedMinutes[matchId] ?: 0) + 1
            elapsedMinutes[matchId] = if (sport == "Football" && nextMinutes > 90) 90 else if (sport == "Basketball" && nextMinutes > 12) 12 else nextMinutes
            
            val clockStr = when (sport) {
                "Football" -> "$nextMinutes'"
                "Basketball" -> "Q4 $nextMinutes'"
                "Tennis" -> "Set $nextMinutes"
                "Esports" -> "Map $nextMinutes"
                else -> "Live"
            }
            
            // Compute randomized organic-looking shifting decimal odds
            val oddsFactor = (1..100).random().toDouble() / 100.0 // 0.0 to 1.0
            val odds1 = 1.15 + (oddsFactor * 3.5)
            val oddsX = 1.40 + (oddsFactor * 2.8)
            val odds2 = 1.25 + (oddsFactor * 4.2)
            
            // Create JSON Payload string matching raw JSON format exactly!
            val payloadJson = """
            {
              "eventId": "sr:match:${matchId}",
              "timestamp": ${System.currentTimeMillis()},
              "sport": "${sport}",
              "status": "Live",
              "score": {
                "home": ${newScore.first},
                "away": ${newScore.second},
                "elapsed": "${clockStr}"
              },
              "markets": [
                {
                  "marketId": "1X2",
                  "status": "active",
                  "odds": [
                    { "outcome": "1", "price": ${String.format(java.util.Locale.US, "%.2f", odds1)} },
                    { "outcome": "X", "price": ${String.format(java.util.Locale.US, "%.2f", oddsX)} },
                    { "outcome": "2", "price": ${String.format(java.util.Locale.US, "%.2f", odds2)} }
                  ]
                },
                {
                  "marketId": "Over/Under",
                  "status": "active",
                  "odds": [
                    { "outcome": "Over 2.5", "price": ${String.format(java.util.Locale.US, "%.2f", 1.2 + oddsFactor * 1.5)} },
                    { "outcome": "Under 2.5", "price": ${String.format(java.util.Locale.US, "%.2f", 1.4 + (1.0 - oddsFactor) * 1.8)} }
                  ]
                }
              ]
            }
            """.trimIndent()
            
            broadcast(payloadJson)
        }
    }
    
    private fun broadcast(payload: String) {
        val deadSessions = mutableListOf<ClientSession>()
        for (client in activeClients) {
            if (!client.isConnected()) {
                deadSessions.add(client)
                continue
            }
            try {
                client.sendFrame(payload)
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending broadcast to client: ${e.message}")
                deadSessions.add(client)
            }
        }
        if (deadSessions.isNotEmpty()) {
            activeClients.removeAll(deadSessions)
            deadSessions.forEach { it.close() }
        }
    }
    
    internal class ClientSession(private val socket: Socket) {
        private var job: Job? = null
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var isHandshaked = false
        
        fun start() {
            job = scope.launch {
                try {
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val out = socket.getOutputStream()
                    var line: String? = reader.readLine()
                    val headers = mutableMapOf<String, String>()
                    
                    while (line != null && line.isNotEmpty()) {
                        val parts = line.split(":", limit = 2)
                        if (parts.size == 2) {
                            headers[parts[0].trim().lowercase()] = parts[1].trim()
                        }
                        line = reader.readLine()
                    }
                    
                    val wsKey = headers["sec-websocket-key"]
                    if (wsKey != null) {
                        val acceptVal = calculateAcceptValue(wsKey)
                        val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                                "Upgrade: websocket\r\n" +
                                "Connection: Upgrade\r\n" +
                                "Sec-WebSocket-Accept: $acceptVal\r\n\r\n"
                        out.write(response.toByteArray(Charsets.UTF_8))
                        out.flush()
                        isHandshaked = true
                        Log.d(TAG, "WebSocket Handshake completed successfully!")
                        
                        // Keep listening to client messages (heartbeats and pings/pongs)
                        val inStream = socket.getInputStream()
                        while (isActive && isConnected()) {
                            val firstByte = inStream.read()
                            if (firstByte == -1) break
                            
                            val secondByte = inStream.read()
                            if (secondByte == -1) break
                            
                            val isMasked = (secondByte and 0x80) != 0
                            var payloadLen = secondByte and 0x7F
                            
                            if (payloadLen == 126) {
                                val b1 = inStream.read()
                                val b2 = inStream.read()
                                payloadLen = (b1 ushr 8) or b2
                            } else if (payloadLen == 127) {
                                // 64 bit, skip big values
                                for (i in 1..8) inStream.read()
                                payloadLen = 0
                            }
                            
                            val maskingKey = ByteArray(4)
                            if (isMasked) {
                                inStream.read(maskingKey)
                            }
                            
                            val payload = ByteArray(payloadLen)
                            var readBytes = 0
                            while (readBytes < payloadLen) {
                                val chunk = inStream.read(payload, readBytes, payloadLen - readBytes)
                                if (chunk == -1) break
                                readBytes += chunk
                            }
                            
                            if (isMasked) {
                                for (i in 0 until payloadLen) {
                                    payload[i] = (payload[i].toInt() xor maskingKey[i % 4].toInt()).toByte()
                                }
                            }
                            
                            val textMsg = String(payload, Charsets.UTF_8)
                            if (textMsg.contains("ping")) {
                                sendFrame("{\"type\":\"pong\"}")
                            }
                        }
                    } else {
                        socket.close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in client thread: ${e.message}")
                } finally {
                    close()
                }
            }
        }
        
        fun sendFrame(payload: String) {
            if (!isHandshaked || !isConnected()) return
            synchronized(socket) {
                val out = socket.getOutputStream()
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val len = bytes.size
                out.write(0x81) // Text frame, fin bit set
                if (len <= 125) {
                    out.write(len)
                } else if (len <= 65535) {
                    out.write(126)
                    out.write((len ushr 8) and 0xFF)
                    out.write(len and 0xFF)
                } else {
                    out.write(127)
                    out.write(0)
                    out.write(0)
                    out.write(0)
                    out.write(0)
                    out.write((len ushr 24) and 0xFF)
                    out.write((len ushr 16) and 0xFF)
                    out.write((len ushr 8) and 0xFF)
                    out.write(len and 0xFF)
                }
                out.write(bytes)
                out.flush()
            }
        }
        
        fun isConnected(): Boolean {
            return !socket.isClosed && socket.isConnected
        }
        
        fun close() {
            job?.cancel()
            job = null
            try {
                socket.close()
            } catch (e: Exception) {}
        }
        
        private fun calculateAcceptValue(key: String): String {
            val md = MessageDigest.getInstance("SHA-1")
            val input = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            return android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP)
        }
    }
}
