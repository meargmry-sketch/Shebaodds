package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.BetViewModel
import java.text.SimpleDateFormat
import java.util.*

// Model representing security logs
data class SecurityLogEntry(
    val id: String,
    val timestamp: String,
    val type: String, // "AUTH", "PAYMENT", "SYS", "CASINO"
    val description: String,
    val success: Boolean
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserProfileDialog(
    viewModel: BetViewModel,
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.wallet.collectAsState()
    val bioQuickBetEnabled by viewModel.biometricQuickBetEnabled.collectAsState()
    val bioPaymentsEnabled by viewModel.biometricPaymentsEnabled.collectAsState()
    val bioLoginEnabled by viewModel.biometricLoginEnabled.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allBets by viewModel.allBets.collectAsState()
    // 🎰 NEW: Fetch casino games from ViewModel
    val allCasinoGames by viewModel.allCasinoGames.collectAsState()

    // Manage profile security preferences locally / via ViewModel
    var bioProfileLockEnabled by remember { mutableStateOf(true) }

    // Seed dynamic logs list
    val sdf = SimpleDateFormat("HH:mm:ss.S", Locale.getDefault())
    val baseLogs = remember {
        mutableStateListOf(
            SecurityLogEntry("1", sdf.format(Date(System.currentTimeMillis() - 450000)), "AUTH", "Biometric verification layer initialized safely", true),
            SecurityLogEntry("2", sdf.format(Date(System.currentTimeMillis() - 320000)), "SYS", "Sec-Keystore key-pair rotation verification - COMPLETE", true),
            SecurityLogEntry("3", sdf.format(Date(System.currentTimeMillis() - 150000)), "PAYMENT", "Payment gateway secure token generated for TeleBirr", true),
            // 🎰 NEW: Casino security log
            SecurityLogEntry("4", sdf.format(Date(System.currentTimeMillis() - 60000)), "CASINO", "Casino Quick Bet TouchID authorized for Aviator", true)
        )
    }

    // Add log when biometric values toggle
    fun addSecurityLog(type: String, desc: String, success: Boolean) {
        val entry = SecurityLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = sdf.format(Date()),
            type = type,
            description = desc,
            success = success
        )
        baseLogs.add(0, entry)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
                .testTag("user_profile_modal_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateDarkBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateCardBG)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(PrimarySapphire.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimarySapphire,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "USER PROFILE HUB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Identity Protection and Cryptographic Keys",
                                    fontSize = 8.5.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(30.dp)
                                .background(SlateSurfaceL2, CircleShape)
                                .testTag("close_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Divider(color = BorderColor, thickness = 1.dp)

                // Main body content (Scrollable)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp)
                ) {
                    // Profile Info Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Simulated VIP Badge avatar
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(AmberAccent, Color(0xFFF59E0B))
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "KL",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = SlateDarkBG
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = wallet?.username ?: "King Lalibela II",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = TextWhite
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(AmberAccent.copy(0.12f), RoundedCornerShape(4.dp))
                                                    .border(0.5.dp, AmberAccent.copy(0.4f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "VIP-3",
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = AmberAccent
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = wallet?.email ?: "mry3bcha@gmail.com",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ID: SHEBA-901-4432-88",
                                            fontSize = 9.sp,
                                            color = TextGrey,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = BorderColor.copy(0.35f))
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "AVAILABLE FUNDS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextGrey)
                                        Text(text = "${String.format("%,.2f", wallet?.balance ?: 523600.00)} ETB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "CLEARANCE ROLE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextGrey)
                                        Text(text = (wallet?.role ?: "SUPER_ADMIN").uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimarySapphire)
                                    }
                                }
                            }
                        }
                    }

                    // Betting Insights Section Header
                    item {
                        Text(
                            text = "BETTING INSIGHTS & PERFORMANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        BettingInsightsCard(
                            allBets = allBets,
                            onSeedHistory = { viewModel.seedDemoHistory() }
                        )
                    }

                    // 🎰 NEW: Casino Game Favorites & Stats Section
                    item {
                        Text(
                            text = "🎰 CASINO GAME FAVORITES & STATS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        CasinoFavoritesCard(
                            casinoGames = allCasinoGames,
                            onToggleFavorite = { gameId, isFavorite ->
                                viewModel.toggleCasinoFavorite(gameId, isFavorite)
                                addSecurityLog("CASINO", "Favorite status changed for game $gameId: ${if (isFavorite) "Added" else "Removed"}", true)
                            }
                        )
                    }

                    // Transaction & Betting History Section Header
                    item {
                        Text(
                            text = "TRANSACTION & BETTING HISTORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        TransactionBetHistoryCard(
                            allTransactions = allTransactions,
                            allBets = allBets
                        )
                    }

                    // Security Preferences Section Header
                    item {
                        Text(
                            text = "BIOMETRIC AUTH SECURITY SETTINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    // Security Switch 1: Profile Shield Lock
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(PrimarySapphire.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimarySapphire, modifier = Modifier.size(15.dp))
                                    }
                                    Column {
                                        Text(text = "Secure Profile Access", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text(text = "Require fingerprint check to access profile logs", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                                Switch(
                                    checked = bioProfileLockEnabled,
                                    onCheckedChange = {
                                        bioProfileLockEnabled = it
                                        addSecurityLog("SYS", "Secure Profile Lock changed to: $it", true)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(0.3f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SlateSurfaceL2
                                    ),
                                    modifier = Modifier.testTag("bio_profile_lock_switch")
                                )
                            }
                        }
                    }

                    // Security Switch 2: Payment Confirmations Lock
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NeonGreen.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                                    }
                                    Column {
                                        Text(text = "Payment Gateways Confirmation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text(text = "Secure deposits & withdrawals with TouchID", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                                Switch(
                                    checked = bioPaymentsEnabled,
                                    onCheckedChange = {
                                        viewModel.setBiometricPaymentsEnabled(it)
                                        addSecurityLog("PAYMENT", "Payment Protection Layer changed to: $it", true)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(0.3f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SlateSurfaceL2
                                    ),
                                    modifier = Modifier.testTag("bio_payments_lock_switch")
                                )
                            }
                        }
                    }

                    // Security Switch 3: Quick Bet Layer
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(AmberAccent.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(15.dp))
                                    }
                                    Column {
                                        Text(text = "Quick Bet TouchID Guard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text(text = "Secure single slip placements instantly", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                                Switch(
                                    checked = bioQuickBetEnabled,
                                    onCheckedChange = {
                                        viewModel.setBiometricQuickBetEnabled(it)
                                        addSecurityLog("AUTH", "Quick Bet TouchID changed to: $it", true)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(0.3f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SlateSurfaceL2
                                    ),
                                    modifier = Modifier.testTag("bio_quick_bet_switch")
                                )
                            }
                        }
                    }

                    // Security Switch 4: Biometric Login Unlock (Optional Login Layer)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NeonGreen.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                                    }
                                    Column {
                                        Text(text = "Biometric Login Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text(text = "Allow fingerprint/face unlock for secure, instant account sign-in", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                                Switch(
                                    checked = bioLoginEnabled,
                                    onCheckedChange = {
                                        viewModel.setBiometricLoginEnabled(it)
                                        addSecurityLog("AUTH", "Biometric Login Unlock changed to: $it", true)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(0.3f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SlateSurfaceL2
                                    ),
                                    modifier = Modifier.testTag("bio_login_switch")
                                )
                            }
                        }
                    }

                    // Cryptographic Logs Section Header
                    item {
                        Text(
                            text = "RESPONSIBLE GAMING - BET LIMITS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        ResponsibleLimitsCard(viewModel = viewModel, onLogEvent = { desc ->
                            addSecurityLog("SYS", desc, true)
                        })
                    }

                    item {
                        Text(
                            text = "RESPONSIBLE GAMING - DEPOSIT LIMITS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        ResponsibleDepositLimitsCard(viewModel = viewModel, onLogEvent = { desc ->
                            addSecurityLog("SYS", desc, true)
                        })
                    }

                    // Cryptographic Logs Section Header
                    item {
                        Text(
                            text = "CRYPTOGRAPHIC SECURITY LOGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    // Dynamic logs container
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceL2),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (baseLogs.isEmpty()) {
                                    Text(
                                        text = "No recorded biometric transactions.",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                } else {
                                    baseLogs.forEach { log ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "[${log.timestamp}]",
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                color = PrimarySapphire,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "•",
                                                fontSize = 9.sp,
                                                color = if (log.success) NeonGreen else LightRed
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = log.description,
                                                    fontSize = 9.5.sp,
                                                    color = TextWhite,
                                                    lineHeight = 13.sp
                                                )
                                                Text(
                                                    text = "Type: ${log.type}  |  STATUS: ${if (log.success) "SECURED" else "DENIED"}",
                                                    fontSize = 7.5.sp,
                                                    color = TextMuted,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Divider(color = BorderColor.copy(0.2f), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    // HELP & SUPPORT DESK SECTION
                    item {
                        Text(
                            text = "HELP & SUPPORT CENTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("frequently_asked_questions_card"),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "FAQS & TROUBLESHOOTING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                var expandedQ1 by remember { mutableStateOf(false) }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedQ1 = !expandedQ1 }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "1. How do odds alerts work?",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedQ1) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (expandedQ1) {
                                        Text(
                                            text = "When you click the bell icon on a sports card, the match is added to your Tracking List. If any odds change by 0.05 or more, a push notification is triggered and saved in the Hub.",
                                            fontSize = 9.sp,
                                            color = TextMuted,
                                            modifier = Modifier.padding(top = 4.dp),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                Divider(color = BorderColor.copy(0.3f), thickness = 0.5.dp)

                                var expandedQ2 by remember { mutableStateOf(false) }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedQ2 = !expandedQ2 }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "2. Is Telebirr deposit fast?",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedQ2) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (expandedQ2) {
                                        Text(
                                            text = "Yes, all deposits made with our Telebirr integration are processed instantly using secure biometric validation channels.",
                                            fontSize = 9.sp,
                                            color = TextMuted,
                                            modifier = Modifier.padding(top = 4.dp),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                                Divider(color = BorderColor.copy(0.3f), thickness = 0.5.dp)

                                var expandedQ3 by remember { mutableStateOf(false) }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedQ3 = !expandedQ3 }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "3. How are limits enforced?",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedQ3) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (expandedQ3) {
                                        Text(
                                            text = "Daily and weekly limits caps are tracked live inside your profile ledger. Placements are locked once you breach the cap.",
                                            fontSize = 9.sp,
                                            color = TextMuted,
                                            modifier = Modifier.padding(top = 4.dp),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Contact Customer Support inquiries triggers
                                var showContactSuccess by remember { mutableStateOf(false) }
                                if (showContactSuccess) {
                                    Text(
                                        text = "Inquiry filed securely! Our support agents will reply shortly in the Support Chat tab.",
                                        fontSize = 9.sp,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            showContactSuccess = true
                                            addSecurityLog("SYS", "Customer service inquiry created successfully", true)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("contact_support_dialog_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Text("Contact Live Desk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SIGN OUT SESSION WIDGET
                    item {
                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_logout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sign Out",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "LOG OUT OF SHEBAODDS SESSION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// 🎰 NEW: Casino Favorites & Stats Card
// ==========================================================
@Composable
fun CasinoFavoritesCard(
    casinoGames: List<com.example.data.model.CasinoGameEntity>,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("casino_favorites_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your favorite casino games and stats",
                fontSize = 9.5.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val favorites = casinoGames.filter { it.isFavorite }
            if (favorites.isEmpty()) {
                Text(
                    text = "No favorite casino games selected yet. Tap the star icon on any casino game card to add it.",
                    fontSize = 9.sp,
                    color = TextMuted
                )
            } else {
                favorites.take(10).forEach { game ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = game.icon, fontSize = 16.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = game.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(text = "Played: ${game.timesPlayed} | Wagered: ${game.totalWagered.toInt()} ETB", fontSize = 9.sp, color = TextMuted)
                        }
                        IconButton(
                            onClick = { onToggleFavorite(game.id, false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Remove from favorites",
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// EXISTING COMPOSABLES (Kept unchanged, but their imports updated)
// ==========================================================

@Composable
fun ResponsibleLimitsCard(
    viewModel: BetViewModel,
    onLogEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val weeklyLimit by viewModel.weeklyLimit.collectAsState()
    val monthlyLimit by viewModel.monthlyLimit.collectAsState()

    val dailySpent by viewModel.dailyWagerTotal.collectAsState()
    val weeklySpent by viewModel.weeklyWagerTotal.collectAsState()
    val monthlySpent by viewModel.monthlyWagerTotal.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("responsible_limits_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Keep gaming fun and safe by setting limits on your total betting stakes. Once a limit is reached, further placements will be rejected until the period resets.",
                fontSize = 9.5.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Daily Limit Section
            LimitRow(
                title = "DAILY LIMIT",
                currentLimit = dailyLimit,
                spent = dailySpent,
                onSetLimit = { limit ->
                    viewModel.setDailyLimit(limit)
                    onLogEvent("Daily betting limit changed to: ${limit?.let { "$it ETB" } ?: "UNLIMITED"}")
                },
                inputTag = "limit_daily_input",
                setTag = "limit_daily_set_btn",
                clearTag = "limit_daily_clear_btn"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(0.3f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Weekly Limit Section
            LimitRow(
                title = "WEEKLY LIMIT",
                currentLimit = weeklyLimit,
                spent = weeklySpent,
                onSetLimit = { limit ->
                    viewModel.setWeeklyLimit(limit)
                    onLogEvent("Weekly betting limit changed to: ${limit?.let { "$it ETB" } ?: "UNLIMITED"}")
                },
                inputTag = "limit_weekly_input",
                setTag = "limit_weekly_set_btn",
                clearTag = "limit_weekly_clear_btn"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(0.3f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Monthly Limit Section
            LimitRow(
                title = "MONTHLY LIMIT",
                currentLimit = monthlyLimit,
                spent = monthlySpent,
                onSetLimit = { limit ->
                    viewModel.setMonthlyLimit(limit)
                    onLogEvent("Monthly betting limit changed to: ${limit?.let { "$it ETB" } ?: "UNLIMITED"}")
                },
                inputTag = "limit_monthly_input",
                setTag = "limit_monthly_set_btn",
                clearTag = "limit_monthly_clear_btn"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitRow(
    title: String,
    currentLimit: Double?,
    spent: Double,
    onSetLimit: (Double?) -> Unit,
    inputTag: String,
    setTag: String,
    clearTag: String,
    spentLabel: String = "Spent"
) {
    var textInput by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            if (currentLimit != null) {
                Text(
                    text = "${String.format("%,.1f", currentLimit)} ETB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen
                )
            } else {
                Text(
                    text = "NO LIMIT SET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar if a limit is set
        if (currentLimit != null) {
            val progress = if (currentLimit > 0) (spent / currentLimit).coerceIn(0.0, 1.0).toFloat() else 0f
            val percent = (progress * 100).toInt()

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress > 0.85f) LightRed else AmberAccent,
                    trackColor = SlateSurfaceL2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$spentLabel: ${String.format("%,.1f", spent)} / ${String.format("%,.1f", currentLimit)} ETB",
                        fontSize = 8.5.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$percent%",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progress > 0.85f) LightRed else AmberAccent
                    )
                }
            }
        } else {
            Text(
                text = "$spentLabel this period: ${String.format("%,.1f", spent)} ETB",
                fontSize = 8.5.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentLimit != null) {
                    Text(
                        text = "Modify Limit",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimarySapphire,
                        modifier = Modifier
                            .clickable { isEditing = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Clear Limit",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightRed,
                        modifier = Modifier
                            .testTag(clearTag)
                            .clickable { onSetLimit(null) }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                } else {
                    Text(
                        text = "+ Set Period Limit",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        modifier = Modifier
                            .testTag("activate_" + inputTag)
                            .clickable { isEditing = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Amount (ETB)", fontSize = 10.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag(inputTag),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SlateSurfaceL2,
                        unfocusedContainerColor = SlateSurfaceL2,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedIndicatorColor = AmberAccent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )

                Button(
                    onClick = {
                        val value = textInput.toDoubleOrNull()
                        if (value != null && value > 0) {
                            onSetLimit(value)
                            textInput = ""
                            isEditing = false
                        }
                    },
                    modifier = Modifier
                        .height(38.dp)
                        .testTag(setTag),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Text(
                    text = "Cancel",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey,
                    modifier = Modifier
                        .clickable {
                            textInput = ""
                            isEditing = false
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ResponsibleDepositLimitsCard(
    viewModel: BetViewModel,
    onLogEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyLimit by viewModel.dailyDepositLimit.collectAsState()
    val weeklyLimit by viewModel.weeklyDepositLimit.collectAsState()

    val dailySpent by viewModel.dailyDepositTotal.collectAsState()
    val weeklySpent by viewModel.weeklyDepositTotal.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("responsible_deposit_limits_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Promote safer betting habits by capping your maximum allowed deposits. Once a limit is reached, further top-ups will be restricted until the period resets.",
                fontSize = 9.5.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Daily Deposit Limit Section
            LimitRow(
                title = "DAILY DEPOSIT LIMIT",
                currentLimit = dailyLimit,
                spent = dailySpent,
                onSetLimit = { limit ->
                    viewModel.setDailyDepositLimit(limit)
                    onLogEvent("Daily deposit limit changed to: ${limit?.let { "$it ETB" } ?: "UNLIMITED"}")
                },
                inputTag = "limit_deposit_daily_input",
                setTag = "limit_deposit_daily_set_btn",
                clearTag = "limit_deposit_daily_clear_btn",
                spentLabel = "Deposited"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(0.3f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Weekly Deposit Limit Section
            LimitRow(
                title = "WEEKLY DEPOSIT LIMIT",
                currentLimit = weeklyLimit,
                spent = weeklySpent,
                onSetLimit = { limit ->
                    viewModel.setWeeklyDepositLimit(limit)
                    onLogEvent("Weekly deposit limit changed to: ${limit?.let { "$it ETB" } ?: "UNLIMITED"}")
                },
                inputTag = "limit_deposit_weekly_input",
                setTag = "limit_deposit_weekly_set_btn",
                clearTag = "limit_deposit_weekly_clear_btn",
                spentLabel = "Deposited"
            )
        }
    }
}

@Composable
fun TransactionBetHistoryCard(
    allTransactions: List<com.example.data.model.TransactionRecord>,
    allBets: List<com.example.data.model.Bet>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Financials, 1: Bet Slips

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_bet_history_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TRANSACTION & BET HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 0.5.sp
                )

                // Segmented buttons
                Row(
                    modifier = Modifier
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 0) PrimarySapphire else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("history_tab_txs"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Financials",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) TextWhite else TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 1) PrimarySapphire else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("history_tab_bets"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bet Slips",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) TextWhite else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Financial transactions
                if (allTransactions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No financial transactions recorded.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Render list of transactions
                    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US) }
                    allTransactions.take(8).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Icon representing deposit vs withdrawal
                            val isDeposit = tx.type == "DEPOSIT"
                            val iconColor = if (isDeposit) {
                                if (tx.status == "APPROVED") NeonGreen else if (tx.status == "PENDING") AmberAccent else LightRed
                            } else {
                                if (tx.status == "APPROVED") Color(0xFF60A5FA) else if (tx.status == "PENDING") AmberAccent else LightRed
                            }

                            Box(
                                modifier = Modifier
                                    .size(31.dp)
                                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isDeposit) "Funds Deposited via ${tx.method}" else "Funds Withdrawn via ${tx.method}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val displayTime = remember(tx.timestamp) {
                                    if (tx.timestamp > 0) {
                                        sdf.format(Date(tx.timestamp))
                                    } else {
                                        tx.timeLabel
                                    }
                                }
                                Text(
                                    text = displayTime,
                                    fontSize = 8.sp,
                                    color = TextMuted
                                )
                            }

                            // Amount and Status Flag
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isDeposit) "+" else "-"}${String.format(Locale.US, "%,.1f", tx.amount)} ETB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDeposit && tx.status == "APPROVED") NeonGreen else if (!isDeposit && tx.status == "APPROVED") TextWhite else TextMuted
                                )
                                Spacer(modifier = Modifier.height(3.dp))

                                val statusBg = when (tx.status) {
                                    "APPROVED" -> NeonGreen.copy(0.12f)
                                    "PENDING" -> AmberAccent.copy(0.12f)
                                    else -> LightRed.copy(0.12f)
                                }
                                val statusTextColor = when (tx.status) {
                                    "APPROVED" -> NeonGreen
                                    "PENDING" -> AmberAccent
                                    else -> LightRed
                                }

                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, statusTextColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tx.status,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusTextColor
                                    )
                                }
                            }
                        }
                        Divider(color = BorderColor.copy(alpha = 0.2f), thickness = 0.5.dp)
                    }
                }
            } else {
                // Placed Bets history
                if (allBets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No bets placed yet.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US) }
                    allBets.take(8).forEach { bet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Icon for Sport ticket type
                            val sportIcon = when (bet.sport) {
                                "Football" -> Icons.Default.SportsSoccer
                                "Basketball" -> Icons.Default.SportsBasketball
                                "Tennis" -> Icons.Default.SportsBaseball
                                "Esports" -> Icons.Default.SportsEsports
                                else -> Icons.Default.Sports
                            }
                            Box(
                                modifier = Modifier
                                    .size(31.dp)
                                    .background(PrimarySapphire.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = sportIcon,
                                    contentDescription = null,
                                    tint = PrimarySapphire,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Team matches & market info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${bet.teamA} vs ${bet.teamB}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Selection: ${bet.selection} @ ${String.format(Locale.US, "%.2f", bet.odds)} odds | Stake: ${String.format(Locale.US, "%,.1f", bet.stake)} ETB",
                                    fontSize = 8.sp,
                                    color = TextLight,
                                    lineHeight = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Ticket: ${bet.ticketNumber} • ${sdf.format(Date(bet.timestamp))}",
                                    fontSize = 7.5.sp,
                                    color = TextMuted
                                )
                            }

                            // Pay-out Returns & Status indicator
                            Column(horizontalAlignment = Alignment.End) {
                                val displayReturn = if (bet.status == "WON" || bet.status == "CASHOUT") {
                                    bet.potentialReturn
                                } else {
                                    0.0
                                }
                                Text(
                                    text = if (displayReturn > 0) "+${String.format(Locale.US, "%,.1f", displayReturn)} ETB" else "${String.format(Locale.US, "%,.1f", bet.potentialReturn)} ETB",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (bet.status == "WON") NeonGreen else if (bet.status == "CASHOUT") Color(0xFFC084FC) else TextWhite
                                )
                                Spacer(modifier = Modifier.height(3.dp))

                                val (statusBg, statusTextColor) = when (bet.status) {
                                    "WON" -> Pair(NeonGreen.copy(0.12f), NeonGreen)
                                    "LOST" -> Pair(LightRed.copy(0.12f), LightRed)
                                    "CASHOUT" -> Pair(Color(0xFFC084FC).copy(0.12f), Color(0xFFC084FC))
                                    "PENDING" -> Pair(AmberAccent.copy(0.12f), AmberAccent)
                                    else -> Pair(SlateSurfaceL2, TextMuted)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(statusBg, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, statusTextColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = bet.status,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusTextColor
                                    )
                                }
                            }
                        }
                        Divider(color = BorderColor.copy(alpha = 0.2f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun BettingInsightsCard(
    allBets: List<com.example.data.model.Bet>,
    onSeedHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canvasBorderColor = BorderColor
    val canvasSlateDarkBG = SlateDarkBG
    val canvasPrimarySapphire = PrimarySapphire
    val canvasNeonGreen = NeonGreen
    val canvasLightRed = LightRed
    val canvasAmberAccent = AmberAccent

    var selectedTab by remember { mutableStateOf(0) }

    val settledBets = remember(allBets) {
        allBets.filter { it.status != "PENDING" }.sortedBy { it.timestamp }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("betting_insights_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BETTING INSIGHTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("ROI Curve", "Win/Loss", "Arenas").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTab == index) PrimarySapphire else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                .testTag("insights_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (allBets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(AmberAccent.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Analytics Hub Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "No tickets placed inside the Sportsbook yet.",
                        fontSize = 8.5.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSeedHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("seed_insights_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Generate 14-Day History",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        val totalStaked = allBets.sumOf { it.stake }
                        val settledWon = allBets.filter { it.status == "WON" || it.status == "CASHOUT" }
                        val totalReturns = settledWon.sumOf { it.potentialReturn }
                        val netProfit = totalReturns - totalStaked
                        val netRoi = if (totalStaked > 0) (netProfit / totalStaked) * 100 else 0.0
                        val metricColor = if (netProfit >= 0) canvasNeonGreen else canvasLightRed

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NET ACCUMULATED SAVINGS ROI",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGrey
                                )
                                Text(
                                    text = "${if (netProfit >= 0) "+" else ""}${String.format(Locale.US, "%,.1f", netProfit)} ETB",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = metricColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(metricColor.copy(0.12f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, metricColor.copy(0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${if (netRoi >= 0) "+" else ""}${String.format(Locale.US, "%,.1f", netRoi)}% ROI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = metricColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val trendPoints = remember(settledBets) {
                            val points = mutableListOf<Float>()
                            points.add(0f)
                            var accum = 0f
                            for (bet in settledBets) {
                                val profitPart = if (bet.status == "WON" || bet.status == "CASHOUT") {
                                    (bet.potentialReturn - bet.stake).toFloat()
                                } else {
                                    (-bet.stake).toFloat()
                                }
                                accum += profitPart
                                points.add(accum)
                            }
                            points
                        }

                        val minVal = trendPoints.minOrNull() ?: 0f
                        val maxVal = trendPoints.maxOrNull() ?: 1000f
                        val valueRange = (maxVal - minVal).coerceAtLeast(10f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .background(SlateDarkBG.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                val zeroY = h - ((0f - minVal) / valueRange) * h
                                if (zeroY in 0f..h) {
                                    drawLine(
                                        color = canvasBorderColor.copy(alpha = 0.5f),
                                        start = Offset(0f, zeroY),
                                        end = Offset(w, zeroY),
                                        strokeWidth = 1f.dp.toPx(),
                                        pathEffect = null
                                    )
                                }

                                val points = trendPoints.mapIndexed { idx, value ->
                                    val x = idx * (w / (trendPoints.size - 1).coerceAtLeast(1))
                                    val y = h - ((value - minVal) / valueRange) * h
                                    Offset(x, y)
                                }

                                val linePath = Path().apply {
                                    if (points.isNotEmpty()) {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            val previous = points[i - 1]
                                            val current = points[i]
                                            val controlX1 = previous.x + (current.x - previous.x) / 2f
                                            val controlY1 = previous.y
                                            val controlX2 = previous.x + (current.x - previous.x) / 2f
                                            val controlY2 = current.y
                                            cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
                                        }
                                    }
                                }

                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    if (points.isNotEmpty()) {
                                        lineTo(points.last().x, h)
                                        lineTo(points.first().x, h)
                                        close()
                                    }
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(metricColor.copy(alpha = 0.18f), Color.Transparent),
                                        startY = 0f,
                                        endY = h
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    color = metricColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )

                                if (points.isNotEmpty()) {
                                    drawCircle(
                                        color = metricColor,
                                        radius = 3.dp.toPx(),
                                        center = points.last()
                                    )
                                    drawCircle(
                                        color = metricColor.copy(0.3f),
                                        radius = 6.dp.toPx(),
                                        center = points.last()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "14 Days ago", fontSize = 7.5.sp, color = TextMuted)
                            Text(text = "Evolution timeline", fontSize = 7.5.sp, color = TextGrey, fontWeight = FontWeight.Bold)
                            Text(text = "Current", fontSize = 7.5.sp, color = TextMuted)
                        }
                    }

                    1 -> {
                        val settledCount = settledBets.size
                        val wonCount = settledBets.count { it.status == "WON" || it.status == "CASHOUT" }
                        val lostCount = settledBets.count { it.status == "LOST" }
                        val pendingCount = allBets.count { it.status == "PENDING" }
                        val winRate = if (settledCount > 0) (wonCount.toFloat() / settledCount) * 100f else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizeStroke = 7.dp.toPx()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = (size.width - sizeStroke) / 2f

                                    drawCircle(
                                        color = canvasBorderColor,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = sizeStroke)
                                    )

                                    val winSweep = if (settledCount > 0) (wonCount.toFloat() / settledCount) * 360f else 0f
                                    if (winSweep > 0f) {
                                        drawArc(
                                            color = canvasNeonGreen,
                                            startAngle = -90f,
                                            sweepAngle = winSweep,
                                            useCenter = false,
                                            size = Size(radius * 2, radius * 2),
                                            topLeft = Offset(center.x - radius, center.y - radius),
                                            style = Stroke(width = sizeStroke, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.US, "%.0f%%", winRate),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "WIN RATE",
                                        fontSize = 5.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextMuted
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(5.dp).background(NeonGreen, CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Won Settled tickets", fontSize = 8.sp, color = TextLight)
                                    }
                                    Text(text = "$wonCount slips", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(5.dp).background(LightRed, CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Refunds or Loss slips", fontSize = 8.sp, color = TextLight)
                                    }
                                    Text(text = "$lostCount slips", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(5.dp).background(AmberAccent, CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Unresolved tickets", fontSize = 8.sp, color = TextLight)
                                    }
                                    Text(text = "$pendingCount open", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }

                                Divider(color = BorderColor.copy(0.2f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Volume (Settled slips)", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(text = "${wonCount + lostCount} tickets", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = PrimarySapphire)
                                }
                            }
                        }
                    }

                    2 -> {
                        val sports = listOf("Football", "Basketball", "Tennis", "Esports")
                        val sportsCounts = sports.map { s ->
                            Pair(s, allBets.count { it.sport.equals(s, ignoreCase = true) })
                        }.sortedByDescending { it.second }

                        val totalVol = allBets.size.toFloat().coerceAtLeast(1f)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sportsCounts.forEach { (sport, count) ->
                                val pct = (count / totalVol) * 100f
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val icon = when (sport) {
                                                "Football" -> Icons.Default.SportsSoccer
                                                "Basketball" -> Icons.Default.SportsBasketball
                                                "Tennis" -> Icons.Default.SportsBaseball
                                                "Esports" -> Icons.Default.SportsEsports
                                                else -> Icons.Default.Sports
                                            }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = TextWhite,
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = sport,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                        }
                                        Text(
                                            text = "${count} slips (${String.format(Locale.US, "%.0f%%", pct)})",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimarySapphire
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .background(SlateDarkBG, RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth((pct / 100f).coerceIn(0f, 1f))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(PrimarySapphire, NeonGreen)
                                                    ),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}