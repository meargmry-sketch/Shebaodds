package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasinoGame
import com.example.ui.theme.*

@Composable
fun CasinoGameHost(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050B18))) {
        when (game.id) {
            // ── Slots ────────────────────────────────────────────
            "slot", "lucky7"                     -> SlotsGame(game, balance, onSettle, onExit)

            // ── Table ────────────────────────────────────────────
            "blackjack"                          -> BlackjackGame(game, balance, onSettle, onExit)
            "baccarat"                           -> BaccaratGame(game, balance, onSettle, onExit)
            "roulette", "roulette360"            -> RouletteGame(game, balance, onSettle, onExit)
            "dice", "craps", "pokerdice"         -> DiceGame(game, balance, onSettle, onExit)
            "sicbo"                              -> DiceGame(game, balance, onSettle, onExit)
            "war", "coinflip"                    -> CoinFlipGame(game, balance, onSettle, onExit)
            "wheel", "spinwin", "megawheel"      -> WheelGame(game, balance, onSettle, onExit)

            // ── Classic ──────────────────────────────────────────
            "videopoker"                         -> VideoPokerGame(game, balance, onSettle, onExit)
            "hilo", "reddog"                     -> HiLoGame(game, balance, onSettle, onExit)

            // ── Crash ────────────────────────────────────────────
            "crash", "aviator", "chickenroad"    -> CrashGame(game, balance, onSettle, onExit)
            "mines", "tower"                     -> MinesGame(game, balance, onSettle, onExit)

            // ── Keno / Bingo ─────────────────────────────────────
            "keno", "bingo"                      -> KenoGame(game, balance, onSettle, onExit)

            // ── Race engine (NEW) ────────────────────────────────
            "horseracing", "greyhound",
            "motorbike", "virtualsports"         -> RaceGame(game, balance, onSettle, onExit)

            // ── Prediction engine (NEW) ──────────────────────────
            "football", "basketball", "tennis",
            "baseball", "cricket", "knockout",
            "penalty", "darts"                   -> PredictionGame(game, balance, onSettle, onExit)

            // ── Not implemented ──────────────────────────────────
            else                                 -> ComingSoonGame(game, onExit)
        }
    }
}

@Composable
fun CasinoGameTopBar(game: CasinoGame, balance: Double, onExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(SlateCardBG)
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
                Text(game.nameAm, fontSize = 10.sp, color = TextMuted)
            }
        }
        Row(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(SlateSurfaceL2)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = NeonGreen, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text("${String.format("%,.2f", balance)} ETB",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
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
                Text("COMING SOON", fontSize = 16.sp, fontWeight = FontWeight.Black,
                    color = AmberAccent, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text(game.description, fontSize = 12.sp, color = TextMuted,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }
    }
}

@Composable
fun GameButton(
    text: String,
    color: Color = AmberAccent,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color else color.copy(alpha = 0.35f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (color == AmberAccent || color == NeonGreen) Color.Black else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ResultBanner(text: String, win: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (win) NeonGreen.copy(alpha = 0.15f) else LightRed.copy(alpha = 0.15f))
            .border(1.dp, if (win) NeonGreen else LightRed, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (win) NeonGreen else LightRed,
            fontWeight = FontWeight.Black, fontSize = 13.sp)
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
    val maxAllowed = minOf(maxBet, balance).coerceAtLeast(minBet)
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text("Wager: ${String.format("%,.2f", wager)} ETB",
            fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Slider(
            value = wager.toFloat().coerceIn(minBet.toFloat(), maxAllowed.toFloat()),
            onValueChange = { onWagerChange(it.toDouble()) },
            valueRange = minBet.toFloat()..maxAllowed.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = AmberAccent,
                activeTrackColor = AmberAccent,
                inactiveTrackColor = BorderColor
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1.0, 2.0, 5.0, 10.0).forEach { m ->
                val amt = (minBet * m).coerceAtMost(maxAllowed)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateSurfaceL2)
                        .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                        .clickable(enabled = enabled) { onWagerChange(amt) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("${amt.toInt()}", fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}