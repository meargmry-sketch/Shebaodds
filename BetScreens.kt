package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.Bet
import com.example.data.model.SportMatch
import com.example.data.model.TransactionRecord
import com.example.data.model.UserWallet
import com.example.ui.theme.*
import com.example.viewmodel.BetViewModel
import com.example.viewmodel.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.CornerRadius
import kotlin.math.roundToInt

// ============================================================
// 0. MULTI-LANGUAGE STRINGS
// ============================================================
enum class AppLanguage { EN, AM }
val LocalLanguage = compositionLocalOf { AppLanguage.EN }

object AppStrings {
    fun get(lang: AppLanguage): Map<String, String> = when (lang) {
        AppLanguage.EN -> mapOf(
            "home" to "Home",
            "live" to "Live",
            "bet_slip" to "Bet Slip",
            "my_bets" to "My Bets",
            "casino" to "Casino",
            "profile" to "Profile",
            "total_balance" to "Total Balance",
            "deposit" to "Deposit",
            "welcome_bonus" to "Welcome Bonus",
            "get_100" to "Get 100% up to 10,000 ETB",
            "place_bet" to "Place Bet",
            "potential_return" to "Potential Return",
            "stake" to "Stake",
            "odds" to "Odds",
            "select_payment" to "Select Payment Method",
            "enter_amount" to "Enter Amount (ETB)",
            "all" to "All",
            "football" to "Football",
            "basketball" to "Basketball",
            "tennis" to "Tennis",
            "esports" to "Esports",
            "popular_games" to "Popular Games",
            "live_casino" to "Live Casino",
            "play_now" to "Play Now",
            "settings" to "Settings",
            "support" to "Support",
            "telegram_bot" to "Telegram Bot",
            "logout" to "Logout",
            "sponsors" to "Partners & Sponsors",
            "sponsor_desc" to "Exclusive offers from our trusted partners."
        )
        AppLanguage.AM -> mapOf(
            "home" to "መነሻ",
            "live" to "ቀጥታ",
            "bet_slip" to "የውርርድ ቲኬት",
            "my_bets" to "ውርርዶቼ",
            "casino" to "ካዚኖ",
            "profile" to "መገለጫ",
            "total_balance" to "ጠቅላላ ቀሪ ሂሳብ",
            "deposit" to "ገንዘብ አስገባ",
            "welcome_bonus" to "የእንኳን ደህና መጣችሁ ቦነስ",
            "get_100" to "እስከ 10,000 ETB 100% ያግኙ",
            "place_bet" to "ውርርድ ፈጽም",
            "potential_return" to "ሊያሸንፉ የሚችሉት",
            "stake" to "ውርርድ",
            "odds" to "ዕድሎች",
            "select_payment" to "የክፍያ ዘዴ ይምረጡ",
            "enter_amount" to "መጠን ያስገቡ (ETB)",
            "all" to "ሁሉም",
            "football" to "እግር ኳስ",
            "basketball" to "ቅርጫት ኳስ",
            "tennis" to "ቴኒስ",
            "esports" to "ኢስፖርት",
            "popular_games" to "ታዋቂ ጨዋታዎች",
            "live_casino" to "ቀጥታ ካዚኖ",
            "play_now" to "ተጫወት",
            "settings" to "ቅንብሮች",
            "support" to "ድጋፍ",
            "telegram_bot" to "ቴሌግራም ቦት",
            "logout" to "ውጣ",
            "sponsors" to "አጋሮች እና ስፖንሰሮች",
            "sponsor_desc" to "ከአጋሮቻችን ልዩ ቅናሾች."
        )
    }
}

