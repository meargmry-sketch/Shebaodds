import express, { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import mongoose, { Schema, Document } from 'mongoose';
import bcrypt from 'bcryptjs';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { validatePasswordStrength } from './passwordValidator';
import { Wallet, Wager } from './MongoDBWalletEngine';
import { JackpotPool, JackpotTicket } from './jackpotSchema';
import { VIPTier } from './vipTierSchema';
import { getCurrentEthiopiaTime, formatMatchTime } from './timezoneConfig';

// ==============================================================================
// 🔐 SCHEMA & DATA MODEL DEFINITIONS
// ==============================================================================

export enum UserRole {
  PLAYER = 'Player',
  AGENT = 'Agent',
  MASTER_ADMIN = 'SuperAdmin'
}

interface IUserProfile extends Document {
  userId: string;
  email: string;
  passwordHash: string;
  fullName: string;
  phone: string;
  role: UserRole;
  preferredLanguage: string;
  isFlagged: boolean;
  nationalIdNumber?: string;
  deviceHardwareHash?: string;
}

const UserProfileSchema = new Schema<IUserProfile>({
  userId: { type: String, required: true, unique: true, index: true },
  email: { type: String, required: true, unique: true },
  passwordHash: { type: String, required: true },
  fullName: { type: String, required: true },
  phone: { type: String, required: true },
  role: { type: String, enum: Object.values(UserRole), default: UserRole.PLAYER, required: true },
  preferredLanguage: { type: String, default: 'am', required: true },
  isFlagged: { type: Boolean, default: false, required: true },
  nationalIdNumber: { type: String },
  deviceHardwareHash: { type: String }
}, { timestamps: true });

export const UserProfileModel = mongoose.models.UserProfile || mongoose.model<IUserProfile>('UserProfile', UserProfileSchema);

// --- Match Schema (Sports Feed) ---
interface IMatch extends Document {
  id: number;
  homeTeam: string;
  awayTeam: string;
  status: 'Scheduled' | 'Live' | 'Finished';
  homeScore: number;
  awayScore: number;
  minute: number;
  commenceTime: Date;
  sport: string;
  league: string;
  odds: {
    homeWin: number;
    draw: number;
    awayWin: number;
    over25: number;
    under25: number;
    bttsYes: number;
    bttsNo: number;
  };
}

const MatchSchema = new Schema<IMatch>({
  id: { type: Number, required: true, unique: true },
  homeTeam: { type: String, required: true },
  awayTeam: { type: String, required: true },
  status: { type: String, enum: ['Scheduled', 'Live', 'Finished'], default: 'Scheduled' },
  homeScore: { type: Number, default: 0 },
  awayScore: { type: Number, default: 0 },
  minute: { type: Number, default: 0 },
  commenceTime: { type: Date, required: true },
  sport: { type: String, default: 'Football' },
  league: { type: String, required: true },
  odds: {
    homeWin: { type: Number, required: true },
    draw: { type: Number, required: true },
    awayWin: { type: Number, required: true },
    over25: { type: Number, required: true },
    under25: { type: Number, required: true },
    bttsYes: { type: Number, required: true },
    bttsNo: { type: Number, required: true }
  }
}, { timestamps: true });

export const MatchModel = mongoose.models.Match || mongoose.model<IMatch>('Match', MatchSchema);

// --- Bet / Slip Schema ---
interface ISelection {
  matchId: number;
  homeTeam: string;
  awayTeam: string;
  market: string;
  selection: string;
  odds: number;
  status: 'Pending' | 'Won' | 'Lost' | 'Void';
}

interface IBet extends Document {
  userId: string;
  ticketId: string;
  selections: ISelection[];
  stake: number;
  odds: number;
  potentialPayout: number;
  taxDeducted: number;
  netPayout: number;
  slipType: 'Single' | 'Accumulator' | 'System';
  status: 'Pending' | 'Won' | 'Lost' | 'Void' | 'CashedOut';
}

const SelectionSchema = new Schema<ISelection>({
  matchId: { type: Number, required: true },
  homeTeam: { type: String, required: true },
  awayTeam: { type: String, required: true },
  market: { type: String, required: true },
  selection: { type: String, required: true },
  odds: { type: Number, required: true },
  status: { type: String, enum: ['Pending', 'Won', 'Lost', 'Void'], default: 'Pending' }
});

const BetSchema = new Schema<IBet>({
  userId: { type: String, required: true, index: true },
  ticketId: { type: String, required: true, unique: true, index: true },
  selections: [SelectionSchema],
  stake: { type: Number, required: true },
  odds: { type: Number, required: true },
  potentialPayout: { type: Number, required: true },
  taxDeducted: { type: Number, default: 0 },
  netPayout: { type: Number, required: true },
  slipType: { type: String, enum: ['Single', 'Accumulator', 'System'], required: true },
  status: { type: String, enum: ['Pending', 'Won', 'Lost', 'Void', 'CashedOut'], default: 'Pending' }
}, { timestamps: true });

export const BetModel = mongoose.models.Bet || mongoose.model<IBet>('Bet', BetSchema);

// --- Transaction Schema ---
interface ITransaction extends Document {
  transactionId: string;
  userId: string;
  amount: number;
  type: 'Deposit' | 'Withdrawal' | 'Bet_Stake' | 'Bet_Payout' | 'Jackpot_Entry' | 'Jackpot_Payout' | 'VIP_Cashback';
  paymentGateway: 'TeleBirr' | 'CBE_Birr' | 'Chapa' | 'Stripe' | 'Internal';
  status: 'Pending' | 'Success' | 'Failed';
  reference: string;
}

const TransactionSchema = new Schema<ITransaction>({
  transactionId: { type: String, required: true, unique: true, index: true },
  userId: { type: String, required: true, index: true },
  amount: { type: Number, required: true },
  type: { type: String, enum: ['Deposit', 'Withdrawal', 'Bet_Stake', 'Bet_Payout', 'Jackpot_Entry', 'Jackpot_Payout', 'VIP_Cashback'], required: true },
  paymentGateway: { type: String, enum: ['TeleBirr', 'CBE_Birr', 'Chapa', 'Stripe', 'Internal'], default: 'Internal' },
  status: { type: String, enum: ['Pending', 'Success', 'Failed'], default: 'Success' },
  reference: { type: String, required: true }
}, { timestamps: true });

export const TransactionModel = mongoose.models.Transaction || mongoose.model<ITransaction>('Transaction', TransactionSchema);

// ==============================================================================
// 🎰 NEW: CASINO GAME MODEL & DATA
// ==============================================================================

interface ICasinoGame extends Document {
  gameId: string;          // e.g., 'dice', 'aviator'
  name: string;
  nameAm: string;
  icon: string;
  category: string;       // 'crash', 'classic', 'table', 'slots', 'sports', 'special'
  minBet: number;
  maxBet: number;
  isFavorite: boolean;
  timesPlayed: number;
  totalWagered: number;
  totalWon: number;
}

const CasinoGameSchema = new Schema<ICasinoGame>({
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

export const CasinoGameModel = mongoose.models.CasinoGame || mongoose.model<ICasinoGame>('CasinoGame', CasinoGameSchema);

// 51+ Casino games static data for seeding
const CASINO_GAMES_DATA = [
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

// Seed casino games if collection empty
const ensureCasinoGamesSeeded = async () => {
  const count = await CasinoGameModel.countDocuments();
  if (count === 0) {
    await CasinoGameModel.insertMany(CASINO_GAMES_DATA);
    console.log('✅ Seeded 51+ casino games.');
  }
};

// ==============================================================================
// 🛡️ JWT & RBAC SECURITY MIDDLEWARES
// ==============================================================================

export interface AuthenticatedRequest extends Request {
  user?: {
    userId: string;
    email: string;
    role: UserRole;
  };
}

const JWT_SECRET = process.env.JWT_SECRET || 'sheba_odds_jwt_secret_high_entropy_fallback_token_99812';

export function authenticateToken(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      success: false,
      error: 'Access Token Missing',
      message: 'Authentication bearer token is required to access this endpoint.'
    });
  }

  jwt.verify(token, JWT_SECRET, (err, decoded: any) => {
    if (err) {
      return res.status(403).json({
        success: false,
        error: 'Invalid Access Token',
        message: 'The token provided is expired, malformed, or structurally invalid.'
      });
    }

    req.user = {
      userId: decoded.userId,
      email: decoded.email,
      role: decoded.role as UserRole
    };
    next();
  });
}

export function authorizeRoles(...allowedRoles: UserRole[]) {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: 'Unauthenticated Request',
        message: 'Identity verification credentials were not located in the processing context.'
      });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        error: 'Access Denied',
        message: `Your current security role (${req.user.role}) is insufficient to invoke this procedure. Required tiers: [${allowedRoles.join(', ')}]`
      });
    }

    next();
  };
}


