import mongoose, { Schema, Document } from 'mongoose';

// ==============================================================================
// 1. Jackpot Pool Schema (now supports both Sports and Casino)
// ==============================================================================
interface IJackpotPool extends Document {
  title: string;                    // e.g., "Grand Weekend 12 Jackpot" or "Aviator Monthly Challenge"
  type: 'sports' | 'casino';        // New: distinguishes between sports and casino jackpots
  matchIds?: number[];              // For sports: array of 12 match IDs (optional for casino)
  casinoGameId?: string;            // For casino: e.g., 'aviator', 'slot'
  criteria?: string;                // For casino: 'highest_multiplier', 'highest_total_winnings'
  grandPrize: number;               // e.g., 100000 ETB
  entryFee: number;                 // e.g., 50 ETB
  status: 'Open' | 'Locked' | 'Settled';
  results?: string[];               // For sports: array of 12 outcomes ("1", "X", "2")
  winnerUserId?: string;            // For both: store the winning user ID after settlement
}

const JackpotPoolSchema = new Schema<IJackpotPool>({
  title: { type: String, required: true },
  type: { type: String, enum: ['sports', 'casino'], default: 'sports', required: true },
  matchIds: {
    type: [Number],
    validate: {
      validator: function(this: IJackpotPool, val: number[]) {
        // Only validate if it's a sports jackpot
        return this.type === 'sports' ? val.length === 12 : true;
      },
      message: 'Sports jackpots must contain exactly 12 games.'
    }
  },
  casinoGameId: { type: String },
  criteria: { type: String },
  grandPrize: { type: Number, default: 100000 },
  entryFee: { type: Number, default: 50 },
  status: { type: String, enum: ['Open', 'Locked', 'Settled'], default: 'Open' },
  results: { type: [String] },
  winnerUserId: { type: String }
}, { timestamps: true });

// ==============================================================================
// 2. Jackpot Ticket Schema (stores player performance for casino jackpots)
// ==============================================================================
interface IJackpotTicket extends Document {
  jackpotPoolId: mongoose.Types.ObjectId;
  userId: string;
  // For sports: array of 12 predictions; for casino: optional (may store multiplier or other data)
  predictions?: string[];
  // Performance metrics for casino jackpots
  multiplier?: number;      // highest multiplier achieved (e.g., Aviator)
  totalWon?: number;        // total winnings accumulated (e.g., slots)
  correctGuessesCount: number; // for sports
  isWinner: boolean;
}

const JackpotTicketSchema = new Schema<IJackpotTicket>({
  jackpotPoolId: { type: Schema.Types.ObjectId, ref: 'JackpotPool', required: true },
  userId: { type: String, required: true, index: true },
  predictions: { type: [String] },
  multiplier: { type: Number, default: 0 },
  totalWon: { type: Number, default: 0 },
  correctGuessesCount: { type: Number, default: 0 },
  isWinner: { type: Boolean, default: false }
}, { timestamps: true });

// ==============================================================================
// 3. Export models
// ==============================================================================
export const JackpotPool = mongoose.model<IJackpotPool>('JackpotPool', JackpotPoolSchema);
export const JackpotTicket = mongoose.model<IJackpotTicket>('JackpotTicket', JackpotTicketSchema);