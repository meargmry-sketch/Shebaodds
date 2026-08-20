// ============================================
// SHEBAODDS - TRANSACTION MODEL
// Mongoose 8 + TypeScript
// ============================================

import mongoose, {
  Document,
  Model,
  Schema,
  Types,
} from 'mongoose';

// ============================================================
// TRANSACTION TYPES
// ============================================================

export const TRANSACTION_TYPES = {
  DEPOSIT: 'deposit',
  WITHDRAWAL: 'withdrawal',

  BET_PLACE: 'bet_place',
  BET_WIN: 'bet_win',
  BET_LOSS: 'bet_loss',

  BONUS: 'bonus',
  CASHBACK: 'cashback',
  REFUND: 'refund',

  ADJUSTMENT: 'adjustment',
  TAX: 'tax',
  FEE: 'fee',
  TRANSFER: 'transfer',
  PROMOTION: 'promotion',
  JACKPOT: 'jackpot',
} as const;

export type TransactionType =
  (typeof TRANSACTION_TYPES)[keyof typeof TRANSACTION_TYPES];

// ============================================================
// PAYMENT METHODS
// ============================================================

export const PAYMENT_METHODS = {
  TELE_BIRR: 'tele_birr',
  CBE: 'cbe',
  CHAPA: 'chapa',

  STRIPE: 'stripe',
  PAYPAL: 'paypal',

  CRYPTO_BTC: 'crypto_btc',
  CRYPTO_ETH: 'crypto_eth',
  CRYPTO_USDT: 'crypto_usdt',

  BANK_TRANSFER: 'bank_transfer',

  BONUS: 'bonus',
  CASH: 'cash',
} as const;

export type PaymentMethodType =
  (typeof PAYMENT_METHODS)[keyof typeof PAYMENT_METHODS];

// ============================================================
// TRANSACTION STATUS
// ============================================================

export const TRANSACTION_STATUS = {
  PENDING: 'pending',
  PROCESSING: 'processing',
  COMPLETED: 'completed',

  FAILED: 'failed',
  CANCELLED: 'cancelled',
  REFUNDED: 'refunded',
} as const;

export type TransactionStatusType =
  (typeof TRANSACTION_STATUS)[keyof typeof TRANSACTION_STATUS];

// ============================================================
// PAYMENT DETAILS
// ============================================================

export interface PaymentDetails {
  phoneNumber?: string;
  accountNumber?: string;

  transactionId?: string;

  cardLast4?: string;
  cardBrand?: string;
  cardExpiry?: string;

  cryptoCurrency?: string;
  cryptoAddress?: string;
  cryptoTxHash?: string;
  cryptoConfirmations: number;

  bankName?: string;
  bankAccount?: string;
  bankReference?: string;

  notes?: string;

  metadata?: unknown;
}

// ============================================================
// TRANSACTION INTERFACE
// ============================================================

export interface ITransaction extends Document {
  userId: Types.ObjectId;

  betId?: Types.ObjectId;
  bonusId?: Types.ObjectId;

  type: TransactionType;

  subType?: string;

  amount: number;

  fee: number;

  taxAmount: number;

  netAmount: number;

  paymentMethod?: PaymentMethodType;

  paymentReference?: string;

  paymentGatewayReference?: string;

  paymentDetails: PaymentDetails;

  previousBalance: number;

  previousBonusBalance: number;

  newBalance: number;

  newBonusBalance: number;

  status: TransactionStatusType;

  failureReason?: string;

  failureCode?: string;

  requiresApproval: boolean;

  approvedBy?: Types.ObjectId;

  approvedAt?: Date;

  processedBy?: Types.ObjectId;

  processedAt?: Date;

  ipAddress?: string;

  userAgent?: string;

  location?: {
    country?: string;
    city?: string;
  };

  notes?: string;

  metadata?: unknown;

  createdAt: Date;

  completedAt?: Date;

  updatedAt: Date;

  isDeposit: boolean;
  isWithdrawal: boolean;
  isCredit: boolean;
  isDebit: boolean;

  complete(): Promise<ITransaction>;

  fail(
    reason: string,
    code?: string
  ): Promise<ITransaction>;

  approve(
    adminId: Types.ObjectId
  ): Promise<ITransaction>;
}

// ============================================================
// MODEL INTERFACE
// ============================================================

export interface ITransactionModel
  extends Model<ITransaction> {

  getUserBalance(
    userId: string | Types.ObjectId
  ): Promise<number>;

  getUserDepositTotal(
    userId: string | Types.ObjectId
  ): Promise<number>;
}

// ============================================================
// SCHEMA
// ============================================================