// ==============================================================================
// 🚀 UNIFIED EXPRESS API GATEWAY ROUTER
// ==============================================================================

const router = express.Router();

// Mock Redis stream / memory fallback cache
const localMemoryCache = new Map<string, { value: string; expiresAt: number }>();

const cacheService = {
  get: async (key: string): Promise<string | null> => {
    const item = localMemoryCache.get(key);
    if (!item) return null;
    if (Date.now() > item.expiresAt) {
      localMemoryCache.delete(key);
      return null;
    }
    return item.value;
  },
  setEx: async (key: string, seconds: number, value: string): Promise<void> => {
    localMemoryCache.set(key, {
      value,
      expiresAt: Date.now() + (seconds * 1000)
    });
  },
  del: async (key: string): Promise<void> => {
    localMemoryCache.delete(key);
  }
};

// Seed dynamic matches in-memory if MongoDB collection is empty
const ensureMatchesSeeded = async () => {
  const count = await MatchModel.countDocuments();
  if (count === 0) {
    const sampleMatches = [
      {
        id: 1001,
        homeTeam: 'Ethiopia Coffee',
        awayTeam: 'Saint George SA',
        status: 'Live',
        homeScore: 2,
        awayScore: 1,
        minute: 67,
        commenceTime: new Date(Date.now() - 3600000),
        sport: 'Football',
        league: 'Ethiopian Premier League',
        odds: { homeWin: 2.10, draw: 3.10, awayWin: 3.40, over25: 1.95, under25: 1.80, bttsYes: 1.70, bttsNo: 2.05 }
      },
      // ... (keep your existing sample matches)
    ];
    await MatchModel.insertMany(sampleMatches);
  }
};

