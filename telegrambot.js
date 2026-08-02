// ============================================
// SHEBAODDS - TELEGRAM BOT
// Enterprise Backend Integration
// 85% Black / 15% Gold | Smart Bets. Real Wins.
// ============================================

const TelegramBot = require('node-telegram-bot-api');
const express = require('express');
const mongoose = require('mongoose');
const axios = require('axios');
const crypto = require('crypto');
const moment = require('moment-timezone');
const winston = require('winston');
const { createClient } = require('redis');

require('dotenv').config();

// ─── CONFIGURATION ───
const config = {
  botToken: process.env.BOT_TOKEN || '8792216823:AAE3-QD5nA4-IvoCqktFtUhOSVqSr5f66T4',
  adminId: parseInt(process.env.ADMIN_ID) || 5853555436,
  webappUrl: process.env.WEBAPP_URL || 'https://meargmry-sketch.github.io/ShebaOdds-Bot/',
  apiUrl: process.env.API_URL || 'http://localhost:3000',
  mongoUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/shebaodds',
  redisUrl: process.env.REDIS_URL || 'redis://localhost:6379',
  port: process.env.BOT_PORT || 3001,
  environment: process.env.NODE_ENV || 'development'
};

// ─── LOGGER ───
const logger = winston.createLogger({
  level: config.environment === 'production' ? 'info' : 'debug',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' }),
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple()
      )
    })
  ]
});

// ─── REDIS CLIENT ───
let redis;
try {
  redis = createClient({ url: config.redisUrl });
  redis.on('error', (err) => logger.error('Redis error:', err));
  redis.connect();
} catch (error) {
  logger.warn('Redis not available, using in-memory cache');
  redis = {
    get: async () => null,
    set: async () => {},
    del: async () => {},
    incr: async () => {},
    expire: async () => {}
  };
}

// ─── MONGOOSE CONNECTION ───
let User, Wallet, Transaction, Bet;

const connectDB = async () => {
  try {
    await mongoose.connect(config.mongoUri, {
      useNewUrlParser: true,
      useUnifiedTopology: true
    });
    logger.info('MongoDB connected');

    // ─── MODELS ───
    const UserSchema = new mongoose.Schema({
      telegramId: { type: String, unique: true, sparse: true },
      email: { type: String, required: true, unique: true, lowercase: true },
      firstName: { type: String, required: true },
      lastName: { type: String },
      username: { type: String, unique: true, sparse: true },
      role: { type: String, enum: ['user', 'admin', 'superadmin'], default: 'user' },
      isActive: { type: Boolean, default: true },
      isBanned: { type: Boolean, default: false },
      vip: {
        level: { type: Number, default: 1 },
        points: { type: Number, default: 0 },
        tier: { type: String, enum: ['bronze', 'silver', 'gold', 'platinum', 'diamond', 'black'], default: 'bronze' }
      },
      settings: {
        language: { type: String, enum: ['en', 'am'], default: 'en' },
        currency: { type: String, default: 'ETB' },
        notifications: { type: Boolean, default: true }
      },
      referralCode: { type: String, unique: true },
      referredBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
      lastActive: { type: Date, default: Date.now },
      totalBets: { type: Number, default: 0 },
      totalWins: { type: Number, default: 0 },
      totalWagered: { type: Number, default: 0 }
    }, { timestamps: true });

    const WalletSchema = new mongoose.Schema({
      userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, unique: true },
      balance: { type: Number, default: 0, min: 0 },
      bonusBalance: { type: Number, default: 0, min: 0 },
      lockedBalance: { type: Number, default: 0, min: 0 },
      currency: { type: String, default: 'ETB' },
      totalDeposited: { type: Number, default: 0 },
      totalWithdrawn: { type: Number, default: 0 },
      totalWon: { type: Number, default: 0 },
      totalLost: { type: Number, default: 0 },
      totalTaxPaid: { type: Number, default: 0 },
      dailyDeposits: { type: Number, default: 0 },
      dailyWithdraws: { type: Number, default: 0 },
      dailyWagered: { type: Number, default: 0 },
      lastDailyReset: { type: Date, default: Date.now },
      activeBonuses: [{
        type: { type: String, enum: ['welcome', 'deposit', 'cashback', 'referral', 'vip'] },
        amount: Number,
        wageringRequirement: Number,
        wageredAmount: Number,
        expiresAt: Date,
        status: { type: String, enum: ['active', 'completed', 'expired'], default: 'active' }
      }]
    }, { timestamps: true });

    const TransactionSchema = new mongoose.Schema({
      userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
      type: { type: String, enum: ['deposit', 'withdrawal', 'bet', 'win', 'bonus', 'tax', 'fee', 'game'], required: true },
      amount: { type: Number, required: true },
      oldBalance: { type: Number, required: true },
      newBalance: { type: Number, required: true },
      currency: { type: String, default: 'ETB' },
      status: { type: String, enum: ['pending', 'completed', 'failed', 'cancelled'], default: 'pending' },
      reference: { type: String, unique: true },
      metadata: { type: Map, of: mongoose.Schema.Types.Mixed },
      description: { type: String },
      completedAt: { type: Date }
    }, { timestamps: true });

    const BetSchema = new mongoose.Schema({
      userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
      gameId: { type: String, required: true },
      gameName: { type: String, required: true },
      amount: { type: Number, required: true },
      result: { type: String, enum: ['win', 'lose', 'push', 'pending'], default: 'pending' },
      profit: { type: Number, default: 0 },
      multiplier: { type: Number, default: 1 },
      details: { type: Map, of: mongoose.Schema.Types.Mixed },
      status: { type: String, enum: ['pending', 'completed', 'cancelled'], default: 'pending' },
      completedAt: { type: Date }
    }, { timestamps: true });

    User = mongoose.model('User', UserSchema);
    Wallet = mongoose.model('Wallet', WalletSchema);
    Transaction = mongoose.model('Transaction', TransactionSchema);
    Bet = mongoose.model('Bet', BetSchema);

  } catch (error) {
    logger.error('MongoDB connection error:', error);
  }
};