const transactionSchema =
  new Schema<
    ITransaction,
    ITransactionModel
  >(
    {
      // --------------------------------------------------------
      // USER
      // --------------------------------------------------------

      userId: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        index: true,
      },

      // --------------------------------------------------------
      // BET
      // --------------------------------------------------------

      betId: {
        type: Schema.Types.ObjectId,
        ref: 'Bet',
        index: true,
      },

      // --------------------------------------------------------
      // BONUS
      // --------------------------------------------------------

      bonusId: {
        type: Schema.Types.ObjectId,
        ref: 'Bonus',
        index: true,
      },

      // --------------------------------------------------------
      // TYPE
      // --------------------------------------------------------

      type: {
        type: String,
        enum: Object.values(TRANSACTION_TYPES),
        required: true,
        index: true,
      },

      subType: {
        type: String,
      },

      // --------------------------------------------------------
      // MONEY
      // --------------------------------------------------------

      amount: {
        type: Number,
        required: true,
        min: 0,
      },

      fee: {
        type: Number,
        default: 0,
        min: 0,
      },

      taxAmount: {
        type: Number,
        default: 0,
        min: 0,
      },

      netAmount: {
        type: Number,
        required: true,
      },

      // --------------------------------------------------------
      // PAYMENT METHOD
      // --------------------------------------------------------

      paymentMethod: {
        type: String,
        enum: Object.values(PAYMENT_METHODS),
        index: true,
      },

      paymentReference: {
        type: String,
        unique: true,
        sparse: true,
        index: true,
      },

      paymentGatewayReference: {
        type: String,
        index: true,
      },

      // --------------------------------------------------------
      // PAYMENT DETAILS
      // --------------------------------------------------------

      paymentDetails: {
        phoneNumber: String,

        accountNumber: String,

        transactionId: String,

        cardLast4: String,

        cardBrand: String,

        cardExpiry: String,

        cryptoCurrency: String,

        cryptoAddress: String,

        cryptoTxHash: String,

        cryptoConfirmations: {
          type: Number,
          default: 0,
        },

        bankName: String,

        bankAccount: String,

        bankReference: String,

        notes: String,

        metadata: Schema.Types.Mixed,
      },

      // --------------------------------------------------------
      // BALANCES
      // --------------------------------------------------------

      previousBalance: {
        type: Number,
        required: true,
      },

      previousBonusBalance: {
        type: Number,
        default: 0,
      },

      newBalance: {
        type: Number,
        required: true,
      },

      newBonusBalance: {
        type: Number,
        default: 0,
      },

      // --------------------------------------------------------
      // STATUS
      // --------------------------------------------------------

      status: {
        type: String,
        enum: Object.values(TRANSACTION_STATUS),
        default: TRANSACTION_STATUS.PENDING,
        index: true,
      },

      failureReason: {
        type: String,
      },

      failureCode: {
        type: String,
      },

      // --------------------------------------------------------
      // APPROVAL
      // --------------------------------------------------------

      requiresApproval: {
        type: Boolean,
        default: false,
        index: true,
      },

      approvedBy: {
        type: Schema.Types.ObjectId,
        ref: 'User',
      },

      approvedAt: {
        type: Date,
      },

      // --------------------------------------------------------
      // PROCESSING
      // --------------------------------------------------------

      processedBy: {
        type: Schema.Types.ObjectId,
        ref: 'User',
      },

      processedAt: {
        type: Date,
      },

      // --------------------------------------------------------
      // REQUEST INFORMATION
      // --------------------------------------------------------

      ipAddress: {
        type: String,
      },

      userAgent: {
        type: String,
      },

      location: {
        country: String,
        city: String,
      },

      notes: {
        type: String,
      },

      metadata: {
        type: Schema.Types.Mixed,
      },

      // --------------------------------------------------------
      // DATES
      // --------------------------------------------------------

      createdAt: {
        type: Date,
        default: Date.now,
        index: true,
      },

      completedAt: {
        type: Date,
      },

      updatedAt: {
        type: Date,
        default: Date.now,
      },
    },
    {
      timestamps: true,

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

transactionSchema.index({
  userId: 1,
  createdAt: -1,
});

transactionSchema.index({
  userId: 1,
  type: 1,
});

transactionSchema.index({
  status: 1,
  createdAt: -1,
});

transactionSchema.index({
  status: 1,
  type: 1,
  createdAt: -1,
});

transactionSchema.index({
  paymentReference: 1,
});

transactionSchema.index({
  createdAt: -1,
});

// ============================================================
// VIRTUAL: IS DEPOSIT
// ============================================================

transactionSchema
  .virtual('isDeposit')
  .get(function (
    this: ITransaction
  ): boolean {
    return (
      this.type ===
      TRANSACTION_TYPES.DEPOSIT
    );
  });

// ============================================================
// VIRTUAL: IS WITHDRAWAL
// ============================================================

transactionSchema
  .virtual('isWithdrawal')
  .get(function (
    this: ITransaction
  ): boolean {
    return (
      this.type ===
      TRANSACTION_TYPES.WITHDRAWAL
    );
  });

// ============================================================
// VIRTUAL: IS CREDIT
// ============================================================

transactionSchema
  .virtual('isCredit')
  .get(function (
    this: ITransaction
  ): boolean {
    return [
      TRANSACTION_TYPES.DEPOSIT,
      TRANSACTION_TYPES.BET_WIN,
      TRANSACTION_TYPES.BONUS,
      TRANSACTION_TYPES.CASHBACK,
      TRANSACTION_TYPES.REFUND,
      TRANSACTION_TYPES.JACKPOT,
    ].includes(this.type);
  });

// ============================================================
// VIRTUAL: IS DEBIT
// ============================================================

transactionSchema
  .virtual('isDebit')
  .get(function (
    this: ITransaction
  ): boolean {
    return [
      TRANSACTION_TYPES.BET_PLACE,
      TRANSACTION_TYPES.WITHDRAWAL,
      TRANSACTION_TYPES.FEE,
      TRANSACTION_TYPES.TAX,
    ].includes(this.type);
  });

// ============================================================
// COMPLETE
// ============================================================

transactionSchema.methods.complete =
  function (
    this: ITransaction
  ): Promise<ITransaction> {
    this.status =
      TRANSACTION_STATUS.COMPLETED;

    this.completedAt = new Date();

    this.updatedAt = new Date();

    return this.save();
  };

// ============================================================
// FAIL
// ============================================================

transactionSchema.methods.fail =
  function (
    this: ITransaction,
    reason: string,
    code?: string
  ): Promise<ITransaction> {
    this.status =
      TRANSACTION_STATUS.FAILED;

    this.failureReason = reason;

    if (code) {
      this.failureCode = code;
    }

    this.updatedAt = new Date();

    return this.save();
  };

// ============================================================
// APPROVE
// ============================================================

transactionSchema.methods.approve =
  function (
    this: ITransaction,
    adminId: Types.ObjectId
  ): Promise<ITransaction> {
    this.requiresApproval = false;

    this.approvedBy = adminId;

    this.approvedAt = new Date();

    this.status =
      TRANSACTION_STATUS.PROCESSING;

    this.updatedAt = new Date();

    return this.save();
  };

// ============================================================
// STATIC: GET USER BALANCE
// ============================================================

transactionSchema.statics.getUserBalance =
  async function (
    this: ITransactionModel,
    userId: string | Types.ObjectId
  ): Promise<number> {

    const objectId =
      typeof userId === 'string'
        ? new Types.ObjectId(userId)
        : userId;

    const result = await this.aggregate<{
      totalCredit: number;
      totalDebit: number;
    }>([
      {
        $match: {
          userId: objectId,

          status:
            TRANSACTION_STATUS.COMPLETED,
        },
      },

      {
        $group: {
          _id: null,

          totalCredit: {
            $sum: {
              $cond: [
                {
                  $in: [
                    '$type',
                    [
                      TRANSACTION_TYPES.DEPOSIT,
                      TRANSACTION_TYPES.BET_WIN,
                      TRANSACTION_TYPES.BONUS,
                      TRANSACTION_TYPES.CASHBACK,
                      TRANSACTION_TYPES.REFUND,
                      TRANSACTION_TYPES.JACKPOT,
                    ],
                  ],
                },

                '$netAmount',

                0,
              ],
            },
          },

          totalDebit: {
            $sum: {
              $cond: [
                {
                  $in: [
                    '$type',
                    [
                      TRANSACTION_TYPES.BET_PLACE,
                      TRANSACTION_TYPES.WITHDRAWAL,
                      TRANSACTION_TYPES.FEE,
                      TRANSACTION_TYPES.TAX,
                    ],
                  ],
                },

                '$netAmount',

                0,
              ],
            },
          },
        },
      },
    ]);

    const first = result[0];

    if (!first) {
      return 0;
    }

    return (
      (first.totalCredit || 0) -
      (first.totalDebit || 0)
    );
  };

// ============================================================
// STATIC: GET USER DEPOSIT TOTAL
// ============================================================

transactionSchema.statics.getUserDepositTotal =
  async function (
    this: ITransactionModel,
    userId: string | Types.ObjectId
  ): Promise<number> {

    const objectId =
      typeof userId === 'string'
        ? new Types.ObjectId(userId)
        : userId;

    const result = await this.aggregate<{
      total: number;
    }>([
      {
        $match: {
          userId: objectId,

          type:
            TRANSACTION_TYPES.DEPOSIT,

          status:
            TRANSACTION_STATUS.COMPLETED,
        },
      },

      {
        $group: {
          _id: null,

          total: {
            $sum: '$amount',
          },
        },
      },
    ]);

    return result[0]?.total || 0;
  };

// ============================================================
// IMPORTANT MODEL EXPORT
// ============================================================
//
// This is the important TypeScript fix.
//
// Do NOT use:
//
// mongoose.models.Transaction || mongoose.model(...)
//
// directly.
//
// Explicitly cast the existing model so TypeScript sees
// exactly ONE model type.
//

const ExistingTransaction =
  mongoose.models.Transaction as
    | ITransactionModel
    | undefined;

export const Transaction: ITransactionModel =
  ExistingTransaction ??
  mongoose.model<
    ITransaction,
    ITransactionModel
  >(
    'Transaction',
    transactionSchema
  );

export default Transaction;