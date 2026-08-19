// ============================================
// SHEBAODDS - USER MODEL
// Mongoose 8 + TypeScript
// Production-ready user schema
// ============================================

import mongoose, {
  Document,
  Model,
  Schema,
  Types,
} from 'mongoose';

import bcrypt from 'bcryptjs';
import crypto from 'crypto';
import speakeasy from 'speakeasy';

// ============================================
// WALLET
// ============================================

export interface IUserWallet {
  balance: number;
  bonusBalance: number;
  lockedBalance: number;
  currency: string;

  totalDeposited: number;
  totalWithdrawn: number;
  totalWagered: number;
  totalWon: number;
  totalLost: number;
  totalTaxPaid: number;

  totalBonusReceived: number;
  totalCashbackReceived: number;
}

export function createDefaultWallet(): IUserWallet {
  return {
    balance: 0,
    bonusBalance: 0,
    lockedBalance: 0,
    currency: 'ETB',

    totalDeposited: 0,
    totalWithdrawn: 0,
    totalWagered: 0,
    totalWon: 0,
    totalLost: 0,
    totalTaxPaid: 0,

    totalBonusReceived: 0,
    totalCashbackReceived: 0,
  };
}

// ============================================
// ENUMS / TYPES
// ============================================

export type KYCDocumentType =
  | 'national_id'
  | 'passport'
  | 'drivers_license'
  | 'proof_of_address'
  | 'selfie';

export type KYCStatus =
  | 'unverified'
  | 'pending'
  | 'verified'
  | 'rejected';

export type KYCReviewStatus =
  | 'pending'
  | 'approved'
  | 'rejected';

export type Platform =
  | 'web'
  | 'ios'
  | 'android'
  | 'admin';

export type SavedPaymentMethodType =
  | 'tele_birr'
  | 'cbe'
  | 'card'
  | 'paypal'
  | 'crypto'
  | 'bank_transfer';

// ============================================
// SUBDOCUMENT TYPES
// ============================================

export interface IUserStatistics {
  totalBets: number;
  totalWins: number;
  totalLosses: number;
  winningPercentage: number;
  currentWinStreak: number;
  longestWinStreak: number;
  biggestWin: number;
  biggestLoss: number;
  averageOdds: number;
}

export interface IUserVIP {
  level: number;
  name: string;
  loyaltyPoints: number;
  cashbackPercentage: number;
  personalManager: boolean;
  higherLimits: boolean;
  exclusivePromotions: boolean;
  fasterWithdrawals: boolean;
}

export interface IUserTaxProfile {
  taxExempt: boolean;
  taxId?: string;
  taxRegistrationNumber?: string;
  isTaxRegistered: boolean;
  totalTaxPaid: number;
  totalWinningsTaxed: number;
  lastTaxCalculation?: Date;
}

export interface IResponsibleGambling {
  depositLimit: number;
  lossLimit: number;
  wagerLimit: number;
  sessionTimeout: number;
  realityCheckInterval: number;

  selfExcluded: boolean;
  selfExclusionEndDate?: Date;
  coolingOffPeriodEnd?: Date;
  lastRealityCheck?: Date;
}

export interface IUserNotifications {
  email: boolean;
  push: boolean;
  sms: boolean;
  betSettlements: boolean;
  promotions: boolean;
  aiTips: boolean;
  systemUpdates: boolean;
  securityAlerts: boolean;
}

export interface IUserDevice {
  deviceId: string;
  deviceName?: string;
  platform?: Platform;
  browser?: string;
  os?: string;
  ipAddress?: string;
  location?: string;
  pushToken?: string;

  biometricEnabled: boolean;
  biometricPublicKey?: string;

  lastUsed: Date;
  isActive: boolean;
}

export interface IUserSession {
  sessionId: string;

  ipAddress?: string;
  userAgent?: string;
  deviceId?: string;

  loginAt: Date;
  lastActivity: Date;
  expiresAt?: Date;

  /**
   * SHA-256 hash of the refresh token.
   * Never expose this field through JSON.
   */
  refreshTokenHash?: string;
}

export interface ISavedPaymentMethod {
  type: SavedPaymentMethodType;

  identifier?: string;
  last4?: string;
  brand?: string;

  isDefault: boolean;
  addedAt: Date;

