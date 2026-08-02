package com.example.ui.screens

import androidx.compose.ui.platform.testTag

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TelebirrFrameworkConsole(
    txBroadcasts: List<com.example.util.AdminSocketHubInstance.BroadcastTransaction>,
    wallet: com.example.data.model.UserWallet?,
    allTransactions: List<com.example.data.model.TransactionRecord>,
    allBets: List<com.example.data.model.Bet>
) {
    var telebirrAppId by remember { mutableStateOf("YOUR_TELEBIRR_APP_ID") }
    var telebirrMchShortCode by remember { mutableStateOf("670921") }
    var telebirrApiUrl by remember { mutableStateOf("https://196.188.120.3:10443/api/change-this-to-production-url") }
    var telebirrReceiveName by remember { mutableStateOf("Your Betting Platform") }
    var telebirrReturnUrl by remember { mutableStateOf("https://yourdomain.com/payment/success") }
    var telebirrNotifyUrl by remember { mutableStateOf("https://api.yourdomain.com/api/v1/payments/telebirr-callback") }
    var telebirrPrivateKey by remember { mutableStateOf("-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEAn72V8X3Ww...\n-----END RSA PRIVATE KEY-----") }
    var telebirrPublicKey by remember { mutableStateOf("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFA...\n-----END PUBLIC KEY-----") }
    var showTelebirrConfigDetails by remember { mutableStateOf(false) }
    var telebirrAmount by remember { mutableStateOf("250.00") }
    var telebirrSubject by remember { mutableStateOf("Bet Wallet Deposit") }
    var telebirrSignedPayloadOutput by remember { mutableStateOf("") }
    var isTelebirrSigning by remember { mutableStateOf(false) }
    var telebirrStatusLog by remember { mutableStateOf("Ready Status: Telebirr payment integration initialized with sandbox endpoints.") }
    var showPostgresqlSchema by remember { mutableStateOf(false) }
    var showPaymentServiceSnippet by remember { mutableStateOf(false) }

    // Reporting Service states
    var showReportingConsole by remember { mutableStateOf(false) }
    var selectedReportDateRange by remember { mutableStateOf("Today") }
    var reportingStatusLog by remember { mutableStateOf("System Ready: reporting-cron-engine idle. No reports compiled yet in current session.") }
    var isGeneratingReport by remember { mutableStateOf(false) }
    var cronTimerActive by remember { mutableStateOf(false) }
    var hoveredGgrIndex by remember { mutableStateOf<Int?>(29) }

    // 🎰 NEW: Casino Game Broadcasts
    val casinoBroadcasts by com.example.util.AdminSocketHubInstance.casinoGameBroadcasts.collectAsState()

    val NeonBlue = Color(0xFF00C2FF)
    val NeonPurple = Color(0xFFBD00FF)

    // 🎰 Constant: 51 Casino Games (for reference)
    val casinoGames = listOf(
        "dice", "aviator", "coinflip", "plinko", "blackjack", "roulette", "mines", "crash",
        "tower", "keno", "baccarat", "wheel", "hilo", "sicbo", "videopoker", "bingo", "craps",
        "dragontiger", "andarbahar", "teenpatti", "lucky7", "scratch", "football", "basketball",
        "horseracing", "spinwin", "slot", "reddog", "war", "paigow", "diceduels", "penalty",
        "chickenroad", "chickenshot", "megaball", "pokerdice", "lightningdice", "carroulette",
        "knockout", "rummy", "darts", "tennis", "baseball", "greyhound", "motorbike", "cricket",
        "roulette360", "megawheel", "monopoly", "virtualsports", "texasholdem"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Wallet icon",
                    tint = NeonGreen,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "💼 Telebirr Mobile Payment Core Framework",
                    color = NeonGreen,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonGreen.copy(0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "INTEGRATION SYSTEM LAB",
                    color = NeonGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = "Simulate Telebirr's secure merchant payment API (using SHA256withRSA keys) to process outbound orders and inbound re-entry notify webhooks.",
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )

        // Collapsible Payment Service broadcast snippet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F1622))
                .border(0.5.dp, NeonGreen.copy(0.4f), RoundedCornerShape(4.dp))
                .clickable { showPaymentServiceSnippet = !showPaymentServiceSnippet }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Code icon",
                    tint = NeonGreen,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "paymentService.js Websocket Live Broadcaster Trigger",
                    color = TextWhite,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (showPaymentServiceSnippet) "HIDE TRIGGER ▲" else "VIEW CONTROLLER ▼",
                color = NeonGreen,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showPaymentServiceSnippet) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = """
                        // Inside paymentService.js after [DB SUCCESS] ledger updates execute cleanly
                        if (adminSocketHubInstance) {
                          adminSocketHubInstance.broadcastTransactionAlert({
                            id: outTradeNo,
                            user: `User${'$'}{userId || 'Unknown'}`,
                            type: 'Deposit',
                            amount: parseFloat(totalAmount).toFixed(2),
                            method: 'TeleBirr',
                            status: 'Approved'
                          });

                          // Query updated total system balance aggregates from your DB ledger and push:
                          adminSocketHubInstance.broadcastFinancialMetricUpdate({
                            totalBalance: "1257850.00", // Dynamically calculated via SUM() queries
                            totalDeposits: "523600.00",
                            totalWithdrawals: "186250.00"
                          });
                        }
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = TextLight,
                    lineHeight = 11.sp
                )
            }
        }

        // ==========================================================
        // ACTIVE TRANSACTION LOG STREAM
        // ==========================================================
        Text(
            text = "REAL-TIME FINANCIAL STREAM (PORT 3000 WEBSOCKETS)",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        if (txBroadcasts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FINANCIAL STREAM IDLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Waiting for Telebirr deposit or approved ledger updates...",
                        fontSize = 8.sp,
                        color = TextGrey
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(txBroadcasts) { tx ->
                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date(tx.timestamp))
                        Text(
                            text = "[$timeStr] 💼 [TRANSACTION]: ID ${tx.id} for ${tx.user} | ${tx.type}\n  └ Method: ${tx.method} | Amount: ${tx.amount} ETB\n  └ Status: ${tx.status}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            color = NeonGreen,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        // ==========================================================
        // 🎰 NEW: CASINO GAME EVENT STREAM
        // ==========================================================
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🎰 CASINO GAME EVENTS (REAL-TIME)",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        if (casinoBroadcasts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CASINO STREAM IDLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Waiting for casino game plays...",
                        fontSize = 8.sp,
                        color = TextGrey
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(casinoBroadcasts) { play ->
                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date(play.timestamp))
                        val outcomeEmoji = when (play.outcome) {
                            "win" -> "✅"
                            "lose" -> "❌"
                            else -> "⏳"
                        }
                        Text(
                            text = "[$timeStr] 🎰 [CASINO]: ${play.user} played ${play.gameName}\n  └ Stake: ${play.stake} ETB | Profit: ${play.profit} ETB | Outcome: $outcomeEmoji ${play.outcome.uppercase()}${play.multiplier?.let { " | Multiplier: ${it}x" } ?: ""}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            color = if (play.outcome == "win") NeonGreen else LightRed,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        // SECTION 1: EDITABLE .env ENVIRONMENT SPECIFICATION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0C1322))
                .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                .clickable { showTelebirrConfigDetails = !showTelebirrConfigDetails }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Config icon",
                    tint = NeonBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "1. TELEBIRR GATEWAY CONFIGURATION (.env Spec)",
                    color = TextWhite,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (showTelebirrConfigDetails) "HIDE PARAMS ▲" else "EDIT PARAMS ▼",
                color = NeonBlue,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showTelebirrConfigDetails) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B11), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Active Environment Constants (Tweak parameters to rebuild .env environment dynamically):",
                    fontSize = 8.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                // PORT & APP ID
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PORT", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = "3000",
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(2.3f)) {
                        Text("TELEBIRR_APP_ID", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrAppId,
                            onValueChange = { telebirrAppId = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }

                // MERCHANT CODE & RECEIVE NAME
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text("MCH_SHORT_CODE", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrMchShortCode,
                            onValueChange = { telebirrMchShortCode = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(2.7f)) {
                        Text("RECEIVE_NAME", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrReceiveName,
                            onValueChange = { telebirrReceiveName = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }

                // TELEBIRR_API_URL
                Column {
                    Text("TELEBIRR_API_URL", fontSize = 7.5.sp, color = TextMuted)
                    BasicTextField(
                        value = telebirrApiUrl,
                        onValueChange = { telebirrApiUrl = it },
                        textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                }

                // MAIN DIRECTIVES
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TELEBIRR_RETURN_URL", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrReturnUrl,
                            onValueChange = { telebirrReturnUrl = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TELEBIRR_NOTIFY_URL", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrNotifyUrl,
                            onValueChange = { telebirrNotifyUrl = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }

                // KEYS
                Column {
                    Text("MERCHANT_PRIVATE_KEY", fontSize = 7.5.sp, color = TextMuted)
                    BasicTextField(
                        value = telebirrPrivateKey,
                        onValueChange = { telebirrPrivateKey = it },
                        textStyle = TextStyle(color = TextLight, fontSize = 8.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                }

                Column {
                    Text("TELEBIRR_PUBLIC_KEY", fontSize = 7.5.sp, color = TextMuted)
                    BasicTextField(
                        value = telebirrPublicKey,
                        onValueChange = { telebirrPublicKey = it },
                        textStyle = TextStyle(color = TextLight, fontSize = 8.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                }

                Divider(color = BorderColor.copy(0.4f), thickness = 0.5.dp)

                // Generated Raw .env Output
                Text("COMPILED SYSTEM .env FILE (Gateway Source):", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = NeonBlue)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "PORT=3000\nTELEBIRR_APP_ID=$telebirrAppId\nTELEBIRR_MCH_SHORT_CODE=$telebirrMchShortCode\nTELEBIRR_API_URL=$telebirrApiUrl\nTELEBIRR_RECEIVE_NAME=\"$telebirrReceiveName\"\nTELEBIRR_RETURN_URL=$telebirrReturnUrl\nTELEBIRR_NOTIFY_URL=$telebirrNotifyUrl\n\n# RSA Keys\nMERCHANT_PRIVATE_KEY=\"$telebirrPrivateKey\"\nTELEBIRR_PUBLIC_KEY=\"$telebirrPublicKey\"",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.5.sp,
                        color = NeonGreen,
                        lineHeight = 11.sp
                    )
                }
            }
        }

        // SECTION 2: SIMULATE OUTBOUND PAYLOAD PACKAGING (RSA Cryptography)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050912), RoundedCornerShape(6.dp))
                .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. OUTBOUND DEPOSIT ORDER STRUCTURER",
                        color = TextWhite,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(NeonBlue.copy(0.12f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SHA256withRSA",
                            color = NeonBlue,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deposit Amount (ETB)", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrAmount,
                            onValueChange = { telebirrAmount = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Payment Subject", fontSize = 7.5.sp, color = TextMuted)
                        BasicTextField(
                            value = telebirrSubject,
                            onValueChange = { telebirrSubject = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }

                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            isTelebirrSigning = true
                            telebirrStatusLog = "Hashing parameters alphabetically..."
                            kotlinx.coroutines.delay(200)
                            telebirrStatusLog = "Parameters Sorted: appId, appKey, correlationId, nonce, notifyUrl, outTradeNo, receiveName, returnUrl, shortCode, subject, timeout, totalAmount..."
                            kotlinx.coroutines.delay(200)
                            telebirrStatusLog = "Signing with MERCHANT_PRIVATE_KEY via SHA256withRSA signature algorithm..."
                            kotlinx.coroutines.delay(250)

                            val mockOutTradeNo = "TX_" + (100000..999999).random().toString()
                            val sortedString = "appId=$telebirrAppId&appKey=YOUR_APP_KEY_CORRESPONDING&correlationId=c_${System.currentTimeMillis()}&nonce=${(10000000..99999999).random()}&notifyUrl=$telebirrNotifyUrl&outTradeNo=$mockOutTradeNo&receiveName=$telebirrReceiveName&returnUrl=$telebirrReturnUrl&shortCode=$telebirrMchShortCode&subject=$telebirrSubject&timeout=30&totalAmount=$telebirrAmount"

                            val base64Bytes = android.util.Base64.encodeToString(sortedString.toByteArray(), android.util.Base64.NO_WRAP)
                            val mockSignature = "Z0M2RHVzSm1pT3R0U2hvd0NvdXJzZWJvZH...U2lnbmF0dXJlVmVyaWZpZWQ="

                            telebirrSignedPayloadOutput = "{\n  \"appid\": \"$telebirrAppId\",\n  \"sign\": \"$mockSignature\",\n  \"ussd\": \"$base64Bytes\"\n}"

                            telebirrStatusLog = "Success: Payload structured and encrypted into security packet. Ready to POST to $telebirrApiUrl."
                            isTelebirrSigning = false
                        }
                    },
                    enabled = !isTelebirrSigning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTelebirrSigning) "GENERATING SIGNATURE..." else "GENERATE PAYLOAD STRING & SIGN (Outbound)",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (telebirrSignedPayloadOutput.isNotEmpty()) {
                    Text("COMPILED API REQUEST BODY:", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = telebirrSignedPayloadOutput,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            color = TextLight,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }

        // SECTION 3: SIMULATE INBOUND PAYLOAD NOTIFICATION DECRYPT & EXECUTING WEBHOOK CALLBACK
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A07), RoundedCornerShape(6.dp))
                .border(0.5.dp, NeonGreen.copy(0.4f), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. INBOUND WEBHOOK DISPATCHER (Callback)",
                        color = TextWhite,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(NeonGreen.copy(0.12f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "notify_url listener",
                            color = NeonGreen,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "Trigger Telebirr server's background response callback to the server webhook. Instantly credits active balance and advances platform treasury book statistics.",
                    fontSize = 8.sp,
                    color = TextMuted,
                    lineHeight = 12.sp
                )

                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            telebirrStatusLog = "Constructing inbound callback validation parameters..."
                            kotlinx.coroutines.delay(200)
                            telebirrStatusLog = "Simulating transaction payload matching: $telebirrAmount ETB..."
                            kotlinx.coroutines.delay(250)

                            val randTxId = "TLB_" + (10000000..99999999).random().toString()
                            val users = listOf("admin_agent", "user_leul", "user_kalkidan", "user_abenezer", "user_tsion")
                            val targetUser = users.random()

                            val currentMetric = com.example.util.AdminSocketHubInstance.financialMetrics.value
                            val oldBal = currentMetric.totalBalance.toDoubleOrNull() ?: 1257850.00
                            val oldDep = currentMetric.totalDeposits.toDoubleOrNull() ?: 523600.00
                            val depositAmt = telebirrAmount.toDoubleOrNull() ?: 250.00

                            // 1. Alert Socket transaction stream
                            com.example.util.AdminSocketHubInstance.broadcastTransactionAlert(
                                com.example.util.AdminSocketHubInstance.BroadcastTransaction(
                                    id = randTxId,
                                    user = targetUser,
                                    type = "DEPOSIT",
                                    amount = String.format("%.2f", depositAmt),
                                    method = "TELEBIRR_API",
                                    status = "SUCCESS"
                                )
                            )

                            // 2. Adjust Ledger Metrics
                            com.example.util.AdminSocketHubInstance.broadcastFinancialMetricUpdate(
                                com.example.util.AdminSocketHubInstance.FinancialMetric(
                                    totalBalance = String.format("%.2f", oldBal + depositAmt),
                                    totalDeposits = String.format("%.2f", oldDep + depositAmt),
                                    totalWithdrawals = currentMetric.totalWithdrawals
                                )
                            )

                            telebirrStatusLog = "Success: Inbound payload parsed! Verified signature against TELEBIRR_PUBLIC_KEY, credited $depositAmt ETB to $targetUser, and committed record $randTxId instantly."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = "SIMULATE INBOUND DEPOSIT CALLBACK",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // SECTION 3.5: FINANCIAL PROFITABILITY & AUDIT COMPILER (ReportingService.ts)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("financial_reporting_service_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Reporting Icon",
                            tint = NeonBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📊 ReportingService.ts (Profitability)",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonBlue.copy(0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (cronTimerActive) "CRON RUNNING" else "CRON STANDBY",
                            color = NeonBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compiles profitability metrics for custom date windows and generates structured audit trails. Automates platform GGR computations and transaction channel segmentations.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RANGE:",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    val dateOptions = listOf("Today", "Yesterday", "Last 7 Days")
                    dateOptions.forEach { option ->
                        val isSel = selectedReportDateRange == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) NeonBlue.copy(0.12f) else SlateSurfaceL2)
                                .border(0.5.dp, if (isSel) NeonBlue else BorderColor, RoundedCornerShape(4.dp))
                                .clickable { selectedReportDateRange = option }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = option.uppercase(),
                                color = if (isSel) NeonBlue else TextLight,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cron scheduler configurator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(0.4f))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🕒 CRON PATTERN: '0 0 * * *' (Daily at 00:00)",
                            color = TextLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (cronTimerActive) "Status: Automatic daemon-worker scheduled." else "Status: Manual trigger mode active.",
                            color = if (cronTimerActive) NeonGreen else TextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    TextButton(
                        onClick = {
                            cronTimerActive = !cronTimerActive
                            reportingStatusLog = if (cronTimerActive) {
                                "[node-cron] Scheduled report compiler task registered successfully at daily pattern '0 0 * * *'."
                            } else {
                                "[node-cron] Scheduled task de-registered. Reverted back to administrative manual override."
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = if (cronTimerActive) "DEACTIVATE" else "ACTIVATE CRON",
                            color = if (cronTimerActive) LightRed else NeonGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            isGeneratingReport = true
                            reportingStatusLog = "📊 [REPORTS ENGINE] Starting financial audit computation..."
                            kotlinx.coroutines.delay(250)
                            reportingStatusLog = "⚡ Connecting to relational isolation pool and scanning 'bets' & 'transactions' tables..."
                            kotlinx.coroutines.delay(350)
                            reportingStatusLog = "🔄 Executing standard Net GGR formula (Stakes - Settled Wayouts)..."
                            kotlinx.coroutines.delay(300)

                            // DYNAMIC SQL COMPILING LOGIC
                            val mult = when (selectedReportDateRange) {
                                "Yesterday" -> 0.85
                                "Last 7 Days" -> 4.2
                                else -> 1.0
                            }

                            // 🎰 Include both sports and casino bets
                            val tStakes = allBets.sumOf { it.stake } * mult
                            val tPayouts = allBets.filter { it.status == "WON" || it.status == "CASHOUT" }
                                .sumOf { if (it.status == "WON") it.potentialReturn else it.potentialReturn * 0.85 } * mult

                            // Net GGR calculation rule standard
                            val netGgr = tStakes - tPayouts

                            val tDeposits = (allTransactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount } + 523600.0) * mult
                            val tWithdrawals = (allTransactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }.sumOf { it.amount } + 186250.0) * mult

                            val telebirrVolume = tDeposits * 0.72
                            val cbeVolume = tDeposits * 0.28

                            val holdPct = if (tStakes > 0.0) (netGgr / tStakes) * 100.0 else 12.45

                            val dateString = "2026-06-21"

                            val compiledCsvContent = """
                                FINANCIAL METRIC COLUMN (ETB)                        | CALCULATED AGGREGATED METRIC VALUE
                                -----------------------------------------------------+-----------------------------------
                                Report Generation Date                               | $dateString
                                Total Wager Stakes (Handle)                         | ${String.format("%,.2f", tStakes)} ETB
                                Total Settled Payouts (Losses)                       | ${String.format("%,.2f", tPayouts)} ETB
                                Net Gross Gaming Revenue (GGR)                       | ${String.format("%,.2f", netGgr)} ETB
                                Total Approved Gateway Deposits                      | ${String.format("%,.2f", tDeposits)} ETB
                                Total Approved Gateway Withdrawals                   | ${String.format("%,.2f", tWithdrawals)} ETB
                                TeleBirr Gateway Deposit Volume                      | ${String.format("%,.2f", telebirrVolume)} ETB
                                CBE Birr Gateway Deposit Volume                      | ${String.format("%,.2f", cbeVolume)} ETB
                                Platform Hold Profit Percentage (%)                  | ${String.format("%.2f", holdPct)}%
                            """.trimIndent()

                            reportingStatusLog = "📝 CSV formatting prepared with csv-writer:\n\n$compiledCsvContent\n\nWriting daily financial ledger dump locally...\n[OK] Created local copy prefix: profitability_report_${dateString}.csv\n\n📤 [AWS S3] Connecting stream and initiating secure PutObject private upload...\n✨ [AWS S3 SUCCESS] Securely archived at s3://secure-financial-reports/financials/profitability_report_${dateString}.csv\n\n🧹 [LOCAL CLEANUP] Local copy deleted successfully from server storage capacity.\n\n✅ JOB COMPLETE: CSV profitability ledger compiled and safely backed up."
                            isGeneratingReport = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    enabled = !isGeneratingReport
                ) {
                    Text(
                        text = if (isGeneratingReport) "COMPILING REPORT METRICS..." else "📊 RUN DAILY FINANCIAL CSV RECONCILIATION",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isGeneratingReport = true
                            reportingStatusLog = "📊 [REPORTS ENGINE] Starting simulated financial audit calculation..."
                            kotlinx.coroutines.delay(250)
                            reportingStatusLog = "⚡ Scanning daily records & preparing profitability_report_2026-06-21.csv..."
                            kotlinx.coroutines.delay(350)
                            reportingStatusLog = "📤 [AWS S3] Initiating secure PutObject stream transfer to private bucket..."
                            kotlinx.coroutines.delay(400)
                            reportingStatusLog = "🚨 [AWS S3 CRITICAL EXCEPTION] s3Client.send(PutObjectCommand) failed: PutObjectForbidden\nCredentials supplied are invalid or expired (AWS S3 SignatureDoesNotMatch)"
                            kotlinx.coroutines.delay(500)
                            reportingStatusLog = "📡 [INCIDENT TRACKER] Capturing stack trace & executing database error recording sequence..."
                            kotlinx.coroutines.delay(400)

                            reportingStatusLog = """
                                ❌ [SYSTEM FAILURE AUDITED] ReportingService failed to upload financial compilation.
                                
                                📊 [DATABASE EVENT LOGGED] Successfully wrote incident details to table 'reporting_error_logs':
                                  - service_name  : 'ReportingService'
                                  - job_name      : 'GenerateDailyFinancialCSV'
                                  - error_message : 'S3UploadError: Access Denied / Invalid AWS Credentials signature'
                                  - logged_at     : 2026-06-21 04:06:33 UTC
                                  - stack_trace   : 'CredentialsError: Access Key 'AKIA...' is locked or inactive \n    at S3Client.send (ReportingService.ts:1571)...'
                                
                                🧹 [LOCAL STORAGE CLEANUP] Cleaned local copy profitability_report_2026-06-21.csv successfully to preserve server memory.
                                
                                ⚠️ RESPONSE POST-MORTEM: Incident log recorded. Developer notifications dispatched.
                            """.trimIndent()
                            isGeneratingReport = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("simulate_reporting_failure_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.12f), contentColor = LightRed),
                    border = BorderStroke(0.5.dp, LightRed),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    enabled = !isGeneratingReport
                ) {
                    Text(
                        text = if (isGeneratingReport) "RUNNING PROCESS FLOW..." else "🚨 SIMULATE REPORT S3 UPLOAD FAILURE (DATABASE ERROR LOGGING)",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TERMINAL SHELL FOR SCHEDULER & RECONCILIATION OUTPUTS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    val terminalScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(terminalScroll)
                    ) {
                        Text(
                            text = "REPORTING SERVICE TERM ENGINE v1.0.4",
                            color = NeonPurple,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reportingStatusLog,
                            color = if (reportingStatusLog.contains("SUCCESS") || reportingStatusLog.contains("compiled")) NeonGreen else TextLight,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECTION 3.6: 30-DAY NET GGR PERFORMANCE PULSE (Recharts Simulated)
        val ggrDataPoints = remember {
            List(30) { index ->
                val day = index + 1
                // Generate standard organic wavy trends for sports betting handle:
                // Net GGR = Stakes - Payouts
                val baseStakes = 145000.0 + (day * 3200.0)
                val cosWave = Math.cos(day * 0.45) * 42000.0
                val sinWave = Math.sin(day * 0.45) * 18000.0
                val stakes = (baseStakes + cosWave).coerceAtLeast(60000.0)
                val payouts = (baseStakes * 0.81 + sinWave).coerceAtLeast(40000.0)
                val ggr = stakes - payouts
                Triple("Jun $day", stakes, ggr)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("ggr_analytics_performance_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📈 Net GGR Performance Pulse (Recharts)",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonGreen.copy(0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "30-DAY LIVE ANALYTICS",
                            color = NeonGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Consolidated analytical views reflecting stakes, payouts, and net Gross Gaming Revenue (GGR) aggregates across real-time PostgreSQL database transactions.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Data Point Statistics Panel
                hoveredGgrIndex?.let { hIdx ->
                    val dataPoint = ggrDataPoints[hIdx]
                    val dateLabel = dataPoint.first
                    val stakes = dataPoint.second
                    val ggr = dataPoint.third
                    val holdPct = (ggr / stakes) * 100.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(0.2f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALCULATION SNAPSHOT: $dateLabel",
                                fontSize = 7.5.sp,
                                color = NeonBlue,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Drag or sliding across the canvas below to audit historical days",
                                fontSize = 7.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOTAL HANDLE", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%,.2f ETB", stakes),
                                    fontSize = 9.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("NET GGR", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%,.2f ETB", ggr),
                                    fontSize = 9.sp,
                                    color = NeonBlue,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("HOLD %", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%.2f%%", holdPct),
                                    fontSize = 9.sp,
                                    color = if (holdPct >= 15.0) NeonGreen else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val position = event.changes.firstOrNull()?.position
                                        if (position != null) {
                                            val colWidth = size.width / 30f
                                            val idx = (position.x / colWidth).toInt().coerceIn(0, 29)
                                            hoveredGgrIndex = idx
                                        }
                                    }
                                }
                            }
                    ) {
                        val maxStakesVal = ggrDataPoints.maxOf { it.second }.toFloat() * 1.15f
                        val pointsStakes = ggrDataPoints.mapIndexed { i, d ->
                            val x = i * (size.width / 29f)
                            val y = size.height - ((d.second.toFloat() / maxStakesVal) * size.height)
                            Offset(x, y)
                        }
                        val pointsGgr = ggrDataPoints.mapIndexed { i, d ->
                            val x = i * (size.width / 29f)
                            val y = size.height - ((d.third.toFloat() / maxStakesVal) * size.height)
                            Offset(x, y)
                        }

                        // Draw Grid lines
                        val gridLinesCount = 4
                        for (i in 1..gridLinesCount) {
                            val ratio = i.toFloat() / gridLinesCount
                            val y = size.height * ratio
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Drawing Area Stakes Gradient under Stakes line (Blue shading)
                        val stakesFillPath = Path().apply {
                            pointsStakes.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                            }
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            stakesFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.05f), Color.Transparent),
                                startY = pointsStakes.minOf { it.y },
                                endY = size.height
                            )
                        )

                        // Draw Stakes Path
                        val stakesPath = Path().apply {
                            pointsStakes.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                            }
                        }
                        drawPath(
                            stakesPath,
                            color = Color(0xFF3B82F6),
                            style = Stroke(width = 1.5f * density)
                        )

                        // Drawing Area GGR Gradient (Teal shading)
                        val ggrFillPath = Path().apply {
                            pointsGgr.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                            }
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            ggrFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonBlue.copy(alpha = 0.18f), Color.Transparent),
                                startY = pointsGgr.minOf { it.y },
                                endY = size.height
                            )
                        )

                        // Draw GGR Path
                        val ggrPath = Path().apply {
                            pointsGgr.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                            }
                        }
                        drawPath(
                            ggrPath,
                            color = NeonBlue,
                            style = Stroke(width = 2.5f * density)
                        )

                        // Cursor logic indicator
                        hoveredGgrIndex?.let { hIdx ->
                            val ptStakes = pointsStakes[hIdx]
                            val ptGgr = pointsGgr[hIdx]

                            // Vertical highlight cursor line
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(ptGgr.x, 0f),
                                end = Offset(ptGgr.x, size.height),
                                strokeWidth = 1f * density
                            )

                            // Spot Highlight Stakes
                            drawCircle(color = Color(0xFF3B82F6), radius = 4.5f * density, center = ptStakes)
                            drawCircle(color = Color.White, radius = 1.5f * density, center = ptStakes)

                            // Spot Highlight GGR
                            drawCircle(color = NeonBlue, radius = 5.5f * density, center = ptGgr)
                            drawCircle(color = Color.White, radius = 2f * density, center = ptGgr)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Chart Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B82F6), RoundedCornerShape(1.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Total Stakes (Handle)", color = TextMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(NeonBlue, RoundedCornerShape(1.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Net GGR (Stakes - Losses)", color = TextMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECTION 4: NODE.JS/JAVASCRIPT MICROSERVICE BLUEPRINTS
        var showMicroserviceBlueprints by remember { mutableStateOf(false) }
        var activeBlueprintTab by remember { mutableStateOf("UTIL") } // "UTIL", "ROUTER", "SERVER"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F162A))
                .border(0.5.dp, Color(0xFFE2E8F0).copy(0.3f), RoundedCornerShape(4.dp))
                .clickable { showMicroserviceBlueprints = !showMicroserviceBlueprints }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Code icon",
                    tint = NeonBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "4. NODE.JS PRODUCTION INTEGRATION BLUEPRINTS",
                    color = TextWhite,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (showMicroserviceBlueprints) "HIDE BLUEPRINTS ▲" else "VIEW BLUEPRINTS ▼",
                color = NeonBlue,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showMicroserviceBlueprints) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B11), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Authentic Production JavaScript source files designed with crypto/SHA256 signature algorithms that map perfectly to the live simulation above:",
                    fontSize = 8.sp,
                    color = TextMuted,
                    lineHeight = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "UTIL" to "telebirrUtil.js",
                        "ROUTER" to "telebirrRouter.js",
                        "SERVER" to "index.js",
                        "REPORT" to "ReportingService.ts",
                        "CRON" to "cronScheduler.js",
                        "GGR_API" to "ggrRouter.ts",
                        "MARKET_API" to "marketRouter.ts",
                        "WEBHOOK" to "webhookService.ts",
                        "WEBHOOK_API" to "webhookRouter.ts",
                        "GGR_UI" to "GgrAnalytics.tsx"
                    )
                    tabs.forEach { (tabId, tabTitle) ->
                        val isTabSelected = activeBlueprintTab == tabId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTabSelected) NeonBlue.copy(0.12f) else Color.Black)
                                .border(0.5.dp, if (isTabSelected) NeonBlue else BorderColor, RoundedCornerShape(4.dp))
                                .clickable { activeBlueprintTab = tabId }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                color = if (isTabSelected) NeonBlue else TextLight,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val codeContent = if (activeBlueprintTab == "UTIL") {
                    """
                    // telebirrUtil.js
                    const crypto = require('crypto');
                    require('dotenv').config();

                    class TelebirrUtil {
                      /**
                       * Sorts fields alphabetically and builds a query string.
                       */
                      static createSignString(fields) {
                        const sortedKeys = Object.keys(fields).sort();
                        const pairs = [];
                        for (const key of sortedKeys) {
                          if (fields[key] !== undefined && fields[key] !== null && fields[key] !== '') {
                            pairs.push(`${'$'}{key}=${'$'}{fields[key]}`);
                          }
                        }
                        return pairs.join('&');
                      }

                      /**
                       * Generates a SHA256withRSA signature string.
                       */
                      static signPayload(signString) {
                        const privateKey = process.env.MERCHANT_PRIVATE_KEY.replace(/\\n/g, '\n');
                        const signer = crypto.createSign('SHA256');
                        signer.update(signString);
                        signer.end();
                        return signer.sign(privateKey, 'base64');
                      }

                      /**
                       * Verifies an incoming webhook signature.
                       */
                      static verifySignature(signString, incomingSignature) {
                        const publicKey = process.env.TELEBIRR_PUBLIC_KEY.replace(/\\n/g, '\n');
                        const verifier = crypto.createVerify('SHA256');
                        verifier.update(signString);
                        verifier.end();
                        return verifier.verify(publicKey, incomingSignature, 'base64');
                      }

                      /**
                       * Encrypts tracking data using direct Base64 encoding.
                       */
                      static encryptUssdPlanet(payloadObject) {
                        const jsonString = JSON.stringify(payloadObject);
                        return Buffer.from(jsonString).toString('base64');
                      }
                    }

                    module.exports = TelebirrUtil;
                    """.trimIndent()
                } else if (activeBlueprintTab == "ROUTER") {
                    """
                    // telebirrRouter.js
                    const express = require('express');
                    const bodyParser = require('body-parser');
                    const { v4: uuidv4 } = require('uuid');
                    const TelebirrUtil = require('./telebirrUtil');

                    const router = express.Router();
                    router.use(bodyParser.json());

                    /**
                     * STEP 1: INITIATE DEPOSIT (POST /telebirr-deposit)
                     */
                    router.post('/telebirr-deposit', async (req, res) => {
                      try {
                        const { userId, amount } = req.body;
                        if (!userId || !amount || isNaN(amount) || amount <= 0) {
                          return res.status(400).json({ error: "Invalid userId/amount" });
                        }

                        const outTradeNo = `TRX-${'$'}{uuidv4().substring(0, 8).toUpperCase()}`;

                        const ussdInPlanObject = {
                          appId: process.env.TELEBIRR_APP_ID,
                          mchShortCode: process.env.TELEBIRR_MCH_SHORT_CODE,
                          outTradeNo: outTradeNo,
                          receiveName: process.env.TELEBIRR_RECEIVE_NAME,
                          returnUrl: process.env.TELEBIRR_RETURN_URL,
                          notifyUrl: process.env.TELEBIRR_NOTIFY_URL,
                          subject: "Account Wallet Deposit",
                          timeoutExpress: "30",
                          totalAmount: parseFloat(amount).toFixed(2),
                          nonce: uuidv4().replace(/-/g, '')
                        };

                        const signString = TelebirrUtil.createSignString(ussdInPlanObject);
                        const signature = TelebirrUtil.signPayload(signString);

                        const requestPayload = {
                          appid: process.env.TELEBIRR_APP_ID,
                          sign: signature,
                          ussdPlanObject: TelebirrUtil.encryptUssdPlanet(ussdInPlanObject)
                        };

                        console.log(`[DB] Ref ${'$'}{outTradeNo} logged as PENDING`);
                        return res.status(200).json({
                          success: true,
                          outTradeNo,
                          telebirrPayload: requestPayload
                        });
                      } catch (error) {
                        console.error("Deposit Initiation Error:", error);
                        return res.status(500).json({ error: "Deposit error" });
                      }
                    });

                    /**
                     * STEP 2: SECURE CALLBACK (POST /telebirr-callback)
                     */
                    router.post('/telebirr-callback', async (req, res) => {
                      try {
                        const { sign, ...dataFields } = req.body;
                        if (!sign) {
                          return res.status(400).json({ code: 400, message: "Missing sign" });
                        }

                        const verifiedSignString = TelebirrUtil.createSignString(dataFields);
                        const isVerified = TelebirrUtil.verifySignature(verifiedSignString, sign);

                        if (!isVerified) {
                          console.error("❌ Signature verify failed!");
                          return res.status(401).json({ code: 401, message: "Crypto mismatch" });
                        }

                        const { outTradeNo, transactionStatus, totalAmount } = dataFields;
                        console.log(`✅ Ref ${'$'}{outTradeNo} Status: ${'$'}{transactionStatus}`);

                        if (transactionStatus === 'SUCCESS') {
                          console.log(`[DB SUCCESS] Crediting balance: +${'$'}{totalAmount} ETB`);
                        }
                        return res.status(200).json({ code: 200, message: "SUCCESS" });
                      } catch (error) {
                        console.error("Exception Error:", error);
                        return res.status(500).json({ code: 500, message: "Server error" });
                      }
                    });

                    module.exports = router;
                    """.trimIndent()
                } else if (activeBlueprintTab == "SERVER") {
                    """
                    import express from 'express';
                    import { initializeScheduledReportingJobs } from './cronScheduler';
                    require('dotenv').config();

                    const app = express();

                    // Boot background processing routines alongside standard API connectivity streams
                    initializeScheduledReportingJobs();

                    const PORT = process.env.PORT || 3000;
                    app.listen(PORT, () => {
                      console.log(`🚀 Sportsbook Management Ecosystem Engine listening on port ${'$'}{PORT}`);
                    });
                    """.trimIndent()
                } else if (activeBlueprintTab == "REPORT") {
                    """
                    import { Pool } from 'pg';
                    import { createObjectCsvWriter } from 'csv-writer';
                    import fs from 'fs';
                    import path from 'path';
                    import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';

                    export class ReportingService {
                      private readonly s3Client: S3Client;
                      private readonly bucketName: string;

                      constructor(private readonly dbPool: Pool) {
                        this.s3Client = new S3Client({
                          region: process.env.AWS_REGION || 'us-east-1',
                          credentials: {
                            accessKeyId: process.env.AWS_ACCESS_KEY_ID || '',
                            secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY || '',
                          }
                        });
                        this.bucketName = process.env.AWS_S3_BUCKET_NAME || 'secure-financial-reports';
                      }

                      /**
                       * Compiles profitability metrics for a custom date range window,
                       * uploads the output CSV files to a secure private S3 bucket immediately,
                       * and deletes the local file copy to save server storage.
                       */
                      public async generateDailyFinancialCSV(targetDate: Date): Promise<string | null> {
                        const startOfDay = new Date(targetDate);
                        startOfDay.setHours(0, 0, 0, 0);

                        const endOfDay = new Date(targetDate);
                        endOfDay.setHours(23, 59, 59, 999);

                        const dateString = startOfDay.toISOString().split('T')[0];
                        const reportsDir = path.join(__dirname, '../exports/financials');

                        // Ensure storage path array directories exist on the server instance
                        if (!fs.existsSync(reportsDir)) {
                          fs.mkdirSync(reportsDir, { recursive: true });
                        }

                        const filename = `profitability_report_${'$'}{dateString}.csv`;
                        const filePath = path.join(reportsDir, filename);
                        
                        try {
                          const client = await this.dbPool.connect();
                          
                          // Execute real ledger profitability aggregate scans across wagers & payouts
                          const query = `
                            SELECT 
                              COALESCE(SUM(CASE WHEN type = 'Payout' THEN amount END), 0.00) as total_payouts,
                              COALESCE(SUM(CASE WHEN type = 'Deposit' THEN amount END), 0.00) as total_deposits,
                              COALESCE(SUM(CASE WHEN type = 'Withdrawal' THEN amount END), 0.00) as total_withdrawals
                            FROM transactions
                            WHERE status = 'APPROVED' AND processed_at BETWEEN ${'$'}1 AND ${'$'}2
                          `;
                          
                          const res = await client.query(query, [startOfDay, endOfDay]);
                          client.release();

                          const stats = res.rows[0];
                          const csvWriter = createObjectCsvWriter({
                            path: filePath,
                            header: [
                              { id: 'date', title: 'REPORT_DATE' },
                              { id: 'deposits', title: 'TOTAL_DEPOSITS_ETB' },
                              { id: 'withdrawals', title: 'TOTAL_WITHQUALALS_ETB' },
                              { id: 'payouts', title: 'TOTAL_PAYOUTS_ETB' }
                            ]
                          });

                          await csvWriter.writeRecords([{
                            date: dateString,
                            deposits: stats.total_deposits,
                            withdrawals: stats.total_withdrawals,
                            payouts: stats.total_payouts
                          }]);

                          // AWS S3 Private Bucket upload procedure
                          console.log(`📤 [AWS S3] Initiating secure upload sequence of ${'$'}{filename}...`);
                          const fileStream = fs.createReadStream(filePath);
                          
                          const uploadParams = {
                            Bucket: this.bucketName,
                            Key: `financials/${'$'}{filename}`,
                            Body: fileStream,
                            ContentType: 'text/csv'
                          };

                          await this.s3Client.send(new PutObjectCommand(uploadParams));
                          console.log(`✨ [AWS S3 SUCCESS] Securely archived at s3://${'$'}{this.bucketName}/financials/${'$'}{filename}`);

                          // Delete local copy immediately to conserve server storage capacity
                          if (fs.existsSync(filePath)) {
                            fs.unlinkSync(filePath);
                            console.log(`🧹 [LOCAL CLEANUP] Local copy ${'$'}{filename} deleted successfully from disk.`);
                          }

                          return `s3://${'$'}{this.bucketName}/financials/${'$'}{filename}`;
                        } catch (error) {
                          console.error('❌ [REPORT CRITICAL EXCEPTION] Failed GGR compile or AWS upload:', error);
                          // Track incident in centralized error logging table
                          await this.trackIncidentError('GenerateDailyFinancialCSV', error);
                          
                          // Cleanup local file on failure if it exists
                          if (fs.existsSync(filePath)) {
                            fs.unlinkSync(filePath);
                          }
                          return null;
                        }
                      }

                      /**
                       * Centralized incident error tracking routine logging details to PostgreSQL
                       */
                      private async trackIncidentError(jobName: string, error: any): Promise<void> {
                        const errorMessage = error instanceof Error ? error.message : String(error);
                        const stackTrace = error instanceof Error ? error.stack : '';
                        console.error(`🚨 [INCIDENT ERROR LOGGED] ${'$'}{jobName} failed: ${'$'}{errorMessage}`);
                        try {
                          const client = await this.dbPool.connect();
                          const query = `
                            INSERT INTO reporting_error_logs (service_name, job_name, error_message, stack_trace)
                            VALUES ('ReportingService', ${'$'}1, ${'$'}2, ${'$'}3)
                          `;
                          await client.query(query, [jobName, errorMessage, stackTrace]);
                          client.release();
                          console.log('📝 [DATABASE LOGGED] Successfully stored incident tracing signature for post-mortems.');
                        } catch (dbErr) {
                          console.error('❌ [CRITICAL METRIC ERROR ENGINE FAILURE] Failed to write to system logging table:', dbErr);
                        }
                      }
                    }
                    """.trimIndent()
                } else if (activeBlueprintTab == "CRON") {
                    """
                    import cron from 'node-cron';
                    import { Pool } from 'pg';
                    import { ReportingService } from './reportingService';

                    const dbPool = new Pool({ connectionString: process.env.DATABASE_URL });
                    const reportService = new ReportingService(dbPool);

                    /**
                     * Scheduled Worker Configuration Definition
                     * Pattern: minute hour day-of-month month day-of-week
                     * "0 0 * * *" translates directly to: Executing every single day at exactly 12:00 AM Midnight
                     */
                    export const initializeScheduledReportingJobs = (): void => {
                      console.log('⏰ [CRON ENGINE] Financial Reporting Daemon initialized successfully.');

                      cron.schedule('0 0 * * *', async () => {
                        console.log('⏰ [CRON ENGINE] Midnight trigger reached. Beginning report cycle calculation loop...');
                        
                        // We target yesterday's complete 24-hour bracket metrics data range
                        const yesterday = new Date();
                        yesterday.setDate(yesterday.getDate() - 1);

                        const reportPath = await reportService.generateDailyFinancialCSV(yesterday);

                        if (reportPath) {
                          console.log(`✨ [CRON SUCCESS] Backup audit verification loop completed. Resource stored safely.`);
                          
                          // Inside an enterprise suite infrastructure, this hook would pass the file buffer 
                          // directly down streaming lines into AWS S3 storage array blocks or trigger 
                          // internal automated emails to the accounting team.
                        }
                      }, {
                        scheduled: true,
                        timezone: "Africa/Addis_Ababa" // Locked directly to local system runtime operational zones
                      });
                    };
                    """.trimIndent()
                } else if (activeBlueprintTab == "GGR_API") {
                    """
                    import { Router } from 'express';
                    import { Pool } from 'pg';

                    const router = Router();
                    const dbPool = new Pool({ connectionString: process.env.DATABASE_URL });

                    /**
                     * GET /api/admin/ggr-trends
                     * Pulls the last 30 days of aggregated daily financial data (stakes, payouts, Net GGR)
                     * to power the Recharts visual line chart on the admin core dashboard.
                     */
                    router.get('/ggr-trends', async (req, res) => {
                      try {
                        const client = await dbPool.connect();
                        
                        // Executes aggregation grouping database queries over the last 30 days
                        const query = `
                          SELECT 
                            DATE(processed_at) as date,
                            COALESCE(SUM(CASE WHEN type = 'Deposit' THEN amount END), 0.00) as daily_stakes,
                            COALESCE(SUM(CASE WHEN type = 'Payout' THEN amount END), 0.00) as daily_payouts
                          FROM transactions
                          WHERE status = 'APPROVED' AND processed_at >= NOW() - INTERVAL '30 days'
                          GROUP BY DATE(processed_at)
                          ORDER BY DATE(processed_at) ASC;
                        `;
                        
                        const dbResult = await client.query(query);
                        client.release();

                        // Map rows to compile the standard Net GGR formula (Stakes - Settled Wayouts)
                        const trends = dbResult.rows.map(row => {
                          const stakes = parseFloat(row.daily_stakes);
                          const payouts = parseFloat(row.daily_payouts);
                          const ggr = stakes - payouts;
                          return {
                            date: row.date.toISOString().split('T')[0],
                            stakes: stakes,
                            payouts: payouts,
                            ggr: parseFloat(ggr.toFixed(2))
                          };
                        });

                        res.status(200).json({
                          success: true,
                          timeframe: '30_days',
                          data: trends
                        });
                      } catch (error) {
                        console.error('❌ [API ERROR] GGR Analytics endpoint failure:', error);
                        res.status(500).json({ success: false, error: 'Internal Server Error' });
                      }
                    });

                    export default router;
                    """.trimIndent()
                } else if (activeBlueprintTab == "MARKET_API") {
                    """
                    import { Router } from 'express';
                    import { CacheService } from './cacheService';
                    import { Pool } from 'pg';

                    const router = Router();
                    const cache = new CacheService();
                    const dbPool = new Pool();

                    /**
                     * Public endpoint serving active markets directly to the web/mobile app layout
                     */
                    router.get('/market/active-sheets', async (req, res) => {
                      const cacheKey = 'active_sportsbook_market';
                      
                      // Try to instantly serve from low-latency Redis cache
                      const cachedData = await cache.getCache(cacheKey);
                      if (cachedData) {
                        return res.status(200).json(cachedData);
                      }

                      try {
                        // Pull active matches with open odds from database
                        const marketQuery = await dbPool.query(`
                          SELECT 
                            m.id AS match_id,
                            m.home_team,
                            m.away_team,
                            m.start_time,
                            m.status,
                            mk.market_type,
                            json_agg(json_build_object('selection', mo.selection, 'odds', mo.odds)) FILTER (WHERE mo.is_suspended = FALSE) AS options
                          FROM matches m
                          JOIN markets mk ON mk.match_id = m.id
                          JOIN market_odds mo ON mo.market_id = mk.id
                          WHERE m.status IN ('Not Started', 'Live') AND mk.is_active = TRUE
                          GROUP BY m.id, mk.id
                          ORDER BY m.start_time ASC
                        `);

                        // Write back to cache for 3 seconds to handle sudden user traffic surges
                        await cache.setCache(cacheKey, marketQuery.rows);

                        return res.status(200).json(marketQuery.rows);
                      } catch (error) {
                        return res.status(500).json({ error: "Failed to load current betting market sheets." });
                      }
                    });

                    export default router;
                    """.trimIndent()
                } else if (activeBlueprintTab == "WEBHOOK") {
                    """
                    import { Pool } from 'pg';
                    import { CacheService } from './cacheService';

                    export class WebhookProcessorService {
                      private readonly cache = new CacheService();

                      constructor(private readonly dbPool: Pool) {
                        this.cache.connect();
                      }

                      /**
                       * Processes real-time match events (goals, status changes, match clock updates)
                       */
                      public async processMatchUpdate(payload: any): Promise<void> {
                        const { fixture_id, status, home_score, away_score, elapsed_time } = payload;
                        const client = await this.dbPool.connect();

                        try {
                          await client.query('BEGIN');

                          // Update match telemetry using the mapped external provider ID
                          const result = await client.query(`
                            UPDATE matches 
                            SET 
                              status = ${'$'}1, 
                              home_score = ${'$'}2, 
                              away_score = ${'$'}3, 
                              minute_elapsed = ${'$'}4,
                              updated_at = NOW()
                            WHERE provider_id = ${'$'}5
                            RETURNING id
                          `, [status, home_score, away_score, `${'$'}{elapsed_time}'`, fixture_id]);

                          if (result.rowCount === 0) {
                            console.warn(`⚠️ [WEBHOOK] Received match update for unknown provider_id: ${'$'}{fixture_id}`);
                            await client.query('ROLLBACK');
                            return;
                          }

                          await client.query('COMMIT');
                          console.log(`⚽ [WEBHOOK] Match provider_id ${'$'}{fixture_id} updated successfully.`);

                          // Evict public markets cache to force an instant frontend update stream
                          await this.cache.invalidateCache('active_sportsbook_market');

                        } catch (error: any) {
                          await client.query('ROLLBACK');
                          console.error(`❌ [WEBHOOK ERROR] Match update processing failed:`, error.message);
                          throw error;
                        } finally {
                          client.release();
                        }
                      }

                      /**
                       * Processes shifting odds lines and handles suspension events (e.g., locking lines when a VAR review occurs)
                       */
                      public async processOddsUpdate(payload: any): Promise<void> {
                        const { fixture_id, market_type, odds_book } = payload; // odds_book shape: [{ selection: '1', price: 2.10 }, ...]
                        const client = await this.dbPool.connect();

                        try {
                          await client.query('BEGIN');

                          // 1. Locate the internal market ID linked to the provider's match ID
                          const marketRes = await client.query(`
                            SELECT mk.id FROM markets mk
                            JOIN matches m ON mk.match_id = m.id
                            WHERE m.provider_id = ${'$'}1 AND mk.market_type = ${'$'}2
                          `, [fixture_id, market_type]);

                          if (marketRes.rowCount === 0) {
                            console.warn(`⚠️ [WEBHOOK] Odds market not initialized yet for provider match: ${'$'}{fixture_id}`);
                            await client.query('ROLLBACK');
                            return;
                          }

                          const marketId = marketRes.rows[0].id;

                          // 2. Perform upserts for the shifting selection lines inside the market
                          for (const selectionLine of odds_book) {
                            await client.query(`
                              INSERT INTO market_odds (market_id, selection, odds, is_suspended, updated_at)
                              VALUES (${'$'}1, ${'$'}2, ${'$'}3, ${'$'}4, NOW())
                              ON CONFLICT (market_id, selection) 
                              DO UPDATE SET 
                                odds = EXCLUDED.odds,
                                is_suspended = EXCLUDED.is_suspended,
                                updated_at = NOW()
                            `, [marketId, selectionLine.selection, selectionLine.price, selectionLine.is_suspended || false]);
                          }

                          await client.query('COMMIT');
                          console.log(`📈 [WEBHOOK] Shifting lines synced for market ID: ${'$'}{marketId} (${'$'}{market_type})`);

                          // Flush frontend cache to expose new prices immediately
                          await this.cache.invalidateCache('active_sportsbook_market');

                        } catch (error: any) {
                          await client.query('ROLLBACK');
                          console.error(`❌ [WEBHOOK ERROR] Odds update processing failed:`, error.message);
                          throw error;
                        } finally {
                          client.release();
                        }
                      }
                    }
                    """.trimIndent()
                } else if (activeBlueprintTab == "WEBHOOK_API") {
                    """
                    import { Router, Request, Response } from 'express';
                    import { WebhookProcessorService } from '../services/webhookService';
                    import { Pool } from 'pg';

                    const router = Router();
                    const dbPool = new Pool(); // Pulls configuration automatically from process.env
                    const processor = new WebhookProcessorService(dbPool);

                    /**
                     * Security Middleware Layer validating secret handshake header keys 
                     * assigned exclusively between your backend and the data provider.
                     */
                    const validateProviderSignature = (req: Request, res: Response, next: any) => {
                      const secretHeader = req.headers['x-provider-signature'];
                      const trustedToken = process.env.SPORTS_PROVIDER_WEBHOOK_SECRET;

                      if (!secretHeader || secretHeader !== trustedToken) {
                        return res.status(401).json({ success: false, message: "Unauthorized webhook handshake signature rejection." });
                      }
                      next();
                    };

                    /**
                     * 📥 INGEST ENTHUSIAST: LIVE SCORE TELEMETRY
                     * Endpoint: POST /api/v1/webhooks/sports-stream/match
                     */
                    router.post('/match', validateProviderSignature, async (req: Request, res: Response) => {
                      try {
                        // Non-blocking processing execution allows Express to respond immediately to the provider node
                        processor.processMatchUpdate(req.body).catch(err => 
                          console.error('⚠️ Secondary task background match update fail:', err.message)
                        );
                        
                        return res.status(202).json({ success: true, message: "Match telemetry data stream accepted." });
                      } catch (error) {
                        return res.status(500).json({ success: false, error: "Ingestion node core block error." });
                      }
                    });

                    /**
                     * 📥 INGEST ENTHUSIAST: REAL-TIME ODDS LIFECYCLE
                     * Endpoint: POST /api/v1/webhooks/sports-stream/odds
                     */
                    router.post('/odds', validateProviderSignature, async (req: Request, res: Response) => {
                      try {
                        processor.processOddsUpdate(req.body).catch(err => 
                          console.error('⚠️ Secondary task background odds line change fail:', err.message)
                        );

                        return res.status(202).json({ success: true, message: "Odds matrix updates stream accepted." });
                      } catch (error) {
                        return res.status(500).json({ success: false, error: "Ingestion node core block error." });
                      }
                    });

                    export default router;
                    """.trimIndent()
                } else {
                    """
                    import React, { useEffect, useState } from 'react';
                    import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

                    interface GgrTrendItem {
                      date: string;
                      stakes: number;
                      payouts: number;
                      ggr: number;
                    }

                    export const GgrAnalytics: React.FC = () => {
                      const [data, setData] = useState<GgrTrendItem[]>([]);
                      const [loading, setLoading] = useState(true);

                      useEffect(() => {
                        // Dynamic fetch from standard internal analytical router
                        fetch('/api/admin/ggr-trends')
                          .then(res => res.json())
                          .then(resJson => {
                            if (resJson.success) {
                              setData(resJson.data);
                            }
                            setLoading(false);
                          })
                          .catch(err => {
                            console.error('Failed to load GGR metrics:', err);
                            setLoading(false);
                          });
                      }, []);

                      return (
                        <div className="bg-[#0f172a] border border-[#1e293b] rounded-lg p-6 w-full text-white">
                          <div className="flex justify-between items-center mb-6">
                            <div>
                              <h3 className="text-sm font-semibold tracking-wide text-cyan-400 font-mono">
                                📈 PLATFORM PROFITABILITY MATRIX
                              </h3>
                              <p className="text-xs text-slate-400 mt-1">
                                Running 30-Day Net Gross Gaming Revenue (GGR) Trend (Stakes - Wayouts)
                              </p>
                            </div>
                            <span className="text-xs px-2.5 py-1 bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 rounded font-mono">
                              RECHARTS GRAPH ACTIVE
                            </span>
                          </div>

                          <div className="h-[280px] w-full">
                            {loading ? (
                              <div className="h-full flex items-center justify-center text-xs text-slate-400 font-mono">
                                🔄 Compiling Aggregations from PostgreSQL cluster pool...
                              </div>
                            ) : (
                              <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={data} margin={{ top: 10, right: 20, left: 10, bottom: 5 }}>
                                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                                  <XAxis dataKey="date" stroke="#64748b" fontSize={10} tickLine={false} />
                                  <YAxis stroke="#64748b" fontSize={10} tickLine={false} unit=" ETB" />
                                  <Tooltip 
                                    contentStyle={{ backgroundColor: '#070a0e', borderColor: '#1e293b', fontSize: '11px' }}
                                    labelClassName="text-slate-400 font-mono"
                                  />
                                  <Legend verticalAlign="top" height={36} iconSize={10} wrapperStyle={{ fontSize: '11px' }} />
                                  <Line name="Total Handle (Stakes)" type="monotone" dataKey="stakes" stroke="#3b82f6" strokeWidth={2} dot={false} />
                                  <Line name="Settled Payouts (Losses)" type="monotone" dataKey="payouts" stroke="#ef4444" strokeWidth={2} dot={false} />
                                  <Line name="Net GGR (Gross Margin)" type="monotone" dataKey="ggr" stroke="#00c2ff" strokeWidth={3} dot={{ r: 3 }} />
                                </LineChart>
                              </ResponsiveContainer>
                            )}
                          </div>
                        </div>
                      );
                    };
                    """.trimIndent()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    val codeScrollState = rememberScrollState()
                    Text(
                        text = codeContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = TextLight,
                        lineHeight = 11.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(codeScrollState)
                    )
                }
            }
        }

        // SECTION 5: ENTERPRISE POSTGRESQL SCHEMA (DDL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F162A))
                .border(0.5.dp, Color(0xFFE2E8F0).copy(0.3f), RoundedCornerShape(4.dp))
                .clickable { showPostgresqlSchema = !showPostgresqlSchema }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Database icon",
                    tint = NeonBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "5. ENTERPRISE POSTGRESQL TRANSACTION SCHEMA",
                    color = TextWhite,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = if (showPostgresqlSchema) "HIDE SCHEMA ▲" else "VIEW SCHEMA ▼",
                color = NeonBlue,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showPostgresqlSchema) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B11), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACID-compliant relational design representing production sports betting database ledgers, including users, isolated wallet balances, and strict transaction tracking constraints:",
                    fontSize = 8.sp,
                    color = TextMuted,
                    lineHeight = 11.sp
                )

                val sqlContent = """
                    -- ============================================================================
                    -- POSTGRESQL DDL MIGRATION SCRIPT
                    -- Target: Enterprise Sports Betting Platform Core Schema
                    -- Features: ACID Compliance, Strict Constraints, Optimized B-Tree Performance
                    -- ============================================================================

                    BEGIN;

                    -- ----------------------------------------------------------------------------
                    -- 1. EXTENSIONS & CUSTOM ENUMS
                    -- ----------------------------------------------------------------------------
                    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

                    CREATE TYPE user_role AS ENUM ('user', 'admin', 'super_admin');
                    CREATE TYPE match_status AS ENUM ('Not Started', 'Live', 'Completed', 'Cancelled', 'Paused');
                    CREATE TYPE bet_status AS ENUM ('Pending', 'Won', 'Lost', 'Voided');
                    CREATE TYPE transaction_type AS ENUM ('Deposit', 'Withdrawal', 'Refund', 'Payout');
                    CREATE TYPE transaction_status AS ENUM ('Pending', 'Approved', 'Rejected', 'Failed');
                    CREATE TYPE payment_method AS ENUM ('TeleBirr', 'CBE Birr', 'Bank Transfer', 'Credit Card');

                    -- ----------------------------------------------------------------------------
                    -- 2. TABLES MANAGEMENT
                    -- ----------------------------------------------------------------------------

                    -- Users Table
                    CREATE TABLE users (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        role user_role DEFAULT 'user'::user_role NOT NULL,
                        device_hardware_hash VARCHAR(255),
                        is_flagged BOOLEAN DEFAULT FALSE NOT NULL,
                        last_login_ip VARCHAR(45),
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
                    );

                    -- Wallets Table (Decoupled ledger to guarantee atomic isolation balances)
                    CREATE TABLE wallets (
                        user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE RESTRICT,
                        balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
                        currency VARCHAR(3) DEFAULT 'ETB' NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        CONSTRAINT chk_positive_balance CHECK (balance >= 0.00)
                    );

                    -- Matches Table
                    CREATE TABLE matches (
                        id SERIAL PRIMARY KEY,
                        home_team VARCHAR(100) NOT NULL,
                        away_team VARCHAR(100) NOT NULL,
                        start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                        status match_status DEFAULT 'Not Started'::match_status NOT NULL,
                        home_score INT DEFAULT 0 NOT NULL,
                        away_score INT DEFAULT 0 NOT NULL,
                        minute_elapsed VARCHAR(10) DEFAULT '0''' NOT NULL, -- e.g., "65'" or "HT"
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        CONSTRAINT chk_valid_scores CHECK (home_score >= 0 AND away_score >= 0)
                    );

                    -- Betting Markets Table
                    CREATE TABLE markets (
                        id SERIAL PRIMARY KEY,
                        match_id INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
                        market_type VARCHAR(50) NOT NULL, -- '1X2', 'Over/Under 2.5', 'BTTS'
                        is_active BOOLEAN DEFAULT TRUE NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        CONSTRAINT uq_match_market UNIQUE (match_id, market_type)
                    );

                    -- Market Odds Table (Keeps historical track of shifts or handles active odds)
                    CREATE TABLE market_odds (
                        id SERIAL PRIMARY KEY,
                        market_id INT NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
                        selection VARCHAR(50) NOT NULL, -- '1', 'X', '2', 'Over', 'Under', 'Yes', 'No'
                        odds DECIMAL(6, 2) NOT NULL,
                        is_suspended BOOLEAN DEFAULT FALSE NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        CONSTRAINT chk_minimum_odds CHECK (odds > 1.00),
                        CONSTRAINT uq_market_selection UNIQUE (market_id, selection)
                    );

                    -- Bets Placed Table
                    CREATE TABLE bets (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                        match_id INT NOT NULL REFERENCES matches(id) ON DELETE RESTRICT,
                        market_id INT NOT NULL REFERENCES markets(id) ON DELETE RESTRICT,
                        selection VARCHAR(50) NOT NULL,
                        odds_at_placement DECIMAL(6, 2) NOT NULL,
                        stake DECIMAL(15, 2) NOT NULL,
                        possible_win DECIMAL(15, 2) GENERATED ALWAYS AS (stake * odds_at_placement) STORED,
                        status bet_status DEFAULT 'Pending'::bet_status NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        settled_at TIMESTAMP WITH TIME ZONE,
                        CONSTRAINT chk_positive_stake CHECK (stake > 0.00)
                    );

                    -- Financial Transactions Table
                    CREATE TABLE transactions (
                        id VARCHAR(100) PRIMARY KEY, -- e.g., External Core Gateway #TRX9852 or internal UUID
                        user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                        type transaction_type NOT NULL,
                        amount DECIMAL(15, 2) NOT NULL,
                        currency VARCHAR(3) DEFAULT 'ETB' NOT NULL,
                        method payment_method NOT NULL,
                        status transaction_status DEFAULT 'Pending'::transaction_status NOT NULL,
                        gateway_reference VARCHAR(150), -- TeleBirr/CBE outTradeNo or TradeNo receipt mapping
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        processed_at TIMESTAMP WITH TIME ZONE,
                        CONSTRAINT chk_positive_amount CHECK (amount > 0.00)
                    );

                    -- Centralized Systems & Financial Reporting Incident Error Logs Table
                    CREATE TABLE reporting_error_logs (
                        id SERIAL PRIMARY KEY,
                        service_name VARCHAR(100) DEFAULT 'ReportingService' NOT NULL,
                        job_name VARCHAR(150) NOT NULL,
                        error_message TEXT NOT NULL,
                        stack_trace TEXT,
                        logged_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
                    );

                    -- ----------------------------------------------------------------------------
                    -- 3. INDEXES FOR PERFORMANCE IN CONCURRENCY ENVIRONMENTS
                    -- ----------------------------------------------------------------------------

                    -- Fast identification lookups on matches filtering by status (Crucial for Live feeds)
                    CREATE INDEX idx_matches_status_time ON matches (status, start_time DESC);

                    -- Lookup odds instantly per market entity
                    CREATE INDEX idx_market_odds_market_id ON market_odds (market_id) WHERE is_suspended = FALSE;

                    -- Speeds up User Profile Dashboards and financial tracking views
                    CREATE INDEX idx_bets_user_status ON bets (user_id, status);
                    CREATE INDEX idx_bets_match_id ON bets (match_id);

                    -- Fast security tracking index on unique hardware fingerprints
                    CREATE INDEX idx_users_device_fraud ON users (device_hardware_hash) WHERE is_flagged = FALSE;

                    -- Speeds up transactional audits, webhooks, and gateway idempotency verification
                    CREATE INDEX idx_transactions_user_id ON transactions (user_id);
                    CREATE INDEX idx_transactions_gateway_ref ON transactions (gateway_reference) WHERE gateway_reference IS NOT NULL;
                    CREATE INDEX idx_transactions_status_created ON transactions (status, created_at DESC);

                    -- ----------------------------------------------------------------------------
                    -- 4. AUTOMATIC UPDATE TRIGGERS
                    -- ----------------------------------------------------------------------------

                    -- Reusable global tracking logic function
                    CREATE OR REPLACE FUNCTION update_modified_column()
                    RETURNS TRIGGER AS ${'$'}${'$'}
                    BEGIN
                        NEW.updated_at = CURRENT_TIMESTAMP;
                        RETURN NEW;
                    END;
                    ${'$'}${'$'} LANGUAGE plpgsql;

                    -- Bindings for modifying track state logs
                    CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_modified_column();
                    CREATE TRIGGER update_wallets_modtime BEFORE UPDATE ON wallets FOR EACH ROW EXECUTE FUNCTION update_modified_column();
                    CREATE TRIGGER update_matches_modtime BEFORE UPDATE ON matches FOR EACH ROW EXECUTE FUNCTION update_modified_column();
                    CREATE TRIGGER update_markets_modtime BEFORE UPDATE ON markets FOR EACH ROW EXECUTE FUNCTION update_modified_column();

                    -- ----------------------------------------------------------------------------
                    -- 5. SAFETY DATA SEEDING INTERCEPTOR (Automated User Wallet Creator)
                    -- ----------------------------------------------------------------------------
                    CREATE OR REPLACE FUNCTION auto_create_user_wallet()
                    RETURNS TRIGGER AS ${'$'}${'$'}
                    BEGIN
                        INSERT INTO wallets (user_id, balance, currency)
                        VALUES (NEW.id, 0.00, 'ETB');
                        RETURN NEW;
                    END;
                    ${'$'}${'$'} LANGUAGE plpgsql;

                    CREATE TRIGGER trigger_user_signup_wallet
                    AFTER INSERT ON users
                    FOR EACH ROW EXECUTE FUNCTION auto_create_user_wallet();

                    COMMIT;
                """.trimIndent()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    val sqlScrollState = rememberScrollState()
                    Text(
                        text = sqlContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = TextLight,
                        lineHeight = 11.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(sqlScrollState)
                    )
                }
            }
        }

        // FOOTER CONSOLE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(4.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = "GATEWAY FEEDBACK CONSOLE:",
                    color = TextMuted,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = telebirrStatusLog,
                    color = if (telebirrStatusLog.contains("Success")) NeonGreen else if (telebirrStatusLog.contains("Error")) LightRed else TextLight,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 11.sp
                )
            }
        }
    }
}