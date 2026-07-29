import mongoose, { Schema, Document } from 'mongoose';

// ==============================================================================
// 1. Jackpot Pool Schema (supports both Sports and Casino)
// ==============================================================================
export interface IJackpotPool extends Document {
  title: string;                    // e.g., "Grand Weekend 12 Jackpot"
  type: 'sports' | 'casino';        // Distinguishes between sports and casino
  matchIds?: number[];              // For sports: array of 12 match IDs (optional for casino)
  casinoGameId?: string;            // For casino: e.g., 'aviator', 'slot'
  criteria?: string;                // For casino: 'highest_multiplier' or 'highest_total_winnings'
  grandPrize: number;               // Total prize pool (e.g., 100000 ETB)
  entryFee: number;                 // Cost per ticket (e.g., 50 ETB)
  status: 'Open' | 'Locked' | 'Settled';
  results?: string[];               // For sports: array of 12 outcomes ("1", "X", "2")
  winnerUserId?: string;            // Store the winning user ID(s) after settlement (comma-separated if multiple)
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
          // Only validate if it's a sports jackpot
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
// 2. Jackpot Ticket Schema (stores player predictions and performance)
// ==============================================================================
export interface IJackpotTicket extends Document {
  jackpotPoolId: mongoose.Types.ObjectId;
  userId: string;
  predictions?: string[];              // For sports: array of 12 predictions
  multiplier?: number;                 // For casino: highest multiplier achieved
  totalWon?: number;                   // For casino: total winnings accumulated
  correctGuessesCount: number;         // For sports: number of correct predictions
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

// ==============================================================================
// 3. Export Models
// ==============================================================================
export const JackpotPool = mongoose.model<IJackpotPool>('JackpotPool', JackpotPoolSchema);
export const JackpotTicket = mongoose.model<IJackpotTicket>('JackpotTicket', JackpotTicketSchema);