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
  // Full Time Markets
  FT_1X2: 'ft_1x2',
  FT_DOUBLE_CHANCE: 'ft_double_chance',
  FT_BTTS: 'ft_btts',
  FT_TOTAL_GOALS: 'ft_total_goals',
  FT_ASIAN_HANDICAP: 'ft_asian_handicap',
  FT_CORRECT_SCORE: 'ft_correct_score',
  FT_HALF_TIME_FULL_TIME: 'ft_half_time_full_time',
  FT_FIRST_GOAL: 'ft_first_goal',
  FT_LAST_GOAL: 'ft_last_goal',
  FT_GOAL_IN_BOTH_HALVES: 'ft_goal_in_both_halves',
  FT_WIN_TO_NIL: 'ft_win_to_nil',
  FT_WIN_BOTH_HALVES: 'ft_win_both_halves',
  FT_HIGHEST_SCORING_HALF: 'ft_highest_scoring_half',
  FT_RACE_TO_GOALS: 'ft_race_to_goals',
  
  // Half Time Markets
  HT_1X2: 'ht_1x2',
  HT_DOUBLE_CHANCE: 'ht_double_chance',
  HT_BTTS: 'ht_btts',
  HT_TOTAL_GOALS: 'ht_total_goals',
  HT_ASIAN_HANDICAP: 'ht_asian_handicap',
  HT_CORRECT_SCORE: 'ht_correct_score',
  
  // Second Half Markets
  SNDHLF_1X2: 'sndhlf_1x2',
  SNDHLF_BTTS: 'sndhlf_btts',
  SNDHLF_TOTAL_GOALS: 'sndhlf_total_goals',
  
  // Player Props
  PLAYER_ANYTIME_SCORER: 'player_anytime_scorer',
  PLAYER_FIRST_SCORER: 'player_first_scorer',
  PLAYER_LAST_SCORER: 'player_last_scorer',
  PLAYER_TO_SCORE_2PLUS: 'player_to_score_2plus',
  PLAYER_TO_SCORE_3PLUS: 'player_to_score_3plus',
  PLAYER_TO_ASSIST: 'player_to_assist',
  PLAYER_TO_GET_CARD: 'player_to_get_card',
  PLAYER_TO_GET_RED_CARD: 'player_to_get_red_card',
  PLAYER_SHOTS_ON_TARGET: 'player_shots_on_target',
  PLAYER_PASSES: 'player_passes',
  PLAYER_TACKLES: 'player_tackles',
  PLAYER_SAVES: 'player_saves',
  
  // Corner Markets
  CORNERS_1X2: 'corners_1x2',
  CORNERS_TOTAL: 'corners_total',
  CORNERS_ASIAN_HANDICAP: 'corners_asian_handicap',
  CORNERS_RACE_TO: 'corners_race_to',
  CORNERS_FIRST: 'corners_first',
  CORNERS_LAST: 'corners_last',
  CORNERS_HT_TOTAL: 'corners_ht_total',
  
  // Card Markets
  CARDS_1X2: 'cards_1x2',
  CARDS_TOTAL: 'cards_total',
  CARDS_ASIAN_HANDICAP: 'cards_asian_handicap',
  CARDS_RACE_TO: 'cards_race_to',
  CARDS_FIRST: 'cards_first',
  CARDS_LAST: 'cards_last',
  
  // Combination Markets
  COMBO_1X2_BTTS: 'combo_1x2_btts',
  COMBO_1X2_OVER: 'combo_1x2_over',
  COMBO_BTTS_OVER: 'combo_btts_over',
  COMBO_DC_BTTS: 'combo_dc_btts',
  COMBO_HT_FT: 'combo_ht_ft',
  
  // Live Betting Markets
  LIVE_NEXT_GOAL: 'live_next_goal',
  LIVE_NEXT_CORNER: 'live_next_corner',
  LIVE_NEXT_CARD: 'live_next_card',
  LIVE_GOAL_IN_10MIN: 'live_goal_in_10min',
  LIVE_GOAL_IN_20MIN: 'live_goal_in_20min',
  LIVE_PENALTY: 'live_penalty',
  LIVE_RED_CARD: 'live_red_card',
  
  // Bet Builder
  BET_BUILDER: 'bet_builder'
} as const;

