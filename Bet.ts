// ============================================================
// SHEBAODDS - BET MODEL
// Mongoose 8 + TypeScript
// Production-ready typed Bet model
// ============================================================

import {
  HydratedDocument,
  Model,
  Schema,
  Types,
  model,
  models,
} from 'mongoose';

// ============================================================
// ENUMS
// ============================================================

export const BET_STATUSES = [
  'pending',
  'won',
  'lost',
  'void',
  'cancelled',
  'cashout',
  'half_won',
  'half_lost',
  'push',
] as const;

export type BetStatus = (typeof BET_STATUSES)[number];

export const BET_TYPES = [
  'single',
  'multiple',
  'accumulator',
  'system',
  'bet_builder',
] as const;

export type BetType = (typeof BET_TYPES)[number];

export const BET_PERIODS = [
  'full',
  'first_half',
  'second_half',
  'extra_time',
  'penalties',
] as const;

export type BetPeriod = (typeof BET_PERIODS)[number];

// ============================================================
// ACCUMULATOR SELECTION
// ============================================================

export interface IAccumulatorSelection {
  matchId: Types.ObjectId;
  selection: string;
  odds: number;
  status?: string;
  outcome?: string;
}

// ============================================================
// BET BUILDER SELECTION
// ============================================================

export interface IBetBuilderSelection {
  marketType: string;
  selection: string;
  odds: number;
}

// ============================================================
// STATUS HISTORY
// ============================================================

export interface IBetStatusHistory {
  status: BetStatus;
  timestamp: Date;
  reason?: string;
}

// ============================================================
// BET DOCUMENT INTERFACE
// ============================================================

export interface IBet {
  // ----------------------------------------------------------
  // User / Match
  // ----------------------------------------------------------

  userId: Types.ObjectId;

  matchId: Types.ObjectId;

  // ----------------------------------------------------------
  // Bet Details
  // ----------------------------------------------------------

  betType: BetType;

  marketType: string;

  selection: string;

  odds: number;

  stake: number;

  potentialWin: number;

  actualWin: number;

  // ----------------------------------------------------------
  // Tax
  // ----------------------------------------------------------

  taxAmount: number;

  taxRate: number;

  netWin: number;

  taxTransactionId?: Types.ObjectId;

  // ----------------------------------------------------------
  // Accumulator
  // ----------------------------------------------------------

  isAccumulator: boolean;

  accumulatorId?: string;

  accumulatorSelections: IAccumulatorSelection[];

  combinedOdds: number;

  // ----------------------------------------------------------
  // Bet Builder
  // ----------------------------------------------------------

  isBetBuilder: boolean;

  betBuilderSelections: IBetBuilderSelection[];

  // ----------------------------------------------------------
  // Live Betting
  // ----------------------------------------------------------

  isLive: boolean;

  betPlacedAtMinute?: number;

  // ----------------------------------------------------------
  // Cash Out
  // ----------------------------------------------------------

  cashOutAvailable: boolean;

  cashOutAmount?: number;

  cashOutMultiplier?: number;

  cashedOutAt?: Date;

  autoCashOutMultiplier?: number;

  autoCashOutTriggered: boolean;

  // ----------------------------------------------------------
  // Status
  // ----------------------------------------------------------

  status: BetStatus;

  statusHistory: IBetStatusHistory[];

  // ----------------------------------------------------------
  // Settlement
  // ----------------------------------------------------------

  settledAt?: Date;

  settledScore?: string;

  actualOutcome?: string;

  winAmount: number;

  // ----------------------------------------------------------
  // Partial Settlement
  // ----------------------------------------------------------

  isHalfWin: boolean;

  isHalfLoss: boolean;

  isPush: boolean;

  // ----------------------------------------------------------
  // Period
  // ----------------------------------------------------------

  period: BetPeriod;

  // ----------------------------------------------------------
  // Metadata
  // ----------------------------------------------------------

  deviceInfo?: string;

  ipAddress?: string;

  // ----------------------------------------------------------
  // Timestamps
  // ----------------------------------------------------------

  createdAt: Date;

  updatedAt: Date;
}

// ============================================================
// MODEL TYPE
// ============================================================

export type BetDocument = HydratedDocument<IBet>;

export type BetModel = Model<IBet>;

// ============================================================
// ACCUMULATOR SCHEMA
// ============================================================

