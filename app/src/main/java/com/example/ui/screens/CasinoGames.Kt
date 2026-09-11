package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
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
import kotlin.math.max
import kotlin.random.Random

// ── Shared components ───────────────────────────────────────────
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (win) NeonGreen.copy(alpha = 0.15f) else LightRed.copy(alpha = 0.15f))
            .border(1.dp, if (win) NeonGreen else LightRed, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (win) NeonGreen else LightRed,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 1. SLOTS
// ═══════════════════════════════════════════════════════════════
private val slotSymbols = listOf("🍒", "🍋", "🍇", "⭐", "💎", "7️⃣", "🔔")

@Composable
fun SlotsGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var reels by remember { mutableStateOf(listOf("🍒", "🍒", "🍒")) }
    var spinning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var lastWin by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()

    fun spin() {
        if (spinning || wager > balance || wager < game.minBet) return
        scope.launch {
            spinning = true
            message = null
            onSettle(wager, 0.0, "Slots bet")
            repeat(12) {
                reels = List(3) { slotSymbols.random() }
                delay(60)
            }
            val result = List(3) { slotSymbols.random() }
            reels = result
            val mult = when {
                result[0] == result[1] && result[1] == result[2] ->
                    when (result[0]) {
                        "7️⃣" -> 50.0
                        "💎" -> 25.0
                        "⭐" -> 15.0
                        else -> 10.0
                    }
                result[0] == result[1] || result[1] == result[2] || result[0] == result[2] -> 2.0
                else -> 0.0
            }
            val win = wager * mult
            if (win > 0) {
                onSettle(0.0, win, "Slots win")
                lastWin = win
                message = "WIN! ${String.format("%,.2f", win)} ETB (${mult}x)"
            } else {
                lastWin = 0.0
                message = "No match. Try again!"
            }
            spinning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCardBG)
                .border(2.dp, AmberAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            reels.forEach { sym ->
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateSurfaceL2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sym, fontSize = 42.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !spinning)
        Spacer(Modifier.height(12.dp))
        message?.let {
            ResultBanner(it, it.startsWith("WIN"))
            Spacer(Modifier.height(8.dp))
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (spinning) "SPINNING..." else "SPIN", enabled = !spinning && wager <= balance && wager >= game.minBet) { spin() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 2. BLACKJACK
// ═══════════════════════════════════════════════════════════════
private data class Card(val rank: String, val suit: String) {
    val value: Int
        get() = when (rank) {
            "A" -> 11
            "K", "Q", "J" -> 10
            else -> rank.toInt()
        }
    override fun toString() = "$rank$suit"
}

private fun newDeck(): List<Card> {
    val suits = listOf("♠", "♥", "♦", "♣")
    val ranks = listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
    return suits.flatMap { s -> ranks.map { r -> Card(r, s) } }.shuffled()
}

private fun handValue(hand: List<Card>): Int {
    var total = hand.sumOf { it.value }
    var aces = hand.count { it.rank == "A" }
    while (total > 21 && aces > 0) { total -= 10; aces-- }
    return total
}

@Composable
fun BlackjackGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var deck by remember { mutableStateOf(newDeck()) }
    var player by remember { mutableStateOf<List<Card>>(emptyList()) }
    var dealer by remember { mutableStateOf<List<Card>>(emptyList()) }
    var phase by remember { mutableStateOf("IDLE") } // IDLE, PLAYER, DONE
    var result by remember { mutableStateOf<String?>(null) }

    fun start() {
        if (wager > balance || wager < game.minBet) return
        onSettle(wager, 0.0, "Blackjack bet")
        val d = newDeck()
        player = listOf(d[0], d[2])
        dealer = listOf(d[1], d[3])
        deck = d.drop(4)
        phase = "PLAYER"
        result = null
        // Natural blackjack
        if (handValue(player) == 21) {
            val win = wager * 2.5
            onSettle(0.0, win, "Blackjack natural")
            result = "BLACKJACK! Won ${String.format("%,.2f", win)} ETB"
            phase = "DONE"
        }
    }

    fun hit() {
        val newCard = deck.first()
        deck = deck.drop(1)
        player = player + newCard
        if (handValue(player) > 21) {
            result = "BUST! You went over 21."
            phase = "DONE"
        }
    }

    fun stand() {
        var d = dealer.toMutableList()
        var remaining = deck.toMutableList()
        while (handValue(d) < 17) {
            d.add(remaining.first()); remaining = remaining.drop(1).toMutableList()
        }
        dealer = d
        deck = remaining
        val pv = handValue(player); val dv = handValue(d)
        val win = when {
            dv > 21 || pv > dv -> wager * 2.0
            pv == dv -> wager // push
            else -> 0.0
        }
        if (win > 0) onSettle(0.0, win, "Blackjack payout")
        result = when {
            dv > 21 -> "DEALER BUST! Won ${String.format("%,.2f", win)} ETB"
            pv > dv -> "YOU WIN ${String.format("%,.2f", win)} ETB"
            pv == dv -> "PUSH — bet returned"
            else -> "Dealer wins."
        }
        phase = "DONE"
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(12.dp))
        // Dealer
        Text("DEALER ${if (phase == "PLAYER") "(?)" else "(${handValue(dealer)})"}", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
        CardRow(dealer, hideSecond = phase == "PLAYER")
        Spacer(Modifier.height(20.dp))
        Text("YOU (${handValue(player)})", color = AmberAccent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
        CardRow(player)

        Spacer(Modifier.weight(1f))
        result?.let { ResultBanner(it, it.contains("WIN") || it.contains("BLACKJACK") || it.contains("BUST")); Spacer(Modifier.height(8.dp)) }
        if (phase == "IDLE" || phase == "DONE") {
            BetControls(balance, game.minBet, game.maxBet, wager, { wager = it })
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GameButton("DEAL", enabled = wager <= balance && wager >= game.minBet) { start() }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { GameButton("HIT", color = NeonGreen) { hit() } }
                Box(Modifier.weight(1f)) { GameButton("STAND", color = LightRed) { stand() } }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun CardRow(hand: List<Card>, hideSecond: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hand.forEachIndexed { i, card ->
            val hidden = hideSecond && i == 1
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 74.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (hidden) Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF312E81))) else Color.White)
                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!hidden) Text(card.toString(), fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (card.suit in listOf("♥","♦")) Color(0xFFDC2626) else Color.Black)
                else Text("?", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 3. ROULETTE (European)
// ═══════════════════════════════════════════════════════════════
private val redNumbers = setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)

@Composable
fun RouletteGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var betType by remember { mutableStateOf("RED") } // RED, BLACK, EVEN, ODD, LOW, HIGH
    var spinning by remember { mutableStateOf(false) }
    var resultNum by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun spin() {
        if (spinning || wager > balance || wager < game.minBet) return
        scope.launch {
            spinning = true; message = null
            onSettle(wager, 0.0, "Roulette bet")
            repeat(20) { resultNum = Random.nextInt(0, 37); delay(60) }
            val num = Random.nextInt(0, 37)
            resultNum = num
            val isRed = num in redNumbers
            val isBlack = num != 0 && !isRed
            val mult = when (betType) {
                "RED" -> if (isRed) 2.0 else 0.0
                "BLACK" -> if (isBlack) 2.0 else 0.0
                "EVEN" -> if (num != 0 && num % 2 == 0) 2.0 else 0.0
                "ODD" -> if (num % 2 == 1) 2.0 else 0.0
                "LOW" -> if (num in 1..18) 2.0 else 0.0
                "HIGH" -> if (num in 19..36) 2.0 else 0.0
                else -> 0.0
            }
            val win = wager * mult
            if (win > 0) {
                onSettle(0.0, win, "Roulette win")
                message = "WIN ${String.format("%,.2f", win)} ETB! ($num ${if (isRed) "RED" else if (isBlack) "BLACK" else "GREEN"})"
            } else {
                message = "LOSE — landed on $num (${if (isRed) "RED" else if (isBlack) "BLACK" else "GREEN"})"
            }
            spinning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    if (resultNum == null) Brush.radialGradient(listOf(SlateSurfaceL2, SlateCardBG))
                    else if (resultNum == 0) Brush.radialGradient(listOf(Color(0xFF16A34A), Color(0xFF065F46)))
                    else if (resultNum in redNumbers) Brush.radialGradient(listOf(Color(0xFFDC2626), Color(0xFF7F1D1D)))
                    else Brush.radialGradient(listOf(Color(0xFF1F2937), Color(0xFF000000)))
                )
                .border(4.dp, AmberAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                resultNum?.toString() ?: "?",
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("BET ON", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("RED","BLACK","EVEN","ODD","LOW","HIGH").forEach { t ->
                val selected = betType == t
                val c = when (t) {
                    "RED" -> Color(0xFFDC2626)
                    "BLACK" -> Color(0xFF1F2937)
                    else -> SlateSurfaceL2
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (selected) c else SlateSurfaceL2)
                        .border(if (selected) 2.dp else 1.dp, if (selected) AmberAccent else BorderColor, RoundedCornerShape(6.dp))
                        .clickable(enabled = !spinning) { betType = t }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) { Text(t, fontSize = 9.sp, color = if (selected && t == "RED") Color.White else TextLight, fontWeight = FontWeight.Black) }
            }
        }
        Spacer(Modifier.height(16.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !spinning)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (spinning) "SPINNING..." else "SPIN", enabled = !spinning && wager <= balance && wager >= game.minBet) { spin() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 4. DICE
// ═══════════════════════════════════════════════════════════════
@Composable
fun DiceGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var target by remember { mutableStateOf(7) }
    var mode by remember { mutableStateOf("OVER") } // OVER, UNDER, EXACT
    var roll by remember { mutableStateOf<Pair<Int,Int>?>(null) }
    var spinning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun rollDice() {
        if (spinning || wager > balance || wager < game.minBet) return
        scope.launch {
            spinning = true; message = null
            onSettle(wager, 0.0, "Dice bet")
            repeat(15) { roll = Random.nextInt(1,7) to Random.nextInt(1,7); delay(50) }
            val r = Random.nextInt(1,7) to Random.nextInt(1,7)
            roll = r
            val sum = r.first + r.second
            val mult = when (mode) {
                "OVER" -> if (sum > target) (target + 1) / (12.0 - target).coerceAtLeast(1.0) * 1.2 else 0.0
                "UNDER" -> if (sum < target) (13 - target) / target.toDouble().coerceAtLeast(1.0) * 1.2 else 0.0
                "EXACT" -> if (sum == target) 6.0 else 0.0
                else -> 0.0
            }.coerceAtLeast(if (mode == "EXACT") 6.0 else 0.0)
            val win = wager * mult
            if (win > 0) { onSettle(0.0, win, "Dice win"); message = "WIN ${String.format("%,.2f", win)} ETB! Sum $sum (${String.format("%.2f", mult)}x)" }
            else message = "LOSE — sum was $sum"
            spinning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            roll?.let {
                DiceFace(it.first); Spacer(Modifier.width(20.dp)); DiceFace(it.second)
            } ?: run { DiceFace(1); Spacer(Modifier.width(20.dp)); DiceFace(1) }
        }
        Spacer(Modifier.height(24.dp))
        Text("MODE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("OVER","UNDER","EXACT").forEach { m ->
                Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (mode==m) AmberAccent else SlateSurfaceL2)
                    .clickable(enabled = !spinning) { mode = m }
                    .padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(m, fontSize = 11.sp, color = if (mode==m) Color.Black else TextLight, fontWeight = FontWeight.Black)
                }
            }
        }
        if (mode != "EXACT") {
            Text("Target: $target", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Slider(value = target.toFloat(), onValueChange = { target = it.toInt() }, valueRange = 2f..12f, steps = 9,
                colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent), enabled = !spinning)
        } else {
            Text("Target: $target", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Slider(value = target.toFloat(), onValueChange = { target = it.toInt() }, valueRange = 2f..12f, steps = 9,
                colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent), enabled = !spinning)
        }
        Spacer(Modifier.height(8.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !spinning)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (spinning) "ROLLING..." else "ROLL", enabled = !spinning && wager <= balance && wager >= game.minBet) { rollDice() }
        }
    }
}

@Composable
private fun DiceFace(n: Int) {
    val dotMap = mapOf(
        1 to listOf(4), 2 to listOf(0,8), 3 to listOf(0,4,8), 4 to listOf(0,2,6,8),
        5 to listOf(0,2,4,6,8), 6 to listOf(0,2,3,5,6,8)
    )
    val dots = dotMap[n] ?: listOf(4)
    Column(
        Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).border(2.dp, BorderColor, RoundedCornerShape(12.dp)).padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0..2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (col in 0..2) {
                    val i = row * 3 + col
                    Box(
                        Modifier.size(16.dp).clip(CircleShape).background(if (i in dots) Color.Black else Color.Transparent)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 5. COIN FLIP
// ═══════════════════════════════════════════════════════════════
@Composable
fun CoinFlipGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var pick by remember { mutableStateOf("HEADS") }
    var spinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun flip() {
        if (spinning || wager > balance || wager < game.minBet) return
        scope.launch {
            spinning = true; message = null
            onSettle(wager, 0.0, "Coin flip bet")
            repeat(10) { result = listOf("HEADS","TAILS").random(); delay(80) }
            val landed = listOf("HEADS","TAILS").random()
            result = landed
            if (landed == pick) {
                onSettle(0.0, wager * 2, "Coin flip win")
                message = "WIN ${String.format("%,.2f", wager * 2)} ETB!"
            } else message = "LOSE — landed on $landed"
            spinning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(160.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(AmberAccent, Color(0xFFB45309))))
                .border(4.dp, Color(0xFFFBBF24), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(result ?: "🪙", fontSize = if (result == null) 64.sp else 22.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("HEADS","TAILS").forEach { side ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (pick == side) AmberAccent else SlateSurfaceL2)
                        .border(1.dp, if (pick == side) AmberAccent else BorderColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !spinning) { pick = side }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text(side, fontSize = 13.sp, color = if (pick == side) Color.Black else TextLight, fontWeight = FontWeight.Black) }
            }
        }
        Spacer(Modifier.height(20.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !spinning)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (spinning) "FLIPPING..." else "FLIP", enabled = !spinning && wager <= balance && wager >= game.minBet) { flip() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 6. WHEEL OF FORTUNE
// ═══════════════════════════════════════════════════════════════
private val wheelSegments = listOf(0.0, 1.2, 0.0, 1.5, 0.0, 2.0, 0.0, 1.5, 0.0, 3.0, 0.0, 5.0)

@Composable
fun WheelGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var angle by remember { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun spin() {
        if (spinning || wager > balance || wager < game.minBet) return
        scope.launch {
            spinning = true; message = null
            onSettle(wager, 0.0, "Wheel bet")
            val target = Random.nextInt(wheelSegments.size)
            val segAngle = 360f / wheelSegments.size
            val spins = 5
            val targetAngle = spins * 360f + (360f - (target + 0.5f) * segAngle)
            val start = angle
            val duration = 3000
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val t = (elapsed.toFloat() / duration).coerceAtMost(1f)
                val eased = 1f - (1f - t) * (1f - t) * (1f - t)
                angle = start + (targetAngle - start + 360f) % 360f * eased + spins * 360f * eased
                if (t >= 1f) break
                delay(16)
            }
            angle = targetAngle % 360f
            val mult = wheelSegments[target]
            val win = wager * mult
            if (win > 0) { onSettle(0.0, win, "Wheel win"); message = "WIN ${String.format("%,.2f", win)} ETB (${mult}x)" }
            else message = "LOSE — landed on 0x"
            spinning = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simple segmented wheel drawn with rotated colored arcs approximated by multiple rings
            Box(
                Modifier.fillMaxSize().rotate(angle).clip(CircleShape)
                    .background(Brush.sweepGradient(listOf(Color(0xFFDC2626), Color(0xFFF59E0B), Color(0xFF16A34A), Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFFDB2777), Color(0xFFDC2626)))),
                contentAlignment = Alignment.Center
            ) {
                repeat(wheelSegments.size) { i ->
                    val a = i * (360f / wheelSegments.size) + (360f / wheelSegments.size) / 2
                    Box(
                        Modifier.fillMaxSize().rotate(a),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            "${wheelSegments[i]}x",
                            fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
            Box(Modifier.size(50.dp).clip(CircleShape).background(SlateCardBG).border(3.dp, AmberAccent, CircleShape))
            // Pointer
            Box(
                Modifier.align(Alignment.TopCenter).size(width = 0.dp, height = 0.dp)
                    .padding(top = 2.dp)
            ) {
                Text("▼", color = AmberAccent, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(20.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !spinning)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (spinning) "SPINNING..." else "SPIN WHEEL", enabled = !spinning && wager <= balance && wager >= game.minBet) { spin() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 7. CRASH
// ═══════════════════════════════════════════════════════════════
@Composable
fun CrashGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var multiplier by remember { mutableStateOf(1.0) }
    var running by remember { mutableStateOf(false) }
    var crashed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var crashPoint by remember { mutableStateOf(0.0) }
    var cashedOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun start() {
        if (running || wager > balance || wager < game.minBet) return
        onSettle(wager, 0.0, "Crash bet")
        running = true; crashed = false; cashedOut = false; multiplier = 1.0; message = null
        crashPoint = 1.0 + Random.nextDouble(0.0, 8.0).pow(0.7)
        scope.launch {
            while (running) {
                delay(50)
                multiplier += 0.03 * multiplier
                if (multiplier >= crashPoint) {
                    crashed = true; running = false
                    message = "CRASHED at ${String.format("%.2f", crashPoint)}x"
                    break
                }
            }
        }
    }

    fun cashOut() {
        if (!running || cashedOut) return
        cashedOut = true; running = false
        val win = wager * multiplier
        onSettle(0.0, win, "Crash cashout")
        message = "CASHED OUT at ${String.format("%.2f", multiplier)}x — Won ${String.format("%,.2f", win)} ETB"
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Box(
            Modifier.fillMaxWidth().height(220.dp).padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(SlateCardBG, SlateSurfaceL2)))
                .border(2.dp, if (crashed) LightRed else AmberAccent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format("%.2f", multiplier) + "x",
                    fontSize = 56.sp, fontWeight = FontWeight.Black,
                    color = if (crashed) LightRed else if (cashedOut) NeonGreen else AmberAccent
                )
                if (crashed) Text("CRASHED", color = LightRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
                else if (cashedOut) Text("CASHED OUT", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !running)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("CASHED")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (running) GameButton("CASH OUT ${String.format("%.2f", wager * multiplier)} ETB", color = NeonGreen) { cashOut() }
            else GameButton("LAUNCH", enabled = wager <= balance && wager >= game.minBet) { start() }
        }
    }
}

private fun Double.pow(e: Double) = Math.pow(this, e)

// ═══════════════════════════════════════════════════════════════
// 8. MINES
// ═══════════════════════════════════════════════════════════════
@Composable
fun MinesGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var mineCount by remember { mutableStateOf(3) }
    var mines by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var revealed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var active by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun multiplierFor(k: Int, minesN: Int): Double {
        var m = 1.0
        repeat(k) { i ->
            m *= (25 - i).toDouble() / (25 - minesN - i).toDouble()
        }
        return m * 0.97
    }

    fun start() {
        if (wager > balance || wager < game.minBet) return
        onSettle(wager, 0.0, "Mines bet")
        mines = (0 until 25).shuffled().take(mineCount).toSet()
        revealed = emptySet(); active = true; message = null
    }

    fun reveal(i: Int) {
        if (!active || i in revealed) return
        if (i in mines) {
            revealed = revealed + i
            active = false
            message = "BOOM! You hit a mine."
        } else {
            revealed = revealed + i
            message = "Current: ${String.format("%.2f", multiplierFor(revealed.size, mineCount))}x"
        }
    }

    fun cashOut() {
        if (!active || revealed.isEmpty()) return
        val mult = multiplierFor(revealed.size, mineCount)
        val win = wager * mult
        onSettle(0.0, win, "Mines cashout")
        active = false
        message = "CASHED OUT ${String.format("%,.2f", win)} ETB (${String.format("%.2f", mult)}x)"
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(12.dp))
        if (active) {
            Text("MULT: ${String.format("%.2f", multiplierFor(revealed.size, mineCount))}x", fontSize = 13.sp, color = AmberAccent, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Mines: $mineCount", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Slider(value = mineCount.toFloat(), onValueChange = { mineCount = it.toInt() }, valueRange = 1f..24f, steps = 22,
                    modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = AmberAccent, activeTrackColor = AmberAccent))
            }
        }
        Spacer(Modifier.height(8.dp))
        // 5x5 grid
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 0 until 5) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (c in 0 until 5) {
                        val i = r * 5 + c
                        val isMine = i in mines && (!active || i in revealed)
                        val isRevealed = i in revealed
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isMine -> Brush.linearGradient(listOf(LightRed, Color(0xFF991B1B)))
                                        isRevealed -> Brush.linearGradient(listOf(NeonGreen, Color(0xFF065F46)))
                                        else -> Brush.linearGradient(listOf(SlateSurfaceL2, SlateCardBG))
                                    }
                                )
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .clickable(enabled = active && !isRevealed) { reveal(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                when {
                                    isMine -> "💣"
                                    isRevealed -> "💎"
                                    else -> ""
                                },
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (active) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GameButton("CASH OUT ${String.format("%,.2f", wager * multiplierFor(revealed.size, mineCount))} ETB", color = NeonGreen, enabled = revealed.isNotEmpty()) { cashOut() }
            }
        } else {
            BetControls(balance, game.minBet, game.maxBet, wager, { wager = it })
            Spacer(Modifier.height(10.dp))
            message?.let { ResultBanner(it, it.startsWith("CASHED")); Spacer(Modifier.height(8.dp)) }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GameButton("START", enabled = wager <= balance && wager >= game.minBet) { start() }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
// 9. HI-LO
// ═══════════════════════════════════════════════════════════════
@Composable
fun HiLoGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var current by remember { mutableStateOf<Card?>(null) }
    var next by remember { mutableStateOf<Card?>(null) }
    var multiplier by remember { mutableStateOf(1.0) }
    var active by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun start() {
        if (wager > balance || wager < game.minBet) return
        onSettle(wager, 0.0, "HiLo bet")
        current = newDeck().first()
        next = null; multiplier = 1.0; active = true; message = null
    }

    fun guess(higher: Boolean) {
        if (!active) return
        val card = newDeck().first()
        next = card
        val cur = current!!
        val higherWins = card.value > cur.value
        val lowerWins = card.value < cur.value
        if ((higher && higherWins) || (!higher && lowerWins)) {
            multiplier *= 1.9
            message = "CORRECT! Next multiplier: ${String.format("%.2f", multiplier * 1.9)}x"
            current = card
        } else if (card.value == cur.value) {
            message = "TIE — card was ${card.rank}. Try again."
            current = card
        } else {
            active = false
            message = "WRONG — card was ${card}. You lost."
        }
    }

    fun cashOut() {
        if (!active || multiplier <= 1.0) return
        val win = wager * multiplier
        onSettle(0.0, win, "HiLo cashout")
        active = false
        message = "CASHED OUT ${String.format("%,.2f", win)} ETB (${String.format("%.2f", multiplier)}x)"
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            current?.let { SingleCard(it, "CURRENT") }
            if (next != null) { Spacer(Modifier.width(24.dp)); SingleCard(next!!, "NEXT") }
        }
        Spacer(Modifier.height(24.dp))
        if (active) {
            Text("Multiplier: ${String.format("%.2f", multiplier)}x", fontSize = 14.sp, color = AmberAccent, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { GameButton("HIGHER ▲", color = NeonGreen) { guess(true) } }
                Box(Modifier.weight(1f)) { GameButton("LOWER ▼", color = LightRed) { guess(false) } }
            }
            Spacer(Modifier.height(12.dp))
            if (multiplier > 1.0) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GameButton("CASH OUT ${String.format("%,.2f", wager * multiplier)} ETB", color = AmberAccent) { cashOut() }
                }
            }
        } else {
            BetControls(balance, game.minBet, game.maxBet, wager, { wager = it })
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GameButton("DEAL CARD", enabled = wager <= balance && wager >= game.minBet) { start() }
            }
        }
        Spacer(Modifier.height(12.dp))
        message?.let { ResultBanner(it, it.contains("CORRECT") || it.startsWith("CASHED")); Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SingleCard(c: Card, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.size(width = 90.dp, height = 130.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
                .border(2.dp, BorderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(c.toString(), fontSize = 30.sp, fontWeight = FontWeight.Black, color = if (c.suit in listOf("♥","♦")) Color(0xFFDC2626) else Color.Black)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 10. KENO
// ═══════════════════════════════════════════════════════════════
@Composable
fun KenoGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var picked by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var drawn by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var playing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun toggle(n: Int) {
        if (playing) return
        picked = if (n in picked) picked - n else if (picked.size < 10) picked + n else picked
    }

    fun play() {
        if (playing || picked.isEmpty() || wager > balance || wager < game.minBet) return
        scope.launch {
            playing = true; drawn = emptySet(); message = null
            onSettle(wager, 0.0, "Keno bet")
            val pool = (1..80).shuffled()
            val picks = pool.take(20).toSet()
            repeat(20) { i ->
                drawn = drawn + pool[i]
                delay(60)
            }
            val matches = picked.intersect(picks).size
            val mult = when (picked.size) {
                1 -> if (matches == 1) 3.0 else 0.0
                2 -> when (matches) { 2 -> 12.0; else -> 0.0 }
                3 -> when (matches) { 3 -> 45.0; 2 -> 2.0; else -> 0.0 }
                4 -> when (matches) { 4 -> 100.0; 3 -> 5.0; 2 -> 1.0; else -> 0.0 }
                5 -> when (matches) { 5 -> 300.0; 4 -> 15.0; 3 -> 2.0; else -> 0.0 }
                else -> when (matches) {
                    picked.size -> 500.0
                    picked.size - 1 -> 50.0
                    picked.size - 2 -> 10.0
                    picked.size - 3 -> 2.0
                    else -> 0.0
                }
            }
            val win = wager * mult
            if (win > 0) { onSettle(0.0, win, "Keno win"); message = "$matches matches! WIN ${String.format("%,.2f", win)} ETB (${mult}x)" }
            else message = "$matches matches — no payout."
            playing = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Text("Pick ${picked.size}/10 numbers", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        // Grid 1..80 (10 cols x 8 rows)
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            for (r in 0 until 8) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (c in 1..10) {
                        val n = r * 10 + c
                        val isPicked = n in picked
                        val isDrawn = n in drawn
                        val match = isPicked && isDrawn
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(5.dp))
                                .background(
                                    when {
                                        match -> NeonGreen
                                        isDrawn -> LightRed
                                        isPicked -> AmberAccent
                                        else -> SlateSurfaceL2
                                    }
                                )
                                .border(1.dp, BorderColor, RoundedCornerShape(5.dp))
                                .clickable { toggle(n) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(n.toString(), fontSize = 9.sp, color = if (isPicked || isDrawn) Color.Black else TextLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !playing)
        Spacer(Modifier.height(8.dp))
        message?.let { ResultBanner(it, it.contains("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (playing) "DRAWING..." else "PLAY", enabled = !playing && picked.isNotEmpty() && wager <= balance && wager >= game.minBet) { play() }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
// 11. VIDEO POKER (Jack or Better — simplified)
// ═══════════════════════════════════════════════════════════════
@Composable
fun VideoPokerGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var hand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var held by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var phase by remember { mutableStateOf("IDLE") } // IDLE, HOLD, DONE
    var message by remember { mutableStateOf<String?>(null) }

    fun deal() {
        if (wager > balance || wager < game.minBet) return
        onSettle(wager, 0.0, "Video poker bet")
        hand = newDeck().take(5)
        held = emptySet()
        phase = "HOLD"
        message = null
    }

    fun draw() {
        val d = newDeck()
        var di = 0
        hand = hand.mapIndexed { i, c -> if (i in held) c else d[di++] }
        val ranks = hand.map { it.rank }
        val rankCounts = ranks.groupingBy { it }.eachCount().values.sortedDescending()
        val isFlush = hand.map { it.suit }.distinct().size == 1
        val values = hand.map { it.value }.sorted()
        val isStraight = values == (values.first()..values.last()).toList() && values.distinct().size == 5
        val mult = when {
            isFlush && isStraight && values.first() == 10 -> 250.0
            rankCounts.firstOrNull() == 4 -> 50.0
            rankCounts.getOrNull(0) == 3 && rankCounts.getOrNull(1) == 2 -> 25.0
            isFlush -> 6.0
            isStraight -> 4.0
            rankCounts.firstOrNull() == 3 -> 3.0
            rankCounts.getOrNull(0) == 2 && rankCounts.getOrNull(1) == 2 -> 2.0
            rankCounts.firstOrNull() == 2 && hand.any { it.rank in listOf("J","Q","K","A") && ranks.count { r -> r == it.rank } == 2 } -> 1.0
            else -> 0.0
        }
        val win = wager * mult
        if (win > 0) { onSettle(0.0, win, "Video poker win"); message = "WIN ${String.format("%,.2f", win)} ETB (${mult}x)" }
        else message = "No winning hand."
        phase = "DONE"
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            hand.forEachIndexed { i, c ->
                val isHeld = i in held
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(8.dp))
                            .background(Color.White).border(if (isHeld) 3.dp else 1.dp, if (isHeld) NeonGreen else BorderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = phase == "HOLD") { held = if (isHeld) held - i else held + i },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hand.isEmpty()) Text("", fontSize = 20.sp)
                        else Text(c.toString(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (c.suit in listOf("♥","♦")) Color(0xFFDC2626) else Color.Black)
                    }
                    if (isHeld) Text("HELD", fontSize = 8.sp, color = NeonGreen, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        when (phase) {
            "IDLE", "DONE" -> {
                BetControls(balance, game.minBet, game.maxBet, wager, { wager = it })
                Spacer(Modifier.height(10.dp))
                message?.let { ResultBanner(it, it.contains("WIN")); Spacer(Modifier.height(8.dp)) }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GameButton("DEAL", enabled = wager <= balance && wager >= game.minBet) { deal() }
                }
            }
            "HOLD" -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GameButton("DRAW", color = NeonGreen) { draw() }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 12. BACCARAT (simplified)
// ═══════════════════════════════════════════════════════════════
@Composable
fun BaccaratGame(
    game: CasinoGame,
    balance: Double,
    onSettle: (Double, Double, String) -> Unit,
    onExit: () -> Unit
) {
    var wager by remember { mutableStateOf(game.minBet.coerceAtMost(balance)) }
    var pick by remember { mutableStateOf("PLAYER") } // PLAYER, BANKER, TIE
    var player by remember { mutableStateOf<List<Card>>(emptyList()) }
    var banker by remember { mutableStateOf<List<Card>>(emptyList()) }
    var result by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun baccaratVal(hand: List<Card>) = hand.sumOf { if (it.value >= 10) 0 else it.value } % 10

    fun deal() {
        if (busy || wager > balance || wager < game.minBet) return
        scope.launch {
            busy = true; message = null; result = null
            onSettle(wager, 0.0, "Baccarat bet")
            val d = newDeck()
            player = listOf(d[0], d[2])
            banker = listOf(d[1], d[3])
            delay(500)
            val pv = baccaratVal(player); val bv = baccaratVal(banker)
            val winner = when {
                pv > bv -> "PLAYER"
                bv > pv -> "BANKER"
                else -> "TIE"
            }
            result = "$winner WINS — Player $pv / Banker $bv"
            val mult = when {
                winner == pick && winner == "PLAYER" -> 2.0
                winner == pick && winner == "BANKER" -> 1.95
                winner == "TIE" && pick == "TIE" -> 8.0
                else -> 0.0
            }
            val win = wager * mult
            if (win > 0) { onSettle(0.0, win, "Baccarat win"); message = "WIN ${String.format("%,.2f", win)} ETB" }
            else message = "LOSE — $winner wins."
            busy = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        CasinoGameTopBar(game, balance, onExit)
        Spacer(Modifier.height(16.dp))
        Text("PLAYER (${if (player.isEmpty()) "-" else baccaratVal(player)})", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        CardRow(player)
        Spacer(Modifier.height(12.dp))
        Text("BANKER (${if (banker.isEmpty()) "-" else baccaratVal(banker)})", fontSize = 12.sp, color = AmberAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        CardRow(banker)
        Spacer(Modifier.height(20.dp))
        Text("BET ON", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("PLAYER","BANKER","TIE").forEach { p ->
                val sel = pick == p
                val c = when (p) { "PLAYER" -> NeonGreen; "BANKER" -> AmberAccent; else -> Color(0xFFDB2777) }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (sel) c else SlateSurfaceL2)
                        .border(1.dp, if (sel) c else BorderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = !busy) { pick = p }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) { Text(p, fontSize = 11.sp, color = if (sel) Color.Black else TextLight, fontWeight = FontWeight.Black) }
            }
        }
        Spacer(Modifier.height(12.dp))
        result?.let {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                Text(it, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.weight(1f))
        BetControls(balance, game.minBet, game.maxBet, wager, { wager = it }, enabled = !busy)
        Spacer(Modifier.height(10.dp))
        message?.let { ResultBanner(it, it.startsWith("WIN")); Spacer(Modifier.height(8.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GameButton(if (busy) "DEALING..." else "DEAL", enabled = !busy && wager <= balance && wager >= game.minBet) { deal() }
        }
        Spacer(Modifier.height(16.dp))
    }
}