// ==================== AUTHENTICATION PORT ====================

// (Existing auth routes remain unchanged – POST /auth/register, POST /auth/login, POST /auth/biometric-login, GET /player/profile)
// ... (kept as is)

// ==================== SPORTSBOOK MATCHES FEEDS ====================

// (Existing sports routes remain unchanged – GET /sports/matches, GET /sports/live-odds)

// ==================== BETTING & WAGERING MODULE ====================

// (Existing betting routes remain unchanged – POST /player/wager, GET /player/bets, POST /player/cashout)

// ==================== JACKPOT CENTER SUBSYSTEM ====================

// (Existing jackpot routes remain unchanged – GET /jackpot/pools, POST /jackpot/buy-ticket, POST /admin/jackpot/settle)

// ==================== WALLET & BANK DEPOSITS/WITHDRAWALS ====================

// (Existing wallet routes remain unchanged – POST /wallet/deposit, POST /wallet/withdraw, GET /wallet/transactions)

// ==================== VIP PROGRAM SUB-SYSTEM ====================

// (Existing VIP routes remain unchanged – GET /vip/status, POST /vip/claim-cashback)

// ==================== STATUTORY TAX COMPLIANCE CENTER ====================

// (Existing tax routes remain unchanged – GET /tax/report, POST /tax/remit)

// ==================== AI CHATBOT & SPORTS TIPSTER SUPPORT ====================

// (Existing AI chat route remains unchanged – POST /support/chat)

// ==================== SYSTEM LEVEL CONFIGURATIONS & LOCALIZATION ====================

// (Existing branding & localization routes remain unchanged – GET /branding, GET /localization, GET /timezone)

// ==============================================================================
// 🎰 NEW: CASINO GAMES ROUTES
// ==============================================================================

/**
 * GET /casino/games
 * Fetch all casino games (optionally filtered by category)
 */
