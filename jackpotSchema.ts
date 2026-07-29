import mongoose, { Schema, Document } from 'mongoose';

// ==============================================================================
// 1. Jackpot Pool Schema (supports both Sports and Casino)
// ==============================================================================
export interface IJackpotPool extends Document {
  title: string;
  type: 'sports' | 'casino';
  matchIds?: number[];
  casinoGameId?: string;
  criteria?: string;               // 'highest_multiplier' or 'highest_total_winnings'
  grandPrize: number;
  entryFee: number;
  status: 'Open' | 'Locked' | 'Settled';
  results?: string[];
  winnerUserId?: string;           // comma-separated if multiple winners
  createdAt: Date;
  updatedAt: Date;
}

const JackpotPoolSchema = new Schema<IJackpotPool>(
  {
    title: { type: String, required: true },
    type: { type: String, enum: ['sports', 'casino'], default: 'sports', required: true },
    matchIds: {
      type: [Number],
      validate: {
        validator: function (this: IJackpotPool, val: number[]) {
          return this.type === 'sports' ? val && val.length === 12 : true;
        },
        message: 'Sports jackpots must contain exactly 12 games.',
      },
    },
    casinoGameId: { type: String },
    criteria: { type: String, enum: ['highest_multiplier', 'highest_total_winnings'] },
    grandPrize: { type: Number, default: 100000, min: 0 },
    entryFee: { type: Number, default: 50, min: 0 },
    status: { type: String, enum: ['Open', 'Locked', 'Settled'], default: 'Open' },
    results: { type: [String] },
    winnerUserId: { type: String },
  },
  { timestamps: true }
);

// ==============================================================================
// 2. Jackpot Ticket Schema
// ==============================================================================
export interface IJackpotTicket extends Document {
  jackpotPoolId: mongoose.Types.ObjectId;
  userId: string;
  predictions?: string[];          // sports: 12 predictions
  multiplier?: number;             // casino: highest multiplier
  totalWon?: number;               // casino: total winnings
  correctGuessesCount: number;     // sports: number of correct picks
  isWinner: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const JackpotTicketSchema = new Schema<IJackpotTicket>(
  {
    jackpotPoolId: {
      type: Schema.Types.ObjectId,
      ref: 'JackpotPool',
      required: true,
      index: true,
    },
    userId: { type: String, required: true, index: true },
    predictions: { type: [String] },
    multiplier: { type: Number, default: 0 },
    totalWon: { type: Number, default: 0 },
    correctGuessesCount: { type: Number, default: 0 },
    isWinner: { type: Boolean, default: false },
  },
  { timestamps: true }
);

export const JackpotPool = mongoose.model<IJackpotPool>('JackpotPool', JackpotPoolSchema);
export const JackpotTicket = mongoose.model<IJackpotTicket>('JackpotTicket', JackpotTicketSchema);