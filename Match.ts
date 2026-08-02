// ============================================
// SHEBAODDS - MATCH MODEL
// Complete Match Schema with All Markets (1xBet Style)
// ============================================

import mongoose, { Schema, Document, Model } from 'mongoose';

// ===================== ENUMS =====================

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

// 1xBet style market types (complete list)
export const BET_MARKET_TYPES = {
  // Main markets
  MATCH_WINNER: 'match_winner',
  DOUBLE_CHANCE: 'double_chance',
  DRAW_NO_BET: 'draw_no_bet',
  // Total goals
  TOTAL_GOALS: 'total_goals',
  TOTAL_GOALS_OVER_UNDER: 'total_goals_over_under',
  TOTAL_GOALS_ODD_EVEN: 'total_goals_odd_even',
  // Handicap
  ASIAN_HANDICAP: 'asian_handicap',
  EUROPEAN_HANDICAP: 'european_handicap',
  // Both teams to score
  BOTH_TEAMS_TO_SCORE: 'both_teams_to_score',
  BOTH_TEAMS_TO_SCORE_AND_WIN: 'btts_and_win',
  // First half / second half
  FIRST_HALF_WINNER: 'first_half_winner',
  FIRST_HALF_TOTAL: 'first_half_total',
  FIRST_HALF_HANDICAP: 'first_half_handicap',
  SECOND_HALF_WINNER: 'second_half_winner',
  // Correct score
  CORRECT_SCORE: 'correct_score',
  CORRECT_SCORE_FIRST_HALF: 'correct_score_first_half',
  // Player props
  PLAYER_GOALS: 'player_goals',
  PLAYER_ASSISTS: 'player_assists',
  PLAYER_SHOTS: 'player_shots',
  PLAYER_CARDS: 'player_cards',
  // Specials
  FIRST_GOAL_SCORER: 'first_goal_scorer',
  LAST_GOAL_SCORER: 'last_goal_scorer',
  ANYTIME_GOALSCORER: 'anytime_goalscorer',
  PENALTY_AWARDED: 'penalty_awarded',
  PENALTY_SCORED: 'penalty_scored',
  RED_CARD: 'red_card',
  // Corners
  CORNER_TOTAL: 'corner_total',
  CORNER_HANDICAP: 'corner_handicap',
  // Cards
  CARD_TOTAL: 'card_total',
  CARD_HANDICAP: 'card_handicap'
} as const;
export type BetMarketType = typeof BET_MARKET_TYPES[keyof typeof BET_MARKET_TYPES];

// ===================== INTERFACES =====================

export interface IMatchEvent {
  type: 'goal' | 'yellow_card' | 'red_card' | 'substitution' | 'penalty' | 'injury' | 'var';
  team: 'home' | 'away';
  player?: string;
  minute: number;
  extraTimeMinute?: number;
  description?: string;
}

export interface IMatchLineup {
  team: 'home' | 'away';
  formation: string;
  players: Array<{
    number: number;
    name: string;
    position: string;
    captain: boolean;
  }>;
  substitutes: Array<{
    number: number;
    name: string;
    position: string;
  }>;
}

export interface IPlayerProp {
  playerId: string;
  playerName: string;
  market: BetMarketType;
  line: number;
  odds: number;
  status: 'open' | 'won' | 'lost' | 'void';
}

export interface IMarket {
  marketType: BetMarketType;
  name: string;          // display name, e.g., "Match Winner"
  selections: Array<{
    label: string;       // e.g., "Home", "Draw", "Away"
    odds: number;
    handicap?: number;   // for handicap markets
    total?: number;      // for over/under markets
  }>;
  status: 'open' | 'closed' | 'suspended';
}

// Main Match interface
export interface IMatch extends Document {
  // Core
  matchId: number;                    // external ID from provider
  league: string;
  leagueId: string;
  homeTeam: string;
  awayTeam: string;
  homeTeamId: string;
  awayTeamId: string;
  matchDate: Date;
  status: MatchStatusType;
  isFeatured: boolean;