const accumulatorSelectionSchema =
  new Schema<IAccumulatorSelection>(
    {
      matchId: {
        type: Schema.Types.ObjectId,
        ref: 'Match',
        required: true,
      },

      selection: {
        type: String,
        required: true,
        trim: true,
      },

      odds: {
        type: Number,
        required: true,
        min: 1,
      },

      status: {
        type: String,
        trim: true,
      },

      outcome: {
        type: String,
        trim: true,
      },
    },
    {
      _id: false,
    },
  );

// ============================================================
// BET BUILDER SCHEMA
// ============================================================

const betBuilderSelectionSchema =
  new Schema<IBetBuilderSelection>(
    {
      marketType: {
        type: String,
        required: true,
        trim: true,
      },

      selection: {
        type: String,
        required: true,
        trim: true,
      },

      odds: {
        type: Number,
        required: true,
        min: 1,
      },
    },
    {
      _id: false,
    },
  );

// ============================================================
// STATUS HISTORY SCHEMA
// ============================================================

const betStatusHistorySchema =
  new Schema<IBetStatusHistory>(
    {
      status: {
        type: String,
        enum: BET_STATUSES,
        required: true,
      },

      timestamp: {
        type: Date,
        default: Date.now,
      },

      reason: {
        type: String,
        trim: true,
      },
    },
    {
      _id: false,
    },
  );

// ============================================================
// BET SCHEMA
// ============================================================

const betSchema = new Schema<IBet>(
  {
    // ========================================================
    // USER
    // ========================================================

    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },

    // ========================================================
    // MATCH
    // ========================================================

    matchId: {
      type: Schema.Types.ObjectId,
      ref: 'Match',
      required: true,
      index: true,
    },

    // ========================================================
    // BET DETAILS
    // ========================================================

    betType: {
      type: String,
      enum: BET_TYPES,
      required: true,
      default: 'single',
      index: true,
    },

    marketType: {
      type: String,
      required: true,
      trim: true,
      maxlength: 100,
    },

    selection: {
      type: String,
      required: true,
      trim: true,
      maxlength: 200,
    },

    odds: {
      type: Number,
      required: true,
      min: 1,
      max: 100000,
    },

    stake: {
      type: Number,
      required: true,
      min: 0.01,
      max: 100000000,
    },

    potentialWin: {
      type: Number,
      required: true,
      min: 0,
    },

    actualWin: {
      type: Number,
      default: 0,
      min: 0,
    },

    // ========================================================
    // TAX
    // ========================================================

    taxAmount: {
      type: Number,
      default: 0,
      min: 0,
    },

    taxRate: {
      type: Number,
      default: 0.15,
      min: 0,
      max: 1,
    },

    netWin: {
      type: Number,
      default: 0,
      min: 0,
    },

    taxTransactionId: {
      type: Schema.Types.ObjectId,
      ref: 'TaxTransaction',
    },

    // ========================================================
    // ACCUMULATOR
    // ========================================================

    isAccumulator: {
      type: Boolean,
      default: false,
      index: true,
    },

    accumulatorId: {
      type: String,
      trim: true,
      index: true,
    },

    accumulatorSelections: {
      type: [accumulatorSelectionSchema],
      default: [],
    },

    combinedOdds: {
      type: Number,
      default: 1,
      min: 1,
    },

    // ========================================================
    // BET BUILDER
    // ========================================================

    isBetBuilder: {
      type: Boolean,
      default: false,
      index: true,
    },

    betBuilderSelections: {
      type: [betBuilderSelectionSchema],
      default: [],
    },

    // ========================================================
    // LIVE BETTING
    // ========================================================

    isLive: {
      type: Boolean,
      default: false,
      index: true,
    },

    betPlacedAtMinute: {
      type: Number,
      min: 0,
      max: 300,
    },

    // ========================================================
    // CASH OUT
    // ========================================================

    cashOutAvailable: {
      type: Boolean,
      default: false,
      index: true,
    },

    cashOutAmount: {
      type: Number,
      min: 0,
    },

    cashOutMultiplier: {
      type: Number,
      min: 0,
    },

    cashedOutAt: {
      type: Date,
    },

    autoCashOutMultiplier: {
      type: Number,
      min: 0,
    },

    autoCashOutTriggered: {
      type: Boolean,
      default: false,
    },

    // ========================================================
    // STATUS
    // ========================================================

    status: {
      type: String,
      enum: BET_STATUSES,
      default: 'pending',
      required: true,
      index: true,
    },

    statusHistory: {
      type: [betStatusHistorySchema],
      default: [],
    },

    // ========================================================
    // SETTLEMENT
    // ========================================================

    settledAt: {
      type: Date,
    },

    settledScore: {
      type: String,
      trim: true,
      maxlength: 100,
    },

    actualOutcome: {
      type: String,
      trim: true,
      maxlength: 200,
    },

    winAmount: {
      type: Number,
      default: 0,
      min: 0,
    },

    // ========================================================
    // PARTIAL SETTLEMENT
    // ========================================================

    isHalfWin: {
      type: Boolean,
      default: false,
    },

    isHalfLoss: {
      type: Boolean,
      default: false,
    },

    isPush: {
      type: Boolean,
      default: false,
    },

    // ========================================================
    // BET PERIOD
    // ========================================================

    period: {
      type: String,
      enum: BET_PERIODS,
      default: 'full',
      required: true,
    },

    // ========================================================
    // METADATA
    // ========================================================

    deviceInfo: {
      type: String,
      trim: true,
      maxlength: 1000,
    },

    ipAddress: {
      type: String,
      trim: true,
      maxlength: 100,
    },
  },
  {
    timestamps: true,

    // Do not automatically create an `id` virtual.
    id: false,

    // Prevent accidental fields from being persisted.
    strict: true,

    // Avoid returning version key in API responses.
    versionKey: false,
  },
);

