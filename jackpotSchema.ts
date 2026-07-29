import mongoose, { Schema, Document } from 'mongoose';

interface IJackpotPool extends Document {
  title: string;
  type: 'sports' | 'casino';
  matchIds?: number[];
  casinoGameId?: string;
  criteria?: string;
  grandPrize: number;
  entryFee: number;
  status: 'Open' | 'Locked' | 'Settled';
  results?: string[];
  winnerUserId?: string;
}

const JackpotPoolSchema = new Schema<IJackpotPool>({
  title: { type: String, required: true },
  type: { type: String, enum: ['sports', 'casino'], default: 'sports', required: true },
  matchIds: {
    type: [Number],
    validate: {
      validator: function(this: IJackpotPool, val: number[]) {
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

interface IJackpotTicket extends Document {
  jackpotPoolId: mongoose.Types.ObjectId;
  userId: string;
  predictions?: string[];
  multiplier?: number;
  totalWon?: number;
  correctGuessesCount: number;
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

export const JackpotPool = mongoose.model<IJackpotPool>('JackpotPool', JackpotPoolSchema);
export const JackpotTicket = mongoose.model<IJackpotTicket>('JackpotTicket', JackpotTicketSchema);