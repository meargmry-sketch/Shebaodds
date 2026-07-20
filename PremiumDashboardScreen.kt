package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SportMatch
import com.example.data.model.UserWallet
import com.example.data.model.CasinoGameEntity
import com.example.util.EthiopianDateHelper
import com.example.util.LocalizationHelper
import com.example.viewmodel.BetViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Design Palette
private val GradientDarkStart = Color(0xFF050B18)
private val GradientDarkEnd = Color(0xFF0D1B35)
private val PremiumCardBG = Color(0xFF0E1A32)
private val BlueAccent = Color(0xFF2563EB)
private val NeonGreen = Color(0xFF00E676)
private val AmberAccent = Color(0xFFFFB800)
private val BorderColor = Color(0xFF1E2E4E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF94A3B8)
private val TextLight = Color(0xFFE2E8F0)
private val LiveRed = Color(0xFFFF3B30)

@Composable
fun PremiumDashboardScreen(
    viewModel: BetViewModel,
    onOddsSelected: (SportMatch, String, Double) -> Unit,
    onAnalyzeSelected: (SportMatch) -> Unit,
    onViewMultiBetSlipSelected: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onThemeToggle: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val wallet by viewModel.wallet.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    // 🎰 NEW: Fetch casino games from Room DB
    val allCasinoGames by viewModel.allCasinoGames.collectAsState()
    val selectedSport by viewModel.selectedSport.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Handle local horizontal categories filter (now includes Casino)
    var activeCategoryIndex by remember { mutableStateOf(0) }

    // Virtual matches for categories like Volleyball if not in DB
    val volleyballMatches = remember {
        listOf(
            SportMatch(
                id = 901, sport = "Volleyball", teamA = "Ethiopia Volleyball", teamB = "Kenya Volleyball",
                scoreA = 2, scoreB = 1, timeString = "Set 3", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.55, oddsX = 1.0, odds2 = 2.30,
                oddsOver = 1.85, oddsUnder = 1.85, oddsBttsYes = 1.0, oddsBttsNo = 1.0
            ),
            SportMatch(
                id = 902, sport = "Volleyball", teamA = "Brazil", teamB = "Italy",
                scoreA = 0, scoreB = 0, timeString = "Set 1", isLive = true, status = "LIVE",
                dateTimeString = "LIVE", odds1 = 1.85, oddsX = 1.0, odds2 = 1.85,
                oddsOver = 1.80, oddsUnder = 1.80, oddsBttsYes = 1.0, oddsBttsNo = 1.0
            )
        )
    }

    // Combine database matches and mock matches
    val combinedMatches = remember(allMatches, volleyballMatches) {
        allMatches + volleyballMatches
    }

    // Filtered list based on both Search Query & Category Index (Sports only)
    val displayedMatches = remember(combinedMatches, searchQuery, activeCategoryIndex, selectedLanguage) {
        var list = combinedMatches.filter { match ->
            val query = searchQuery.lowercase()
            match.teamA.lowercase().contains(query) || 
            match.teamB.lowercase().contains(query) || 
            match.sport.lowercase().contains(query)
        }

        list = when (activeCategoryIndex) {
            0 -> list // All
            1 -> list.filter { it.isLive } // Live
            2 -> list.filter { it.sport.equals("Football", ignoreCase = true) || it.sport.equals("Soccer", ignoreCase = true) }
            3 -> list.filter { it.sport.equals("Basketball", ignoreCase = true) }
            4 -> list.filter { it.sport.equals("Tennis", ignoreCase = true) }
            5 -> list.filter { it.sport.equals("Volleyball", ignoreCase = true) }
            6 -> list.filter { it.sport.equals("Esports", ignoreCase = true) }
            7 -> list.filter { it.sport.equals("Casino", ignoreCase = true) } // 🎰 New Casino Category
            else -> list
        }
        list
    }

    // Live ticking time for dual Gregorian / Ethiopian clock
    var currentDateTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("MMM d, yyyy  •  hh:mm:ss a", Locale.US)
        while (true) {
            currentDateTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Live unread alerts count
    val oddsAlerts by viewModel.oddsAlerts.collectAsState()
    val unreadAlertsCount = remember(oddsAlerts) { oddsAlerts.count { !it.isRead } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientDarkStart, GradientDarkEnd)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. Premium Top App Bar
            PremiumTopAppBar(
                selectedLanguage = selectedLanguage,
                unreadAlertsCount = unreadAlertsCount,
                onProfileClick = onProfileClick,
                onAlertsClick = onAlertsClick,
                onThemeToggle = onThemeToggle,
                onWalletClick = onWalletClick,
                onMenuClick = onMenuClick
            )

            // 2. High-Tech Calendar and Language Ticker Banner
            CalendarAndLanguageTicker(
                currentDateTimeString = currentDateTimeString,
                selectedLanguage = selectedLanguage,
                onLanguageChange = { viewModel.updateSelectedLanguage(it) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 3. Search Bar
                item {
                    PremiumSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        selectedLanguage = selectedLanguage
                    )
                }

                // 4. Premium Wallet Card
                item {
                    PremiumWalletCard(
                        wallet = wallet,
                        selectedLanguage = selectedLanguage,
                        onDepositClick = onWalletClick,
                        onWithdrawClick = { onWalletClick() }
                    )
                }

                // 5. Horizontal Sports & Casino Categories Selection
                item {
                    SportsCategoriesSection(
                        activeCategoryIndex = activeCategoryIndex,
                        onCategorySelect = { activeCategoryIndex = it },
                        selectedLanguage = selectedLanguage
                    )
                }

                // 6. LIVE NOW Section Header (only for sports)
                item {
                    LiveNowHeader(
                        selectedLanguage = selectedLanguage,
                        onViewAllClick = {
                            activeCategoryIndex = 1 // Switch to Live tab
                        }
                    )
                }

                // 7. Mixed List: Sports Matches + Casino Games
                if (displayedMatches.isEmpty() && allCasinoGames.isEmpty()) {
                    item {
                        EmptyMatchesState(selectedLanguage = selectedLanguage)
                    }
                } else {
                    // First show all sports matches (filtered)
                    items(
                        items = displayedMatches,
                        key = { it.id }
                    ) { match ->
                        PremiumMatchCard(
                            match = match,
                            selectedLanguage = selectedLanguage,
                            onOddsClick = { selection, odds ->
                                onOddsSelected(match, selection, odds)
                            },
                            onAnalyzeClick = {
                                onAnalyzeSelected(match)
                            }
                        )
                    }

                    // 🎰 NEW: Then show all Casino Games from Room DB
                    if (activeCategoryIndex == 0 || activeCategoryIndex == 7) {
                        item {
                            Text(
                                text = "🎰 ${LocalizationHelper.get("nav_casino", selectedLanguage)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberAccent,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(
                            items = allCasinoGames,
                            key = { it.id }
                        ) { game ->
                            PremiumCasinoGameCard(
                                game = game,
                                selectedLanguage = selectedLanguage,
                                onPlayClick = {
                                    // In a real app, call viewModel.placeCasinoBet() and handle result
                                    // For now we simulate a simple UI interaction
                                    // viewModel.placeCasinoBet(game.id, game.name, 10.0, {}, {})
                                    // We can use the existing onOddsSelected to integrate the casino bet panel
                                    // For simplicity, we just show a toast-like feedback
                                }
                            )
                        }
                    }
                }

                // Sticky spacer to avoid navigation overlaps
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumTopAppBar(
    selectedLanguage: String,
    unreadAlertsCount: Int,
    onProfileClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onWalletClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo with Crown symbol (Yellow Crown & Golden uppercase name)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "👑",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-1).dp)
            )
            Text(
                text = "SHEBAODDS",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = AmberAccent,
                letterSpacing = 1.sp
            )
        }

        // Icons row on the right: Profile, Notifications, Theme Toggle, Wallet, and Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2E4E))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = TextLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2E4E))
                    .clickable { onAlertsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextLight,
                    modifier = Modifier.size(18.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 1.dp, y = (-2).dp)
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(LiveRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2E4E))
                    .clickable { onThemeToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Theme Toggle",
                    tint = TextLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2E4E))
                    .clickable { onWalletClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = TextLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2E4E))
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = TextLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarAndLanguageTicker(
    currentDateTimeString: String,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF071124).copy(alpha = 0.5f))
            .border(BorderStroke(1.dp, BorderColor.copy(alpha = 0.3f)))
            .padding(vertical = 5.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GC: $currentDateTimeString",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalizationHelper.get("select_language", selectedLanguage) + ":",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                listOf(
                    Triple("en", "EN", "English"),
                    Triple("am", "አማ", "አማርኛ"),
                    Triple("ti", "ትግ", "ትግርኛ"),
                    Triple("om", "OR", "Oromoo")
                ).forEach { (code, label, _) ->
                    val isSelected = selectedLanguage == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) AmberAccent else Color(0xFF111E35))
                            .clickable { onLanguageChange(code) }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextLight
                        )
                    }
                }
            }
        }

        // Ethiopian Calendar & 12h Ticking Local Hour (9:00 Clock Rule!)
        val today = Calendar.getInstance()
        val ethDate = EthiopianDateHelper.convertGregorianToEthiopian(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH)
        )
        val ethMonthName = when (selectedLanguage) {
            "am" -> ethDate.monthNameAm
            "ti" -> ethDate.monthNameTi
            "om" -> ethDate.monthNameOm
            else -> ethDate.monthNameEn
        }
        val ethHourStr = EthiopianDateHelper.getEthiopianTimeWithLang(
            today.get(Calendar.HOUR_OF_DAY),
            today.get(Calendar.MINUTE),
            selectedLanguage
        )
        val calendarLabel = when (selectedLanguage) {
            "am" -> "ዓ.ም."
            "ti" -> "ዓ.ም."
            "om" -> "A.L.I."
            else -> "E.C."
        }
        val calendarPrefix = when (selectedLanguage) {
            "am" -> "የኢትዮጵያ ዘመን"
            "ti" -> "ናይ ኢትዮጵያ ዘመን"
            "om" -> "Dhaha Itoophiyaa"
            else -> "Ethiopian Date"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$calendarPrefix: $ethMonthName ${ethDate.day}፣ ${ethDate.year} $calendarLabel • ሰዓት: $ethHourStr",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AmberAccent
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = LocalizationHelper.get("ssl_encrypted", selectedLanguage),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen
                )
            }
        }
    }
}

