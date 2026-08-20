import mongoose, {
  HydratedDocument,
  Model,
  Schema,
  Types,
} from 'mongoose';

// ============================================================
// ENUMS
// ============================================================

export const BET_STATUSES = [
  'pending',
  'running',
  'won',
  'lost',
  'void',
  'cancelled',
  'cashout',
  'cashed_out',
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

// Compatibility object used by bettingRoutes.ts
export const BET_STATUS = {
  PENDING: 'pending',
  RUNNING: 'running',
  WON: 'won',
  LOST: 'lost',
  VOID: 'void',
  CANCELLED: 'cancelled',
  CASHOUT: 'cashout',
  CASHED_OUT: 'cashed_out',
  HALF_WON: 'half_won',
  HALF_LOST: 'half_lost',
  PUSH: 'push',
} as const;

// ============================================================
// ACCUMULATOR
// ============================================================

export interface IAccumulatorSelection {
  matchId: Types.ObjectId;
  marketType?: string;
  selection: string;
  odds: number;
  status?: string;
  outcome?: string;
}

// ============================================================
// BET BUILDER
// ============================================================

export interface IBetBuilderSelection {
  marketType: string;
  selection: string;
  odds: number;
}

// ============================================================
// SYSTEM BET
// ============================================================

export interface ISystemSelection {
  matchId: Types.ObjectId;
  selection: string;
  odds: number;
}

export interface ISystemBet {
  selections: number[];
  combinedOdds: number;
  stake: number;
  potentialWin: number;
  status: BetStatus;
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
// BET DOCUMENT
// ============================================================

export interface IBet {

  userId: Types.ObjectId;
  matchId: Types.ObjectId;

  betType: BetType;

  marketType: string;
  selection: string;
  odds: number;

  stake: number;
  potentialWin: number;
  actualWin: number;

  // Tax
  taxAmount: number;
  taxRate: number;
  netWin: number;
  taxTransactionId?: Types.ObjectId;

  // Wallet tracking
  usedRealBalance?: number;
  usedBonusBalance?: number;

  // Accumulator
  isAccumulator: boolean;
  accumulatorId?: string;
  accumulatorSelections: IAccumulatorSelection[];
  combinedOdds: number;

  // Bet builder
  isBetBuilder: boolean;
  betBuilderSelections: IBetBuilderSelection[];

  // System bet
  isSystemBet: boolean;
  systemBetType?: string;
  systemSelections: ISystemSelection[];
  systemBets: ISystemBet[];
  totalSystemStake?: number;
  numberOfBets?: number;

  // Live
  isLive: boolean;
  betPlacedAtMinute?: number;

  // Cashout
  cashOutAvailable: boolean;
  cashOutAmount?: number;
  cashOutMultiplier?: number;
  cashedOutAt?: Date;
  autoCashOutMultiplier?: number;
  autoCashOutTriggered: boolean;

  // Status
  status: BetStatus;
  statusHistory: IBetStatusHistory[];

  // Settlement
  settledAt?: Date;
  settledScore?: string;
  actualOutcome?: string;
  winAmount: number;

  // Partial settlement
  isHalfWin: boolean;
  isHalfLoss: boolean;
  isPush: boolean;

  // Period
  period: BetPeriod;

  // Metadata
  deviceInfo?: string | Record<string, any>;
  ipAddress?: string;

  createdAt: Date;
  updatedAt: Date;
}

// ============================================================
// CASHOUT RESULT
// ============================================================

export interface TaxResult {
  grossWin: number;
  taxAmount: number;
  netWin: number;
  taxRate: number;
}

// ============================================================
// INSTANCE METHODS
// ============================================================

export interface IBetMethods {

  setStatus(
    status: BetStatus,
    reason?: string
  ): Promise<BetDocument>;

  checkCashOutAvailability(
    currentMinute: number,
    currentLiveOdds?: number | null
  ): boolean;

  calculateTax(): TaxResult;
}

// ============================================================
// STATIC METHODS
// ============================================================

export interface IBetModelStatics {

  findUserBets(
    userId: Types.ObjectId | string,
    options?: {
      status?: BetStatus;
      limit?: number;
      skip?: number;
    }
  ): ReturnType<Model<IBet>['find']>;

  findPendingForMatch(
    matchId: Types.ObjectId | string
  ): ReturnType<Model<IBet>['find']>;

  getUserBetStats(
    userId: Types.ObjectId | string
  ): Promise<any>;
}

// ============================================================
// IMPORTANT MONGOOSE 8 TYPES
// ============================================================
//
// IBetModelStatics MUST be the 5th generic argument.
// It must NOT be the 2nd generic argument.
//
// Model<
//   RawDocument,
//   QueryHelpers,
//   InstanceMethods,
//   Virtuals,
//   StaticMethods
// >

export type BetDocument =
  HydratedDocument<IBet, IBetMethods>;

export type BetModel =
  Model<
    IBet,
    {},              // Query helpers
    IBetMethods,     // Instance methods
    {},              // Virtuals
    IBetModelStatics // Static methods
  >;

// ============================================================
// SCHEMAS
// ============================================================

const accumulatorSelectionSchema =
  new Schema<IAccumulatorSelection>(
    {
      matchId: {
        type: Schema.Types.ObjectId,
        ref: 'Match',
        required: true,
      },

      marketType: {
        type: String,
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

      status: {
        type: String,
        trim: true,
      },

      outcome: {
        type: String,
        trim: true,
      },
    },
    { _id: false }
  );

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
    { _id: false }
  );

const systemSelectionSchema =
  new Schema<ISystemSelection>(
    {
      matchId: {
        type: Schema.Types.ObjectId,
        ref: 'Match',
        required: true,
      },

      selection: {
        type: String,
        required: true,
      },

      odds: {
        type: Number,
        required: true,
      },
    },
    { _id: false }
  );

const systemBetSchema =
  new Schema<ISystemBet>(
    {
      selections: {
        type: [Number],
        required: true,
      },

      combinedOdds: {
        type: Number,
        required: true,
      },

      stake: {
        type: Number,
        required: true,
      },

      potentialWin: {
        type: Number,
        required: true,
      },

      status: {
        type: String,
        enum: BET_STATUSES,
        default: 'pending',
      },
    },
    { _id: false }
  );

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
    { _id: false }
  );

// ============================================================
// BET SCHEMA
// ============================================================

const betSchema =
  new Schema<
    IBet,
    BetModel,
    IBetMethods
  >(
    {
      userId: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        index: true,
      },

      matchId: {
        type: Schema.Types.ObjectId,
        ref: 'Match',
        required: true,
        index: true,
      },

      betType: {
        type: String,
        enum: BET_TYPES,
        required: true,
        default: 'single',
        index: true,
      },

      marketType: {
        type: String,
        default: '',
        trim: true,
        maxlength: 100,
      },

      selection: {
        type: String,
        default: '',
        trim: true,
        maxlength: 200,
      },

      odds: {
        type: Number,
        default: 1,
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

      usedRealBalance: {
        type: Number,
        default: 0,
        min: 0,
      },

      usedBonusBalance: {
        type: Number,
        default: 0,
        min: 0,
      },

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

      isBetBuilder: {
        type: Boolean,
        default: false,
        index: true,
      },

      betBuilderSelections: {
        type: [betBuilderSelectionSchema],
        default: [],
      },

      isSystemBet: {
        type: Boolean,
        default: false,
        index: true,
      },

      systemBetType: {
        type: String,
        trim: true,
      },

      systemSelections: {
        type: [systemSelectionSchema],
        default: [],
      },

      systemBets: {
        type: [systemBetSchema],
        default: [],
      },

      totalSystemStake: {
        type: Number,
        default: 0,
      },

      numberOfBets: {
        type: Number,
        default: 0,
      },

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

      settledAt: Date,

      settledScore: {
        type: String,
        trim: true,
      },

      actualOutcome: {
        type: String,
        trim: true,
      },

      winAmount: {
        type: Number,
        default: 0,
        min: 0,
      },

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

      period: {
        type: String,
        enum: BET_PERIODS,
        default: 'full',
        required: true,
      },

      deviceInfo: {
        type: Schema.Types.Mixed,
      },

      ipAddress: {
        type: String,
        trim: true,
        maxlength: 100,
      },
    },
    {
      timestamps: true,
      id: false,
      strict: true,
      versionKey: false,

      toJSON: {
        virtuals: true,
      },

      toObject: {
        virtuals: true,
      },
    }
  );

// ============================================================
// INDEXES
// ============================================================

betSchema.index({
  userId: 1,
  status: 1,
  createdAt: -1,
});

betSchema.index({
  userId: 1,
  createdAt: -1,
});

betSchema.index({
  matchId: 1,
  status: 1,
});

betSchema.index({
  isLive: 1,
  status: 1,
});

betSchema.index({
  accumulatorId: 1,
});

betSchema.index({
  status: 1,
  createdAt: 1,
});

betSchema.index({
  cashOutAvailable: 1,
  status: 1,
});

// ============================================================
// VALIDATION
// ============================================================

betSchema.pre('validate', function (next) {

  if (!this.isAccumulator) {
    this.accumulatorSelections = [];
    this.accumulatorId = undefined;
  }

  if (!this.isBetBuilder) {
    this.betBuilderSelections = [];
  }

  if (!this.isSystemBet) {
    this.systemSelections = [];
    this.systemBets = [];
    this.systemBetType = undefined;
    this.totalSystemStake = 0;
    this.numberOfBets = 0;
  }

  if (
    Number.isFinite(this.stake) &&
    Number.isFinite(this.odds) &&
    this.stake > 0 &&
    this.odds >= 1
  ) {
    if (
      !Number.isFinite(this.potentialWin) ||
      this.potentialWin < 0
    ) {
      this.potentialWin =
        this.stake * this.odds;
    }
  }

  if (
    !this.isAccumulator &&
    (
      !Number.isFinite(this.combinedOdds) ||
      this.combinedOdds < 1
    )
  ) {
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
// INSTANCE: SET STATUS
// ============================================================

betSchema.methods.setStatus =
  function (
    this: BetDocument,
    status: BetStatus,
    reason?: string
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
// INSTANCE: CASHOUT CHECK
// ============================================================

betSchema.methods.checkCashOutAvailability =
  function (
    this: BetDocument,
    currentMinute: number,
    currentLiveOdds?: number | null
  ): boolean {

    if (
      this.status !== 'pending' &&
      this.status !== 'running'
    ) {
      this.cashOutAvailable = false;
      return false;
    }

    if (!this.isLive) {
      this.cashOutAvailable = false;
      return false;
    }

    if (
      !Number.isFinite(currentMinute) ||
      currentMinute < 0
    ) {
      this.cashOutAvailable = false;
      return false;
    }

    if (
      currentLiveOdds !== undefined &&
      currentLiveOdds !== null &&
      Number.isFinite(currentLiveOdds) &&
      currentLiveOdds > 0
    ) {

      const multiplier =
        Math.max(
          0.05,
          Math.min(
            1,
            currentLiveOdds / Math.max(this.odds, 1)
          )
        );

      this.cashOutMultiplier = multiplier;

      this.cashOutAmount =
        Math.max(
          0,
          this.stake * multiplier
        );

      this.cashOutAvailable =
        this.cashOutAmount > 0;

      return this.cashOutAvailable;
    }

    // Fallback cashout value
    const fallbackMultiplier = 0.8;

    this.cashOutMultiplier =
      fallbackMultiplier;

    this.cashOutAmount =
      this.stake * fallbackMultiplier;

    this.cashOutAvailable =
      this.cashOutAmount > 0;

    return this.cashOutAvailable;
  };

// ============================================================
// INSTANCE: TAX CALCULATION
// ============================================================

betSchema.methods.calculateTax =
  function (
    this: BetDocument
  ): TaxResult {

    const grossWin =
      Math.max(
        0,
        Number(this.actualWin || 0)
      );

    const stake =
      Math.max(
        0,
        Number(this.stake || 0)
      );

    // Tax only applies to profit
    const taxableProfit =
      Math.max(
        0,
        grossWin - stake
      );

    const taxRate =
      Number.isFinite(this.taxRate)
        ? this.taxRate
        : 0.15;

    const taxAmount =
      taxableProfit * taxRate;

    const netWin =
      Math.max(
        0,
        grossWin - taxAmount
      );

    this.taxAmount = taxAmount;
    this.netWin = netWin;

    return {
      grossWin,
      taxAmount,
      netWin,
      taxRate,
    };
  };

// ============================================================
// STATIC: FIND USER BETS
// ============================================================

betSchema.statics.findUserBets =
  function (
    this: BetModel,
    userId: Types.ObjectId | string,
    options: {
      status?: BetStatus;
      limit?: number;
      skip?: number;
    } = {}
  ) {

    const query: Record<string, any> = {
      userId,
    };

    if (options.status) {
      query.status = options.status;
    }

    const limit =
      Math.min(
        Math.max(
          Math.floor(options.limit ?? 50),
          1
        ),
        100
      );

    const skip =
      Math.max(
        Math.floor(options.skip ?? 0),
        0
      );

    return this.find(query)
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);
  };

// ============================================================
// STATIC: FIND PENDING BETS
// ============================================================

betSchema.statics.findPendingForMatch =
  function (
    this: BetModel,
    matchId: Types.ObjectId | string
  ) {

    return this.find({
      matchId,
      status: 'pending',
    }).sort({
      createdAt: 1,
    });
  };

// ============================================================
// STATIC: USER BET STATISTICS
// ============================================================

betSchema.statics.getUserBetStats =
  async function (
    this: BetModel,
    userId: Types.ObjectId | string
  ) {

    const result = await this.aggregate([
      {
        $match: {
          userId: new Types.ObjectId(
            userId.toString()
          ),
        },
      },
      {
        $group: {
          _id: null,

          totalBets: {
            $sum: 1,
          },

          totalStake: {
            $sum: '$stake',
          },

          totalWon: {
            $sum: '$actualWin',
          },

          totalPotentialWin: {
            $sum: '$potentialWin',
          },

          wonBets: {
            $sum: {
              $cond: [
                { $eq: ['$status', 'won'] },
                1,
                0,
              ],
            },
          },

          lostBets: {
            $sum: {
              $cond: [
                { $eq: ['$status', 'lost'] },
                1,
                0,
              ],
            },
          },
        },
      },
    ]);

    return (
      result[0] || {
        totalBets: 0,
        totalStake: 0,
        totalWon: 0,
        totalPotentialWin: 0,
        wonBets: 0,
        lostBets: 0,
      }
    );
  };

// ============================================================
// MODEL
// ============================================================

export const Bet: BetModel =
  (
    mongoose.models.Bet as
      | BetModel
      | undefined
  ) ??
  mongoose.model<IBet, BetModel>(
    'Bet',
    betSchema
  );

export default Bet;