  metadata?: mongoose.Schema.Types.Mixed;
}

export interface IBettingPreferences {
  defaultStake: number;
  autoCashoutMultiplier: number;

  favoriteLeagues: string[];
  favoriteTeams: string[];
  excludedMarkets: string[];
}

export interface IAffiliate {
  partnerId?: string;

  commissionRate: number;
  totalCommission: number;
  paidCommission: number;
}

export interface IUserNote {
  note?: string;
  createdBy?: Types.ObjectId;
  createdAt: Date;
}

// ============================================
// USER DOCUMENT
// ============================================

export interface IUser extends Document {
  username: string;
  email: string;

  password?: string;
  passwordHistory?: string[];

  phone: string;
  fullName?: string;
  dateOfBirth?: Date;

  country: string;
  city?: string;
  address?: string;
  postalCode?: string;

  language: string;
  theme: string;
  currency: string;
  timezone: string;

  wallet: IUserWallet;

  statistics: IUserStatistics;
  vip: IUserVIP;
  taxProfile: IUserTaxProfile;

  isActive: boolean;
  isAdmin: boolean;
  isVerified: boolean;
  isBlocked: boolean;
  isSuspended: boolean;

  suspensionReason?: string;
  suspensionEndDate?: Date;

  kycDocuments: Array<{
    type: KYCDocumentType;
    documentUrl?: string;
    documentNumber?: string;

    status: KYCReviewStatus;

    submittedAt: Date;
    reviewedAt?: Date;
    reviewedBy?: string;

    rejectionReason?: string;

    verifiedAt?: Date;
    verifiedBy?: string;
  }>;

  kycStatus: KYCStatus;
  kycLevel: number;

  twoFactorEnabled: boolean;
  twoFactorSecret?: string;
  twoFactorBackupCodes?: string[];

  emailVerified: boolean;
  phoneVerified: boolean;

  emailVerificationToken?: string;
  emailVerificationExpires?: Date;

  phoneVerificationCode?: string;
  phoneVerificationExpires?: Date;

  loginAttempts: number;
  lockedUntil?: Date;

  lastLoginIP?: string;

  lastLoginLocation?: {
    city?: string;
    country?: string;
    lat?: number;
    lng?: number;
  };

  resetPasswordToken?: string;
  resetPasswordExpires?: Date;

  referralCode?: string;
  referredBy?: Types.ObjectId;

  referralCount: number;
  referralEarnings: number;
  referralTier: number;

  responsibleGambling: IResponsibleGambling;
  notifications: IUserNotifications;

  devices: IUserDevice[];
  sessions: IUserSession[];

  savedPaymentMethods: ISavedPaymentMethod[];

  bettingPreferences: IBettingPreferences;

  affiliate: IAffiliate;

  notes: IUserNote[];

  lastLogin?: Date;
  lastActive: Date;

  createdAt: Date;
  updatedAt: Date;

  comparePassword(
    candidatePassword: string
  ): Promise<boolean>;

  generateReferralCode(): string;

  generateTwoFactorSecret(): speakeasy.GeneratedSecret;

  verifyTwoFactorToken(
    token: string
  ): boolean;

  generateBackupCodes(): string[];

  verifyBackupCode(
    code: string
  ): Promise<boolean>;

  generateEmailVerificationToken(): string;

  updateVipLevel(): void;

  canPlaceBet(amount: number): boolean;

  getDepositLimit(): number;
}

// ============================================
// MODEL METHODS
// ============================================

export interface IUserModel extends Model<IUser> {
  findByEmailOrUsername(
    identifier: string
  ): Promise<IUser | null>;

  getTopWagered(
    limit?: number
  ): Promise<IUser[]>;

  getOnlineUsers(): Promise<number>;
}

// ============================================
// SCHEMA
// ============================================