export interface IMatchEvent {
  id: string;
  type: 'goal' | 'own_goal' | 'penalty_goal' | 'missed_penalty' | 'yellow_card' | 'red_card' | 'substitution' | 'injury' | 'var' | 'assist';
  minute: number;
  addedTime?: number;
  team?: 'home' | 'away';
  player?: string;
  playerId?: string;
  assist?: string;
  assistPlayerId?: string;
  substitutionIn?: string;
  substitutionOut?: string;
  description?: string;
  isVar?: boolean;
}

export interface IMatchLineup {
  formation?: string;
  startingXI?: Array<{
    playerId?: string;
    name?: string;
    position?: string;
    number?: number;
    isCaptain?: boolean;
  }>;
  substitutes?: Array<{
    playerId?: string;
    name?: string;
    number?: number;
  }>;
  coach?: string;
}

export interface IMatch extends Document {
  matchId: string;
  externalId?: string;
  provider: string;
  
  league: string;
  leagueId?: string;
  leagueLogo?: string;
  leagueCountry?: string;
  leagueSeason?: string;
  leagueRound?: string;
  
  homeTeam: string;
  awayTeam: string;
  homeTeamId?: string;
  awayTeamId?: string;
  homeTeamLogo?: string;
  awayTeamLogo?: string;
  homeTeamShort?: string;
  awayTeamShort?: string;
  homeTeamForm: string[];
  awayTeamForm: string[];
  homeTeamStats: {
    wins: number;
    draws: number;
    losses: number;
    goalsFor: number;
    goalsAgainst: number;
  };
  awayTeamStats: {
    wins: number;
    draws: number;
    losses: number;
    goalsFor: number;
    goalsAgainst: number;
  };
  
  matchDate: Date;
  stadium?: string;
  stadiumLocation?: string;
  referee?: string;
  attendance?: number;
  weather?: {
    condition?: string;
    temperature?: number;
    humidity?: number;
    windSpeed?: number;
  };
  
  status: string;
  period?: 'first_half' | 'halftime' | 'second_half' | 'extra_time' | 'penalties' | 'finished';
  minute: number;
  addedTime: number;
  isLive: boolean;
  isFeatured: boolean;
  priority: number;
  
  scores: {
    home: number;
    away: number;
    halftime?: { home?: number; away?: number };
    fulltime?: { home?: number; away?: number };
    extratime?: { home?: number; away?: number };
    penalties?: { home?: number; away?: number };
    aggregate?: { home?: number; away?: number };
  };
  
  statistics: {
    possession: { home: number; away: number };
    shots: { home: number; away: number };
    shotsOnTarget: { home: number; away: number };
    shotsOffTarget: { home: number; away: number };
    blockedShots: { home: number; away: number };
    corners: { home: number; away: number };
    fouls: { home: number; away: number };
    yellowCards: { home: number; away: number };
    redCards: { home: number; away: number };
    offsides: { home: number; away: number };
    passes: { home: number; away: number };
    passAccuracy: { home: number; away: number };
    tackles: { home: number; away: number };
    interceptions: { home: number; away: number };
    saves: { home: number; away: number };
    clearances: { home: number; away: number };
    expectedGoals: { home: number; away: number };
    expectedGoalsAgainst: { home: number; away: number };
  };
  
  events: IMatchEvent[];
  lineups: {
    home: IMatchLineup;
    away: IMatchLineup;
  };
  