connectDB();

// ─── BOT INITIALIZATION ───
const bot = new TelegramBot(config.botToken, {
  polling: true,
  filepath: false,
  onlyFirstMatch: true
});

// ─── EXPRESS SERVER ───
const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ─── GAME DATA ───
const GAME_CATEGORIES = {
  crash: { icon: '✈️', label: 'Crash Games' },
  classic: { icon: '♠️', label: 'Classic Games' },
  table: { icon: '🎲', label: 'Table Games' },
  slots: { icon: '🎰', label: 'Slots' },
  sports: { icon: '⚽', label: 'Sports' },
  special: { icon: '✨', label: 'Special' }
};

const GAMES = [
  { id: 'dice', name: 'Dice', nameAm: 'ዳይስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'aviator', name: 'Aviator', nameAm: 'አቪዬተር', icon: '✈️', cat: 'crash', minBet: 1, maxBet: 5000 },
  { id: 'coinflip', name: 'CoinFlip', nameAm: 'ሳንቲም', icon: '🪙', cat: 'crash', minBet: 1, maxBet: 5000 },
  { id: 'plinko', name: 'Plinko', nameAm: 'ፕሊንኮ', icon: '🔴', cat: 'crash', minBet: 1, maxBet: 10000 },
  { id: 'blackjack', name: 'Blackjack', nameAm: 'ብላክጃክ', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
  { id: 'roulette', name: 'Roulette', nameAm: 'ሩሌት', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'mines', name: 'Mines', nameAm: 'ማዕድን', icon: '💣', cat: 'crash', minBet: 1, maxBet: 5000 },
  { id: 'crash', name: 'Crash', nameAm: 'ክራሽ', icon: '📈', cat: 'crash', minBet: 1, maxBet: 5000 },
  { id: 'tower', name: 'Tower', nameAm: 'ግንብ', icon: '🏗️', cat: 'classic', minBet: 1, maxBet: 5000 },
  { id: 'keno', name: 'Keno', nameAm: 'ኬኖ', icon: '🎯', cat: 'slots', minBet: 1, maxBet: 5000 },
  { id: 'baccarat', name: 'Baccarat', nameAm: 'ባካራት', icon: '💎', cat: 'table', minBet: 5, maxBet: 10000 },
  { id: 'wheel', name: 'Wheel of Fortune', nameAm: 'የእድል መንኮራኩር', icon: '🎡', cat: 'table', minBet: 1, maxBet: 5000 },
  { id: 'hilo', name: 'Hilo', nameAm: 'ሂሎ', icon: '⬆️', cat: 'classic', minBet: 1, maxBet: 5000 },
  { id: 'sicbo', name: 'Sic Bo', nameAm: 'ሲክቦ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'videopoker', name: 'Video Poker', nameAm: 'ቪዲዮ ፖከር', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
  { id: 'bingo', name: 'Bingo', nameAm: 'ቢንጎ', icon: '🎱', cat: 'slots', minBet: 1, maxBet: 5000 },
  { id: 'craps', name: 'Craps', nameAm: 'ክራፕስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'dragontiger', name: 'Dragon Tiger', nameAm: 'ድራጎን ነብር', icon: '🐉', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'andarbahar', name: 'Andar Bahar', nameAm: 'አንዳር ባሃር', icon: '🃏', cat: 'table', minBet: 1, maxBet: 10000 },
  { id: 'teenpatti', name: 'Teen Patti', nameAm: 'ቲን ፓቲ', icon: '♠️', cat: 'classic', minBet: 5, maxBet: 10000 },
  { id: 'lucky7', name: 'Lucky 7', nameAm: 'እድለኛ 7', icon: '7️⃣', cat: 'slots', minBet: 1, maxBet: 5000 },
  { id: 'scratch', name: 'Scratch Card', nameAm: 'ስክራች ካርድ', icon: '🪙', cat: 'slots', minBet: 1, maxBet: 1000 },
  { id: 'football', name: 'Football Prediction', nameAm: 'እግር ኳስ ትንበያ', icon: '⚽', cat: 'sports', minBet: 1, maxBet: 10000 },
  { id: 'basketball', name: 'Basketball Prediction', nameAm: 'ቅርጫት ኳስ ትንበያ', icon: '🏀', cat: 'sports', minBet: 1, maxBet: 10000 },
  { id: 'horseracing', name: 'Horse Racing', nameAm: 'የፈረስ እሽቅድምድም', icon: '🐎', cat: 'sports', minBet: 1, maxBet: 10000 },
  { id: 'spinwin', name: 'Spin & Win', nameAm: 'ደብል አሸንፍ', icon: '🎡', cat: 'special', minBet: 1, maxBet: 5000 },
  { id: 'slot', name: 'Slot Machine', nameAm: 'ስሎት ማሽን', icon: '🎰', cat: 'slots', minBet: 1, maxBet: 10000 },
  { id: 'reddog', name: 'Red Dog', nameAm: 'ቀይ ውሻ', icon: '🃏', cat: 'classic', minBet: 1, maxBet: 5000 },
  { id: 'war', name: 'War', nameAm: 'ጦርነት', icon: '⚔️', cat: 'table', minBet: 1, maxBet: 5000 },
  { id: 'paigow', name: 'Pai Gow Poker', nameAm: 'ፓይ ጎው ፖከር', icon: '🀄', cat: 'table',