const userSchema = new Schema<IUser, IUserModel>(
  {
    // ==========================================
    // BASIC
    // ==========================================

    username: {
      type: String,
      required: [true, 'Username is required'],
      unique: true,
      trim: true,
      lowercase: true,

      minlength: [
        3,
        'Username must be at least 3 characters',
      ],

      maxlength: [
        20,
        'Username cannot exceed 20 characters',
      ],

      match: [
        /^[a-zA-Z0-9_]+$/,
        'Username can only contain letters, numbers and underscore',
      ],

      index: true,
    },

    email: {
      type: String,
      required: [true, 'Email is required'],
      unique: true,
      trim: true,
      lowercase: true,

      match: [
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        'Please provide a valid email',
      ],

      index: true,
    },

    password: {
      type: String,
      required: [true, 'Password is required'],

      minlength: [
        8,
        'Password must be at least 8 characters',
      ],

      select: false,
    },

    passwordHistory: {
      type: [String],
      default: [],
      select: false,
    },

    phone: {
      type: String,
      required: [true, 'Phone number is required'],
      unique: true,
      trim: true,
      index: true,

      match: [
        /^\+?[0-9]{10,15}$/,
        'Please provide a valid phone number',
      ],
    },

    fullName: {
      type: String,
      trim: true,
      maxlength: [
        100,
        'Full name cannot exceed 100 characters',
      ],
    },

    dateOfBirth: {
      type: Date,
    },

    country: {
      type: String,
      default: 'Ethiopia',
      trim: true,
    },

    city: {
      type: String,
      trim: true,
    },

    address: {
      type: String,
      trim: true,
    },

    postalCode: {
      type: String,
      trim: true,
    },

    language: {
      type: String,
      enum: [
        'en',
        'am',
        'ar',
        'fr',
        'es',
        'de',
        'it',
        'pt',
        'ru',
        'zh',
      ],
      default: 'en',
    },

    theme: {
      type: String,
      enum: ['light', 'dark'],
      default: 'dark',
    },

    currency: {
      type: String,
      enum: [
        'ETB',
        'USD',
        'EUR',
        'GBP',
        'BTC',
        'ETH',
        'USDT',
      ],
      default: 'ETB',
    },

    timezone: {
      type: String,
      default: 'Africa/Addis_Ababa',
    },

    // ==========================================
    // WALLET
    // ==========================================

    wallet: {
      balance: {
        type: Number,
        default: 0,
        min: 0,
      },

      bonusBalance: {
        type: Number,
        default: 0,
        min: 0,
      },

      lockedBalance: {
        type: Number,
        default: 0,
        min: 0,
      },

      currency: {
        type: String,
        default: 'ETB',
      },

      totalDeposited: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalWithdrawn: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalWagered: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalWon: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalLost: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalTaxPaid: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalBonusReceived: {
        type: Number,
        default: 0,
        min: 0,
      },

      totalCashbackReceived: {
        type: Number,
        default: 0,
        min: 0,
      },
    },

    // ==========================================
    // STATISTICS
    // ==========================================

    statistics: {
      totalBets: {
        type: Number,
        default: 0,
      },

      totalWins: {
        type: Number,
        default: 0,
      },

      totalLosses: {
        type: Number,
        default: 0,
      },

      winningPercentage: {
        type: Number,
        default: 0,
      },

      currentWinStreak: {
        type: Number,
        default: 0,
      },

      longestWinStreak: {
        type: Number,
        default: 0,
      },

      biggestWin: {
        type: Number,
        default: 0,
      },

      biggestLoss: {
        type: Number,
        default: 0,
      },

      averageOdds: {
        type: Number,
        default: 0,
      },
    },

    // ==========================================
    // VIP
    // ==========================================

    vip: {
      level: {
        type: Number,
        default: 0,
        enum: [0, 1, 2, 3, 4, 5, 6, 7, 8],
      },

      name: {
        type: String,
        default: 'Bronze',
      },

      loyaltyPoints: {
        type: Number,
        default: 0,
      },

      cashbackPercentage: {
        type: Number,
        default: 0,
      },

      personalManager: {
        type: Boolean,
        default: false,
      },

      higherLimits: {
        type: Boolean,
        default: false,
      },

      exclusivePromotions: {
        type: Boolean,
        default: false,
      },

      fasterWithdrawals: {
        type: Boolean,
        default: false,
      },
    },

    // ==========================================
    // TAX
    // ==========================================

    taxProfile: {
      taxExempt: {
        type: Boolean,
        default: false,
      },

      taxId: {
        type: String,
        sparse: true,
      },

      taxRegistrationNumber: {
        type: String,
        sparse: true,
      },

      isTaxRegistered: {
        type: Boolean,
        default: false,
      },

      totalTaxPaid: {
        type: Number,
        default: 0,
      },

      totalWinningsTaxed: {
        type: Number,
        default: 0,
      },

      lastTaxCalculation: Date,
    },

    // ==========================================
    // STATUS
    // ==========================================

    isActive: {
      type: Boolean,
      default: true,
      index: true,
    },

    isAdmin: {
      type: Boolean,
      default: false,
      index: true,
    },

    isVerified: {
      type: Boolean,
      default: false,
      index: true,
    },

    isBlocked: {
      type: Boolean,
      default: false,
      index: true,
    },

    isSuspended: {
      type: Boolean,
      default: false,
    },

    suspensionReason: String,

    suspensionEndDate: Date,

    // ==========================================
    // KYC
    // ==========================================

    kycDocuments: [
      {
        type: {
          type: String,
          enum: [
            'national_id',
            'passport',
            'drivers_license',
            'proof_of_address',
            'selfie',
          ],
        },

        documentUrl: String,
        documentNumber: String,

        status: {
          type: String,
          enum: [
            'pending',
            'approved',
            'rejected',
          ],
          default: 'pending',
        },

        submittedAt: {
          type: Date,
          default: Date.now,
        },

        reviewedAt: Date,
        reviewedBy: String,
        rejectionReason: String,

        verifiedAt: Date,
        verifiedBy: String,
      },
    ],

    kycStatus: {
      type: String,
      enum: [
        'unverified',
        'pending',
        'verified',
        'rejected',
      ],
      default: 'unverified',
    },

    kycLevel: {
      type: Number,
      default: 0,
      enum: [0, 1, 2, 3],
    },

    // ==========================================
    // 2FA
    // ==========================================

    twoFactorEnabled: {
      type: Boolean,
      default: false,
    },

    twoFactorSecret: {
      type: String,
      select: false,
    },

    twoFactorBackupCodes: {
      type: [String],
      default: [],
      select: false,
    },

    // ==========================================
    // VERIFICATION
    // ==========================================

    emailVerified: {
      type: Boolean,
      default: false,
    },

    phoneVerified: {
      type: Boolean,
      default: false,
    },

    emailVerificationToken: {
      type: String,
      select: false,
    },

    emailVerificationExpires: Date,

    phoneVerificationCode: {
      type: String,
      select: false,
    },

    phoneVerificationExpires: Date,

    // ==========================================
    // LOGIN SECURITY
    // ==========================================

    loginAttempts: {
      type: Number,
      default: 0,
    },

    lockedUntil: Date,

    lastLoginIP: String,

    lastLoginLocation: {
      city: String,
      country: String,
      lat: Number,
      lng: Number,
    },

    // ==========================================
    // PASSWORD RESET
    // ==========================================

    resetPasswordToken: {
      type: String,
      select: false,
    },

    resetPasswordExpires: Date,

    // ==========================================
    // REFERRALS
    // ==========================================

    referralCode: {
      type: String,
      unique: true,
      sparse: true,
      uppercase: true,
      index: true,
    },

    referredBy: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      index: true,
    },

    referralCount: {
      type: Number,
      default: 0,
    },

    referralEarnings: {
      type: Number,
      default: 0,
    },

    referralTier: {
      type: Number,
      default: 1,
    },

    // ==========================================
    // RESPONSIBLE GAMBLING
    // ==========================================

    responsibleGambling: {
      depositLimit: {
        type: Number,
        default: 10000,
      },

      lossLimit: {
        type: Number,
        default: 5000,
      },

      wagerLimit: {
        type: Number,
        default: 50000,
      },

      sessionTimeout: {
        type: Number,
        default: 120,
      },

      realityCheckInterval: {
        type: Number,
        default: 60,
      },

      selfExcluded: {
        type: Boolean,
        default: false,
      },

      selfExclusionEndDate: Date,

      coolingOffPeriodEnd: Date,

      lastRealityCheck: Date,
    },

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    notifications: {
      email: {
        type: Boolean,
        default: true,
      },

      push: {
        type: Boolean,
        default: true,
      },

      sms: {
        type: Boolean,
        default: false,
      },

      betSettlements: {
        type: Boolean,
        default: true,
      },

      promotions: {
        type: Boolean,
        default: true,
      },

      aiTips: {
        type: Boolean,
        default: true,
      },

      systemUpdates: {
        type: Boolean,
        default: true,
      },

      securityAlerts: {
        type: Boolean,
        default: true,
      },
    },

    // ==========================================
    // DEVICES
    // ==========================================

    devices: [
      {
        deviceId: {
          type: String,
          required: true,
        },

        deviceName: String,

        platform: {
          type: String,
          enum: [
            'web',
            'ios',
            'android',
            'admin',
          ],
        },

        browser: String,
        os: String,
        ipAddress: String,
        location: String,
        pushToken: String,

        biometricEnabled: {
          type: Boolean,
          default: false,
        },

        biometricPublicKey: String,

        lastUsed: {
          type: Date,
          default: Date.now,
        },

        isActive: {
          type: Boolean,
          default: true,
        },
      },
    ],

    // ==========================================
    // SESSIONS
    // ==========================================

    sessions: [
      {
        sessionId: {
          type: String,
          required: true,
        },

        ipAddress: String,
        userAgent: String,
        deviceId: String,

        loginAt: {
          type: Date,
          default: Date.now,
        },

        lastActivity: {
          type: Date,
          default: Date.now,
        },

        expiresAt: Date,

        refreshTokenHash: {
          type: String,
          select: false,
        },
      },
    ],

    // ==========================================
    // PAYMENT METHODS
    // ==========================================

    savedPaymentMethods: [
      {
        type: {
          type: String,
          enum: [
            'tele_birr',
            'cbe',
            'card',
            'paypal',
            'crypto',
            'bank_transfer',
          ],
        },

        identifier: String,
        last4: String,
        brand: String,

        isDefault: {
          type: Boolean,
          default: false,
        },

        addedAt: {
          type: Date,
          default: Date.now,
        },

        metadata: Schema.Types.Mixed,
      },
    ],

    // ==========================================
    // BETTING PREFERENCES
    // ==========================================

    bettingPreferences: {
      defaultStake: {
        type: Number,
        default: 10,
      },

      autoCashoutMultiplier: {
        type: Number,
        default: 0,
      },

      favoriteLeagues: {
        type: [String],
        default: [],
      },

      favoriteTeams: {
        type: [String],
        default: [],
      },

      excludedMarkets: {
        type: [String],
        default: [],
      },
    },

    // ==========================================
    // AFFILIATE
    // ==========================================

    affiliate: {
      partnerId: String,

      commissionRate: {
        type: Number,
        default: 0,
      },

      totalCommission: {
        type: Number,
        default: 0,
      },

      paidCommission: {
        type: Number,
        default: 0,
      },
    },

    // ==========================================
    // NOTES
    // ==========================================

    notes: [
      {
        note: String,

        createdBy: {
          type: Schema.Types.ObjectId,
          ref: 'User',
        },

        createdAt: {
          type: Date,
          default: Date.now,
        },
      },
    ],

    // ==========================================
    // ACTIVITY
    // ==========================================

    lastLogin: Date,

    lastActive: {
      type: Date,
      default: Date.now,
      index: true,
    },
  },

  {
    timestamps: true,

    toJSON: {
      virtuals: true,

      transform: (_doc, ret) => {
        delete ret.password;
        delete ret.passwordHistory;

        delete ret.twoFactorSecret;
        delete ret.twoFactorBackupCodes;

        delete ret.resetPasswordToken;
        delete ret.emailVerificationToken;
        delete ret.phoneVerificationCode;

        if (Array.isArray(ret.sessions)) {
          ret.sessions = ret.sessions.map(
            (session: Record<string, unknown>) => {
              const clean = {
                ...session,
              };

              delete clean.refreshTokenHash;

              return clean;
            }
          );
        }

        return ret;
      },
    },

    toObject: {
      virtuals: true,
    },
  }
);