  prematchOdds: {
    homeWin: number;
    draw: number;
    awayWin: number;
    doubleChance?: {
      homeDraw?: number;
      homeAway?: number;
      drawAway?: number;
    };
    btts?: {
      yes?: number;
      no?: number;
    };
    totalGoals?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
      over25?: number; under25?: number;
      over3?: number; under3?: number;
      over35?: number; under35?: number;
      over4?: number; under4?: number;
      over45?: number; under45?: number;
      over5?: number; under5?: number;
      over55?: number; under55?: number;
      over6?: number; under6?: number;
    };
    asianHandicap?: Array<{
      line?: string;
      home?: number;
      away?: number;
    }>;
    correctScore?: Record<string, number>;
    htFt?: Record<string, number>;
    firstGoal?: { home?: number; away?: number; noGoal?: number };
    lastGoal?: { home?: number; away?: number; noGoal?: number };
    goalInBothHalves?: { yes?: number; no?: number };
    winToNil?: { home?: number; away?: number };
    highestScoringHalf?: { first?: number; second?: number; equal?: number };
    raceTo3Goals?: { home?: number; away?: number; neither?: number };
    raceTo5Goals?: { home?: number; away?: number; neither?: number };
  };
  
  firstHalfOdds?: {
    homeWin?: number;
    draw?: number;
    awayWin?: number;
    doubleChance?: { homeDraw?: number; homeAway?: number; drawAway?: number };
    btts?: { yes?: number; no?: number };
    totalGoals?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
      over25?: number; under25?: number;
    };
    asianHandicap?: Array<{ line?: string; home?: number; away?: number }>;
    correctScore?: Record<string, number>;
  };
  
  secondHalfOdds?: {
    homeWin?: number;
    draw?: number;
    awayWin?: number;
    btts?: { yes?: number; no?: number };
    totalGoals?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
      over25?: number; under25?: number;
    };
  };
  
  liveOdds?: {
    homeWin?: number;
    draw?: number;
    awayWin?: number;
    nextGoal?: { home?: number; away?: number; noGoal?: number };
    nextCorner?: { home?: number; away?: number };
    nextCard?: { home?: number; away?: number };
    nextSubstitution?: { home?: number; away?: number };
    goalIn10Min?: { yes?: number; no?: number };
    goalIn20Min?: { yes?: number; no?: number };
    penalty?: { yes?: number; no?: number };
    redCard?: { yes?: number; no?: number };
    asianHandicap?: Array<{ line?: string; home?: number; away?: number }>;
    totalGoals?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
    };
    lastUpdated?: Date;
  };
  
  cornerOdds?: {
    total?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
      over25?: number; under25?: number;
      over3?: number; under3?: number;
    };
    asianHandicap?: Array<{ line?: string; home?: number; away?: number }>;
    raceTo3?: { home?: number; away?: number };
    raceTo5?: { home?: number; away?: number };
    firstCorner?: { home?: number; away?: number };
  };
  
  cardOdds?: {
    total?: {
      over05?: number; under05?: number;
      over1?: number; under1?: number;
      over15?: number; under15?: number;
      over2?: number; under2?: number;
      over25?: number; under25?: number;
      over3?: number; under3?: number;
    };
    asianHandicap?: Array<{ line?: string; home?: number; away?: number }>;
    firstCard?: { home?: number; away?: number };
  };
  
  playerProps: Array<{
    playerId?: string;
    playerName?: string;
    team?: string;
    position?: string;
    markets?: {
      anytimeScorer?: number;
      firstScorer?: number;
      lastScorer?: number;
      toScore2OrMore?: number;
      toScore3OrMore?: number;
      toAssist?: number;
      toGetCard?: number;
      toGetRedCard?: number;
      shotsOnTargetOver05?: number;
      shotsOnTargetOver15?: number;
      tacklesOver15?: number;
      passesOver25?: number;
      savesOver2?: number;
    };
  }>;
  
  comboMarkets: Array<{
    id?: string;
    name?: string;
    selections?: Array<{
      selection?: string;
      odds?: number;
    }>;
  }>;
  
  oddsHistory: Array<{
    timestamp?: Date;
    homeWin?: number;
    draw?: number;
    awayWin?: number;
    over25?: number;
    under25?: number;
  }>;
  
  aiPredictions?: {
    predictedWinner?: 'home' | 'draw' | 'away';
    predictedScore?: string;
    confidence?: number;
    probabilityHome?: number;
    probabilityDraw?: number;
    probabilityAway?: number;
    probabilityOver25?: number;
    probabilityBTTS?: number;
    analysis?: string;
    keyFactors?: string[];
    statisticsUsed?: string[];
    modelVersion?: string;
    updatedAt?: Date;
    accuracy?: number;
  };
  
  streaming?: {
    hasStream?: boolean;
    streamUrl?: string;
    streamType?: 'hls' | 'dash' | 'iframe';
    streamKey?: string;
    requiresSubscription?: boolean;
    streamQuality?: 'SD' | 'HD' | 'FHD' | '4K';
    languages?: string[];
  };
  
  metadata: {
    popularity: number;
    viewCount: number;
    betCount: number;
    totalWagered: number;
    hasHighlights?: boolean;
    highlightsUrl?: string;
  };
  
  virtualBetting?: {
    enabled?: boolean;
    simulationMode?: 'ai' | 'historical' | 'random';
  };
  
  createdAt: Date;
  updatedAt: Date;
  lastOddsUpdate?: Date;
  lastScoreUpdate?: Date;

  // Virtual Properties defined as TypeScript getters/methods for typing
  isFinished: boolean;
  isLiveNow: boolean;
  hasLiveOdds: boolean;
  scoreDisplay: string;

  // Instance methods
  updateLiveScore(homeScore: number, awayScore: number, minute: number): Promise<IMatch>;
  updateLiveOdds(odds: any): Promise<IMatch>;
  addEvent(event: any): Promise<IMatch>;
}

