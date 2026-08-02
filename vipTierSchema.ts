import mongoose, { Schema, Document } from 'mongoose';

export interface IVIPTier extends Document {
  userId: string;
  currentTier: 'Bronze' | 'Silver' | 'Gold' | 'Sheba_Black';
  prestigePoints: number;               // Accumulated via total wagering volume (sports + casino)
  unlockedPrivileges: string[];
  customAvatarBorderHex: string;        // Hex color for frontend avatar border

  // 🎰 Casino-specific tracking
  totalWagered: number;                 // Sum of all sportsbook + casino wagers
  casinoWagered: number;                // Sum of casino-only wagers
  casinoGamesPlayed: number;            // Number of distinct casino games played
  casinoBonusEarned: number;            // Bonus points from casino activity (optional)
}

const VIPTierSchema = new Schema<IVIPTier>({
  userId: { type: String, required: true, unique: true, index: true },
  currentTier: {
    type: String,
    enum: ['Bronze', 'Silver', 'Gold', 'Sheba_Black'],
    default: 'Bronze'
  },
  prestigePoints: { type: Number, default: 0 },
  unlockedPrivileges: [String],
  customAvatarBorderHex: { type: String, default: '#8B5CF6' },

  // 🎰 New casino statistics
  totalWagered: { type: Number, default: 0 },
  casinoWagered: { type: Number, default: 0 },
  casinoGamesPlayed: { type: Number, default: 0 },
  casinoBonusEarned: { type: Number, default: 0 }
}, { timestamps: true });

export const VIPTier = mongoose.model<IVIPTier>('VIPTier', VIPTierSchema);