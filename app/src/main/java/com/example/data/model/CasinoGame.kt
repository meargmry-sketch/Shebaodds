package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class CasinoCategory(val label: String, val icon: String) {
    SLOTS("Slots", "🎰"),
    TABLE("Table Games", "🃏"),
    LIVE("Live Dealer", "🎥"),
    INSTANT("Instant Win", "⚡"),
    LOTTERY("Lottery", "🎱")
}

data class CasinoGame(
    val id: String,
    val name: String,
    val category: CasinoCategory,
    val emoji: String,
    val minBet: Double,
    val maxBet: Double,
    val rtp: Double,
    val description: String,
    val gradient: List<Color>,
    val isPlayable: Boolean = true
)

object CasinoGamesCatalog {
    private val purpleBlue = listOf(Color(0xFF7C3AED), Color(0xFF2563EB))
    private val redOrange  = listOf(Color(0xFFDC2626), Color(0xFFF59E0B))
    private val greenTeal  = listOf(Color(0xFF059669), Color(0xFF14B8A6))
    private val pinkRose   = listOf(Color(0xFFDB2777), Color(0xFFF43F5E))
    private val gold       = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    private val blueCyan   = listOf(Color(0xFF0EA5E9), Color(0xFF22D3EE))
    private val slate      = listOf(Color(0xFF334155), Color(0xFF475569))