// ============================================
// INDEXES
// ============================================

userSchema.index({
  createdAt: -1,
});

userSchema.index({
  lastActive: -1,
});

userSchema.index({
  'wallet.balance': -1,
});

userSchema.index({
  'vip.level': -1,
});

userSchema.index({
  'vip.loyaltyPoints': -1,
});

userSchema.index({
  referralCode: 1,
});

userSchema.index({
  referredBy: 1,
});

userSchema.index({
  'devices.deviceId': 1,
});

userSchema.index({
  'sessions.sessionId': 1,
});

userSchema.index({
  kycStatus: 1,
});

// ============================================
// PASSWORD HASHING
// ============================================

userSchema.pre('save', async function (next) {
  try {
    if (!this.isModified('password')) {
      return next();
    }

    if (!this.password) {
      return next();
    }

    const plainPassword = this.password;

    const hashedPassword = await bcrypt.hash(
      plainPassword,
      12
    );

    const history = Array.isArray(
      this.passwordHistory
    )
      ? [...this.passwordHistory]
      : [];

    history.unshift(hashedPassword);

    this.passwordHistory =
      history.slice(0, 5);

    this.password = hashedPassword;

    next();
  } catch (error) {
    next(
      error instanceof Error
        ? error
        : new Error('Password hashing failed')
    );
  }
});

