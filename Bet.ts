// ============================================================
// SHEBAODDS - BET MODEL
// Mongoose 8 + TypeScript
// Production-ready typed Bet model
// ============================================================

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
  marketType?: string;
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
// SYSTEM BET SELECTION
// ============================================================

export interface ISystemSelection {
  matchId: Types.ObjectId;
  selection: string;
  odds: number;
}

// ============================================================
// SYSTEM BET
// ============================================================

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
// BET DOCUMENT INTERFACE
// ============================================================

export interface IBet {

  // ----------------------------------------------------------
  // USER / MATCH
  // ----------------------------------------------------------

  userId: Types.ObjectId;
  matchId: Types.ObjectId;

  // ----------------------------------------------------------
  // BET DETAILS
  // ----------------------------------------------------------

  betType: BetType;

  marketType: string;

  selection: string;

  odds: number;

  stake: number;

  potentialWin: number;

  actualWin: number;

  // ----------------------------------------------------------
  // TAX
  // ----------------------------------------------------------

  taxAmount: number;

  taxRate: number;

  netWin: number;

  taxTransactionId?: Types.ObjectId;

  // ----------------------------------------------------------
  // ACCUMULATOR
  // ----------------------------------------------------------

  isAccumulator: boolean;

  accumulatorId?: string;

  accumulatorSelections: IAccumulatorSelection[];

  combinedOdds: number;

  // ----------------------------------------------------------
  // BET BUILDER
  // ----------------------------------------------------------

  isBetBuilder: boolean;

  betBuilderSelections: IBetBuilderSelection[];

  // ----------------------------------------------------------
  // SYSTEM BET
  // ----------------------------------------------------------

  isSystemBet: boolean;

  systemBetType?: string;

  systemSelections: ISystemSelection[];

  systemBets: ISystemBet[];

  totalSystemStake?: number;

  numberOfBets?: number;

  // ----------------------------------------------------------
  // LIVE BETTING
  // ----------------------------------------------------------

  isLive: boolean;

  betPlacedAtMinute?: number;

  // ----------------------------------------------------------
  // CASH OUT
  // ----------------------------------------------------------

  cashOutAvailable: boolean;

  cashOutAmount?: number;

  cashOutMultiplier?: number;

  cashedOutAt?: Date;

  autoCashOutMultiplier?: number;

  autoCashOutTriggered: boolean;

  // ----------------------------------------------------------
  // STATUS
  // ----------------------------------------------------------

  status: BetStatus;

  statusHistory: IBetStatusHistory[];

  // ----------------------------------------------------------
  // SETTLEMENT
  // ----------------------------------------------------------

  settledAt?: Date;

  settledScore?: string;

  actualOutcome?: string;

  winAmount: number;

  // ----------------------------------------------------------
  // PARTIAL SETTLEMENT
  // ----------------------------------------------------------

  isHalfWin: boolean;

  isHalfLoss: boolean;

  isPush: boolean;

  // ----------------------------------------------------------
  // PERIOD
  // ----------------------------------------------------------

  period: BetPeriod;

  // ----------------------------------------------------------
  // METADATA
  // ----------------------------------------------------------

  deviceInfo?: string;

  ipAddress?: string;

  // ----------------------------------------------------------
  // TIMESTAMPS
  // ----------------------------------------------------------

  createdAt: Date;

  updatedAt: Date;
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
  ): any;

  findPendingForMatch(
    matchId: Types.ObjectId | string
  ): any;

  getUserBetStats(
    userId: Types.ObjectId | string
  ): Promise<any>;
}

// ============================================================
// INSTANCE METHODS
// ============================================================

export interface IBetMethods {

  setStatus(
    status: BetStatus,
    reason?: string
  ): Promise<BetDocument>;
}

// ============================================================
// DOCUMENT TYPE
// ============================================================

export type BetDocument =
  HydratedDocument<
    IBet,
    IBetMethods
  >;

// ============================================================
// MODEL TYPE
// IMPORTANT:
// Mongoose 8 generic order:
// Model<Doc, QueryHelpers, Methods, Virtuals, Statics>
// ============================================================

export type BetModel =
  Model<
    IBet,
    {},
    IBetMethods,
    {},
    IBetModelStatics
  >;

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
    {
      _id: false,
    }
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
    }
  );

// ============================================================
// SYSTEM SELECTION SCHEMA
// ============================================================

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
    }
  );