// ============================================================
// 1. MAIN NAVIGATION & SCREENS
// ============================================================
@Composable
fun MainScreen(viewModel: BetViewModel) {
    val lang = LocalLanguage.current
    val strings = AppStrings.get(lang)
    var selectedNavItem by remember { mutableIntStateOf(0) }
    val wallet by viewModel.wallet.collectAsState()

    Scaffold(
        containerColor = SlateDarkBG,
        bottomBar = {
            BottomAppBar(
                containerColor = SlateCardBG,
                contentColor = TextWhite,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                val navItems = listOf(
                    BottomNavItem(0, Icons.Default.Home, strings["home"]!!),
                    BottomNavItem(1, Icons.Default.SportsSoccer, strings["live"]!!),
                    BottomNavItem(2, Icons.Default.ReceiptLong, strings["bet_slip"]!!),
                    BottomNavItem(3, Icons.Default.SportsBasketball, strings["my_bets"]!!),
                    BottomNavItem(4, Icons.Default.Casino, strings["casino"]!!),
                    BottomNavItem(5, Icons.Default.Person, strings["profile"]!!)
                )
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedNavItem == item.id,
                        onClick = { selectedNavItem = item.id },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberAccent,
                            selectedTextColor = AmberAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = AmberAccent.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedNavItem) {
                0 -> HomeScreen(viewModel, strings)
                1 -> LiveScreen(viewModel, strings)
                2 -> BetSlipScreen(viewModel, strings)
                3 -> MyBetsScreen(viewModel, strings)
                4 -> CasinoScreen(viewModel, strings)
                5 -> ProfileScreen(viewModel, strings)
            }
        }
    }
}