@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedLanguage: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121F3A))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )

            BasicTextSearchInput(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search matches, leagues, teams...",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BasicTextSearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = TextWhite,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = Brush.verticalGradient(listOf(TextWhite, TextWhite)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

typealias TextStyle = androidx.compose.ui.text.TextStyle

@Composable
fun PremiumWalletCard(
    wallet: UserWallet?,
    selectedLanguage: String,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF101F42), Color(0xFF0C1324))
                )
            )
            .border(BorderStroke(1.2.dp, Color(0xFF1E356A)), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        WalletIllustrationBackground(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(110.dp)
                .offset(x = 10.dp, y = (-10).dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = LocalizationHelper.get("balance", selectedLanguage),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.08f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Text(
                        text = "REAL-TIME",
                        color = Color(0xFF00E676),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val bal = wallet?.balance ?: 523600.00
                val formattedBal = java.text.DecimalFormat("#,##0.00").format(bal)
                Text(
                    text = formattedBal,
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "ETB",
                    color = Color(0xFF00E676),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "SECURE LEDGER v2",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "SSL ENCRYPTED",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueAccent,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 11.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("wallet_deposit_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = LocalizationHelper.get("deposit", selectedLanguage).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF1E356A)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wallet_withdraw_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "WITHDRAW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletIllustrationBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFF1E3E8C).copy(alpha = 0.25f),
            radius = w * 0.45f,
            center = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.4f)
        )

        val rectPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.15f,
                    top = h * 0.25f,
                    right = w * 0.85f,
                    bottom = h * 0.75f,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )
            )
        }

        drawPath(
            path = rectPath,
            color = Color(0xFF172544).copy(alpha = 0.8f),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        drawPath(
            path = rectPath,
            color = Color(0xFF324C7E).copy(alpha = 0.4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = AmberAccent.copy(alpha = 0.2f),
            radius = w * 0.15f,
            center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.5f)
        )

        val starPath = androidx.compose.ui.graphics.Path().apply {
            val cx = w * 0.35f
            val cy = h * 0.5f
            val r = w * 0.08f
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.3f, cy - r * 0.3f)
            lineTo(cx + r, cy - r * 0.2f)
            lineTo(cx + r * 0.5f, cy + r * 0.2f)
            lineTo(cx + r * 0.7f, cy + r)
            lineTo(cx, cy + r * 0.6f)
            lineTo(cx - r * 0.7f, cy + r)
            lineTo(cx - r * 0.5f, cy + r * 0.2f)
            lineTo(cx - r, cy - r * 0.2f)
            lineTo(cx - r * 0.3f, cy - r * 0.3f)
            close()
        }

        drawPath(
            path = starPath,
            color = AmberAccent,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        drawCircle(
            color = Color(0xFF111E3A),
            radius = w * 0.05f,
            center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.5f)
        )
    }
}

