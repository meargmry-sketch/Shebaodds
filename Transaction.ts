import mongoose, { Schema, Document, Model } from 'mongoose';

/**
 * SHEBAODDS TRANSACTION MODEL
 * Compatible with walletRoutes, adminTransactionRoutes,
 * bettingRoutes and responsibleGamblingMiddleware.
 */

/* ============================================================
   TRANSACTION STATUS
   ============================================================ */

export const TX_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  CANCELLED: 'cancelled',
  PROCESSING: 'processing',
  COMPLETED: 'completed',
  FAILED: 'failed'
} as const;

export type TxStatus = typeof TX_STATUS[keyof typeof TX_STATUS];

/**
 * Backwards-compatible export used by existing routes.
 */
export const TRANSACTION_STATUS = {
  PENDING: TX_STATUS.PENDING,
  APPROVED: TX_STATUS.APPROVED,
  REJECTED: TX_STATUS.REJECTED,
  CANCELLED: TX_STATUS.CANCELLED,
  PROCESSING: TX_STATUS.PROCESSING,
  COMPLETED: TX_STATUS.COMPLETED,
  FAILED: TX_STATUS.FAILED
} as const;

export type TransactionStatus =
  typeof TRANSACTION_STATUS[keyof typeof TRANSACTION_STATUS];


/* ============================================================
   TRANSACTION TYPES
   ============================================================ */

export const TX_TYPE = {
  DEPOSIT: 'deposit',
  WITHDRAWAL: 'withdrawal',
  BET: 'bet',
  WIN: 'win',
  REFUND: 'refund',
  BONUS: 'bonus',
  ADJUSTMENT: 'adjustment'
} as const;

export type TxType = typeof TX_TYPE[keyof typeof TX_TYPE];

/**
 * Backwards-compatible export.
 */
export const TRANSACTION_TYPES = {
  DEPOSIT: TX_TYPE.DEPOSIT,
  WITHDRAWAL: TX_TYPE.WITHDRAWAL,
  BET: TX_TYPE.BET,
  WIN: TX_TYPE.WIN,
  REFUND: TX_TYPE.REFUND,
  BONUS: TX_TYPE.BONUS,
  ADJUSTMENT: TX_TYPE.ADJUSTMENT
} as const;

export type TransactionType =
  typeof TRANSACTION_TYPES[keyof typeof TRANSACTION_TYPES];


/* ============================================================
   PAYMENT METHODS
   ============================================================ */

export const PAYMENT_METHOD = {
  TELEBIRR: 'telebirr',
  CBE_BIRR: 'cbe_birr',
  AWASH_BIRR: 'awash_birr',
  M_PESA: 'm_pesa',
  HELLO_CASH: 'hello_cash'
} as const;

export type PaymentMethod =
  typeof PAYMENT_METHOD[keyof typeof PAYMENT_METHOD];

/**
 * Existing code imports PAYMENT_METHODS.
 * Keep both names available.
 */
export const PAYMENT_METHODS = {
  TELEBIRR: PAYMENT_METHOD.TELEBIRR,
  CBE_BIRR: PAYMENT_METHOD.CBE_BIRR,
  AWASH_BIRR: PAYMENT_METHOD.AWASH_BIRR,
  M_PESA: PAYMENT_METHOD.M_PESA,
  HELLO_CASH: PAYMENT_METHOD.HELLO_CASH
} as const;


/* ============================================================
   TRANSACTION INTERFACE
   ============================================================ */

export interface ITransaction extends Document {

  // Identity
  txId: string;
  userId: string;
  username: string;

  // Transaction
  type: TransactionType;
  method: PaymentMethod;
  amount: number;
  status: TransactionStatus;

  // Payment proof
  senderAccount?: string;
  receiverAccount?: string;
  externalRef?: string;
  screenshotUrl?: string;

  // Payment gateway
  paymentReference?: string;
  paymentGatewayReference?: string;
  paymentMethod?: string;
  paymentDetails?: Record<string, unknown>;

  // Failure / completion
  failureReason?: string;
  completedAt?: Date;

  // Admin approval workflow
  requiresApproval?: boolean;
  approvedBy?: string;
  approvedAt?: Date;

  processedBy?: string;
  processedAt?: Date;

  notes?: string;

  // Ledger
  previousBalance?: number;
  newBalance?: number;

  previousBonusBalance?: number;
  newBonusBalance?: number;

  balanceBefore?: number;
  balanceAfter?: number;

  // Calculated amount
  netAmount?: number;

  // Review information
  reviewedBy?: string;
  reviewedByUsername?: string;
  reviewNote?: string;
  reviewedAt?: Date;

  // Timestamps
  createdAt: Date;
  updatedAt: Date;
}


/* ============================================================
   SCHEMA
   ============================================================ */

const txSchema = new Schema<ITransaction>(
  {
    txId: {
      type: String,
      required: true,
      unique: true,
      index: true
    },

    userId: {
      type: String,
      required: true,
      index: true
    },

    username: {
      type: String,
      required: true
    },

    type: {
      type: String,
      enum: Object.values(TRANSACTION_TYPES),
      required: true,
      index: true
    },

    method: {
      type: String,
      enum: Object.values(PAYMENT_METHODS),
      required: true
    },

    amount: {
      type: Number,
      required: true,
      min: 0
    },

    status: {
      type: String,
      enum: Object.values(TRANSACTION_STATUS),
      default: TRANSACTION_STATUS.PENDING,
      index: true
    },


    /* ---------------------------------------------------------
       Payment information
       --------------------------------------------------------- */

    senderAccount: String,

    receiverAccount: String,

    externalRef: {
      type: String,
      index: true
    },

    screenshotUrl: String,

    paymentReference: {
      type: String,
      index: true
    },

    paymentGatewayReference: {
      type: String,
      index: true
    },

    paymentMethod: String,

    paymentDetails: {
      type: Schema.Types.Mixed
    },


    /* ---------------------------------------------------------
       Processing
       --------------------------------------------------------- */

    failureReason: String,

    completedAt: Date,

    requiresApproval: {
      type: Boolean,
      default: false
    },

    approvedBy: String,

    approvedAt: Date,

    processedBy: String,

    processedAt: Date,

    notes: String,


    /* ---------------------------------------------------------
       Ledger
       --------------------------------------------------------- */

    previousBalance: Number,

    newBalance: Number,

    previousBonusBalance: Number,

    newBonusBalance: Number,

    balanceBefore: Number,

    balanceAfter: Number,

    netAmount: Number,


    /* ---------------------------------------------------------
       Review
       --------------------------------------------------------- */

    reviewedBy: String,

    reviewedByUsername: String,

    reviewNote: String,

    reviewedAt: Date
  },
  {
    timestamps: true
  }
);


/* ============================================================
   INDEXES
   ============================================================ */

txSchema.index({
  status: 1,
  createdAt: -1
});

txSchema.index({
  userId: 1,
  createdAt: -1
});

/**
 * Prevent duplicate external payment references.
 *
 * Sparse means transactions without an externalRef are allowed.
 */
txSchema.index(
  { externalRef: 1 },
  {
    unique: true,
    sparse: true
  }
);


/* ============================================================
   MODEL
   ============================================================ */

export const Transaction =
  (mongoose.models.Transaction as Model<ITransaction>) ||
  mongoose.model<ITransaction>('Transaction', txSchema);

export default Transaction;