router.get('/casino/games', async (req: Request, res: Response) => {
  try {
    await ensureCasinoGamesSeeded();
    const { category } = req.query;
    const query: any = {};
    if (category) query.category = category;

    const cacheKey = `casino_games:${JSON.stringify(req.query)}`;
    const cached = await cacheService.get(cacheKey);
    if (cached) {
      return res.status(200)
        .header('X-Cache-Status', 'HIT')
        .json(JSON.parse(cached));
    }

    const games = await CasinoGameModel.find(query).sort({ name: 1 });
    const response = { success: true, games };
    await cacheService.setEx(cacheKey, 60, JSON.stringify(response));

    return res.status(200)
      .header('X-Cache-Status', 'MISS')
      .json(response);
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /casino/games/:id
 * Fetch a single casino game by ID
 */
router.get('/casino/games/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const cacheKey = `casino_game:${id}`;
    const cached = await cacheService.get(cacheKey);
    if (cached) {
      return res.status(200)
        .header('X-Cache-Status', 'HIT')
        .json(JSON.parse(cached));
    }

    const game = await CasinoGameModel.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, error: 'Casino game not found' });
    }

    const response = { success: true, game };
    await cacheService.setEx(cacheKey, 60, JSON.stringify(response));

    return res.status(200)
      .header('X-Cache-Status', 'MISS')
      .json(response);
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * PUT /casino/games/:id/favorite
 * Toggle favorite status for a casino game (authenticated users)
 */
router.put('/casino/games/:id/favorite', authenticateToken, async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { isFavorite } = req.body;

    if (typeof isFavorite !== 'boolean') {
      return res.status(400).json({ success: false, error: 'isFavorite must be a boolean' });
    }

    const game = await CasinoGameModel.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, error: 'Casino game not found' });
    }

    game.isFavorite = isFavorite;
    await game.save();

    // Clear caches
    await cacheService.del(`casino_game:${id}`);
    await cacheService.del('casino_games:*');

    return res.json({ success: true, game });
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /casino/stats
 * Get platform‑wide casino statistics
 */
router.get('/casino/stats', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'casino_stats';
    const cached = await cacheService.get(cacheKey);
    if (cached) {
      return res.status(200)
        .header('X-Cache-Status', 'HIT')
        .json(JSON.parse(cached));
    }

    const [totalGames, totalWagered, totalWon, mostPlayed] = await Promise.all([
      CasinoGameModel.countDocuments(),
      CasinoGameModel.aggregate([{ $group: { _id: null, total: { $sum: '$totalWagered' } } }]),
      CasinoGameModel.aggregate([{ $group: { _id: null, total: { $sum: '$totalWon' } } }]),
      CasinoGameModel.find().sort({ timesPlayed: -1 }).limit(5).select('gameId name icon timesPlayed')
    ]);

    const stats = {
      totalGames,
      totalWagered: totalWagered[0]?.total || 0,
      totalWon: totalWon[0]?.total || 0,
      mostPlayed
    };

    const response = { success: true, stats };
    await cacheService.setEx(cacheKey, 300, JSON.stringify(response));

    return res.status(200)
      .header('X-Cache-Status', 'MISS')
      .json(response);
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * POST /casino/admin/games/:id/stats
 * Admin: manually update a game's stats
 */
router.post('/casino/admin/games/:id/stats', authenticateToken, authorizeRoles(UserRole.MASTER_ADMIN), async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { timesPlayed, totalWagered, totalWon } = req.body;

    const game = await CasinoGameModel.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, error: 'Casino game not found' });
    }

    if (timesPlayed !== undefined) game.timesPlayed = timesPlayed;
    if (totalWagered !== undefined) game.totalWagered = totalWagered;
    if (totalWon !== undefined) game.totalWon = totalWon;

    await game.save();

    // Clear caches
    await cacheService.del(`casino_game:${id}`);
    await cacheService.del('casino_games:*');
    await cacheService.del('casino_stats');

    return res.json({ success: true, game });
  } catch (err: any) {
    return res.status(500).json({ success: false, error: err.message });
  }
});

// ==============================================================================
// EXPORT
// ==============================================================================

export default router;