// ============================================================
// 2. HOME SCREEN
// ============================================================
@Composable
fun HomeScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val wallet by viewModel.wallet.collectAsState()
    val matches by viewModel.filteredMatches.collectAsState()
    val trending by viewModel.trendingMatches.collectAsState()
    var showDepositDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(strings["total_balance"]!!, fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    wallet?.let {
                        Text(
                            text = "${String.format("%,.2f", it.balance)} ${it.currency}",
                            fontSize = 24.sp, color = NeonGreen, fontWeight = FontWeight.Black
                        )
                    }
                }
                Button(
                    onClick = { showDepositDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings["deposit"]!!, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Welcome Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.linearGradient(listOf(AmberAccent.copy(alpha = 0.2f), PrimarySapphire.copy(alpha = 0.1f))), RoundedCornerShape(16.dp))
                    .border(1.dp, AmberAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(strings["welcome_bonus"]!!, color = AmberAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(strings["get_100"]!!, color = TextWhite, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                        Text("CLAIM", color = SlateDarkBG, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        // Sports Filter Chips
        item {
            SportsCategoryTabs(viewModel)
        }

        // Featured Matches (Carousel)
        if (trending.isNotEmpty()) {
            item {
                Text("🔥 Featured Matches", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(trending) { match ->
                        FeaturedMatchCard(match, viewModel)
                    }
                }
            }
        }

        // Top Matches (List)
        item {
            Text("⚽ Top Matches", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        items(matches) { match ->
            MatchBettingCard(match, viewModel)
        }

        // Sponsorship Section (Lower part)
        item {
            Spacer(Modifier.height(16.dp))
            SponsorHubSection(strings)
        }
    }

    if (showDepositDialog) {
        DepositFundsDialog(viewModel, onDismissRequest = { showDepositDialog = false })
    }
}

// ============================================================
// 3. LIVE SCREEN
// ============================================================
@Composable
fun LiveScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val matches by viewModel.allMatches.collectAsState()
    val liveMatches = matches.filter { it.isLive }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text("📡 ${strings["live"]!!} ${strings["betting"] ?: "Matches"}", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        if (liveMatches.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(SlateCardBG, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text("No live matches at the moment", color = TextMuted)
                }
            }
        }
        items(liveMatches) { match ->
            MatchBettingCard(match, viewModel)
        }
    }
}

// ============================================================
// 4. BET SLIP SCREEN
// ============================================================
@Composable
fun BetSlipScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val activeItems by viewModel.activeSlipSelectedItems.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val currentBalance = wallet?.balance ?: 0.0
    var stakeInput by remember { mutableStateOf("") }
    var showPlaceDialog by remember { mutableStateOf(false) }

    val totalOdds = activeItems.fold(1.0) { acc, item -> acc * item.odds }
    val potentialWin = (stakeInput.toDoubleOrNull() ?: 0.0) * totalOdds

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings["bet_slip"]!!, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)

        Card(colors = CardDefaults.cardColors(containerColor = SlateCardBG), border = BorderStroke(1.dp, BorderColor), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activeItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No selections added", color = TextMuted)
                    }
                } else {
                    activeItems.forEach { leg ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${leg.teamA} vs ${leg.teamB}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${leg.selection} @ ${String.format("%.2f", leg.odds)}", color = TextMuted, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.removeSelectionFromSlip(leg.matchId) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = LightRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Divider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Odds", color = TextMuted)
                        Text("@ ${String.format("%.2f", totalOdds)}", color = AmberAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stake & Place Bet
        Card(colors = CardDefaults.cardColors(containerColor = SlateCardBG), border = BorderStroke(1.dp, BorderColor), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(strings["stake"]!!, color = TextMuted, fontSize = 12.sp)
                OutlinedTextField(
                    value = stakeInput,
                    onValueChange = { stakeInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = TextGrey) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextLight, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedContainerColor = SlateSurfaceL2, unfocusedContainerColor = SlateSurfaceL2)
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings["potential_return"]!!, color = TextMuted)
                    Text("${String.format("%,.2f", potentialWin)} ETB", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { if (stakeInput.toDoubleOrNull() ?: 0.0 > 0 && currentBalance >= (stakeInput.toDoubleOrNull() ?: 0.0)) showPlaceDialog = true },
                    enabled = activeItems.isNotEmpty() && (stakeInput.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = SlateDarkBG),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings["place_bet"]!!, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }

    if (showPlaceDialog) {
        PlaceBetDialog(viewModel, stakeInput.toDoubleOrNull() ?: 0.0, totalOdds, onDismiss = { showPlaceDialog = false })
    }
}

@Composable
fun PlaceBetDialog(viewModel: BetViewModel, stake: Double, totalOdds: Double, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = SlateCardBG), border = BorderStroke(1.dp, BorderColor), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Confirm Bet", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Stake: ${String.format("%,.2f", stake)} ETB", color = TextLight)
                Text("Odds: @ ${String.format("%.2f", totalOdds)}", color = AmberAccent)
                Text("Potential Return: ${String.format("%,.2f", stake * totalOdds)} ETB", color = NeonGreen)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2)) {
                        Text("Cancel", color = TextWhite)
                    }
                    Button(onClick = {
                        viewModel.placeMultiBet(stake = stake, onSuccess = { onDismiss() }, onError = {})
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
                        Text("Confirm", color = SlateDarkBG, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================
// 5. MY BETS SCREEN
// ============================================================
@Composable
fun MyBetsScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val bets by viewModel.allBets.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(strings["my_bets"]!!, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        items(bets) { bet ->
            BetRecordCard(
                bet = bet,
                onCashout = { viewModel.cashoutBet(bet) },
                onSimulateWin = { viewModel.resolveBet(bet, true) },
                onSimulateLoss = { viewModel.resolveBet(bet, false) },
                onApprove = { viewModel.approveBet(bet) }
            )
        }
    }
}

// ============================================================
// 6. CASINO SCREEN (51+ Games)
// ============================================================
@Composable
fun CasinoScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val games = remember { generateCasinoGames() }
    var selectedGame by remember { mutableStateOf<CasinoGame?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(SlateDarkBG)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🎰 ${strings["casino"]}", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Row {
                IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Casino Banner
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF8B0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings["live_casino"]!!, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                            Text(strings["play_now"]!!, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Popular Games Grid
            item {
                Text(strings["popular_games"]!!, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        CasinoGameCard(game, onClick = { selectedGame = game })
                    }
                }
            }
        }
    }

    if (selectedGame != null) {
        CasinoGameDialog(game = selectedGame!!, viewModel = viewModel, onDismiss = { selectedGame = null })
    }
}

