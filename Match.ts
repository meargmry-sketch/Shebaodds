// ============================================
// SHEBAODDS - MATCH MODEL
// Complete Match Schema with All Markets (1xBet Style)
// ============================================

import mongoose, { Schema, Document, Model } from 'mongoose';

// Match Status Enum
export const MATCH_STATUS = {
  UPCOMING: 'upcoming',
  LIVE: 'live',
  HALFTIME: 'halftime',
  SECOND_HALF: 'second_half',
  EXTRA_TIME: 'extra_time',
  PENALTIES: 'penalties',
  FINISHED: 'finished',
  POSTPONED: 'postponed',
  CANCELLED: 'cancelled',
  SUSPENDED: 'suspended',
  ABANDONED: 'abandoned',
  AWARDED: 'awarded'
} as const;

export type MatchStatusType = typeof MATCH_STATUS[keyof typeof MATCH_STATUS];

// Market Types (Complete - 1xBet Style)
export const BET_MARKET_TYPES = {
  // ... (all existing market types)
} as const;

export interface IMatchEvent { /* ... */ }
export interface IMatchLineup { /* ... */ }
export interface IMatch extends Document { /* ... */ }
export interface IMatchModel extends Model<IMatch> { /* ... */ }

const matchSchema = new Schema<IMatch, IMatchModel>({
  // ... (all existing schema fields)
});

// ==================== INDEXES ====================
// ... (all existing indexes)

// ==================== VIRTUAL FIELDS ====================
// ... (all existing virtuals)

// ==================== INSTANCE METHODS ====================
// ... (all existing instance methods)

// ==================== STATIC METHODS ====================
// ... (all existing static methods)

export const Match = mongoose.models.Match || mongoose.model<IMatch, IMatchModel>('Match', matchSchema);

// ============================================================================
// 🎰 NEW: CASINO GAME MODEL (for 51+ Games)
// ============================================================================

export interface ICasinoGame extends Document {
  gameId: string;            // e.g., 'dice', 'aviator'
  name: string;
  nameAm: string;
  icon: string;
  category: string;          // 'crash', 'classic', 'table', 'slots', 'sports', 'special'
  minBet: number;
  maxBet: number;
  isFavorite: boolean;       // default false
  timesPlayed: number;       // default 0
  totalWagered: number;      // default 0
  totalWon: number;          // default 0
}

const casinoGameSchema = new Schema<ICasinoGame>({
  gameId: { type: String, required: true, unique: true, index: true },
  name: { type: String, required: true },
  nameAm: { type: String, required: true },
  icon: { type: String, required: true },
  category: { 
    type: String, 
    enum: ['crash', 'classic', 'table', 'slots', 'sports', 'special'], 
    required: true 
  },
  minBet: { type: Number, required: true, min: 1 },
  maxBet: { type: Number, required: true, min: 1 },
  isFavorite: { type: Boolean, default: false },
  timesPlayed: { type: Number, default: 0 },
  totalWagered: { type: Number, default: 0 },
  totalWon: { type: Number, default: 0 }
}, { timestamps: true });

export const CasinoGame = mongoose.models.CasinoGame || mongoose.model<ICasinoGame>('CasinoGame', casinoGameSchema);

// ============================================================================
// 🎰 CONSTANT: 51+ CASINO GAMES DATA
// ============================================================================