export interface IMatchModel extends Model<IMatch> {
  getLiveMatches(): Promise<IMatch[]>;
  getUpcomingMatches(limit?: number): Promise<IMatch[]>;
  getFeaturedMatches(limit?: number): Promise<IMatch[]>;
}

const matchSchema = new Schema<IMatch, IMatchModel>({
  // Basic Match Info
  matchId: { type: String, unique: true, required: true, index: true },
  externalId: { type: String, unique: true, sparse: true, index: true },
  provider: { type: String, default: 'sportmonks' },
  
  // League Information
  league: { type: String, required: true, index: true },
  leagueId: { type: String, index: true },
  leagueLogo: { type: String },
  leagueCountry: { type: String },
  leagueSeason: { type: String },
  leagueRound: { type: String },
  
  // Team Information
  homeTeam: { type: String, required: true, index: true },
  awayTeam: { type: String, required: true, index: true },
  homeTeamId: { type: String },
  awayTeamId: { type: String },
  homeTeamLogo: { type: String },
  awayTeamLogo: { type: String },
  homeTeamShort: { type: String },
  awayTeamShort: { type: String },
  homeTeamForm: [{ type: String }],
  awayTeamForm: [{ type: String }],
  homeTeamStats: {
    wins: { type: Number, default: 0 },
    draws: { type: Number, default: 0 },
    losses: { type: Number, default: 0 },
    goalsFor: { type: Number, default: 0 },
    goalsAgainst: { type: Number, default: 0 }
  },
  awayTeamStats: {
    wins: { type: Number, default: 0 },
    draws: { type: Number, default: 0 },
    losses: { type: Number, default: 0 },
    goalsFor: { type: Number, default: 0 },
    goalsAgainst: { type: Number, default: 0 }
  },
  
  // Match Details
  matchDate: { type: Date, required: true, index: true },
  stadium: { type: String },
  stadiumLocation: { type: String },
  referee: { type: String },
  attendance: { type: Number },
  weather: {
    condition: String,
    temperature: Number,
    humidity: Number,
    windSpeed: Number
  },
  
  // Match Status
  status: { type: String, default: MATCH_STATUS.UPCOMING, index: true },
  period: { type: String, enum: ['first_half', 'halftime', 'second_half', 'extra_time', 'penalties', 'finished'] },
  minute: { type: Number, default: 0 },
  addedTime: { type: Number, default: 0 },
  isLive: { type: Boolean, default: false, index: true },
  isFeatured: { type: Boolean, default: false, index: true },
  priority: { type: Number, default: 0 },
  
  // Scores
  scores: {
    home: { type: Number, default: 0 },
    away: { type: Number, default: 0 },
    halftime: { home: Number, away: Number },
    fulltime: { home: Number, away: Number },
    extratime: { home: Number, away: Number },
    penalties: { home: Number, away: Number },
    aggregate: { home: Number, away: Number }
  },
  
  // Live Statistics (Real-time)
  statistics: {
    possession: { home: { type: Number, default: 50 }, away: { type: Number, default: 50 } },
    shots: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    shotsOnTarget: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    shotsOffTarget: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    blockedShots: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    corners: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    fouls: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    yellowCards: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    redCards: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    offsides: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    passes: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    passAccuracy: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    tackles: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    interceptions: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    saves: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    clearances: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    expectedGoals: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } },
    expectedGoalsAgainst: { home: { type: Number, default: 0 }, away: { type: Number, default: 0 } }
  },
  
  // Match Events
  events: [{
    id: { type: String, required: true },
    type: { type: String, enum: ['goal', 'own_goal', 'penalty_goal', 'missed_penalty', 'yellow_card', 'red_card', 'substitution', 'injury', 'var', 'assist'] },
    minute: { type: Number, required: true },
    addedTime: { type: Number, default: 0 },
    team: { type: String, enum: ['home', 'away'] },
    player: { type: String },
    playerId: { type: String },
    assist: { type: String },
    assistPlayerId: { type: String },
    substitutionIn: { type: String },
    substitutionOut: { type: String },
    description: { type: String },
    isVar: { type: Boolean, default: false }
  }],
  
  // Lineups
  lineups: {
    home: {
      formation: { type: String },
      startingXI: [{
        playerId: String,
        name: String,
        position: String,
        number: Number,
        isCaptain: Boolean
      }],
      substitutes: [{
        playerId: String,
        name: String,
        number: Number
      }],
      coach: String
    },
    away: {
      formation: { type: String },
      startingXI: [{
        playerId: String,
        name: String,
        position: String,
        number: Number,
        isCaptain: Boolean
      }],
      substitutes: [{
        playerId: String,
        name: String,
        number: Number
      }],
      coach: String
    }
  },
  
  // ==================== ODDS (Complete - 1xBet Style) ====================
  
  // Pre-match Odds
  prematchOdds: {
    // 1X2
    homeWin: { type: Number, required: true },
    draw: { type: Number, required: true },
    awayWin: { type: Number, required: true },
    
    // Double Chance
    doubleChance: {
      homeDraw: Number,
      homeAway: Number,
      drawAway: Number
    },
    
    // Both Teams to Score
    btts: {
      yes: Number,
      no: Number
    },
    
    // Total Goals (Over/Under)
    totalGoals: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number,
      over25: Number, under25: Number,
      over3: Number, under3: Number,
      over35: Number, under35: Number,
      over4: Number, under4: Number,
      over45: Number, under45: Number,
      over5: Number, under5: Number,
      over55: Number, under55: Number,
      over6: Number, under6: Number
    },
    
    // Asian Handicap (Multiple Lines)
    asianHandicap: [{
      line: String,
      home: Number,
      away: Number
    }],
    
    // Correct Score
    correctScore: { type: Map, of: Number },
    
    // Half Time/Full Time
    htFt: { type: Map, of: Number },
    
    // First Goal / Last Goal
    firstGoal: {
      home: Number,
      away: Number,
      noGoal: Number
    },
    lastGoal: {
      home: Number,
      away: Number,
      noGoal: Number
    },
    
    // Goal in Both Halves
    goalInBothHalves: {
      yes: Number,
      no: Number
    },
    
    // Win to Nil
    winToNil: {
      home: Number,
      away: Number
    },
    
    // Highest Scoring Half
    highestScoringHalf: {
      first: Number,
      second: Number,
      equal: Number
    },
    
    // Race to Goals
    raceTo3Goals: { home: Number, away: Number, neither: Number },
    raceTo5Goals: { home: Number, away: Number, neither: Number }
  },
  
  // First Half Odds
  firstHalfOdds: {
    homeWin: Number,
    draw: Number,
    awayWin: Number,
    doubleChance: { homeDraw: Number, homeAway: Number, drawAway: Number },
    btts: { yes: Number, no: Number },
    totalGoals: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number,
      over25: Number, under25: Number
    },
    asianHandicap: [{ line: String, home: Number, away: Number }],
    correctScore: { type: Map, of: Number }
  },
  
  // Second Half Odds
  secondHalfOdds: {
    homeWin: Number,
    draw: Number,
    awayWin: Number,
    btts: { yes: Number, no: Number },
    totalGoals: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number,
      over25: Number, under25: Number
    }
  },
  
  // Live Odds (Dynamic - Updated in Real-time)
  liveOdds: {
    homeWin: Number,
    draw: Number,
    awayWin: Number,
    nextGoal: { home: Number, away: Number, noGoal: Number },
    nextCorner: { home: Number, away: Number },
    nextCard: { home: Number, away: Number },
    nextSubstitution: { home: Number, away: Number },
    goalIn10Min: { yes: Number, no: Number },
    goalIn20Min: { yes: Number, no: Number },
    penalty: { yes: Number, no: Number },
    redCard: { yes: Number, no: Number },
    asianHandicap: [{ line: String, home: Number, away: Number }],
    totalGoals: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number
    },
    lastUpdated: { type: Date, default: Date.now }
  },
  
  // Corner Odds
  cornerOdds: {
    total: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number,
      over25: Number, under25: Number,
      over3: Number, under3: Number
    },
    asianHandicap: [{ line: String, home: Number, away: Number }],
    raceTo3: { home: Number, away: Number },
    raceTo5: { home: Number, away: Number },
    firstCorner: { home: Number, away: Number }
  },
  
  // Card Odds
  cardOdds: {
    total: {
      over05: Number, under05: Number,
      over1: Number, under1: Number,
      over15: Number, under15: Number,
      over2: Number, under2: Number,
      over25: Number, under25: Number,
      over3: Number, under3: Number
    },
    asianHandicap: [{ line: String, home: Number, away: Number }],
    firstCard: { home: Number, away: Number }
  },
  
  // Player Props
  playerProps: [{
    playerId: String,
    playerName: String,
    team: String,
    position: String,
    markets: {
      anytimeScorer: Number,
      firstScorer: Number,
      lastScorer: Number,
      toScore2OrMore: Number,
      toScore3OrMore: Number,
      toAssist: Number,
      toGetCard: Number,
      toGetRedCard: Number,
      shotsOnTargetOver05: Number,
      shotsOnTargetOver15: Number,
      tacklesOver15: Number,
      passesOver25: Number,
      savesOver2: Number
    }
  }],
  
  // Combination Markets
  comboMarkets: [{
    id: String,
    name: String,
    selections: [{
      selection: String,
      odds: Number
    }]
  }],
  
  // Odds History (for charts)
  oddsHistory: [{
    timestamp: { type: Date, default: Date.now },
    homeWin: Number,
    draw: Number,
    awayWin: Number,
    over25: Number,
    under25: Number
  }],
  
  // ==================== AI PREDICTIONS ====================
  aiPredictions: {
    predictedWinner: { type: String, enum: ['home', 'draw', 'away'] },
    predictedScore: String,
    confidence: { type: Number, min: 0, max: 100 },
    probabilityHome: Number,
    probabilityDraw: Number,
    probabilityAway: Number,
    probabilityOver25: Number,
    probabilityBTTS: Number,
    analysis: String,
    keyFactors: [String],
    statisticsUsed: [String],
    modelVersion: String,
    updatedAt: Date,
    accuracy: Number
  },
  
  // ==================== STREAMING ====================
  streaming: {
    hasStream: { type: Boolean, default: false },
    streamUrl: String,
    streamType: { type: String, enum: ['hls', 'dash', 'iframe'] },
    streamKey: String,
    requiresSubscription: { type: Boolean, default: false },
    streamQuality: { type: String, enum: ['SD', 'HD', 'FHD', '4K'], default: 'HD' },
    languages: [String]
  },
  
  // ==================== METADATA ====================
  metadata: {
    popularity: { type: Number, default: 0 },
    viewCount: { type: Number, default: 0 },
    betCount: { type: Number, default: 0 },
    totalWagered: { type: Number, default: 0 },
    hasHighlights: { type: Boolean, default: false },
    highlightsUrl: String
  },
  
  // Virtual Betting (Simulation)
  virtualBetting: {
    enabled: { type: Boolean, default: false },
    simulationMode: { type: String, enum: ['ai', 'historical', 'random'], default: 'ai' }
  },
  
  // Timestamps
  createdAt: { type: Date, default: Date.now, index: true },
  updatedAt: { type: Date, default: Date.now },
  lastOddsUpdate: { type: Date },
  lastScoreUpdate: { type: Date }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// ==================== INDEXES ====================
