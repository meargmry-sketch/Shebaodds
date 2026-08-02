import mongoose, { Schema, Document, Model } from "mongoose";

/* ============================================================================
   Jackpot Pool Schema
============================================================================ */

export interface IJackpotPool extends Document {
  title: string;
  type: "sports" | "casino";

  matchIds?: number[];

  casinoGameId?: string;
  criteria?: "highest_multiplier" | "highest_total_winnings";

  grandPrize: number;
  entryFee: number;

  status: "Open" | "Locked" | "Settled";

  results?: string[];

  winnerUserId?: string;

  createdAt: Date;
  updatedAt: Date;
}

const JackpotPoolSchema = new Schema<IJackpotPool>(
  {
    title: {
      type: String,
      required: true,
      trim: true,
    },

    type: {
      type: String,
      enum: ["sports", "casino"],
      required: true,
      default: "sports",
    },

    matchIds: {
      type: [Number],
      validate: {
        validator: function (this: IJackpotPool, value: number[]) {
          if (this.type === "sports") {
            return Array.isArray(value) && value.length === 12;
          }
          return true;
        },
        message: "Sports jackpots must contain exactly 12 matches.",
      },
    },

    casinoGameId: {
      type: String,
      required: function (this: IJackpotPool) {
        return this.type === "casino";
      },
    },

    criteria: {
      type: String,
      enum: [
        "highest_multiplier",
        "highest_total_winnings",
      ],
      required: function (this: IJackpotPool) {
        return this.type === "casino";
      },
    },

    grandPrize: {
      type: Number,
      default: 100000,
      min: 0,
    },

    entryFee: {
      type: Number,
      default: 50,
      min: 0,
    },

    status: {
      type: String,
      enum: ["Open", "Locked", "Settled"],
      default: "Open",
    },

    results: {
      type: [String],
      validate: {
        validator: function (this: IJackpotPool, value: string[]) {
          if (this.type === "sports") {
            return !value || value.length === 12;
          }
          return true;
        },
        message: "Sports results must contain exactly 12 outcomes.",
      },
    },

    winnerUserId: {
      type: String,
      default: null,
    },
  },
  {
    timestamps: true,
  }
);

/* ============================================================================
   Jackpot Ticket Schema
============================================================================ */

export interface IJackpotTicket extends Document {
  jackpotPoolId: mongoose.Types.ObjectId;

  userId: string;

  predictions?: string[];

  multiplier?: number;

  totalWon?: number;

  correctGuessesCount: number;

  isWinner: boolean;

  createdAt: Date;
  updatedAt: Date;
}

const JackpotTicketSchema = new Schema<IJackpotTicket>(
  {
    jackpotPoolId: {
      type: Schema.Types.ObjectId,
      ref: "JackpotPool",
      required: true,
      index: true,
    },

    userId: {
      type: String,
      required: true,
      index: true,
    },

    predictions: {
      type: [String],
      validate: {
        validator: function (value: string[]) {
          return !value || value.length === 12;
        },
        message: "Exactly 12 predictions are required.",
      },
    },

    multiplier: {
      type: Number,
      default: 0,
      min: 0,
    },

    totalWon: {
      type: Number,
      default: 0,
      min: 0,
    },

    correctGuessesCount: {
      type: Number,
      default: 0,
      min: 0,
      max: 12,
    },

    isWinner: {
      type: Boolean,
      default: false,
    },
  },
  {
    timestamps: true,
  }
);

/* ============================================================================
   Export Models
============================================================================ */

export const JackpotPool: Model<IJackpotPool> =
  mongoose.models.JackpotPool ||
  mongoose.model<IJackpotPool>(
    "JackpotPool",
    JackpotPoolSchema
  );

export const JackpotTicket: Model<IJackpotTicket> =
  mongoose.models.JackpotTicket ||
  mongoose.model<IJackpotTicket>(
    "JackpotTicket",
    JackpotTicketSchema
  );

export default {
  JackpotPool,
  JackpotTicket,
};