export const CASINO_GAMES_DATA = [
  { gameId: 'dice', name: 'Dice', nameAm: 'ዳይስ', icon: '🎲', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'aviator', name: 'Aviator', nameAm: 'አቪዬተር', icon: '✈️', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'coinflip', name: 'CoinFlip', nameAm: 'ሳንቲም', icon: '🪙', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'plinko', name: 'Plinko', nameAm: 'ፕሊንኮ', icon: '📉', category: 'crash', minBet: 1, maxBet: 10000 },
  { gameId: 'blackjack', name: 'Blackjack', nameAm: 'ብላክጃክ', icon: '🃏', category: 'classic', minBet: 5, maxBet: 10000 },
  { gameId: 'roulette', name: 'Roulette', nameAm: 'ሩሌት', icon: '🎡', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'mines', name: 'Mines', nameAm: 'ማይንስ', icon: '💣', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'crash', name: 'Crash', nameAm: 'ክራሽ', icon: '📈', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'tower', name: 'Tower', nameAm: 'ግንብ', icon: '🏗️', category: 'classic', minBet: 1, maxBet: 5000 },
  { gameId: 'keno', name: 'Keno', nameAm: 'ኬኖ', icon: '🔢', category: 'slots', minBet: 1, maxBet: 5000 },
  { gameId: 'baccarat', name: 'Baccarat', nameAm: 'ባካራት', icon: '♣️', category: 'table', minBet: 5, maxBet: 10000 },
  { gameId: 'wheel', name: 'Wheel of Fortune', nameAm: 'የዕድል መንኮራኩር', icon: '🎰', category: 'table', minBet: 1, maxBet: 5000 },
  { gameId: 'hilo', name: 'Hilo', nameAm: 'ሂሎ', icon: '⬆️⬇️', category: 'classic', minBet: 1, maxBet: 5000 },
  { gameId: 'sicbo', name: 'Sic Bo', nameAm: 'ሲክቦ', icon: '🎲🎲🎲', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'videopoker', name: 'Video Poker', nameAm: 'ቪዲዮ ፖከር', icon: '🃏', category: 'classic', minBet: 5, maxBet: 10000 },
  { gameId: 'bingo', name: 'Bingo', nameAm: 'ቢንጎ', icon: '🎯', category: 'slots', minBet: 1, maxBet: 5000 },
  { gameId: 'craps', name: 'Craps', nameAm: 'ክራፕስ', icon: '🎲', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'dragontiger', name: 'Dragon Tiger', nameAm: 'ድራጎን ታይገር', icon: '🐉🐯', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'andarbahar', name: 'Andar Bahar', nameAm: 'አንዳር ባሃር', icon: '🃏', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'teenpatti', name: 'Teen Patti', nameAm: 'ቲን ፓቲ', icon: '♠️', category: 'classic', minBet: 5, maxBet: 10000 },
  { gameId: 'lucky7', name: 'Lucky 7', nameAm: 'ላኪ 7', icon: '🍀7️⃣', category: 'slots', minBet: 1, maxBet: 5000 },
  { gameId: 'scratch', name: 'Scratch Card', nameAm: 'ስክራች ካርድ', icon: '🎫', category: 'slots', minBet: 1, maxBet: 10000 },
  { gameId: 'football', name: 'Football Prediction', nameAm: 'እግር ኳስ ትንበያ', icon: '⚽', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'basketball', name: 'Basketball Prediction', nameAm: 'ቅርጫት ኳስ ትንበያ', icon: '🏀', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'horseracing', name: 'Horse Racing', nameAm: 'ፈረስ እሽቅድምድም', icon: '🐎', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'spinwin', name: 'Spin & Win', nameAm: 'ደብል አሸንፍ', icon: '🌀', category: 'special', minBet: 1, maxBet: 5000 },
  { gameId: 'slot', name: 'Slot Machine', nameAm: 'ስሎት ማሽን', icon: '🎰', category: 'slots', minBet: 1, maxBet: 10000 },
  { gameId: 'reddog', name: 'Red Dog', nameAm: 'ቀይ ውሻ', icon: '🐕', category: 'classic', minBet: 1, maxBet: 5000 },
  { gameId: 'war', name: 'War', nameAm: 'ጦርነት', icon: '⚔️', category: 'table', minBet: 1, maxBet: 5000 },
  { gameId: 'paigow', name: 'Pai Gow Poker', nameAm: 'ፓይ ጋው ፖከር', icon: '🀄️', category: 'table', minBet: 5, maxBet: 10000 },
  { gameId: 'diceduels', name: 'Dice Duels', nameAm: 'ዳይስ ዱኤልስ', icon: '⚔️🎲', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'penalty', name: 'Penalty', nameAm: 'ፍፃጎት ምት', icon: '⚽', category: 'sports', minBet: 1, maxBet: 5000 },
  { gameId: 'chickenroad', name: 'Chicken Road', nameAm: 'ዶሮ መንገድ', icon: '🐔', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'chickenshot', name: 'Chicken Shot', nameAm: 'ዶሮ ምት', icon: '🔫🐔', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'megaball', name: 'Mega Ball', nameAm: 'ሜጋ ቦል', icon: '⚾', category: 'slots', minBet: 1, maxBet: 5000 },
  { gameId: 'pokerdice', name: 'Poker Dice', nameAm: 'ፖከር ዳይስ', icon: '🎲', category: 'classic', minBet: 1, maxBet: 5000 },
  { gameId: 'lightningdice', name: 'Lightning Dice', nameAm: 'መብረቅ ዳይስ', icon: '⚡🎲', category: 'crash', minBet: 1, maxBet: 5000 },
  { gameId: 'carroulette', name: 'Car Roulette', nameAm: 'መኪና ሩሌት', icon: '🚗', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'knockout', name: 'Knock Out', nameAm: 'ናክ አውት', icon: '🥊', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'rummy', name: 'Rummy', nameAm: 'ራሚ', icon: '🃏', category: 'classic', minBet: 5, maxBet: 10000 },
  { gameId: 'darts', name: 'Darts', nameAm: 'ዳርትስ', icon: '🎯', category: 'special', minBet: 1, maxBet: 5000 },
  { gameId: 'tennis', name: 'Tennis', nameAm: 'ቴኒስ', icon: '🎾', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'baseball', name: 'Baseball', nameAm: 'ቤዝቦል', icon: '⚾', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'greyhound', name: 'Greyhound Racing', nameAm: 'ግሬይሀውንድ እሽቅድምድም', icon: '🐕‍🦺', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'motorbike', name: 'Motorbike Racing', nameAm: 'ሞተር እሽቅድምድም', icon: '🏍️', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'cricket', name: 'Cricket', nameAm: 'ክሪኬት', icon: '🏏', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'roulette360', name: 'Roulette 360', nameAm: 'ሩሌት 360', icon: '🎡', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'megawheel', name: 'Mega Wheel', nameAm: 'ሜጋ መንኮራኩር', icon: '🎡', category: 'table', minBet: 1, maxBet: 10000 },
  { gameId: 'monopoly', name: 'Monopoly', nameAm: 'ሞኖፖሊ', icon: '🎩', category: 'table', minBet: 1, maxBet: 5000 },
  { gameId: 'virtualsports', name: 'Virtual Sports', nameAm: 'ቨርቹዋል ስፖርት', icon: '🎮', category: 'sports', minBet: 1, maxBet: 10000 },
  { gameId: 'texasholdem', name: 'Texas Hold\'em', nameAm: 'ቴክሳስ ሆልደም', icon: '♠️', category: 'classic', minBet: 5, maxBet: 10000 }
];

// Helper function to seed casino games into the DB
export async function seedCasinoGames() {
  const existing = await CasinoGame.countDocuments();
  if (existing === 0) {
    await CasinoGame.insertMany(CASINO_GAMES_DATA);
    console.log('✅ Seeded 51+ casino games.');
  }
}

export default Match;