matchSchema.index({ matchDate: 1 });
matchSchema.index({ status: 1, matchDate: 1 });
matchSchema.index({ league: 1, status: 1 });
matchSchema.index({ homeTeam: 1, awayTeam: 1 });
matchSchema.index({ 'liveOdds.lastUpdated': -1 });
matchSchema.index({ isFeatured: -1, matchDate: 1 });
matchSchema.index({ 'streaming.hasStream': 1 });
matchSchema.index({ 'metadata.popularity': -1 });

// ==================== VIRTUAL FIELDS ====================
matchSchema.virtual('isFinished').get(function(this: IMatch) {
  return this.status === MATCH_STATUS.FINISHED;
});

matchSchema.virtual('isLiveNow').get(function(this: IMatch) {
  return [MATCH_STATUS.LIVE, MATCH_STATUS.HALFTIME, MATCH_STATUS.SECOND_HALF, MATCH_STATUS.EXTRA_TIME, MATCH_STATUS.PENALTIES].includes(this.status as any);
});

matchSchema.virtual('hasLiveOdds').get(function(this: IMatch) {
  return this.isLiveNow && this.liveOdds && Object.keys(this.liveOdds).length > 0;
});

matchSchema.virtual('scoreDisplay').get(function(this: IMatch) {
  return `${this.scores.home} - ${this.scores.away}`;
});

