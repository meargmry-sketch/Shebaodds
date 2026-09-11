package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasinoCategory
import com.example.data.model.CasinoGame
import com.example.data.model.CasinoGamesCatalog
import com.example.ui.theme.*
import com.example.viewmodel.BetViewModel

@Composable
fun CasinoScreen(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.wallet.collectAsState()
    val balance = wallet?.balance ?: 0.0

    var selectedCategory by remember { mutableStateOf<CasinoCategory?>(null) }
    var activeGame by remember { mutableStateOf<CasinoGame?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Settle handler — adjust this one call to match your ViewModel API
    val onSettle: (wager: Double, payout: Double, description: String) -> Unit = { w, p, _ ->
        viewModel.placeCasinoBet(wager = w, payout = p)
    }

    // ── Game host overlay ────────────────────────────────────────
    activeGame?.let { game ->
        CasinoGameHost(
            game = game,
            balance = balance,
            onSettle = onSettle,
            onExit = { activeGame = null }
        )
    }

    val gamesToShow = remember(selectedCategory, searchQuery) {
        CasinoGamesCatalog.games.filter { g ->
            (selectedCategory == null || g.category == selectedCategory) &&
            (searchQuery.isBlank() || g.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateCardBG)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "CASINO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 2.sp
                )
                Text(
                    "${CasinoGamesCatalog.games.size} games • RTP up to 99.5%",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceL2)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${String.format("%,.2f", balance)} ETB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Search games...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmberAccent,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = AmberAccent
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // Category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip("All", "🏆", selectedCategory == null) { selectedCategory = null }
            CasinoCategory.entries.forEach { cat ->
                CategoryChip(cat.label, cat.icon, selectedCategory == cat) {
                    selectedCategory = cat
                }
            }
        }

        // Games grid
        if (gamesToShow.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No games match your search.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(gamesToShow, key = { it.id }) { game ->
                    CasinoGameCard(game) { activeGame = game }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AmberAccent else SlateSurfaceL2)
            .border(1.dp, if (selected) AmberAccent else BorderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            "$icon $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else TextLight
        )
    }
}

@Composable
private fun CasinoGameCard(game: CasinoGame, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(game.gradient))
            .border(1.dp, BorderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(game.emoji, fontSize = 34.sp)
                if (!game.isPlayable) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("SOON", fontSize = 8.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                    }
                }
            }
            Column {
                Text(
                    game.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    game.description,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min ${game.minBet.toInt()}", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                    Text("RTP ${game.rtp}%", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Game host dispatcher ─────────────────────────────────────────
@Composable
fun CasinoGameHost(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050B18))) {
        if (!game.isPlayable) {
            ComingSoonGame(game, onExit)
            return@Box
        }
        when (game.id) {
            "slots_classic", "slots_mega", "slots_egypt" -> SlotsGame(game, balance, onSettle, onExit)
            "blackjack"        -> BlackjackGame(game, balance, onSettle, onExit)
            "roulette_eu"      -> RouletteGame(game, balance, onSettle, onExit)
            "baccarat"         -> BaccaratGame(game, balance, onSettle, onExit)
            "video_poker"      -> VideoPokerGame(game, balance, onSettle, onExit)
            "dice"             -> DiceGame(game, balance, onSettle, onExit)
            "coinflip"         -> CoinFlipGame(game, balance, onSettle, onExit)
            "crash"            -> CrashGame(game, balance, onSettle, onExit)
            "mines"            -> MinesGame(game, balance, onSettle, onExit)
            "wheel"            -> WheelGame(game, balance, onSettle, onExit)
            "hilo"             -> HiLoGame(game, balance, onSettle, onExit)
            "keno"             -> KenoGame(game, balance, onSettle, onExit)
            else               -> ComingSoonGame(game, onExit)
        }
    }
}

@Composable
fun CasinoGameTopBar(game: CasinoGame, balance: Double, onExit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateCardBG)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(game.emoji, fontSize = 22.sp)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(game.name, fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextWhite)
                Text("RTP ${game.rtp}% • Min ${game.minBet.toInt()}", fontSize = 9.sp, color = TextMuted)
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SlateSurfaceL2)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = NeonGreen, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text("${String.format("%,.2f", balance)} ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
        }
    }
}

@Composable
fun ComingSoonGame(game: CasinoGame, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, 0.0, onExit)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.emoji, fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text("COMING SOON", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AmberAccent, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text(game.description, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }
    }
}

@Composable
fun BetControls(
    balance: Double,
    minBet: Double,
    maxBet: Double,
    wager: Double,
    onWagerChange: (Double) -> Unit,
    enabled: Boolean = true
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text("Wager: ${String.format("%,.2f", wager)} ETB", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Slider(
            value = wager.toFloat().coerceIn(minBet.toFloat(), min(maxBet, balance).toFloat().coerceAtLeast(minBet.toFloat())),
            onValueChange = { onWagerChange(it.toDouble()) },
            valueRange = minBet.toFloat()..max(maxBet, minBet + 1).toFloat().coerceAtMost(balance.toFloat().coerceAtLeast(minBet.toFloat() + 1f)),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = AmberAccent,
                activeTrackColor = AmberAccent,
                inactiveTrackColor = BorderColor
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(minBet, minBet * 2, minBet * 5, minBet * 10).forEach { amt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateSurfaceL2)
                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                        .clickable(enabled = enabled) { onWagerChange(amt.coerceAtMost(balance).coerceAtLeast(minBet)) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(amt.toInt().toString(), fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}