// ============================================
// NORMALIZATION
// ============================================

userSchema.pre('save', function (next) {
  try {
    if (!this.wallet) {
      this.wallet =
        createDefaultWallet();
    }

    if (!this.referralCode && this.isNew) {
      this.referralCode =
        this.generateReferralCode();
    }

    next();
  } catch (error) {
    next(
      error instanceof Error
        ? error
        : new Error('User normalization failed')
    );
  }
});

// ============================================
// PASSWORD COMPARISON
// ============================================

userSchema.methods.comparePassword =
  async function (
    candidatePassword: string
  ): Promise<boolean> {
    if (
      !candidatePassword ||
      !this.password
    ) {
      return false;
    }

    return bcrypt.compare(
      candidatePassword,
      this.password
    );
  };

// ============================================
// REFERRAL CODE
// ============================================

userSchema.methods.generateReferralCode =
  function (): string {
    const random =
      crypto
        .randomBytes(5)
        .toString('hex')
        .toUpperCase();

    return `SHB${random}`;
  };

// ============================================
// 2FA SECRET
// ============================================

userSchema.methods.generateTwoFactorSecret =
  function (): speakeasy.GeneratedSecret {
    const secret =
      speakeasy.generateSecret({
        length: 20,

        name:
          `SHEBAODDS (${this.email})`,

        issuer:
          'SHEBAODDS',
      });

    this.twoFactorSecret =
      secret.base32;

    return secret;
  };

