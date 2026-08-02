package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color constants matching the dark slate developer theme
private val BorderColor = Color(0xFF1E293B)
private val NeonBlue = Color(0xFF00D2FF)
private val NeonGreen = Color(0xFF00FF87)
private val LightRed = Color(0xFFFF4949)
private val TextWhite = Color(0xFFFFFFFF)
private val TextLight = Color(0xFFCBD5E1)
private val TextMuted = Color(0xFF64748B)
private val SlateSurfaceL2 = Color(0xFF0F172A)
private val AmberAccent = Color(0xFFFBBF24)
private val TextGrey = Color(0xFF94A3B8)

@Composable
fun ExpressJwtTabPanel() {
    var jwtHeaderInput by remember { mutableStateOf("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJzdXBlcl9hZG1pbiIsInJvbGUiOiJzdXBlcl9hZG1pbiJ9.verified_signature") }
    var jwtStatusLog by remember { mutableStateOf("Ready Status: Under active Express.js middleware protection.") }
    var isJwtVerified by remember { mutableStateOf(true) }
    var selectedExpressRoute by remember { mutableStateOf("GET /matches/live-odds") }

    var isTestingSpeedMode by remember { mutableStateOf(false) }
    val maxIdleTimeSeconds by remember(isTestingSpeedMode) { derivedStateOf { if (isTestingSpeedMode) 15 else 15 * 60 } }
    var idleTimeSeconds by remember(isTestingSpeedMode) { mutableStateOf(if (isTestingSpeedMode) 15 else 15 * 60) }
    var isIdleExpired by remember { mutableStateOf(false) }
    var refreshStatusLog by remember { mutableStateOf("Ready Status: Refresh token stored in secured HTTPOnly client cookies.") }

    LaunchedEffect(isTestingSpeedMode) {
        idleTimeSeconds = if (isTestingSpeedMode) 15 else 15 * 60
        isIdleExpired = false
    }

    LaunchedEffect(isIdleExpired, idleTimeSeconds) {
        if (!isIdleExpired) {
            if (idleTimeSeconds <= 0) {
                isIdleExpired = true
                jwtHeaderInput = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.Expired"
                jwtStatusLog = "⛔ SESSION INACTIVITY SECURITY DISCONNECT! Admin credentials revoked because of 15-minute idle limit."
            } else {
                delay(1000)
                idleTimeSeconds--
            }
        }
    }

    val resetTimer: () -> Unit = {
        if (!isIdleExpired) {
            idleTimeSeconds = maxIdleTimeSeconds
        }
    }

    var showJwtExpressTypes by remember { mutableStateOf(false) }
    var showExpressMiddleware by remember { mutableStateOf(false) }

    var showNpmInstaller by remember { mutableStateOf(true) }
    var isNpmInstalling by remember { mutableStateOf(false) }
    var npmInstallProgress by remember { mutableStateOf(0f) }
    var npmConsoleLog by remember { mutableStateOf<List<String>>(listOf("Ready Status: Dependency manager active in workspace root. Standing by...")) }
    var installedPackages by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Express Router Pipeline",
                color = NeonBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isJwtVerified) NeonGreen.copy(0.12f) else AmberAccent.copy(0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isJwtVerified) "ROUTER CLOUD ACTIVE" else "HTTP INTERCEPTED",
                    color = if (isJwtVerified) NeonGreen else AmberAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Simulates the Express gateway. Routes challenge header claims against authenticateToken() and authorizeRole() middleware hooks prior to controller execution.",
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 🔐 ADMINISTRATIVE SESSION SECURITY LAYER
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D26)),
            border = BorderStroke(1.dp, Color(0xFF5B3BBF).copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security Guard",
                            tint = Color(0xFFBD00FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔐 Administrative Inactivity Guardian",
                            color = Color(0xFFE0C1FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isIdleExpired) LightRed.copy(0.12f) else if (idleTimeSeconds < 5) AmberAccent.copy(0.12f) else NeonGreen.copy(0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isIdleExpired) "DISCONNECTED" else "SECURE RUNNING",
                            color = if (isIdleExpired) LightRed else if (idleTimeSeconds < 5) AmberAccent else NeonGreen,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display Remaining countdown timer!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESSION IDLE COUNTDOWN",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        val minutes = idleTimeSeconds / 60
                        val seconds = idleTimeSeconds % 60
                        val timerString = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                        Text(
                            text = if (isIdleExpired) "00:00 (SESSION EXPIRED)" else timerString,
                            color = if (isIdleExpired) LightRed else if (idleTimeSeconds < 60) Color(0xFFFF4D4D) else TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SIMULATION LEVEL",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTestingSpeedMode) "FAST EXPIRY (15s)" else "REAL WORLD (15m)",
                                color = if (isTestingSpeedMode) NeonBlue else TextLight,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Checkbox(
                                checked = isTestingSpeedMode,
                                onCheckedChange = { isTestingSpeedMode = it }
                            )
                        }
                    }
                }

                // WARNING NOTIFICATION BANNER IF IDLE EXCEEDING 85%
                val warningThreshold = if (isTestingSpeedMode) 5 else 120 // Warn if less than 2 mins (real) or 5s (fast)
                if (!isIdleExpired && idleTimeSeconds <= warningThreshold) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberAccent.copy(0.12f))
                            .border(0.5.dp, AmberAccent, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "⚠️ WARNING: Administrative session is idle! Revoking security clearance in ${idleTimeSeconds}s.",
                            color = AmberAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isIdleExpired) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightRed.copy(0.12f))
                            .border(0.5.dp, LightRed, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "⛔ IDLE DEAD-TIMEOUT REVOLUTION: All administrative access levels are revoked. Re-authenticating is required to regain access.",
                            color = LightRed,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderColor, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // REFRESH TOKEN LAYER SPEC
                Text(
                    text = "REFRESH SECURITY TOKEN /auth/refresh PORT",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF030508))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Cookie: [HTTPOnly Secure] refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImFjdGlvbiI6InJlZnJlc2gifQ.eyJpZCI6MywidXNlcm5hbWUiOiJhZG1pbl90ZXN0Iiwic2Vzc2lvbklkIjoiYWlzZGV2MSJ9.verified_refresh_signing_key",
                        color = Color(0xFFA5B4FC),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            resetTimer()
                            val pipelineLog = StringBuilder()
                            pipelineLog.append("🌐 POST /api/v1/auth/refresh\n")
                            pipelineLog.append("--> Processing Secure Cookie Refresh Verification Route\n")
                            pipelineLog.append("└ Resolving cryptographically signed token claims...\n\n")
                            pipelineLog.append("✅ Refresh Token Valid! Re-issued new access token:\n")
                            pipelineLog.append("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiJhZG1pbl90ZXN0Iiwicm9sZSI6ImFkbWluIn0.verified_signature\n\n")
                            pipelineLog.append("🎉 ACCESS GRANTED: Administrative session extended by 15 mins.")

                            refreshStatusLog = pipelineLog.toString()
                            jwtHeaderInput = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiJhZG1pbl90ZXN0Iiwicm9sZSI6ImFkbWluIn0.verified_signature"
                            isIdleExpired = false
                            idleTimeSeconds = maxIdleTimeSeconds
                            jwtStatusLog = "Token refreshed successfully to ADMIN preset. Session renewed for $maxIdleTimeSeconds seconds."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(10.dp), tint = TextWhite)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Re-Issue Token (/auth/refresh)", fontSize = 8.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            jwtHeaderInput = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJzdXBlcl9hZG1pbiIsInJvbGUiOiJzdXBlcl9hZG1pbiJ9.verified_signature"
                            isIdleExpired = false
                            idleTimeSeconds = maxIdleTimeSeconds
                            jwtStatusLog = "Re-Authenticated Admin Session. Token upgraded to SUPER_ADMIN."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(text = "Re-Authenticate (Manual)", fontSize = 8.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "REFRESH PIPELINE FEED:",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF030508))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp)
                ) {
                    Text(
                        text = refreshStatusLog,
                        color = Color(0xFF34D399),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "1. SELECT EXPRESS TARGET ROUTE ENDPOINT",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        val expressRoutes = listOf(
            "GET" to "/matches/live-odds" to "🔓 Public Route (No auth)",
            "POST" to "/bets/place" to "🔒 Requires Role: 'user'",
            "PUT" to "/matches/:id/score" to "🔒 Requires Role: 'admin'",
            "POST" to "/admin/wallets/manual-credit" to "🔒 Requires Role: 'super_admin'"
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            expressRoutes.forEach { routeInfo ->
                val (metPath, info) = routeInfo
                val (method, path) = metPath
                val routeKey = "$method $path"
                val isSelected = selectedExpressRoute == routeKey

                val methodColor = when (method) {
                    "GET" -> NeonGreen
                    "POST" -> NeonBlue
                    else -> AmberAccent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF0F1622) else SlateSurfaceL2)
                        .border(0.5.dp, if (isSelected) NeonBlue.copy(0.8f) else BorderColor.copy(0.5f), RoundedCornerShape(4.dp))
                        .clickable { selectedExpressRoute = routeKey }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(methodColor.copy(0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = method,
                            color = methodColor,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = path,
                            color = if (isSelected) TextWhite else TextLight,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = info,
                            color = TextMuted,
                            fontSize = 8.sp
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(NeonBlue, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "2. CHOOSE CLIENT PRE-SIGNED JWT IDENTITY PRESET",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tokenPresets = listOf(
                "super_admin" to NeonGreen,
                "admin" to NeonBlue,
                "user" to Color(0xFFE2E8F0),
                "invalid" to LightRed
            )
            tokenPresets.forEach { (preset, color) ->
                val isPresetActive = when (preset) {
                    "super_admin" -> jwtHeaderInput.contains("super_admin") && !jwtHeaderInput.contains("BAD_SIGNATURE")
                    "admin" -> jwtHeaderInput.contains("admin_test") && !jwtHeaderInput.contains("BAD_SIGNATURE")
                    "user" -> jwtHeaderInput.contains("john") && !jwtHeaderInput.contains("BAD_SIGNATURE")
                    else -> jwtHeaderInput.contains("BAD_SIGNATURE")
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPresetActive) color.copy(0.12f) else SlateSurfaceL2)
                        .border(0.5.dp, if (isPresetActive) color else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable {
                            jwtHeaderInput = when (preset) {
                                "super_admin" -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJzdXBlcl9hZG1pbiIsInJvbGUiOiJzdXBlcl9hZG1pbiJ9.verified_signature"
                                "admin" -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MywidXNlcm5hbWUiOiJhZG1pbl90ZXN0Iiwicm9sZSI6ImFkbWluIn0.verified_signature"
                                "user" -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTIsInVzZXJuYW1lIjoiam9obiIsInJvbGUiOiJ1c2VyIn0.verified_signature"
                                else -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTIsInVzZXJuYW1lIjoiam9obiIsInJvbGUiOiJ1c2VyIn0.BAD_SIGNATURE_TAMPER"
                            }
                            jwtStatusLog = "Token upgraded to ${preset.uppercase()}. Ready for Express router challenge."
                            isJwtVerified = true
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(preset.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isPresetActive) color else TextLight)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "HTTP AUTHORIZATION BEARER HEADER",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = jwtHeaderInput,
            onValueChange = { jwtHeaderInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Bearer token...", color = TextGrey) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextLight,
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color(0xFF090D13),
                unfocusedContainerColor = Color(0xFF090D13)
            ),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val header = jwtHeaderInput.trim()
                    val token = if (header.startsWith("Bearer ", ignoreCase = true)) {
                        header.substring(7).trim()
                    } else {
                        null
                    }

                    val pipelineLog = StringBuilder()
                    pipelineLog.append("🌐 HTTP REQUEST INTERCEPTED\n")
                    pipelineLog.append("--> $selectedExpressRoute\n\n")

                    if (selectedExpressRoute == "GET /matches/live-odds") {
                        isJwtVerified = true
                        pipelineLog.append("🔓 RULE EVALUATION: Route is public.\n")
                        pipelineLog.append("└ Bypassing authenticateToken & authorizeRole chains.\n\n")
                        pipelineLog.append("✅ HTTP 200 OK\n")
                        pipelineLog.append("{\n  \"status\": \"Success\",\n  \"data\": \"Public odds calculations list feed.\"\n}")
                        jwtStatusLog = pipelineLog.toString()
                        return@Button
                    }

                    pipelineLog.append("⚙️ PIPELINE STEP 1: authenticateToken()\n")
                    if (token.isNullOrEmpty()) {
                        isJwtVerified = false
                        pipelineLog.append("❌ Verification Failed: Header is empty or malformed.\n\n")
                        pipelineLog.append("🚫 HTTP 401 Unauthorized\n")
                        pipelineLog.append("{\n  \"success\": false,\n  \"message\": \"Access Denied: Missing authentication token payload structures.\"\n}")
                        jwtStatusLog = pipelineLog.toString()
                        return@Button
                    }

                    if (token.contains("BAD_SIGNATURE_TAMPER")) {
                        isJwtVerified = false
                        pipelineLog.append("❌ Verification Failed: HS256 signature is invalid!\n\n")
                        pipelineLog.append("🚫 HTTP 403 Forbidden\n")
                        pipelineLog.append("{\n  \"success\": false,\n  \"message\": \"Access Forbidden: Invalid or expired authentication credentials.\"\n}")
                        jwtStatusLog = pipelineLog.toString()
                        return@Button
                    }

                    val isSuperAdmin = token.contains("super_admin")
                    val isAdmin = token.contains("admin_test")
                    val isUser = token.contains("john")
                    val decodedRole = if (isSuperAdmin) "super_admin" else if (isAdmin) "admin" else if (isUser) "user" else null

                    if (decodedRole == null) {
                        isJwtVerified = false
                        pipelineLog.append("❌ Verification Failed: Token has unmapped role claims.\n\n")
                        pipelineLog.append("🚫 HTTP 403 Forbidden\n")
                        pipelineLog.append("{\n  \"success\": false,\n  \"message\": \"Access Forbidden: Invalid or expired authentication credentials.\"\n}")
                        jwtStatusLog = pipelineLog.toString()
                        return@Button
                    }

                    val matchingId = if (isSuperAdmin) 1 else if (isAdmin) 3 else 12
                    pipelineLog.append("✅ Verification Approved!\n")
                    pipelineLog.append("└ Assigned req.user = { id: $matchingId, username: \"$decodedRole\", role: \"$decodedRole\" }\n\n")

                    val requiredRole = when (selectedExpressRoute) {
                        "POST /bets/place" -> "user"
                        "PUT /matches/:id/score" -> "admin"
                        "POST /admin/wallets/manual-credit" -> "super_admin"
                        else -> "user"
                    }

                    pipelineLog.append("⚙️ PIPELINE STEP 2: authorizeRole(\"$requiredRole\")\n")
                    val roleHierarchy = mapOf("user" to 1, "admin" to 2, "super_admin" to 3)
                    val userRoleLevel = roleHierarchy[decodedRole] ?: 1
                    val requiredRoleLevel = roleHierarchy[requiredRole] ?: 1

                    if (userRoleLevel < requiredRoleLevel) {
                        isJwtVerified = false
                        pipelineLog.append("❌ Guard Check Failed: '${decodedRole}' is lower than required '${requiredRole}' tier.\n\n")
                        pipelineLog.append("🚫 HTTP 403 Forbidden\n")
                        pipelineLog.append("{\n  \"success\": false,\n  \"message\": \"Access Forbidden: This operation requires at least a '$requiredRole' access clearance tier.\"\n}")
                    } else {
                        isJwtVerified = true
                        pipelineLog.append("✅ Guard Check Approved: User role Level ($userRoleLevel) satisfies route minimum level ($requiredRoleLevel).\n\n")
                        pipelineLog.append("🎉 PIPELINE RESOLVED: Downstream controller route matches!\n")
                        pipelineLog.append("✅ HTTP 200 OK\n")

                        val responseBody = when (selectedExpressRoute) {
                            "POST /bets/place" -> "{\n  \"success\": true,\n  \"message\": \"Wager accepted for user identity: $matchingId\"\n}"
                            "PUT /matches/:id/score" -> "{\n  \"success\": true,\n  \"message\": \"Match scoreline adjusted by administrative authorization.\"\n}"
                            else -> "{\n  \"success\": true,\n  \"message\": \"Ledger transaction updated manually via super_admin account override rules.\"\n}"
                        }
                        pipelineLog.append(responseBody)
                    }

                    jwtStatusLog = pipelineLog.toString()
                },
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Simulate Route Middleware", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }

            Button(
                onClick = {
                    jwtHeaderInput = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJzdXBlcl9hZG1pbiIsInJvbGUiOiJzdXBlcl9hZG1pbiJ9.verified_signature"
                    jwtStatusLog = "Restored default signed super_admin token. Ready for verification."
                    isJwtVerified = true
                    selectedExpressRoute = "GET /matches/live-odds"
                },
                modifier = Modifier.weight(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Reset Route", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLight)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "EXPRESS GATEWAY ROUTE RESPONSE SYSTEM LOG:",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text = jwtStatusLog,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = if (isJwtVerified) NeonGreen else AmberAccent,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderColor.copy(0.4f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // NPM Dependency Manager Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Install icon",
                    tint = NeonBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "📦 NPM Dependency Manager (Gateway Host)",
                    color = NeonBlue,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (installedPackages.isNotEmpty()) NeonGreen.copy(0.12f) else SlateSurfaceL2)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (installedPackages.isNotEmpty()) "4 PACKAGES RESOLVED" else "STANDBY",
                    color = if (installedPackages.isNotEmpty()) NeonGreen else TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Install essential production-grade services to structure and protect the administrative microservice gateway.",
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Visual command command panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030508), RoundedCornerShape(6.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "terminal - root@express-microservice-host:~#",
                        color = TextMuted,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(LightRed, CircleShape))
                        Box(modifier = Modifier.size(6.dp).background(AmberAccent, CircleShape))
                        Box(modifier = Modifier.size(6.dp).background(NeonGreen, CircleShape))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$ npm install express dotenv body-parser uuid",
                    color = TextWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                if (isNpmInstalling) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { npmInstallProgress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.5.dp)),
                        color = NeonBlue,
                        trackColor = SlateSurfaceL2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 110.dp)
                        .background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    val consoleScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(consoleScrollState)
                    ) {
                        npmConsoleLog.forEach { logLine ->
                            Text(
                                text = logLine,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = if (logLine.startsWith("❌") || logLine.contains("Failed")) LightRed 
                                       else if (logLine.startsWith("[SUCCESS]")) NeonGreen 
                                       else if (logLine.contains("added")) NeonGreen.copy(0.81f)
                                       else TextLight,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    if (isNpmInstalling) return@Button
                    scope.launch {
                        isNpmInstalling = true
                        npmInstallProgress = 0f
                        npmConsoleLog = listOf("[$] npm install express dotenv body-parser uuid")

                        val logSteps = listOf(
                            "--> npm warm: package.json missing name/version descriptor. Initializing spec...",
                            "--> resolving dependencies (direct & peer trees)...",
                            "--> fetching metadata registries from npmjs...",
                            "+ express@4.19.2 (28 dependencies verified)",
                            "+ dotenv@16.4.5 (secure environments standard)",
                            "+ body-parser@1.20.2 (payload security parser)",
                            "+ uuid@9.0.1 (cryptographic tracking token generator)",
                            "--> compiling binary dependency bindings...",
                            "--> checking credentials & package signatures...",
                            "npm success: added 58 packages, updated 1 package, audited 144 packages in 1.84s",
                            "[SUCCESS] Installed packages mapped securely to node_modules/.",
                            "[SUCCESS] Registered 4 key production pipeline libraries inside package.json."
                        )

                        for (i in logSteps.indices) {
                            delay(250)
                            npmInstallProgress = (i + 1).toFloat() / logSteps.size
                            npmConsoleLog = npmConsoleLog + logSteps[i]
                        }

                        installedPackages = setOf("express", "dotenv", "body-parser", "uuid")
                        isNpmInstalling = false
                    }
                },
                enabled = !isNpmInstalling,
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(
                    text = if (isNpmInstalling) "Installing..." else "Execute npm install", 
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color.Black
                )
            }

            Button(
                onClick = {
                    installedPackages = emptySet()
                    npmConsoleLog = listOf("Ready Status: Dependency manager active in workspace root. Standing by...")
                    npmInstallProgress = 0f
                    isNpmInstalling = false
                },
                enabled = !isNpmInstalling && installedPackages.isNotEmpty(),
                modifier = Modifier.weight(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Reset Host", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLight)
            }
        }

        if (installedPackages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "VERIFIED INJECTED PACKAGE REGISTRIES:",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            val metaInfo = listOf(
                Triple("express", "^4.19.2", "High-performance HTTP REST framework & middleware engine routing requests."),
                Triple("dotenv", "^16.4.5", "Secure environmental configuration loader for injecting secrets securely."),
                Triple("body-parser", "^1.20.2", "Inline request body stream parsed into system JSON payload properties."),
                Triple("uuid", "^9.0.1", "Issues cryptographically strong UUID generation for tracking audit logs.")
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                metaInfo.forEach { (pkgName, pkgVer, pkgDesc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF070B11))
                            .border(0.5.dp, BorderColor.copy(0.7f), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified icon",
                            tint = NeonGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkgName,
                                    color = NeonBlue,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pkgVer,
                                    color = TextMuted,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = pkgDesc,
                                color = TextLight,
                                fontSize = 8.sp,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1622))
                    .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                    .clickable { showJwtExpressTypes = !showJwtExpressTypes }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Express types icon",
                        tint = NeonBlue,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "jwt_express_types.d.ts Type Declarations",
                        color = TextWhite,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = if (showJwtExpressTypes) "HIDE TYPES ▲" else "VIEW TYPES ▼",
                    color = NeonBlue,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (showJwtExpressTypes) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = """
                            import { Request } from 'express';

                            export type UserRole = 'user' | 'admin' | 'super_admin';

                            export interface IJwtPayload {
                              id: number;
                              username: string;
                              role: UserRole;
                            }

                            // Extends the global Express Request namespace safely
                            declare global {
                              namespace Express {
                                interface Request {
                                  user?: IJwtPayload;
                                }
                              }
                            }
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = TextLight,
                        lineHeight = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1622))
                    .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                    .clickable { showExpressMiddleware = !showExpressMiddleware }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Express middleware icon",
                        tint = NeonBlue,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "authMiddleware.ts & roleGuard.ts Middlewares",
                        color = TextWhite,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = if (showExpressMiddleware) "HIDE MIDDLEWARE ▲" else "VIEW MIDDLEWARE ▼",
                    color = NeonBlue,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (showExpressMiddleware) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔑 PIPELINE MIDDLEWARE: authenticateToken",
                        color = NeonGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = """
                            import { Request, Response, NextFunction } from 'express';
                            import jwt from 'jsonwebtoken';
                            import { IJwtPayload, UserRole } from './types';

                            const JWT_SECRET = process.env.JWT_SECRET || 'your_super_dense_production_jwt_secret_key';

                            /**
                             * Enforces valid identity verification tokens across endpoints
                             */
                            export const authenticateToken = (req: Request, res: Response, next: NextFunction) => {
                              const authHeader = req.headers['authorization'];
                              
                              // Format expectation: "Bearer <token_string>"
                              const token = authHeader && authHeader.split(' ')[1];

                              if (!token) {
                                return res.status(401).json({ 
                                  success: false, 
                                  message: "Access Denied: Missing authentication token payload structures." 
                                });
                              }

                              try {
                                const verifiedUser = jwt.verify(token, JWT_SECRET) as IJwtPayload;
                                
                                // Assign user payload to request for downstream middlewares to utilize
                                req.user = verifiedUser;
                                next();
                              } catch (error) {
                                return res.status(403).json({ 
                                  success: false, 
                                  message: "Access Forbidden: Invalid or expired authentication credentials." 
                                });
                              }
                            };
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.5.sp,
                        color = TextLight,
                        lineHeight = 10.5.sp
                    )

                    HorizontalDivider(color = BorderColor.copy(0.4f), thickness = 0.5.dp)

                    Text(
                        text = "🛡️ PIPELINE GUARD: authorizeRole",
                        color = NeonBlue,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = """
                            import { Request, Response, NextFunction } from 'express';
                            import { UserRole } from './types';

                            // Numeric hierarchy mapping to enforce inheritance layers cleanly
                            const roleHierarchy: Record<UserRole, number> = {
                              'user': 1,
                              'admin': 2,
                              'super_admin': 3
                            };

                            /**
                             * Authorizes access based on minimum required role capability levels
                             * @param requiredRole The minimum role required to access the target route
                             */
                            export const authorizeRole = (requiredRole: UserRole) => {
                              return (req: Request, res: Response, next: NextFunction) => {
                                // Safety check ensuring authentication middleware was declared preceding this guard
                                if (!req.user) {
                                  return res.status(501).json({ 
                                    success: false, 
                                    message: "Internal Server Configuration Conflict: User identity state unassigned." 
                                  });
                                }

                                const userRoleLevel = roleHierarchy[req.user.role];
                                const requiredRoleLevel = roleHierarchy[requiredRole];

                                // Evaluate structural hierarchy requirements
                                if (userRoleLevel < requiredRoleLevel) {
                                  return res.status(403).json({ 
                                    success: false, 
                                    message: `Access Forbidden: This operation requires at least a '${'$'}{requiredRole}' access clearance tier.` 
                                  });
                                }

                                next();
                              };
                            };
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.5.sp,
                        color = TextLight,
                        lineHeight = 10.5.sp
                    )
                }
            }
        }
    }
}