  // Score
  homeScore?: number;
  awayScore?: number;
  halfTimeHomeScore?: number;
  halfTimeAwayScore?: number;

  // Odds and markets
  odds?: {
    home?: number;
    draw?: number;
    away?: number;
  };
  markets?: IMarket[];               // all markets (1xBet style)
  playerProps?: IPlayerProp[];

  // Live data
  events?: IMatchEvent[];
  statistics?: {
    possession?: { home: number; away: number };
    shots?: { home: number; away: number };
    shotsOnTarget?: { home: number; away: number };
    corners?: { home: number; away: number };
    fouls?: { home: number; away: number };
    yellowCards?: { home: number; away: number };
    redCards?: { home: number; away: number };
  };
  lineups?: IMatchLineup[];

  // Metadata
  oddsHistory?: Array<{
    timestamp: Date;
    home: number;
    draw: number;
    away: number;
  }>;

  // Timestamps
  createdAt: Date;
  updatedAt: Date;
}

export interface IMatchModel extends Model<IMatch> {
  findLive(): Promise<IMatch[]>;
  findUpcoming(limit?: number): Promise<IMatch[]>;
}

// ===================== SCHEMA =====================

const MatchSchema = new Schema<IMatch, IMatchModel>(
  {
    matchId: { type: Number, required: true, unique: true, index: true },
    league: { type: String, required: true },
    leagueId: { type: String, required: true },
    homeTeam: { type: String, required: true },
    awayTeam: { type: String, required: true },
    homeTeamId: { type: String, required: true },
    awayTeamId: { type: String, required: true },
    matchDate: { type: Date, required: true, index: true },
    status: {
      type: String,
      enum: Object.values(MATCH_STATUS),
      default: MATCH_STATUS.UPCOMING,
      index: true
    },
    isFeatured: { type: Boolean, default: false, index: true },

    homeScore: { type: Number, min: 0 },
    awayScore: { type: Number, min: 0 },
    halfTimeHomeScore: { type: Number, min: 0 },
    halfTimeAwayScore: { type: Number, min: 0 },

    odds: {
      home: { type: Number, min: 1 },
      draw: { type: Number, min: 1 },
      away: { type: Number, min: 1 }
    },

    markets: [
      {
        marketType: { type: String, enum: Object.values(BET_MARKET_TYPES), required: true },
        name: { type: String, required: true },
        selections: [
          {
            label: { type: String, required: true },
            odds: { type: Number, min: 1, required: true },
            handicap: { type: Number },
            total: { type: Number }
          }
        ],
        status: { type: String, enum: ['open', 'closed', 'suspended'], default: 'open' }
      }
    ],

    playerProps: [
      {
        playerId: { type: String, required: true },
        playerName: { type: String, required: true },
        market: { type: String, enum: Object.values(BET_MARKET_TYPES), required: true },
        line: { type: Number, required: true },
        odds: { type: Number, min: 1, required: true },
        status: { type: String, enum: ['open', 'won', 'lost', 'void'], default: 'open' }
      }
    ],

    events: [
      {
        type: { type: String, enum: ['goal', 'yellow_card', 'red_card', 'substitution', 'penalty', 'injury', 'var'], required: true },
        team: { type: String, enum: ['home', 'away'], required: true },
        player: { type: String },
        minute: { type: Number, required: true },
        extraTimeMinute: { type: Number },
        description: { type: String }
      }
    ],

    statistics: {
      possession: { home: { type: Number, min: 0, max: 100 }, away: { type: Number, min: 0, max: 100 } },
      shots: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } },
      shotsOnTarget: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } },
      corners: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } },
      fouls: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } },
      yellowCards: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } },
      redCards: { home: { type: Number, min: 0 }, away: { type: Number, min: 0 } }
    },

    lineups: [
      {
        team: { type: String, enum: ['home', 'away'], required: true },
        formation: { type: String, required: true },
        players: [
          {
            number: { type: Number, required: true },
            name: { type: String, required: true },
            position: { type: String, required: true },
            captain: { type: Boolean, default: false }
          }
        ],
        substitutes: [
          {
            number: { type: Number, required: true },
            name: { type: String, required: true },
            position: { type: String, required: true }
          }
        ]
      }
    ],

    oddsHistory: [
      {
        timestamp: { type: Date, default: Date.now },
        home: { type: Number, min: 1 },
        draw: { type: Number, min: 1 },
        away: { type: Number, min: 1 }
      }
    ]
  },
  { timestamps: true }
);

