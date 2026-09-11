package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasinoGame
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

private data class Racer(
    val name: String,
    val emoji: String,
    val odds: Double,
    val color: Color
)

private fun racersFor(gameId: String): List<Racer> {
    val names = when (gameId) {
        "horseracing" -> listOf("Thunder", "Blaze", "Storm", "Comet", "Rocket", "Shadow")
        "greyhound"   -> listOf("Speedy", "Flash", "Dash", "Bolt", "Rapid", "Swift")
        "motorbike"   -> listOf("Viper", "Falcon", "Titan", "Raptor", "Ghost", "Rebel")
        else          -> listOf("Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot")
    }
    val emoji = when (gameId) {
        "horseracing" -> "🐎"
        "greyhound"   -> "🐕"
        "motorbike"   -> "🏍️"
        else          -> "🏎️"
    }
    // Odds pool: favourite → underdog
    val oddsPool = listOf(2.1, 3.0, 4.2, 5.5, 7.0, 9.5)
    val colors = listOf(
        Color(0xFFDC2626), Color(0xFF2563EB), Color(0xFF16A34A),
        Color(0xFFF59E0B), Color(0xFF7C3AED), Color(0xFFDB2777)
    )
    return names.mapIndexed { i, n ->
        Racer(n, emoji, oddsPool[i], colors[i])
    }
}

@Composable
fun RaceGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    val racers = remember { racersFor(game.id) }
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var pick by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(List(racers.size) { 0f }) }
    var racing by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun startRace() {
        if (racing || wager > balance || wager < game.minBet) return
        scope.launch {
            racing = true; winner = null; message = null
            onSettle(wager, 0.0, "Race bet")
            progress = List(racers.size) { 0f }

            // Determine winner based on inverse odds (favourites more likely)
            val weights = racers.map { 1.0 / it.odds }
            val totalW = weights.sum()
            var r = Random.nextDouble() * totalW
            var wIndex = 0
            for (i in weights.indices) {
                r -= weights[i]
                if (r <= 0) { wIndex = i; break }
            }

            // Assign each racer a finish time — winner gets fastest
            val finishTimes = racers.indices.map { i ->
                if (i == wIndex) Random.nextDouble(4.0, 4.3)
                else Random.nextDouble(4.4, 5.6)
            }

            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                progress = finishTimes.map { t ->
                    (elapsed / t).toFloat().coerceIn(0f, 1f)
                }
                if (progress.all { it >= 1f }) break
                delay(50)
            }
            winner = wIndex
            val picked = racers[pick]
            if (wIndex == pick) {
                val win = wager * picked.odds
                onSettle(0.0, win, "Race win")
                message = "🏆 ${picked.name} wins! You won ${String.format("%,.2f", win)} ETB"
            } else {
                message = "🏆 ${racers[wIndex].name} wins. Better luck next time."
            }
            racing = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(8.dp))

        // Track
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp)).background(SlateCardBG)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            racers.forEachIndexed { i, racer ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", fontSize = 10.sp, color = TextMuted,
                        fontWeight = FontWeight.Bold, modifier = Modifier.width(14.dp))
                    Box(
                        modifier = Modifier.weight(1f).height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (pick == i) racer.color.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        // Moving racer
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress[i])
                                .background(
                                    racer.color.copy(alpha = if (pick == i) 0.35f else 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Text(
                            racer.emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (progress[i] * 260).dp.coerceAtMost(260.dp))
                        )
                        if (winner == i) {
                            Text("🏆", fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("${racer.odds}x", fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // Pick row
        if (!racing) {
            Text("YOUR PICK", fontSize = 11.sp, color = TextMuted,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                racers.forEachIndexed { i, r ->
                    val sel = pick == i
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) r.color else SlateSurfaceL2)
                            .border(1.dp, if (sel) r.color else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { pick = i }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(r.emoji, fontSize = 16.sp)
                            Text("${r.odds}x", fontSize = 9.sp,
                                color = if (sel) Color.White else TextLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        message?.let { ResultBanner(it, it.contains("won") || it.contains("You won")) }
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !racing)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(
                if (racing) "RACING..." else "START RACE",
                enabled = !racing && wager <= balance && wager >= game.minBet
            ) { startRace() }
        }
        Spacer(Modifier.height(16.dp))
    }
}