// ============================================
// VERIFY 2FA
// ============================================

userSchema.methods.verifyTwoFactorToken =
  function (
    token: string
  ): boolean {
    if (
      !this.twoFactorSecret ||
      !/^\d{6}$/.test(token)
    ) {
      return false;
    }

    return Boolean(
      speakeasy.totp.verify({
        secret:
          this.twoFactorSecret,

        encoding:
          'base32',

        token,

        window: 1,
      })
    );
  };

// ============================================
// BACKUP CODES
// ============================================

userSchema.methods.generateBackupCodes =
  function (): string[] {
    const codes: string[] = [];

    for (let i = 0; i < 10; i++) {
      codes.push(
        crypto
          .randomBytes(6)
          .toString('hex')
          .toUpperCase()
      );
    }

    this.twoFactorBackupCodes =
      codes.map((code) =>
        bcrypt.hashSync(code, 12)
      );

    return codes;
  };

// ============================================
// VERIFY BACKUP CODE
// ============================================

userSchema.methods.verifyBackupCode =
  async function (
    code: string
  ): Promise<boolean> {
    if (
      !code ||
      !Array.isArray(
        this.twoFactorBackupCodes
      )
    ) {
      return false;
    }

    for (
      let i = 0;
      i <
      this.twoFactorBackupCodes.length;
      i++
    ) {
      const hash =
        this.twoFactorBackupCodes[i];

      if (
        await bcrypt.compare(
          code,
          hash
        )
      ) {
        this.twoFactorBackupCodes.splice(
          i,
          1
        );

        await this.save();

        return true;
      }
    }

    return false;
  };

// ============================================
// EMAIL VERIFICATION TOKEN
// ============================================

userSchema.methods.generateEmailVerificationToken =
  function (): string {
    const rawToken =
      crypto
        .randomBytes(32)
        .toString('hex');

    const hashedToken =
      crypto
        .createHash('sha256')
        .update(rawToken)
        .digest('hex');

    this.emailVerificationToken =
      hashedToken;

    this.emailVerificationExpires =
      new Date(
        Date.now() +
          24 *
            60 *
            60 *
            1000
      );

    return rawToken;
  };

