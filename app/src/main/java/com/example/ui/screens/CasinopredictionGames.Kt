package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasinoGame
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class Matchup(
    val home: String,
    val away: String,
    val homeEmoji: String,
    val awayEmoji: String,
    val homeStrength: Double, // 0..1
    val awayStrength: Double
)

private fun matchupFor(gameId: String): Matchup = when (gameId) {
    "football"   -> Matchup("Arsenal",  "Chelsea",  "🔴", "🔵", 0.62, 0.55)
    "basketball" -> Matchup("Lakers",   "Celtics",  "🟡", "🟢", 0.58, 0.60)
    "tennis"     -> Matchup("Djokovic", "Alcaraz",  "🎾", "🎾", 0.61, 0.57)
    "baseball"   -> Matchup("Yankees",  "Red Sox",  "⚾", "⚾", 0.55, 0.52)
    "cricket"    -> Matchup("India",    "Australia","🏏", "🏏", 0.60, 0.58)
    "knockout"   -> Matchup("Tyson",    "Ali",      "🥊", "🥊", 0.55, 0.58)
    "penalty"    -> Matchup("Striker",  "Keeper",   "⚽", "🧤", 0.70, 0.30)
    "darts"      -> Matchup("Player 1", "Player 2", "🎯", "🎯", 0.53, 0.50)
    else         -> Matchup("Team A",   "Team B",   "🏆", "🏆", 0.50, 0.50)
}

@Composable
fun PredictionGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    val matchup = remember { matchupFor(game.id) }
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var pick by remember { mutableStateOf("HOME") }   // HOME / DRAW / AWAY
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf(3) }
    val scope = rememberCoroutineScope()

    // Compute implied odds from strength, then apply a small margin
    val (homeOdds, drawOdds, awayOdds) = remember(matchup) {
        val hw = matchup.homeStrength
        val aw = matchup.awayStrength
        val dw = (1.0 - hw - aw).coerceAtLeast(0.10)
        val total = hw + aw + dw
        val margin = 1.10
        Triple(
            (total / hw / margin).coerceIn(1.20, 20.0),
            (total / dw / margin).coerceIn(1.20, 20.0),
            (total / aw / margin).coerceIn(1.20, 20.0)
        )
    }

    val isDrawAvailable = game.id !in setOf("tennis", "penalty", "darts", "knockout")

    fun play() {
        if (busy || wager > balance || wager < game.minBet) return
        scope.launch {
            busy = true; message = null; result = null
            onSettle(wager, 0.0, "Prediction bet")

            // Countdown
            for (i in 3 downTo 1) { countdown = i; delay(500) }
            countdown = 0

            // Simulate result weighted by strength
            val r = Random.nextDouble()
            val hw = matchup.homeStrength
            val aw = matchup.awayStrength
            val dw = (1.0 - hw - aw).coerceAtLeast(0.10)
            val total = hw + aw + (if (isDrawAvailable) dw else 0.0)
            val norm = r * total
            val winner = when {
                norm < hw -> "HOME"
                !isDrawAvailable || norm < hw + dw -> if (isDrawAvailable) "DRAW" else "AWAY"
                else -> "AWAY"
            }
            result = winner

            val selectedOdds = when (pick) {
                "HOME" -> homeOdds
                "DRAW" -> drawOdds
                else   -> awayOdds
            }
            val win = if (winner == pick) wager * selectedOdds else 0.0
            if (win > 0) {
                onSettle(0.0, win, "Prediction win")
                message = "WIN! ${String.format("%,.2f", win)} ETB (${String.format("%.2f", selectedOdds)}x)"
            } else {
                message = "LOSE — ${when (winner) {
                    "HOME" -> matchup.home
                    "DRAW" -> "Draw"
                    else   -> matchup.away
                }} won."
            }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(12.dp))

        // Matchup display
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(SlateCardBG, SlateSurfaceL2)))
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(matchup.homeEmoji, fontSize = 44.sp)
                Spacer(Modifier.height(4.dp))
                Text(matchup.home, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (busy) {
                    Text(countdown.toString(), fontSize = 34.sp,
                        color = AmberAccent, fontWeight = FontWeight.Black)
                } else if (result != null) {
                    Text(result!!, fontSize = 13.sp, color = AmberAccent, fontWeight = FontWeight.Black)
                    Text("RESULT", fontSize = 8.sp, color = TextMuted, letterSpacing = 1.sp)
                } else {
                    Text("VS", fontSize = 22.sp, color = TextMuted, fontWeight = FontWeight.Black)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(matchup.awayEmoji, fontSize = 44.sp)
                Spacer(Modifier.height(4.dp))
                Text(matchup.away, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("YOUR PICK", fontSize = 11.sp, color = TextMuted,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickBox(
                label = matchup.home,
                odds = homeOdds,
                selected = pick == "HOME",
                enabled = !busy,
                color = NeonGreen
            ) { pick = "HOME" }
            if (isDrawAvailable) {
                PickBox(
                    label = "Draw",
                    odds = drawOdds,
                    selected = pick == "DRAW",
                    enabled = !busy,
                    color = AmberAccent
                ) { pick = "DRAW" }
            }
            PickBox(
                label = matchup.away,
                odds = awayOdds,
                selected = pick == "AWAY",
                enabled = !busy,
                color = LightRed
            ) { pick = "AWAY" }
        }

        Spacer(Modifier.weight(1f))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !busy)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(
                if (busy) "SIMULATING..." else "PLACE BET",
                enabled = !busy && wager <= balance && wager >= game.minBet
            ) { play() }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PickBox(
    label: String,
    odds: Double,
    selected: Boolean,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.25f) else SlateSurfaceL2)
            .border(1.dp, if (selected) color else BorderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = TextWhite,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(String.format("%.2f", odds), fontSize = 13.sp,
                color = if (selected) color else AmberAccent, fontWeight = FontWeight.Black)
        }
    }
}