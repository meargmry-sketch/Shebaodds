package com.example.data.model

import androidx.compose.ui.graphics.Color

// Matches backend enum: ['crash','classic','table','slots','sports','special']
enum class CasinoCategory(val label: String, val icon: String) {
    CRASH("Crash", "📈"),
    CLASSIC("Classic", "🎴"),
    TABLE("Table", "🎲"),
    SLOTS("Slots", "🎰"),
    SPORTS("Sports", "⚽"),
    SPECIAL("Special", "✨")
}

data class CasinoGame(
    val id: String,               // must equal backend gameId
    val name: String,
    val nameAm: String,           // from backend
    val category: CasinoCategory,
    val emoji: String,
    val minBet: Double,
    val maxBet: Double,
    val description: String,
    val gradient: List<Color>
)

object CasinoGamesCatalog {
    private val purpleBlue = listOf(Color(0xFF7C3AED), Color(0xFF2563EB))
    private val redOrange  = listOf(Color(0xFFDC2626), Color(0xFFF59E0B))
    private val greenTeal  = listOf(Color(0xFF059669), Color(0xFF14B8A6))
    private val pinkRose   = listOf(Color(0xFFDB2777), Color(0xFFF43F5E))
    private val gold       = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    private val blueCyan   = listOf(Color(0xFF0EA5E9), Color(0xFF22D3EE))
    private val slate      = listOf(Color(0xFF334155), Color(0xFF475569))