// ==================== INSTANCE METHODS ====================
matchSchema.methods.updateLiveScore = function(this: IMatch, homeScore: number, awayScore: number, minute: number): Promise<IMatch> {
  this.scores.home = homeScore;
  this.scores.away = awayScore;
  this.minute = minute;
  this.lastScoreUpdate = new Date();
  if (minute === 45) this.status = MATCH_STATUS.HALFTIME;
  else if (minute === 90) this.status = MATCH_STATUS.FINISHED;
  else if (minute > 0 && minute < 45) this.status = MATCH_STATUS.LIVE;
  else if (minute > 45 && minute < 90) this.status = MATCH_STATUS.SECOND_HALF;
  return this.save();
};

matchSchema.methods.updateLiveOdds = function(this: IMatch, odds: any): Promise<IMatch> {
  this.liveOdds = { ...this.liveOdds, ...odds, lastUpdated: new Date() };
  
  // Record history
  this.oddsHistory.push({
    timestamp: new Date(),
    homeWin: this.liveOdds.homeWin,
    draw: this.liveOdds.draw,
    awayWin: this.liveOdds.awayWin,
    over25: this.liveOdds.totalGoals?.over25,
    under25: this.liveOdds.totalGoals?.under25
  });
  
  // Keep only last 100 history entries
  if (this.oddsHistory.length > 100) {
    this.oddsHistory = this.oddsHistory.slice(-100);
  }
  
  this.lastOddsUpdate = new Date();
  return this.save();
};

