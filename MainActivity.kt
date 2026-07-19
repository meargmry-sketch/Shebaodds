package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.model.SportMatch
import com.example.data.model.UserWallet
import com.example.data.model.Bet
import com.example.data.repository.BetRepository
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.BetViewModel
import com.example.viewmodel.BetViewModelFactory
import com.example.viewmodel.OddsAlert
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.animation.core.tween
import java.text.SimpleDateFormat
import java.util.*

private val BlueAccent = Color(0xFF2563EB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start Local WebSocket Server
        com.example.util.LocalWebSocketServer.start()

        // 1. Initialize DB and Repository instance
        val db = AppDatabase.getDatabase(this)
        val repository = BetRepository(db)
        
        // 2. Instantiate view model with standard programmatic factory
        val viewModel = ViewModelProvider(
            this,
            BetViewModelFactory(application, repository)
        )[BetViewModel::class.java]

        setContent {
            val sharedPrefs = remember { getSharedPreferences("shebaodds_prefs", MODE_PRIVATE) }
            var isDarkTheme by remember { mutableStateOf(sharedPrefs.getBoolean("dark_theme", true)) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppMainContent(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        sharedPrefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.util.LocalWebSocketServer.stop()
    }
}

@Composable
fun PersistentHeader(
    wallet: UserWallet?,
    allBets: List<Bet>,
    isTablet: Boolean,
    isDarkTheme: Boolean,
    currentDateTimeString: String,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    onThemeToggle: () -> Unit,
    onBetHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDepositClick: () -> Unit,
    unreadAlertsCount: Int = 0,
    onAlertsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateCardBG)
            .border(0.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Brand Logo title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_shebaodds_logo_1781802352094),
                    contentDescription = "ShebaOdds Logo",
                    modifier = Modifier
                        .size(if (isTablet) 30.dp else 26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, AmberAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Column {
                    Text(
                        text = if (isTablet) "SHEBAODDS SPORTSBOOK" else "SHEBAODDS",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "SECURE LEDGER HUB",
                        fontSize = if (isTablet) 8.sp else 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Right header controls (Theme toggle + Wallet Ledger + Active Bet History Link)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 4.dp)
            ) {
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SlateSurfaceL2, CircleShape)
                        .testTag("header_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out",
                        tint = LightRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SlateSurfaceL2, CircleShape)
                        .testTag("profile_hub_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Hub",
                        tint = PrimarySapphire,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onThemeToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SlateSurfaceL2, CircleShape)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDarkTheme) AmberAccent else PrimarySapphire,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bell alert notifications button with unread count badge
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(
                        onClick = onAlertsClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SlateSurfaceL2, CircleShape)
                            .testTag("odds_alerts_hub_btn")
                    ) {
                        Icon(
                            imageVector = if (unreadAlertsCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Odds Alerts Hub ($unreadAlertsCount unread)",
                            tint = if (unreadAlertsCount > 0) AmberAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (unreadAlertsCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(top = 1.dp, end = 1.dp)
                                .size(14.dp)
                                .background(LightRed, CircleShape)
                                .border(1.dp, SlateCardBG, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadAlertsCount > 9) "9+" else unreadAlertsCount.toString(),
                                fontSize = 8.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Balance display container with on-fly Deposit Trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurfaceL2)
                        .border(1.dp, BorderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = if (isTablet) 10.dp else 8.dp, vertical = if (isTablet) 6.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Ledger",
                        tint = NeonGreen,
                        modifier = Modifier.size(if (isTablet) 15.dp else 12.dp)
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = com.example.util.LocalizationHelper.get("balance", selectedLanguage),
                            fontSize = if (isTablet) 8.sp else 7.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${String.format("%,.2f", wallet?.balance ?: 2500.00)} ETB",
                            fontSize = if (isTablet) 11.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(if (isTablet) 4.dp else 2.dp))
                    
                    // Deposit action button triggering Telebirr dialog flow
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberAccent)
                            .clickable { onDepositClick() }
                            .padding(horizontal = if (isTablet) 8.dp else 6.dp, vertical = if (isTablet) 5.dp else 3.dp)
                            .testTag("header_deposit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = com.example.util.LocalizationHelper.get("deposit", selectedLanguage),
                            fontSize = if (isTablet) 10.sp else 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                // Clickable Pill Badge linking to Active Bet History
                val activeBetsCount = allBets.filter { it.status == "PENDING" }.size
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimarySapphire.copy(alpha = 0.15f))
                        .border(1.dp, PrimarySapphire.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onBetHistoryClick)
                        .padding(horizontal = if (isTablet) 10.dp else 8.dp, vertical = if (isTablet) 6.dp else 4.dp)
                        .testTag("header_active_bets_link"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 6.dp else 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Active Bet History",
                        tint = PrimarySapphire,
                        modifier = Modifier.size(if (isTablet) 14.dp else 12.dp)
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = com.example.util.LocalizationHelper.get("active_bets", selectedLanguage),
                            fontSize = if (isTablet) 8.sp else 7.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$activeBetsCount Slip${if (activeBetsCount != 1) "s" else ""} \u2192",
                            fontSize = if (isTablet) 11.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimarySapphire
                        )
                    }
                }
            }
        }

        // Digital Clock ticking sub-tray with Local Language Toggles + Dual Ethiopian/Gregorian Calandar Ticking
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurfaceL2)
                .padding(vertical = 6.dp, horizontal = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Gregorian Time
                    Text(
                        text = "GC: $currentDateTimeString",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    // Right: Language Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.LocalizationHelper.get("select_language", selectedLanguage) + ":",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        listOf(
                            Triple("en", "EN", "English"),
                            Triple("am", "አማ", "አማርኛ"),
                            Triple("ti", "ትግ", "ትግርኛ"),
                            Triple("om", "OR", "Oromoo")
                        ).forEach { (code, label, fullName) ->
                            val isSelected = selectedLanguage == code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) AmberAccent else SlateCardBG)
                                    .border(1.dp, if (isSelected) AmberAccent else BorderColor, RoundedCornerShape(4.dp))
                                    .clickable { onLanguageChange(code) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextLight
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Ethiopian Date & Clock Time (9:00 clock style!)
                    val today = java.util.Calendar.getInstance()
                    val ethDate = com.example.util.EthiopianDateHelper.convertGregorianToEthiopian(
                        today.get(java.util.Calendar.YEAR),
                        today.get(java.util.Calendar.MONTH) + 1,
                        today.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    val ethMonthName = when (selectedLanguage) {
                        "am" -> ethDate.monthNameAm
                        "ti" -> ethDate.monthNameTi
                        "om" -> ethDate.monthNameOm
                        else -> ethDate.monthNameEn
                    }
                    val ethHourStr = com.example.util.EthiopianDateHelper.getEthiopianTimeWithLang(
                        today.get(java.util.Calendar.HOUR_OF_DAY),
                        today.get(java.util.Calendar.MINUTE),
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
                    
                    Text(
                        text = "$calendarPrefix: $ethMonthName ${ethDate.day}፣ ${ethDate.year} $calendarLabel • ሰዓት: $ethHourStr",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )

                    // Right: SSL encrypted indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "",
                            tint = NeonGreen,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = com.example.util.LocalizationHelper.get("ssl_encrypted", selectedLanguage),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppMainContent(
    viewModel: BetViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("shebaodds_prefs", android.content.Context.MODE_PRIVATE) }
    var isLoggedIn by remember { mutableStateOf(sharedPrefs.getBoolean("logged_in", false)) }

    if (!isLoggedIn) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                isLoggedIn = true
                sharedPrefs.edit().putBoolean("logged_in", true).apply()
            }
        )
    } else {
        var selectedMainTab by remember { mutableStateOf(0) } // 0: Sportsbook, 1: Slips, 2: Finances
        
        // Live ticking date and time (matching screenshot header fidelity!)
        var currentDateTimeString by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            val sdf = SimpleDateFormat("MMM d, yyyy  •  hh:mm:ss a", Locale.getDefault())
            while (true) {
                currentDateTimeString = sdf.format(Date())
                delay(1000)
            }
        }

        val wallet by viewModel.wallet.collectAsState()
        val allBets by viewModel.allBets.collectAsState()
        val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Slip dialog states
    var activeBetMatch by remember { mutableStateOf<SportMatch?>(null) }
    var activeBetSelection by remember { mutableStateOf("") }
    var activeBetOdds by remember { mutableStateOf(0.0) }
    var showMultiBetSlip by remember { mutableStateOf(false) }
    
    // AI Dialog state
    var activeAnalyzeMatch by remember { mutableStateOf<SportMatch?>(null) }
    
    // Profile & Deposit Access states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showProfileAuthDialog by remember { mutableStateOf(false) }
    var showDepositFundsDialog by remember { mutableStateOf(false) }
    var showOddsAlertsDialog by remember { mutableStateOf(false) }
    
    // SnackBar host
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val isTablet = !isPortrait && maxWidth >= 600.dp && maxHeight >= 480.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF050B18), Color(0xFF0D1B35))
                    )
                )
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (selectedMainTab != 0) {
                        val oddsAlerts by viewModel.oddsAlerts.collectAsState()
                        val unreadCount = oddsAlerts.count { !it.isRead }
                        PersistentHeader(
                            wallet = wallet,
                            allBets = allBets,
                            isTablet = isTablet,
                            isDarkTheme = isDarkTheme,
                            currentDateTimeString = currentDateTimeString,
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = { viewModel.updateSelectedLanguage(it) },
                            onThemeToggle = onThemeToggle,
                            onBetHistoryClick = { selectedMainTab = 1 },
                            onProfileClick = { showProfileAuthDialog = true },
                            onLogoutClick = {
                                isLoggedIn = false
                                sharedPrefs.edit().putBoolean("logged_in", false).apply()
                            },
                            onDepositClick = { showDepositFundsDialog = true },
                            unreadAlertsCount = unreadCount,
                            onAlertsClick = { showOddsAlertsDialog = true }
                        )
                    }
                },
            bottomBar = {
                if (!isTablet) {
                    NavigationBar(
                        containerColor = Color(0xFF0C1324),
                        modifier = Modifier
                            .border(1.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .navigationBarsPadding(),
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedMainTab == 0,
                            onClick = { selectedMainTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 0) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                                    contentDescription = "Sportsbook"
                                )
                            },
                            label = { Text(com.example.util.LocalizationHelper.get("sportsbook", selectedLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("nav_sportsbook")
                        )
                        
                        NavigationBarItem(
                            selected = selectedMainTab == 1,
                            onClick = { selectedMainTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 1) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                    contentDescription = "My Bets"
                                )
                            },
                            label = {
                                val lbl = when(selectedLanguage) {
                                    "am" -> "የእኔ ቲኬቶች"
                                    "ti" -> "ናተይ ቲኬታት"
                                    "om" -> "Kaardii Koo"
                                    else -> "My Bets"
                                }
                                Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("nav_my_slips")
                        )

                        NavigationBarItem(
                            selected = selectedMainTab == 2,
                            onClick = { selectedMainTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 2) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Wallet"
                                )
                            },
                            label = {
                                val lbl = when(selectedLanguage) {
                                    "am" -> "ቀሪ ሂሳብ"
                                    "ti" -> "ተረፍ ሕሳብ"
                                    "om" -> "Hanga Maallaqaa"
                                    else -> "Wallet"
                                }
                                Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("nav_secure_bank")
                        )

                        NavigationBarItem(
                            selected = selectedMainTab == 3,
                            onClick = { selectedMainTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 3) Icons.Default.Redeem else Icons.Outlined.Redeem,
                                    contentDescription = "Promotions"
                                )
                            },
                            label = {
                                val lbl = when(selectedLanguage) {
                                    "am" -> "ማስተዋወቂያ"
                                    "ti" -> "ማስተዋወቂያ"
                                    "om" -> "Malleen"
                                    else -> "Promotions"
                                }
                                Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("nav_admin_panel")
                        )

                        NavigationBarItem(
                            selected = selectedMainTab == 4,
                            onClick = { selectedMainTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 4) Icons.Default.SupportAgent else Icons.Outlined.SupportAgent,
                                    contentDescription = "Support"
                                )
                            },
                            label = { Text(com.example.util.LocalizationHelper.get("support", selectedLanguage), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("nav_support_chat")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavigationRail(
                        containerColor = Color(0xFF0C1324),
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(1.dp, BorderColor, RoundedCornerShape(0.dp))
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedMainTab == 0,
                            onClick = { selectedMainTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 0) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                                    contentDescription = "Sportsbook"
                                )
                            },
                            label = { Text("Sports", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("rail_nav_sportsbook")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedMainTab == 1,
                            onClick = { selectedMainTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 1) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                    contentDescription = "My Bets"
                                )
                            },
                            label = { Text("My Bets", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("rail_nav_my_slips")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedMainTab == 2,
                            onClick = { selectedMainTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 2) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Wallet"
                                )
                            },
                            label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("rail_nav_secure_bank")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedMainTab == 3,
                            onClick = { selectedMainTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 3) Icons.Default.Redeem else Icons.Outlined.Redeem,
                                    contentDescription = "Promotions"
                                )
                            },
                            label = { Text("Promos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("rail_nav_admin_panel")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedMainTab == 4,
                            onClick = { selectedMainTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedMainTab == 4) Icons.Default.SupportAgent else Icons.Outlined.SupportAgent,
                                    contentDescription = "Support"
                                )
                            },
                            label = { Text("Support", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TextWhite,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = TextWhite,
                                unselectedTextColor = TextMuted,
                                indicatorColor = BlueAccent
                            ),
                            modifier = Modifier.testTag("rail_nav_support_chat")
                        )
                    }

                    // Animated transition between tab panels inside Row
                    val rowScope = this
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(rowScope.run { Modifier.weight(1f) })
                    ) {
                        AnimatedContent(
                            targetState = selectedMainTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) with fadeOut(animationSpec = tween(180))
                            },
                            label = "scr_switch"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> PremiumDashboardScreen(
                                    viewModel = viewModel,
                                    onOddsSelected = { match, selection, odds ->
                                        activeBetMatch = match
                                        activeBetSelection = selection
                                        activeBetOdds = odds
                                    },
                                    onAnalyzeSelected = { match ->
                                        activeAnalyzeMatch = match
                                    },
                                    onViewMultiBetSlipSelected = {
                                        showMultiBetSlip = true
                                    },
                                    onProfileClick = { showProfileAuthDialog = true },
                                    onAlertsClick = { showOddsAlertsDialog = true },
                                    onThemeToggle = onThemeToggle,
                                    onWalletClick = { showDepositFundsDialog = true },
                                    onMenuClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("ShebaOdds Sportsbook Premium Redesign Active!")
                                        }
                                    }
                                )
                                1 -> BetsHistoryScreen(
                                    viewModel = viewModel
                                )
                                2 -> FinancesScreen(
                                    viewModel = viewModel
                                )
                                3 -> AdminDashboardScreen(
                                    viewModel = viewModel
                                )
                                4 -> com.example.ui.screens.SupportChatScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            } else {
                // Mobile layout using absolute 100% full-screen Box container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = selectedMainTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) with fadeOut(animationSpec = tween(180))
                        },
                        label = "scr_switch"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> PremiumDashboardScreen(
                                viewModel = viewModel,
                                onOddsSelected = { match, selection, odds ->
                                    activeBetMatch = match
                                    activeBetSelection = selection
                                    activeBetOdds = odds
                                },
                                onAnalyzeSelected = { match ->
                                    activeAnalyzeMatch = match
                                },
                                onViewMultiBetSlipSelected = {
                                    showMultiBetSlip = true
                                },
                                onProfileClick = { showProfileAuthDialog = true },
                                onAlertsClick = { showOddsAlertsDialog = true },
                                onThemeToggle = onThemeToggle,
                                onWalletClick = { showDepositFundsDialog = true },
                                onMenuClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("ShebaOdds Sportsbook Premium Redesign Active!")
                                    }
                                }
                            )
                            1 -> BetsHistoryScreen(
                                viewModel = viewModel
                            )
                            2 -> FinancesScreen(
                                viewModel = viewModel
                            )
                            3 -> AdminDashboardScreen(
                                viewModel = viewModel
                            )
                            4 -> com.example.ui.screens.SupportChatScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
} // Closes Scaffold Gradient Box

    // OVERLAY: STAKE BET RECEIPT SLIP WIDGET
    activeBetMatch?.let { match ->
        PlaceBetSlipDialog(
            match = match,
            selection = activeBetSelection,
            odds = activeBetOdds,
            viewModel = viewModel,
            onDismissRequest = { activeBetMatch = null },
            onBetPlacedSuccess = {
                activeBetMatch = null
                selectedMainTab = 1 // Go to Slips to see live active slip!
                scope.launch {
                    snackbarHostState.showSnackbar("Secure wager placed. Good luck!")
                }
            }
        )
    }

    // OVERLAY: AI ADVICE MATCH ANALYSIS
    activeAnalyzeMatch?.let { match ->
        AIPredictionDialog(
            match = match,
            viewModel = viewModel,
            onDismissRequest = { activeAnalyzeMatch = null }
        )
    }

    // OVERLAY: MULTI-BET ACCUMULATOR SLIP DIALOG
    if (showMultiBetSlip) {
        PlaceMultiBetSlipDialog(
            viewModel = viewModel,
            onDismissRequest = { showMultiBetSlip = false },
            onBetPlacedSuccess = {
                showMultiBetSlip = false
                selectedMainTab = 1 // Go to Slips history to see live active slip!
                scope.launch {
                    snackbarHostState.showSnackbar("Secure Accumulator Ticket placed successfully!")
                }
            }
        )
    }

    // OVERLAY: SECURE BIOMETRIC USER PROFILE CHALLENGE
    if (showProfileAuthDialog) {
        SecureBiometricVerificationDialog(
            title = "BIOMETRIC IDENTITY UNLOCK",
            subtitle = "Verification is active for real-time secure decryption of keychains and log indices.",
            onAuthSuccess = {
                showProfileAuthDialog = false
                showProfileDialog = true
            },
            onDismissRequest = {
                showProfileAuthDialog = false
            }
        )
    }

    // OVERLAY: SECURE USER PROFILE ACCESS MODAL
    if (showProfileDialog) {
        UserProfileDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showProfileDialog = false
            },
            onLogoutClick = {
                showProfileDialog = false
                isLoggedIn = false
                sharedPrefs.edit().putBoolean("logged_in", false).apply()
            }
        )
    }

    // OVERLAY: DEPOSIT FUNDS MODAL (Telebirr payment flow Integration)
    if (showDepositFundsDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showDepositFundsDialog = false
            }
        )
    }

    // OVERLAY: ODDS ALERTS HISTORY HUB
    if (showOddsAlertsDialog) {
        val oddsAlerts by viewModel.oddsAlerts.collectAsState()
        OddsAlertsHubDialog(
            alerts = oddsAlerts,
            onDismiss = { showOddsAlertsDialog = false },
            onMarkAllRead = {
                oddsAlerts.forEach { viewModel.markAlertAsRead(it.id) }
            },
            onClearAll = {
                viewModel.clearOddsAlerts()
            },
            onMarkRead = { id ->
                viewModel.markAlertAsRead(id)
            }
        )
    }
}
}