    // ⚠️ 1-to-1 mirror of CASINO_GAMES_DATA (51 games)
    val games: List<CasinoGame> = listOf(
        CasinoGame("dice", "Dice", "ዳይስ", CasinoCategory.TABLE, "🎲", 1.0, 10_000.0, "Roll over/under a target.", greenTeal),
        CasinoGame("aviator", "Aviator", "አቪዬተር", CasinoCategory.CRASH, "✈️", 1.0, 5_000.0, "Cash out before the plane flies away.", redOrange),
        CasinoGame("coinflip", "CoinFlip", "ሳንቲም", CasinoCategory.CRASH, "🪙", 1.0, 5_000.0, "Heads or tails — double up.", gold),
        CasinoGame("plinko", "Plinko", "ፕሊንኮ", CasinoCategory.CRASH, "📉", 1.0, 10_000.0, "Drop the ball, hit multipliers.", pinkRose),
        CasinoGame("blackjack", "Blackjack", "ብላክጃክ", CasinoCategory.CLASSIC, "🃏", 5.0, 10_000.0, "Beat the dealer to 21.", slate),
        CasinoGame("roulette", "Roulette", "ሩሌት", CasinoCategory.TABLE, "🎡", 1.0, 10_000.0, "European single-zero roulette.", redOrange),
        CasinoGame("mines", "Mines", "ማይንስ", CasinoCategory.CRASH, "💣", 1.0, 5_000.0, "Reveal gems, avoid mines.", purpleBlue),
        CasinoGame("crash", "Crash", "ክራሽ", CasinoCategory.CRASH, "📈", 1.0, 5_000.0, "Cash out before the crash.", redOrange),
        CasinoGame("tower", "Tower", "ግንብ", CasinoCategory.CLASSIC, "🏗️", 1.0, 5_000.0, "Climb the tower for higher multipliers.", slate),
        CasinoGame("keno", "Keno", "ኬኖ", CasinoCategory.SLOTS, "🔢", 1.0, 5_000.0, "Pick numbers, match the draw.", purpleBlue),
        CasinoGame("baccarat", "Baccarat", "ባካራት", CasinoCategory.TABLE, "♣️", 5.0, 10_000.0, "Player vs Banker.", gold),
        CasinoGame("wheel", "Wheel of Fortune", "የዕድል መንኮራኩር", CasinoCategory.TABLE, "🎰", 1.0, 5_000.0, "Spin the multiplier wheel.", redOrange),
        CasinoGame("hilo", "Hilo", "ሂሎ", CasinoCategory.CLASSIC, "⬆️⬇️", 1.0, 5_000.0, "Guess the next card higher or lower.", blueCyan),
        CasinoGame("sicbo", "Sic Bo", "ሲክቦ", CasinoCategory.TABLE, "🎲🎲🎲", 1.0, 10_000.0, "Three dice — bet on totals.", greenTeal),
        CasinoGame("videopoker", "Video Poker", "ቪዲዮ ፖከር", CasinoCategory.CLASSIC, "🃏", 5.0, 10_000.0, "Jack-or-Better draw poker.", purpleBlue),
        CasinoGame("bingo", "Bingo", "ቢንጎ", CasinoCategory.SLOTS, "🎯", 1.0, 5_000.0, "Classic bingo draw.", pinkRose),
        CasinoGame("craps", "Craps", "ክራፕስ", CasinoCategory.TABLE, "🎲", 1.0, 10_000.0, "Dice table classic.", slate),
        CasinoGame("dragontiger", "Dragon Tiger", "ድራጎን ታይገር", CasinoCategory.TABLE, "🐉🐯", 1.0, 10_000.0, "Dragon vs Tiger showdown.", redOrange),
        CasinoGame("andarbahar", "Andar Bahar", "አንዳር ባሃር", CasinoCategory.TABLE, "🃏", 1.0, 10_000.0, "Indian card game.", pinkRose),
        CasinoGame("teenpatti", "Teen Patti", "ቲን ፓቲ", CasinoCategory.CLASSIC, "♠️", 5.0, 10_000.0, "Indian 3-card poker.", gold),
        CasinoGame("lucky7", "Lucky 7", "ላኪ 7", CasinoCategory.SLOTS, "🍀7️⃣", 1.0, 5_000.0, "Match three sevens.", greenTeal),
        CasinoGame("scratch", "Scratch Card", "ስክራች ካርድ", CasinoCategory.SLOTS, "🎫", 1.0, 10_000.0, "Instant scratch-and-win.", pinkRose),
        CasinoGame("football", "Football Prediction", "እግር ኳስ ትንበያ", CasinoCategory.SPORTS, "⚽", 1.0, 10_000.0, "Predict match outcomes.", greenTeal),
        CasinoGame("basketball", "Basketball Prediction", "ቅርጫት ኳስ ትንበያ", CasinoCategory.SPORTS, "🏀", 1.0, 10_000.0, "Predict basketball winners.", redOrange),
        CasinoGame("horseracing", "Horse Racing", "ፈረስ እሽቅድምድም", CasinoCategory.SPORTS, "🐎", 1.0, 10_000.0, "Virtual horse races.", gold),
        CasinoGame("spinwin", "Spin & Win", "ደብል አሸንፍ", CasinoCategory.SPECIAL, "🌀", 1.0, 5_000.0, "Spin for random multipliers.", purpleBlue),
        CasinoGame("slot", "Slot Machine", "ስሎት ማሽን", CasinoCategory.SLOTS, "🎰", 1.0, 10_000.0, "Classic 3-reel slots.", purpleBlue),
        CasinoGame("reddog", "Red Dog", "ቀይ ውሻ", CasinoCategory.CLASSIC, "🐕", 1.0, 5_000.0, "Predict card spread.", pinkRose),
        CasinoGame("war", "War", "ጦርነት", CasinoCategory.TABLE, "⚔️", 1.0, 5_000.0, "High card wins.", slate),
        CasinoGame("paigow", "Pai Gow Poker", "ፓይ ጋው ፖከር", CasinoCategory.TABLE, "🀄️", 5.0, 10_000.0, "Chinese domino-poker.", redOrange),
        CasinoGame("diceduels", "Dice Duels", "ዳይስ ዱኤልስ", CasinoCategory.CRASH, "⚔️🎲", 1.0, 5_000.0, "Dice vs dealer.", greenTeal),
        CasinoGame("penalty", "Penalty", "ፍፃጎት ምት", CasinoCategory.SPORTS, "⚽", 1.0, 5_000.0, "Score the penalty.", greenTeal),
        CasinoGame("chickenroad", "Chicken Road", "ዶሮ መንገድ", CasinoCategory.CRASH, "🐔", 1.0, 5_000.0, "Cross the road, cash out.", gold),
        CasinoGame("chickenshot", "Chicken Shot", "ዶሮ ምት", CasinoCategory.CRASH, "🔫🐔", 1.0, 5_000.0, "Shot-multiplier round.", redOrange),
        CasinoGame("megaball", "Mega Ball", "ሜጋ ቦል", CasinoCategory.SLOTS, "⚾", 1.0, 5_000.0, "Draw mega numbers.", blueCyan),
        CasinoGame("pokerdice", "Poker Dice", "ፖከር ዳይስ", CasinoCategory.CLASSIC, "🎲", 1.0, 5_000.0, "Poker hand from five dice.", slate),
        CasinoGame("lightningdice", "Lightning Dice", "መብረቅ ዳይስ", CasinoCategory.CRASH, "⚡🎲", 1.0, 5_000.0, "Fast-round dice.", gold),
        CasinoGame("carroulette", "Car Roulette", "መኪና ሩሌት", CasinoCategory.TABLE, "🚗", 1.0, 10_000.0, "Cars race on roulette ring.", redOrange),
        CasinoGame("knockout", "Knock Out", "ናክ አውት", CasinoCategory.SPORTS, "🥊", 1.0, 10_000.0, "Boxing-style knockout game.", redOrange),
        CasinoGame("rummy", "Rummy", "ራሚ", CasinoCategory.CLASSIC, "🃏", 5.0, 10_000.0, "Classic rummy.", greenTeal),
        CasinoGame("darts", "Darts", "ዳርትስ", CasinoCategory.SPECIAL, "🎯", 1.0, 5_000.0, "Aim for the bullseye.", gold),
        CasinoGame("tennis", "Tennis", "ቴኒስ", CasinoCategory.SPORTS, "🎾", 1.0, 10_000.0, "Virtual tennis matches.", greenTeal),
        CasinoGame("baseball", "Baseball", "ቤዝቦል", CasinoCategory.SPORTS, "⚾", 1.0, 10_000.0, "Virtual baseball at-bats.", redOrange),
        CasinoGame("greyhound", "Greyhound Racing", "ግሬይሀውንድ እሽቅድምድም", CasinoCategory.SPORTS, "🐕‍🦺", 1.0, 10_000.0, "Virtual dog races.", slate),
        CasinoGame("motorbike", "Motorbike Racing", "ሞተር እሽቅድምድም", CasinoCategory.SPORTS, "🏍️", 1.0, 10_000.0, "Virtual bike racing.", pinkRose),
        CasinoGame("cricket", "Cricket", "ክሪኬት", CasinoCategory.SPORTS, "🏏", 1.0, 10_000.0, "Virtual cricket overs.", greenTeal),
        CasinoGame("roulette360", "Roulette 360", "ሩሌት 360", CasinoCategory.TABLE, "🎡", 1.0, 10_000.0, "Multi-wheel roulette.", redOrange),
        CasinoGame("megawheel", "Mega Wheel", "ሜጋ መንኮራኩር", CasinoCategory.TABLE, "🎡", 1.0, 10_000.0, "Big money wheel.", gold),
        CasinoGame("monopoly", "Monopoly", "ሞኖፖሊ", CasinoCategory.TABLE, "🎩", 1.0, 5_000.0, "Monopoly-themed wheel.", greenTeal),
        CasinoGame("virtualsports", "Virtual Sports", "ቨርቹዋል ስፖርት", CasinoCategory.SPORTS, "🎮", 1.0, 10_000.0, "Auto-generated sports events.", purpleBlue),
        CasinoGame("texasholdem", "Texas Hold'em", "ቴክሳስ ሆልደም", CasinoCategory.CLASSIC, "♠️", 5.0, 10_000.0, "Play vs the house.", slate),
    )

    // Composable router only needs to care about these 20 — everything else = ComingSoon
    val playableIds: Set<String> = setOf(
        "slot", "lucky7",
        "blackjack", "videopoker",
        "roulette", "roulette360",
        "dice", "craps", "pokerdice", "sicbo",
        "coinflip", "war",
        "wheel", "spinwin", "megawheel",
        "crash", "aviator", "chickenroad",
        "mines", "tower",
        "hilo", "reddog",
        "keno", "bingo",
        "baccarat"
    )

    fun byId(id: String): CasinoGame? = games.firstOrNull { it.id == id }
    fun byCategory(): Map<CasinoCategory, List<CasinoGame>> = games.groupBy { it.category }
}