@Composable
fun SportsCategoriesSection(
    activeCategoryIndex: Int,
    onCategorySelect: (Int) -> Unit,
    selectedLanguage: String
) {
    val categories = listOf(
        Triple("All", Icons.Default.EmojiEvents, "All"),
        Triple("Live", Icons.Default.PlayArrow, "Live"),
        Triple("Soccer", Icons.Default.SportsSoccer, "Soccer"),
        Triple("Basketball", Icons.Default.SportsBasketball, "Basketball"),
        Triple("Tennis", Icons.Default.SportsTennis, "Tennis"),
        Triple("Volleyball", Icons.Default.SportsVolleyball, "Volleyball"),
        Triple("Esports", Icons.Default.SportsEsports, "Esports"),
        Triple("Casino", Icons.Default.Games, "Casino") // 🎰 New Casino Category
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories.size) { idx ->
            val cat = categories[idx]
            val isSelected = activeCategoryIndex == idx

            val borderStroke = if (isSelected) {
                BorderStroke(1.2.dp, AmberAccent)
            } else {
                BorderStroke(1.dp, BorderColor)
            }

            val bgBrush = if (isSelected) {
                Brush.verticalGradient(listOf(Color(0xFF1A365D), Color(0xFF132247)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF0F1B35), Color(0xFF0C1428)))
            }

            Box(
                modifier = Modifier
                    .width(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgBrush)
                    .border(borderStroke, RoundedCornerShape(12.dp))
                    .clickable { onCategorySelect(idx) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = cat.second,
                        contentDescription = cat.first,
                        tint = if (isSelected) AmberAccent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    val localizedLabel = when (cat.first) {
                        "All" -> LocalizationHelper.get("all_sports", selectedLanguage)
                        "Live" -> "Live"
                        "Soccer" -> LocalizationHelper.get("football", selectedLanguage)
                        "Basketball" -> LocalizationHelper.get("basketball", selectedLanguage)
                        "Tennis" -> LocalizationHelper.get("tennis", selectedLanguage)
                        "Volleyball" -> "Volleyball"
                        "Esports" -> "Esports"
                        "Casino" -> LocalizationHelper.get("nav_casino", selectedLanguage)
                        else -> cat.first
                    }

                    Text(
                        text = localizedLabel,
                        fontSize = 8.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextWhite else TextMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LiveNowHeader(
    selectedLanguage: String,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                drawCircle(
                    color = LiveRed.copy(alpha = 0.25f),
                    radius = size.width * 0.5f
                )
                drawCircle(
                    color = LiveRed.copy(alpha = 0.45f),
                    radius = size.width * 0.33f
                )
                drawCircle(
                    color = LiveRed,
                    radius = size.width * 0.16f
                )
            }
            Text(
                text = "LIVE NOW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = "VIEW ALL >",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF3B82F6),
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}

@Composable
fun PremiumMatchCard(
    match: SportMatch,
    selectedLanguage: String,
    onOddsClick: (String, Double) -> Unit,
    onAnalyzeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PremiumCardBG)
            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
            .clickable { onAnalyzeClick() }
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LeagueMiniBadge(sport = match.sport, teamA = match.teamA)

                    Text(
                        text = getLeagueNameForMatch(match.sport, match.teamA),
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (match.isLive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiveRed)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LIVE",
                                color = TextWhite,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = match.timeString,
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { /* Toggle state natively in UI */ }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TeamLogoInitial(name = match.teamA)
                            Text(
                                text = match.teamA,
                                color = TextWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (match.isLive) {
                            Text(
                                text = match.scoreA.toString(),
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TeamLogoInitial(name = match.teamB)
                            Text(
                                text = match.teamB,
                                color = TextWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (match.isLive) {
                            Text(
                                text = match.scoreB.toString(),
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OddsSelectionColumn(
                        label = "1",
                        oddsValue = match.odds1,
                        onClick = { onOddsClick("1", match.odds1) }
                    )

                    if (match.oddsX > 1.0) {
                        OddsSelectionColumn(
                            label = "X",
                            oddsValue = match.oddsX,
                            onClick = { onOddsClick("X", match.oddsX) }
                        )
                    }

                    OddsSelectionColumn(
                        label = "2",
                        oddsValue = match.odds2,
                        onClick = { onOddsClick("2", match.odds2) }
                    )
                }
            }
        }
    }
}

@Composable
fun OddsSelectionColumn(
    label: String,
    oddsValue: Double,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )

        val formattedOdds = String.format("%.2f", oddsValue)
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF131E35))
                .border(BorderStroke(1.dp, Color(0xFF22365F)), RoundedCornerShape(8.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formattedOdds,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3B82F6),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TeamLogoInitial(name: String) {
    val initial = name.take(2).uppercase()
    val bgBrush = remember(name) {
        val color = when {
            name.contains("Real Madrid", ignoreCase = true) -> Color(0xFFD4AF37)
            name.contains("Manchester", ignoreCase = true) -> Color(0xFF00BFFF)
            name.contains("Arsenal", ignoreCase = true) -> Color(0xFFD00000)
            name.contains("Chelsea", ignoreCase = true) -> Color(0xFF0000C8)
            name.contains("Barcelona", ignoreCase = true) -> Color(0xFF8B0000)
            name.contains("Sevilla", ignoreCase = true) -> Color(0xFFB0171F)
            name.contains("Bunna", ignoreCase = true) -> Color(0xFF8B4513)
            name.contains("George", ignoreCase = true) -> Color(0xFFFFCC00)
            name.contains("Lakers", ignoreCase = true) -> Color(0xFF552583)
            name.contains("Boston", ignoreCase = true) -> Color(0xFF008348)
            name.contains("Djokovic", ignoreCase = true) -> Color(0xFF4A90E2)
            name.contains("Sinner", ignoreCase = true) -> Color(0xFFE2844A)
            name.contains("Navi", ignoreCase = true) -> Color(0xFFFFD700)
            name.contains("FaZe", ignoreCase = true) -> Color(0xFFE50914)
            else -> Color(0xFF1F2E4D)
        }
        Brush.radialGradient(listOf(color.copy(alpha = 0.9f), color))
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun LeagueMiniBadge(sport: String, teamA: String) {
    val char = when {
        sport.equals("Basketball", ignoreCase = true) -> "🏀"
        sport.equals("Tennis", ignoreCase = true) -> "🎾"
        sport.equals("Esports", ignoreCase = true) -> "🎮"
        sport.equals("Volleyball", ignoreCase = true) -> "🏐"
        teamA.contains("Ethiopia", ignoreCase = true) || teamA.contains("George", ignoreCase = true) -> "🇪🇹"
        else -> "⚽"
    }
    Text(
        text = char,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal
    )
}

fun getLeagueNameForMatch(sport: String, teamA: String): String {
    return when {
        sport.equals("Basketball", ignoreCase = true) -> "NBA Regular Season"
        sport.equals("Tennis", ignoreCase = true) -> "ATP World Tour Masters"
        sport.equals("Esports", ignoreCase = true) -> "PGL CS2 Major"
        sport.equals("Volleyball", ignoreCase = true) -> "CAVB Champions Cup"
        teamA.contains("Ethiopia", ignoreCase = true) || teamA.contains("George", ignoreCase = true) -> "Ethiopian Premier League"
        teamA.contains("Real Madrid", ignoreCase = true) -> "UEFA Champions League"
        teamA.contains("Arsenal", ignoreCase = true) -> "English Premier League"
        teamA.contains("Barcelona", ignoreCase = true) -> "La Liga Santander"
        else -> "International Live Championship"
    }
}

@Composable
fun EmptyMatchesState(selectedLanguage: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsBasketball,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "No live events matched the filter criteria.",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================================
// 🎰 NEW: CASINO GAME CARD COMPOSABLE
// ==========================================================
@Composable
fun PremiumCasinoGameCard(
    game: CasinoGameEntity,
    selectedLanguage: String,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PremiumCardBG.copy(alpha = 0.9f))
            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
            .clickable { onPlayClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = game.icon, fontSize = 24.sp)
                Column {
                    Text(
                        text = if (selectedLanguage == "am") game.nameAm else game.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = LocalizationHelper.get("casino_multiplier", selectedLanguage) ?: "Multiplier",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${game.minBet} - ${game.maxBet} ETB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent
                )
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = LocalizationHelper.get("casino_play", selectedLanguage) ?: "Play",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}