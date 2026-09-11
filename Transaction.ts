import mongoose, { Schema, Document, Model } from 'mongoose';

export const TX_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  CANCELLED: 'cancelled'
} as const;
export type TxStatus = typeof TX_STATUS[keyof typeof TX_STATUS];

export const TX_TYPE = {
  DEPOSIT: 'deposit',
  WITHDRAWAL: 'withdrawal'
} as const;
export type TxType = typeof TX_TYPE[keyof typeof TX_TYPE];

export const PAYMENT_METHOD = {
  TELEBIRR: 'telebirr',
  CBE_BIRR: 'cbe_birr',
  AWASH_BIRR: 'awash_birr',
  M_PESA: 'm_pesa',
  HELLO_CASH: 'hello_cash'
} as const;
export type PaymentMethod = typeof PAYMENT_METHOD[keyof typeof PAYMENT_METHOD];

export interface ITransaction extends Document {
  txId: string;                       // internal id
  userId: string;                     // ref User._id
  username: string;                   // denormalized for admin list
  type: TxType;
  method: PaymentMethod;
  amount: number;                     // ETB
  status: TxStatus;

  // Payment proof
  senderAccount?: string;             // phone / account
  receiverAccount?: string;           // our receiving account
  externalRef?: string;               // Telebirr SMS ref / bank txn id
  screenshotUrl?: string;

  // Admin workflow
  reviewedBy?: string;                // admin userId
  reviewedByUsername?: string;
  reviewNote?: string;
  reviewedAt?: Date;

  // Ledger
  balanceBefore?: number;
  balanceAfter?: number;

  createdAt: Date;
  updatedAt: Date;
}

const txSchema = new Schema<ITransaction>({
  txId: { type: String, required: true, unique: true, index: true },
  userId: { type: String, required: true, index: true },
  username: { type: String, required: true },
  type: { type: String, enum: Object.values(TX_TYPE), required: true },
  method: { type: String, enum: Object.values(PAYMENT_METHOD), required: true },
  amount: { type: Number, required: true, min: 10, max: 500000 },
  status: { type: String, enum: Object.values(TX_STATUS), default: TX_STATUS.PENDING, index: true },

  senderAccount: String,
  receiverAccount: String,
  externalRef: { type: String, index: true },
  screenshotUrl: String,

  reviewedBy: String,
  reviewedByUsername: String,
  reviewNote: String,
  reviewedAt: Date,

  balanceBefore: Number,
  balanceAfter: Number
}, { timestamps: true });

txSchema.index({ status: 1, createdAt: -1 });
txSchema.index({ userId: 1, createdAt: -1 });

// Prevent double-spend of the same external reference
txSchema.index({ externalRef: 1 }, { unique: true, sparse: true });

export const Transaction =
  (mongoose.models.Transaction as Model<ITransaction>) ||
  mongoose.model<ITransaction>('Transaction', txSchema);