@Composable
fun OddsAlertsHubDialog(
    alerts: List<OddsAlert>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("odds_alerts_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Odds Alerts Hub",
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ODDS ALERTS HUB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Divider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // Clear & Actions Bar
                if (alerts.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${alerts.count { !it.isRead }} unread / ${alerts.size} total",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Clear All",
                                fontSize = 11.sp,
                                color = LightRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onClearAll() }
                                    .testTag("clear_odds_alerts_button")
                            )
                        }
                    }
                }
                
                // Scrollable Alerts List
                if (alerts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No odds alerts triggered yet.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Click the bell icon on any match card to track odds fluctuations in real-time!",
                            fontSize = 10.sp,
                            color = TextMuted.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(alerts, key = { it.id }) { alert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkRead(alert.id) }
                                    .testTag("alert_item_${alert.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alert.isRead) SlateSurfaceL2 else SlateSurfaceL2.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (alert.isRead) BorderColor else AmberAccent.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (!alert.isRead) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(AmberAccent, CircleShape)
                                                )
                                            }
                                            Text(
                                                text = "${alert.teamA} vs ${alert.teamB}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${alert.fieldChanged} changed from ${String.format("%.2f", alert.oldValue)} to ${String.format("%.2f", alert.newValue)}",
                                            fontSize = 11.sp,
                                            color = TextLight
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
                                        Text(
                                            text = "Sport: ${alert.sport} • Triggered at $timeStr",
                                            fontSize = 9.sp,
                                            color = TextMuted
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