    val games: List<CasinoGame> = listOf(
        // ─── SLOTS ────────────────────────────────────────────────
        CasinoGame("slots_classic", "Classic Slots",  CasinoCategory.SLOTS, "🎰", 10.0,  5_000.0, 95.5, "Spin 3 reels and match fruit symbols.", purpleBlue),
        CasinoGame("slots_mega",    "Mega Jackpot",   CasinoCategory.SLOTS, "💎", 50.0, 20_000.0, 94.2, "Progressive jackpot slots.", redOrange),
        CasinoGame("slots_egypt",   "Egyptian Fortune",CasinoCategory.SLOTS,"🏺", 20.0, 10_000.0, 96.0, "Ancient themed 5-reel slot.", gold),
        CasinoGame("slots_wild",    "Wild West",      CasinoCategory.SLOTS, "🤠", 20.0, 10_000.0, 95.0, "Frontier themed slot action.", greenTeal, isPlayable = false),
        CasinoGame("slots_fruit",   "Fruit Machine",  CasinoCategory.SLOTS, "🍒", 10.0,  5_000.0, 96.5, "Retro fruit slot machine.", pinkRose, isPlayable = false),
        CasinoGame("slots_3d",      "3D Slots Deluxe",CasinoCategory.SLOTS, "✨", 30.0, 15_000.0, 94.8, "Immersive 3D slot reels.", blueCyan, isPlayable = false),

        // ─── TABLE ────────────────────────────────────────────────
        CasinoGame("blackjack",     "Blackjack",      CasinoCategory.TABLE, "♠️", 20.0, 10_000.0, 99.5, "Beat the dealer to 21.", slate),
        CasinoGame("roulette_eu",   "European Roulette",CasinoCategory.TABLE,"🎡", 10.0, 10_000.0, 97.3, "Single-zero roulette wheel.", redOrange),
        CasinoGame("baccarat",      "Baccarat",       CasinoCategory.TABLE, "🃏", 50.0, 20_000.0, 98.9, "Player vs Banker showdown.", gold),
        CasinoGame("video_poker",   "Video Poker",    CasinoCategory.TABLE, "🂡", 20.0,  5_000.0, 99.5, "Jack-or-Better draw poker.", purpleBlue),
        CasinoGame("roulette_us",   "American Roulette",CasinoCategory.TABLE,"🎯",10.0, 10_000.0, 94.7, "Double-zero roulette.", slate, isPlayable = false),
        CasinoGame("caribbean",     "Caribbean Stud", CasinoCategory.TABLE, "🏝️", 25.0,  8_000.0, 94.8, "Poker against the house.", blueCyan, isPlayable = false),
        CasinoGame("three_card",    "Three Card Poker",CasinoCategory.TABLE,"🎴", 20.0,  5_000.0, 96.6, "Fast poker variant.", pinkRose, isPlayable = false),

        // ─── LIVE DEALER ──────────────────────────────────────────
        CasinoGame("live_blackjack","Live Blackjack", CasinoCategory.LIVE, "🎥", 50.0, 50_000.0, 99.5, "Real dealer, live stream.", slate, isPlayable = false),
        CasinoGame("live_roulette", "Live Roulette",  CasinoCategory.LIVE, "📹", 25.0, 50_000.0, 97.3, "Live European roulette.", redOrange, isPlayable = false),
        CasinoGame("live_baccarat", "Live Baccarat",  CasinoCategory.LIVE, "🎬", 50.0,100_000.0, 98.9, "Live baccarat tables.", gold, isPlayable = false),
        CasinoGame("live_dragon",   "Dragon Tiger",   CasinoCategory.LIVE, "🐉", 20.0, 20_000.0, 96.7, "Card showdown — Dragon vs Tiger.", redOrange, isPlayable = false),
        CasinoGame("live_sicbo",    "Live Sic Bo",    CasinoCategory.LIVE, "🎲", 20.0, 20_000.0, 97.2, "Ancient Chinese dice game.", greenTeal, isPlayable = false),
        CasinoGame("live_andar",    "Andar Bahar",    CasinoCategory.LIVE, "🪔", 20.0, 20_000.0, 97.0, "Popular Indian card game.", pinkRose, isPlayable = false),

        // ─── INSTANT WIN ──────────────────────────────────────────
        CasinoGame("dice",          "Dice",           CasinoCategory.INSTANT, "🎲", 10.0, 50_000.0, 99.0, "Roll two dice — bet over/under.", greenTeal),
        CasinoGame("coinflip",      "Coin Flip",      CasinoCategory.INSTANT, "🪙", 10.0, 50_000.0, 99.0, "Heads or tails double-up.", gold),
        CasinoGame("crash",         "Crash",          CasinoCategory.INSTANT, "🚀", 10.0, 50_000.0, 99.0, "Cash out before the crash.", redOrange),
        CasinoGame("mines",         "Mines",          CasinoCategory.INSTANT, "💣", 10.0, 50_000.0, 99.0, "Pick safe tiles, avoid mines.", purpleBlue),
        CasinoGame("wheel",         "Wheel of Fortune",CasinoCategory.INSTANT,"🎡", 10.0, 50_000.0, 97.0, "Spin the multiplier wheel.", redOrange),
        CasinoGame("hilo",          "Hi-Lo",          CasinoCategory.INSTANT, "🃏", 10.0, 20_000.0, 99.0, "Guess the next card higher or lower.", blueCyan),
        CasinoGame("plinko",        "Plinko",         CasinoCategory.INSTANT, "🔻", 10.0, 20_000.0, 99.0, "Drop the ball, hit multipliers.", pinkRose, isPlayable = false),
        CasinoGame("aviator",       "Aviator",        CasinoCategory.INSTANT, "✈️", 10.0, 50_000.0, 99.0, "Fly higher, cash out earlier.", blueCyan, isPlayable = false),
        CasinoGame("limbo",         "Limbo",          CasinoCategory.INSTANT, "📈", 10.0, 50_000.0, 99.0, "Pick a target multiplier and roll.", greenTeal, isPlayable = false),
        CasinoGame("tower",         "Tower",          CasinoCategory.INSTANT, "🏗️", 10.0, 20_000.0, 99.0, "Climb the tower for higher payouts.", slate, isPlayable = false),

        // ─── LOTTERY ──────────────────────────────────────────────
        CasinoGame("keno",          "Keno",           CasinoCategory.LOTTERY, "🔢", 10.0, 5_000.0, 92.0, "Pick numbers, match the draw.", purpleBlue),
        CasinoGame("bingo",         "Bingo",          CasinoCategory.LOTTERY, "🎟️", 10.0, 5_000.0, 90.0, "Classic 90-ball bingo.", pinkRose, isPlayable = false),
        CasinoGame("powerball",     "Powerball",      CasinoCategory.LOTTERY, "🎱", 10.0,100_000.0, 85.0, "Huge jackpot lottery draw.", gold, isPlayable = false),
        CasinoGame("scratch",       "Scratch Cards",  CasinoCategory.LOTTERY, "💳",  5.0,  2_000.0, 92.0, "Instant scratch-and-win.", greenTeal, isPlayable = false),
    )

    fun byCategory(): Map<CasinoCategory, List<CasinoGame>> =
        games.groupBy { it.category }

    fun byId(id: String): CasinoGame? = games.firstOrNull { it.id == id }
}