// ============================================================
// SYSTEM BET SCHEMA
// ============================================================

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
        min: 1,
      },

      stake: {
        type: Number,
        required: true,
        min: 0,
      },

      potentialWin: {
        type: Number,
        required: true,
        min: 0,
      },

      status: {
        type: String,
        enum: BET_STATUSES,
        default: 'pending',
      },
    },
    {
      _id: false,
    }
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
    }
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
        default: 'ft_1x2',
      },

      selection: {
        type: String,
        required: true,
        trim: true,
        maxlength: 200,
        default: 'multiple',
      },

      odds: {
        type: Number,
        required: true,
        min: 1,
        max: 100000,
        default: 1,
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
      // SYSTEM BET
      // ========================================================

      isSystemBet: {
        type: Boolean,
        default: false,
        index: true,
      },

      systemBetType: {
        type: String,
        trim: true,
        index: true,
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
        min: 0,
      },

      numberOfBets: {
        type: Number,
        min: 0,
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
      // PERIOD
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

betSchema.index({
  systemBetType: 1,
  createdAt: -1,
});

// ============================================================
// VALIDATION
// ============================================================

betSchema.pre(
  'validate',
  function (next) {

    // --------------------------------------------------------
    // Non accumulator
    // --------------------------------------------------------

    if (!this.isAccumulator) {

      this.accumulatorSelections = [];

      this.accumulatorId = undefined;
    }

    // --------------------------------------------------------
    // Non builder
    // --------------------------------------------------------

    if (!this.isBetBuilder) {

      this.betBuilderSelections = [];
    }

    // --------------------------------------------------------
    // Non system bet
    // --------------------------------------------------------

    if (!this.isSystemBet) {

      this.systemSelections = [];

      this.systemBets = [];

      this.systemBetType = undefined;

      this.totalSystemStake = undefined;

      this.numberOfBets = undefined;
    }

    // --------------------------------------------------------
    // Potential win
    // --------------------------------------------------------

    if (
      Number.isFinite(this.stake) &&
      Number.isFinite(this.odds) &&
      this.stake > 0 &&
      this.odds >= 1
    ) {

      const calculatedPotentialWin =
        this.stake * this.odds;

      if (
        !Number.isFinite(this.potentialWin) ||
        this.potentialWin < 0
      ) {

        this.potentialWin =
          calculatedPotentialWin;
      }
    }

    // --------------------------------------------------------
    // Combined odds
    // --------------------------------------------------------

    if (
      !this.isAccumulator &&
      !this.isSystemBet &&
      (
        !Number.isFinite(this.combinedOdds) ||
        this.combinedOdds < 1
      )
    ) {

      this.combinedOdds =
        this.odds;
    }

    next();
  }
);

// ============================================================
// STATUS HISTORY
// ============================================================

betSchema.pre(
  'save',
  function (next) {

    if (this.isNew) {

      this.statusHistory.push({
        status: this.status,
        timestamp: new Date(),
        reason: 'Bet created',
      });
    }

    next();
  }
);

// ============================================================
// INSTANCE METHOD
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

    const query: Record<string, unknown> = {
      userId,
    };

    if (options.status) {
      query.status = options.status;
    }

    const limit = Math.min(
      Math.max(
        Math.floor(options.limit ?? 50),
        1
      ),
      100
    );

    const skip = Math.max(
      Math.floor(options.skip ?? 0),
      0
    );

    return this.find(query)
      .sort({
        createdAt: -1,
      })
      .skip(skip)
      .limit(limit);
  };

// ============================================================
// STATIC: FIND PENDING BETS FOR MATCH
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

    const userObjectId =
      typeof userId === 'string'
        ? new Types.ObjectId(userId)
        : userId;

    const result =
      await this.aggregate([
        {
          $match: {
            userId: userObjectId,
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

            pendingBets: {
              $sum: {
                $cond: [
                  {
                    $eq: [
                      '$status',
                      'pending',
                    ],
                  },
                  1,
                  0,
                ],
              },
            },

            wonBets: {
              $sum: {
                $cond: [
                  {
                    $eq: [
                      '$status',
                      'won',
                    ],
                  },
                  1,
                  0,
                ],
              },
            },

            lostBets: {
              $sum: {
                $cond: [
                  {
                    $eq: [
                      '$status',
                      'lost',
                    ],
                  },
                  1,
                  0,
                ],
              },
            },

            cashoutBets: {
              $sum: {
                $cond: [
                  {
                    $eq: [
                      '$status',
                      'cashout',
                    ],
                  },
                  1,
                  0,
                ],
              },
            },

            voidBets: {
              $sum: {
                $cond: [
                  {
                    $eq: [
                      '$status',
                      'void',
                    ],
                  },
                  1,
                  0,
                ],
              },
            },
          },
        },
      ]);

    const stats = result[0];

    if (!stats) {

      return {
        totalBets: 0,
        totalStake: 0,
        totalWon: 0,
        pendingBets: 0,
        wonBets: 0,
        lostBets: 0,
        cashoutBets: 0,
        voidBets: 0,
        profit: 0,
      };
    }

    return {
      totalBets:
        stats.totalBets || 0,

      totalStake:
        stats.totalStake || 0,

      totalWon:
        stats.totalWon || 0,

      pendingBets:
        stats.pendingBets || 0,

      wonBets:
        stats.wonBets || 0,

      lostBets:
        stats.lostBets || 0,

      cashoutBets:
        stats.cashoutBets || 0,

      voidBets:
        stats.voidBets || 0,

      profit:
        (stats.totalWon || 0) -
        (stats.totalStake || 0),
    };
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
  mongoose.model<
    IBet,
    BetModel
  >(
    'Bet',
    betSchema
  );

export default Bet;