// ============================================
// VIP
// ============================================

userSchema.methods.updateVipLevel =
  function (): void {
    const totalWagered =
      this.wallet?.totalWagered || 0;

    let level = 1;
    let name = 'Bronze';
    let cashback = 2;

    if (totalWagered >= 1_000_000) {
      level = 8;
      name = 'Ambassador';
      cashback = 25;
    } else if (totalWagered >= 500_000) {
      level = 7;
      name = 'President';
      cashback = 20;
    } else if (totalWagered >= 250_000) {
      level = 6;
      name = 'Elite';
      cashback = 15;
    } else if (totalWagered >= 100_000) {
      level = 5;
      name = 'Diamond';
      cashback = 10;
    } else if (totalWagered >= 50_000) {
      level = 4;
      name = 'Platinum';
      cashback = 7;
    } else if (totalWagered >= 20_000) {
      level = 3;
      name = 'Gold';
      cashback = 5;
    } else if (totalWagered >= 5_000) {
      level = 2;
      name = 'Silver';
      cashback = 3;
    }

    this.vip.level = level;
    this.vip.name = name;

    this.vip.cashbackPercentage =
      cashback;

    this.vip.personalManager =
      level >= 6;

    this.vip.higherLimits =
      level >= 4;

    this.vip.exclusivePromotions =
      level >= 3;

    this.vip.fasterWithdrawals =
      level >= 5;
  };

// ============================================
// CAN PLACE BET
// ============================================

userSchema.methods.canPlaceBet =
  function (
    amount: number
  ): boolean {
    if (
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      return false;
    }

    if (
      !this.isActive ||
      this.isBlocked ||
      this.isSuspended
    ) {
      return false;
    }

    const now =
      new Date();

    const rg =
      this.responsibleGambling;

    if (
      rg?.selfExcluded &&
      rg.selfExclusionEndDate &&
      rg.selfExclusionEndDate >
        now
    ) {
      return false;
    }

    if (
      rg?.coolingOffPeriodEnd &&
      rg.coolingOffPeriodEnd >
        now
    ) {
      return false;
    }

    return (
      (this.wallet?.balance || 0) >=
      amount
    );
  };

// ============================================
// DEPOSIT LIMIT
// ============================================

userSchema.methods.getDepositLimit =
  function (): number {
    let limit =
      this.responsibleGambling
        ?.depositLimit || 0;

    if (
      this.vip?.higherLimits
    ) {
      limit *= 2;
    }

    if (
      (this.vip?.level || 0) >= 7
    ) {
      limit *= 5;
    }

    return limit;
  };

// ============================================
// STATIC: FIND USER
// ============================================

userSchema.statics.findByEmailOrUsername =
  function (
    identifier: string
  ): Promise<IUser | null> {
    const normalized =
      identifier
        .trim()
        .toLowerCase();

    return this.findOne({
      $or: [
        {
          email: normalized,
        },
        {
          username: normalized,
        },
      ],
    }).exec();
  };

// ============================================
// STATIC: TOP WAGERED
// ============================================

userSchema.statics.getTopWagered =
  function (
    limit = 100
  ): Promise<IUser[]> {
    const safeLimit =
      Math.min(
        Math.max(
          Number(limit) || 100,
          1
        ),
        1000
      );

    return this.find({
      isActive: true,
    })
      .sort({
        'wallet.totalWagered': -1,
      })
      .limit(safeLimit)
      .select(
        'username fullName wallet.totalWagered vip.level'
      )
      .exec();
  };

// ============================================
// STATIC: ONLINE USERS
// ============================================

userSchema.statics.getOnlineUsers =
  function (): Promise<number> {
    const fiveMinutesAgo =
      new Date(
        Date.now() -
          5 *
            60 *
            1000
      );

    return this.countDocuments({
      lastActive: {
        $gte: fiveMinutesAgo,
      },
    }).exec();
  };

// ============================================
// MODEL
// ============================================

const User =
  (mongoose.models.User as IUserModel | undefined) ??
  mongoose.model<IUser, IUserModel>(
    'User',
    userSchema
  );

export {
  User,
};

export default User;