matchSchema.methods.addEvent = function(this: IMatch, event: any): Promise<IMatch> {
  this.events.push({
    id: `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    ...event
  });
  
  // Update score if goal
  if (event.type === 'goal') {
    if (event.team === 'home') this.scores.home++;
    else this.scores.away++;
  }
  
  // Update statistics
  if (event.type === 'yellow_card') {
    if (event.team === 'home') this.statistics.yellowCards.home++;
    else this.statistics.yellowCards.away++;
  }
  
  if (event.type === 'red_card') {
    if (event.team === 'home') this.statistics.redCards.home++;
    else this.statistics.redCards.away++;
  }
  
  if (event.type === 'corner') {
    if (event.team === 'home') this.statistics.corners.home++;
    else this.statistics.corners.away++;
  }
  
  return this.save();
};

// ==================== STATIC METHODS ====================
matchSchema.statics.getLiveMatches = function(this: IMatchModel) {
  return this.find({ 
    status: { $in: [MATCH_STATUS.LIVE, MATCH_STATUS.HALFTIME, MATCH_STATUS.SECOND_HALF] } 
  }).sort({ minute: -1 });
};

matchSchema.statics.getUpcomingMatches = function(this: IMatchModel, limit = 50) {
  return this.find({ 
    status: MATCH_STATUS.UPCOMING, 
    matchDate: { $gt: new Date() } 
  }).sort({ matchDate: 1 }).limit(limit);
};

matchSchema.statics.getFeaturedMatches = function(this: IMatchModel, limit = 10) {
  return this.find({ 
    isFeatured: true, 
    status: MATCH_STATUS.UPCOMING,
    matchDate: { $gt: new Date() }
  }).sort({ matchDate: 1 }).limit(limit);
};

export const Match = mongoose.models.Match || mongoose.model<IMatch, IMatchModel>('Match', matchSchema);
export default Match;