// ===================== INDEXES =====================
MatchSchema.index({ status: 1, matchDate: 1 });
MatchSchema.index({ league: 1, matchDate: -1 });
MatchSchema.index({ homeTeam: 'text', awayTeam: 'text', league: 'text' });

// ===================== VIRTUAL FIELDS =====================
MatchSchema.virtual('isLive').get(function (this: IMatch) {
  return this.status === MATCH_STATUS.LIVE || this.status === MATCH_STATUS.HALFTIME || this.status === MATCH_STATUS.SECOND_HALF;
});

MatchSchema.virtual('isFinished').get(function (this: IMatch) {
  return this.status === MATCH_STATUS.FINISHED;
});

// ===================== INSTANCE METHODS =====================
MatchSchema.methods.addOddsHistory = async function (this: IMatch, home: number, draw: number, away: number) {
  if (!this.oddsHistory) this.oddsHistory = [];
  this.oddsHistory.push({ timestamp: new Date(), home, draw, away });
  this.odds = { home, draw, away };
  await this.save();
};

// ===================== STATIC METHODS =====================
MatchSchema.statics.findLive = function (this: Model<IMatch>) {
  return this.find({ status: { $in: [MATCH_STATUS.LIVE, MATCH_STATUS.HALFTIME, MATCH_STATUS.SECOND_HALF, MATCH_STATUS.EXTRA_TIME, MATCH_STATUS.PENALTIES] } })
    .sort({ matchDate: -1 })
    .limit(50);
};

MatchSchema.statics.findUpcoming = function (this: Model<IMatch>, limit = 20) {
  return this.find({ status: MATCH_STATUS.UPCOMING, matchDate: { $gte: new Date() } })
    .sort({ matchDate: 1 })
    .limit(limit);
};

// ===================== EXPORT =====================
export const Match: IMatchModel = (mongoose.models.Match as IMatchModel) || mongoose.model<IMatch, IMatchModel>('Match', MatchSchema);

// ============================================================================
// 🎰 CASINO GAME MODEL (for 51+ Games)
// ============================================================================

export interface ICasinoGame extends Document {
  gameId: string;
  name: string;
  nameAm: string;
  icon: string;
  category: 'crash' | 'classic' | 'table' | 'slots' | 'sports' | 'special';
  minBet: number;
  maxBet: number;
  isFavorite: boolean;
  timesPlayed: number;
  totalWagered: number;
  totalWon: number;
  createdAt: Date;
  updatedAt: Date;
}

const casinoGameSchema = new Schema<ICasinoGame>(
  {
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
  },
  { timestamps: true }
);

export const CasinoGame: mongoose.Model<ICasinoGame> = (mongoose.models.CasinoGame as mongoose.Model<ICasinoGame>) || mongoose.model<ICasinoGame>('CasinoGame', casinoGameSchema);

// ============================================================================
// 🎰 51+ CASINO GAMES DATA
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

// ===================== SEED FUNCTION =====================

export async function seedCasinoGames() {
  const existing = await CasinoGame.countDocuments();
  if (existing === 0) {
    await CasinoGame.insertMany(CASINO_GAMES_DATA);
    console.log('✅ Seeded 51+ casino games.');
  }
}

// ===================== DEFAULT EXPORT =====================

export default Match;