// ============================================================
// INDEXES
// ============================================================

// User's bets by status and newest first.
betSchema.index({
  userId: 1,
  status: 1,
  createdAt: -1,
});

// User's betting history.
betSchema.index({
  userId: 1,
  createdAt: -1,
});

// Match settlement queries.
betSchema.index({
  matchId: 1,
  status: 1,
});

// Live pending bets.
betSchema.index({
  isLive: 1,
  status: 1,
});

// Accumulator lookup.
betSchema.index({
  accumulatorId: 1,
});

// Settlement queue.
betSchema.index({
  status: 1,
  createdAt: 1,
});

// Cash-out queries.
betSchema.index({
  cashOutAvailable: 1,
  status: 1,
});

// ============================================================
// VALIDATION
// ============================================================

betSchema.pre('validate', function (next) {
  // Single bets should not require accumulator selections.
  if (!this.isAccumulator) {
    this.accumulatorSelections = [];
    this.accumulatorId = undefined;
  }

  // Non-builder bets should not retain builder selections.
  if (!this.isBetBuilder) {
    this.betBuilderSelections = [];
  }

  // Calculate potential win if it was not supplied correctly.
  if (
    Number.isFinite(this.stake) &&
    Number.isFinite(this.odds) &&
    this.stake > 0 &&
    this.odds >= 1 &&
    (!Number.isFinite(this.potentialWin) || this.potentialWin < 0)
  ) {
    this.potentialWin = this.stake * this.odds;
  }

  // Keep combined odds consistent for normal single bets.
  if (!this.isAccumulator && this.combinedOdds <= 0) {
    this.combinedOdds = this.odds;
  }

  next();
});

// ============================================================
// STATUS HISTORY
// ============================================================

betSchema.pre('save', function (next) {
  if (this.isNew) {
    this.statusHistory.push({
      status: this.status,
      timestamp: new Date(),
      reason: 'Bet created',
    });
  }

  next();
});

// ============================================================
// HELPER METHODS
// ============================================================

betSchema.methods.setStatus = function (
  status: BetStatus,
  reason?: string,
): Promise<BetDocument> {
  this.status = status;

  this.statusHistory.push({
    status,
    timestamp: new Date(),
    reason,
  });

  return this.save();
};

// ============================================================
// STATIC HELPERS
// ============================================================

betSchema.statics.findUserBets = function (
  userId: Types.ObjectId | string,
  options: {
    status?: BetStatus;
    limit?: number;
    skip?: number;
  } = {},
) {
  const query: Record<string, unknown> = {
    userId,
  };

  if (options.status) {
    query.status = options.status;
  }

  const limit = Math.min(
    Math.max(options.limit ?? 50, 1),
    100,
  );

  const skip = Math.max(options.skip ?? 0, 0);

  return this.find(query)
    .sort({ createdAt: -1 })
    .skip(skip)
    .limit(limit);
};

betSchema.statics.findPendingForMatch = function (
  matchId: Types.ObjectId | string,
) {
  return this.find({
    matchId,
    status: 'pending',
  }).sort({ createdAt: 1 });
};

// ============================================================
// MODEL
// ============================================================

// Important for hot reload / ts-node / tests.
// This prevents:
// OverwriteModelError: Cannot overwrite `Bet` model once compiled.
const Bet: BetModel =
  (models.Bet as BetModel | undefined) ??
  model<IBet, BetModel>('Bet', betSchema);

export default Bet;

// Named export for code that uses:
// import { Bet } from './Bet';

export { Bet };