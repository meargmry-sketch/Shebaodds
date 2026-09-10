package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderColor
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.BetViewModel

private val CasinoBg = Color(0xFF050B18)
private val CasinoCard = Color(0xFF10192B)
private val CasinoGold = Color(0xFFFFC247)
private val CasinoGoldDark = Color(0xFF8C6415)

private data class CasinoGame(
    val name: String,
    val category: String,
    val description: String,
    val badge: String
)

private val casinoGames = listOf(
    CasinoGame("Live Blackjack", "Table", "Classic 21 with a premium table layout.", "LIVE"),
    CasinoGame("European Roulette", "Table", "Single-zero roulette demo table.", "HOT"),
    CasinoGame("Baccarat Royale", "Table", "Player, Banker and Tie demo game.", "VIP"),
    CasinoGame("Golden Slots", "Slots", "Three-reel luxury slot experience.", "NEW"),
    CasinoGame("Diamond Slots", "Slots", "Fast demo spins with premium visuals.", "TOP"),
    CasinoGame("Crash Arena", "Instant", "Watch the multiplier rise in demo mode.", "LIVE")
)

@Composable
fun CasinoScreen(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedGame by remember { mutableStateOf<CasinoGame?>(null) }

    val categories = listOf("All", "Table", "Slots", "Instant")
    val visibleGames = if (selectedCategory == "All") {
        casinoGames
    } else {
        casinoGames.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CasinoBg, Color(0xFF0B1324)))
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGoldDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(Brush.linearGradient(listOf(CasinoGold, CasinoGoldDark))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Casino, contentDescription = null, tint = Color(0xFF15100A))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SHEBAODDS CASINO", color = CasinoGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                Text("Premium casino lounge", color = TextMuted, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.Security, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Casino games are currently presented in demo mode. No real-money provider is connected by this screen.",
                            color = TextLight,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (category == "All") {
                                { Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = CasinoGold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Featured games", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            items(visibleGames, key = { it.name }) { game ->
                CasinoGameCard(game = game, onClick = { selectedGame = game })
            }
        }
    }

    selectedGame?.let { game ->
        AlertDialog(
            onDismissRequest = { selectedGame = null },
            icon = { Icon(Icons.Default.Casino, contentDescription = null, tint = CasinoGold) },
            title = { Text(game.name, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text(game.description, color = TextLight)
                    Spacer(Modifier.height(10.dp))
                    Text("Demo mode", color = CasinoGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("This launch does not place a real-money wager or debit the wallet.", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedGame = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CasinoGold, contentColor = Color(0xFF15100A))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Open demo")
                }
            },
            dismissButton = {
                Button(onClick = { selectedGame = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun CasinoGameCard(
    game: CasinoGame,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, BorderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF272018), Color(0xFF121827)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Casino, contentDescription = null, tint = CasinoGold, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(game.name, color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(7.dp))
                    Text(game.badge, color = CasinoGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(3.dp))
                Text(game.category, color = CasinoGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(game.description, color = TextMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Open ${game.name}", tint = CasinoGold, modifier = Modifier.size(28.dp))
        }
    }
}