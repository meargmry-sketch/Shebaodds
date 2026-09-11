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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    val onSettle: (Double, Double, String) -> Unit = { w, p, _ ->
        viewModel.placeCasinoBet(wager = w, payout = p)
    }

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
            (searchQuery.isBlank() ||
                g.name.contains(searchQuery, ignoreCase = true) ||
                g.nameAm.contains(searchQuery))
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(SlateCardBG)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CASINO", fontSize = 20.sp, fontWeight = FontWeight.Black,
                    color = TextWhite, letterSpacing = 2.sp)
                Text("${CasinoGamesCatalog.playableIds.size} live • ${CasinoGamesCatalog.games.size} total",
                    fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(SlateSurfaceL2)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("${String.format("%,.2f", balance)} ETB",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Search games…", color = TextMuted, fontSize = 13.sp) },
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

        // Category chips (horizontal scroll)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip("All", "🏆", selectedCategory == null) { selectedCategory = null }
            CasinoCategory.entries.take(3).forEach { cat ->
                CategoryChip(cat.label, cat.icon, selectedCategory == cat) { selectedCategory = cat }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CasinoCategory.entries.drop(3).forEach { cat ->
                CategoryChip(cat.label, cat.icon, selectedCategory == cat) { selectedCategory = cat }
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
        Text("$icon $label", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else TextLight)
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
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(game.emoji, fontSize = 34.sp)
                if (!CasinoGamesCatalog.playableIds.contains(game.id)) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("SOON", fontSize = 8.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                    }
                }
            }
            Column {
                Text(game.name, fontSize = 13.sp, fontWeight = FontWeight.Black,
                    color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(game.nameAm, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(game.description, fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min ${game.minBet.toInt()}", fontSize = 8.5.sp,
                        color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                    Text("Max ${game.maxBet.toInt()}", fontSize = 8.5.sp,
                        color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}