@Composable
fun CasinoGameCard(game: CasinoGame, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(game.bgColors.first().copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.icon, fontSize = 32.sp)
                Spacer(Modifier.height(4.dp))
                Text(game.title, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

// ============================================================
// 7. PROFILE SCREEN (Telegram Bot Integrated)
// ============================================================
@Composable
fun ProfileScreen(viewModel: BetViewModel, strings: Map<String, String>) {
    val context = LocalContext.current
    val wallet by viewModel.wallet.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings["profile"]!!, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)

        Card(colors = CardDefaults.cardColors(containerColor = SlateCardBG), border = BorderStroke(1.dp, BorderColor), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(AmberAccent, CircleShape), contentAlignment = Alignment.Center) {
                    Text("User", color = SlateDarkBG, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(wallet?.username ?: "Guest", color = TextWhite, fontWeight = FontWeight.Bold)
                    Text(wallet?.email ?: "email@example.com", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        // Menu Items
        MenuItem(Icons.Default.Settings, strings["settings"]!!)
        MenuItem(Icons.Default.SupportAgent, strings["support"]!!)
        
        // Telegram Bot Integration
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ShebaOddsBot"))
                context.startActivity(intent)
            },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0088CC).copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, Color(0xFF0088CC)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(strings["telegram_bot"]!!, color = TextWhite, fontWeight = FontWeight.Bold)
                    Text("Powered by Telegram Mini App", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { /* Logout logic */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = LightRed, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings["logout"]!!, color = LightRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextWhite)
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextWhite, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
        }
    }
}

// ============================================================
// 8. MODIFIED PAYMENT SYSTEM (Deposit Dialog)
// ============================================================
@Composable
fun DepositFundsDialog(viewModel: BetViewModel, onDismissRequest: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("TeleBirr") }
    val gateways = listOf("TeleBirr", "CBE Birr", "Bank Transfer", "Stripe")
    val presets = listOf(100, 500, 1000, 5000)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Deposit Funds", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Select Payment Method", color = TextMuted, fontSize = 12.sp)
                
                // Gateway selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gateways.forEach { gw ->
                        val isSelected = selectedGateway == gw
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimarySapphire.copy(alpha = 0.2f) else SlateSurfaceL2)
                                .border(1.dp, if (isSelected) PrimarySapphire else BorderColor, RoundedCornerShape(8.dp))
                                .clickable { selectedGateway = gw }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(gw, color = if (isSelected) TextWhite else TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Text("Enter Amount (ETB)", color = TextMuted, fontSize = 12.sp)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = TextGrey) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextLight, focusedBorderColor = NeonGreen, unfocusedBorderColor = BorderColor, focusedContainerColor = SlateSurfaceL2, unfocusedContainerColor = SlateSurfaceL2)
                )

                // Presets
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                .clickable { amount = p.toString() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+$p", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.depositFunds(amt, selectedGateway) { onDismissRequest() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = SlateDarkBG),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Deposit", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

// ============================================================
// 9. SPONSORSHIP SECTION (Lower part)
// ============================================================
@Composable
fun SponsorHubSection(strings: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(strings["sponsors"]!!, color = AmberAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(strings["sponsor_desc"]!!, color = TextMuted, fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(40.dp).background(SlateSurfaceL2, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    Text("Telebirr", color = TextLight, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).height(40.dp).background(SlateSurfaceL2, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    Text("CBE", color = TextLight, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).height(40.dp).background(SlateSurfaceL2, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    Text("EPL", color = TextLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================
// 10. HELPER COMPOSABLES (Existing logic adapted)
// ============================================================
@Composable
fun FeaturedMatchCard(match: SportMatch, viewModel: BetViewModel) {
    Card(
        modifier = Modifier.width(280.dp).height(160.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${match.teamA} vs ${match.teamB}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OddsButton("1", match.odds1, false) { viewModel.toggleSlipSelection(match, "1", match.odds1) }
                if (match.oddsX > 1.0) OddsButton("X", match.oddsX, false) { viewModel.toggleSlipSelection(match, "X", match.oddsX) }
                OddsButton("2", match.odds2, false) { viewModel.toggleSlipSelection(match, "2", match.odds2) }
            }
        }
    }
}

@Composable
fun MatchBettingCard(match: SportMatch, viewModel: BetViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${match.teamA} vs ${match.teamB}", color = TextWhite, fontWeight = FontWeight.Bold)
                Text(match.timeString, color = TextMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OddsButton("1", match.odds1, false) { viewModel.toggleSlipSelection(match, "1", match.odds1) }
                if (match.oddsX > 1.0) OddsButton("X", match.oddsX, false) { viewModel.toggleSlipSelection(match, "X", match.oddsX) }
                OddsButton("2", match.odds2, false) { viewModel.toggleSlipSelection(match, "2", match.odds2) }
            }
        }
    }
}

@Composable
fun OddsButton(label: String, odds: Double, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AmberAccent.copy(alpha = 0.2f) else SlateSurfaceL2,
            contentColor = if (isSelected) AmberAccent else TextLight
        ),
        border = BorderStroke(1.dp, if (isSelected) AmberAccent else BorderColor),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(String.format("%.2f", odds), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SportsCategoryTabs(viewModel: BetViewModel) {
    val sports = listOf("All", "Football", "Basketball", "Tennis", "Esports")
    val selected by viewModel.selectedSport.collectAsState()

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sports) { sport ->
            val isSelected = if (sport == "All") selected == "All" else selected == sport
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) PrimarySapphire else SlateSurfaceL2)
                    .border(1.dp, if (isSelected) PrimarySapphire else BorderColor, RoundedCornerShape(100.dp))
                    .clickable { viewModel.selectSport(if (sport == "All") "All" else sport) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(sport, color = if (isSelected) TextWhite else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ============================================================
// 11. DATA MODELS (Casino Games)
// ============================================================
data class CasinoGame(
    val id: String,
    val title: String,
    val icon: String,
    val bgColors: List<Color>
)

fun generateCasinoGames(): List<CasinoGame> {
    val games = mutableListOf<CasinoGame>()
    val icons = listOf("🎰", "🎡", "🃏", "🎲", "✈️", "💣", "🐔", "🏀", "⚽", "🥊")
    for (i in 1..51) {
        games.add(CasinoGame(
            id = "game_$i",
            title = "Game ${if (i < 10) "0$i" else i}",
            icon = icons[i % icons.size],
            bgColors = listOf(Color(0xFF6B21A8), Color(0xFF4C1D95))
        ))
    }
    return games
}

// ============================================================
// 12. CASINO GAME DIALOG
// ============================================================
@Composable
fun CasinoGameDialog(game: CasinoGame, viewModel: BetViewModel, onDismiss: () -> Unit) {
    var stake by remember { mutableStateOf("10") }
    val wallet by viewModel.wallet.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.icon, fontSize = 48.sp)
                Text(game.title, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = stake,
                    onValueChange = { stake = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Stake (ETB)", color = TextGrey) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextLight, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val amt = stake.toDoubleOrNull() ?: 10.0
                        viewModel.placeCasinoBet(game.id, game.title, amt, onSuccess = { onDismiss() }, onError = {})
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = SlateDarkBG)
                ) {
                    Text("Play", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ============================================================
// 13. BOTTOM NAV DATA CLASS
// ============================================================
data class BottomNavItem(val id: Int, val icon: ImageVector, val label: String)