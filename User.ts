// ============================================
// SHEBAODDS - USER MODEL
// Mongoose 8 + TypeScript
// ============================================

import mongoose, {
  HydratedDocument,
  Model,
  Schema,
  Types,
} from 'mongoose';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';

// speakeasy has incomplete typings in this project.
// Keep the untyped boundary isolated here.
// eslint-disable-next-line @typescript-eslint/no-var-requires
const speakeasy = require('speakeasy');

// ============================================================
// TYPES
// ============================================================

export type Language =
  | 'en'
  | 'am'
  | 'ar'
  | 'fr'
  | 'es'
  | 'de'
  | 'it'
  | 'pt'
  | 'ru'
  | 'zh';

export type Theme = 'light' | 'dark';

export type Currency =
  | 'ETB'
  | 'USD'
  | 'EUR'
  | 'GBP'
  | 'BTC'
  | 'ETH'
  | 'USDT';

export type DevicePlatform =
  | 'web'
  | 'ios'
  | 'android'
  | 'admin';

export type KycDocumentType =
  | 'national_id'
  | 'passport'
  | 'drivers_license'
  | 'proof_of_address'
  | 'selfie';

export type KycDocumentStatus =
  | 'pending'
  | 'approved'
  | 'rejected';

export type KycStatus =
  | 'unverified'
  | 'pending'
  | 'verified'
  | 'rejected';

export type PaymentMethodType =
  | 'tele_birr'
  | 'cbe'
  | 'card'
  | 'paypal'
  | 'crypto'
  | 'bank_transfer';

// ============================================================
// SUB DOCUMENT INTERFACES
// ============================================================

export interface IWallet {
  balance: number;
  bonusBalance: number;
  lockedBalance: number;
  currency: Currency;

  totalDeposited: number;
  totalWithdrawn: number;
  totalWagered: number;
  totalWon: number;
  totalLost: number;
  totalTaxPaid: number;
  totalBonusReceived: number;
  totalCashbackReceived: number;
}

export interface IStatistics {
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

export interface IVip {
  level: number;
  name: string;
  loyaltyPoints: number;
  cashbackPercentage: number;
  personalManager: boolean;
  higherLimits: boolean;
  exclusivePromotions: boolean;
  fasterWithdrawals: boolean;
}

export interface ITaxProfile {
  taxExempt: boolean;
  taxId?: string;
  taxRegistrationNumber?: string;
  isTaxRegistered: boolean;
  totalTaxPaid: number;
  totalWinningsTaxed: number;
  lastTaxCalculation?: Date;
}

export interface IKycDocument {
  type: KycDocumentType;
  documentUrl?: string;
  documentNumber?: string;
  status: KycDocumentStatus;
  submittedAt: Date;
  reviewedAt?: Date;
  reviewedBy?: string;
  rejectionReason?: string;
  verifiedAt?: Date;
  verifiedBy?: string;
}

export interface ILastLoginLocation {
  city?: string;
  country?: string;
  lat?: number;
  lng?: number;
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

export interface INotifications {
  email: boolean;
  push: boolean;
  sms: boolean;
  betSettlements: boolean;
  promotions: boolean;
  aiTips: boolean;
  systemUpdates: boolean;
  securityAlerts: boolean;
}

export interface IDevice {
  deviceId: string;
  deviceName?: string;
  platform?: DevicePlatform;
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
}

export interface ISavedPaymentMethod {
  type: PaymentMethodType;
  identifier?: string;
  last4?: string;
  brand?: string;
  isDefault: boolean;
  addedAt: Date;
  metadata?: unknown;
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

// ============================================================
// USER INTERFACE
// ============================================================

export interface IUser {
  username: string;
  email: string;
  password: string;
  passwordHistory: string[];

  phone: string;
  fullName?: string;
  dateOfBirth?: Date;

  country: string;
  city?: string;
  address?: string;
  postalCode?: string;

  language: Language;
  theme: Theme;
  currency: Currency;
  timezone: string;

  wallet: IWallet;
  statistics: IStatistics;
  vip: IVip;
  taxProfile: ITaxProfile;

  isActive: boolean;
  isAdmin: boolean;
  isVerified: boolean;
  isBlocked: boolean;
  isSuspended: boolean;

  suspensionReason?: string;
  suspensionEndDate?: Date;

  kycDocuments: IKycDocument[];
  kycStatus: KycStatus;
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
  lastLoginLocation?: ILastLoginLocation;

  resetPasswordToken?: string;
  resetPasswordExpires?: Date;

  referralCode?: string;
  referredBy?: Types.ObjectId;
  referralCount: number;
  referralEarnings: number;
  referralTier: number;

  responsibleGambling: IResponsibleGambling;
  notifications: INotifications;

  devices: IDevice[];
  sessions: IUserSession[];

  savedPaymentMethods: ISavedPaymentMethod[];

  bettingPreferences: IBettingPreferences;

  affiliate: IAffiliate;

  notes: IUserNote[];

  lastLogin?: Date;
  lastActive: Date;

  createdAt: Date;
  updatedAt: Date;
}

// ============================================================
// INSTANCE METHODS
// ============================================================

export interface IUserMethods {
  comparePassword(candidatePassword: string): Promise<boolean>;

  generateReferralCode(): string;

  generateTwoFactorSecret(): {
    ascii?: string;
    hex?: string;
    base32: string;
    otpauth_url?: string;
  };

  verifyTwoFactorToken(token: string): boolean;

  generateBackupCodes(): string[];

  verifyBackupCode(code: string): Promise<boolean>;

  generateEmailVerificationToken(): string;

  updateVipLevel(): void;

  canPlaceBet(amount: number): boolean;

  getDepositLimit(): number;

  toJSON(): Record<string, unknown>;
}

// ============================================================
// DOCUMENT TYPES
// ============================================================

export type UserDocument = HydratedDocument<
  IUser,
  IUserMethods
>;

// ============================================================
// MODEL STATIC METHODS
// ============================================================

export interface IUserModel
  extends Model<IUser, {}, IUserMethods> {
  findByEmailOrUsername(
    identifier: string
  ): Promise<UserDocument | null>;

  getTopWagered(
    limit?: number
  ): Promise<UserDocument[]>;

  getOnlineUsers(): Promise<number>;
}

// ============================================================
// SCHEMA
// ============================================================

const userSchema = new Schema<
  IUser,
  IUserModel,
  IUserMethods
>(
  {
    // ========================================================
    // BASIC INFORMATION
    // ========================================================

    username: {
      type: String,
      required: [true, 'Username is required'],
      unique: true,
      trim: true,
      lowercase: true,
      minlength: [3, 'Username must be at least 3 characters'],
      maxlength: [20, 'Username cannot exceed 20 characters'],
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
      minlength: [8, 'Password must be at least 8 characters'],
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
      match: [
        /^\+?[0-9]{10,15}$/,
        'Please provide a valid phone number',
      ],
      index: true,
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

    // ========================================================
    // PREFERENCES
    // ========================================================

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

    // ========================================================
    // WALLET
    // ========================================================

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

    // ========================================================
    // STATISTICS
    // ========================================================

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

    // ========================================================
    // VIP
    // ========================================================

    vip: {
      level: {
        type: Number,
        default: 0,
        min: 0,
        max: 8,
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

    // ========================================================
    // TAX
    // ========================================================

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

      lastTaxCalculation: {
        type: Date,
      },
    },

    // ========================================================
    // ACCOUNT STATUS
    // ========================================================

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

    suspensionReason: {
      type: String,
    },

    suspensionEndDate: {
      type: Date,
    },

    // ========================================================
    // KYC
    // ========================================================

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
      min: 0,
      max: 3,
    },

    // ========================================================
    // TWO FACTOR AUTHENTICATION
    // ========================================================

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
      default: undefined,
      select: false,
    },

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
    },

    emailVerificationExpires: {
      type: Date,
    },

    phoneVerificationCode: {
      type: String,
    },

    phoneVerificationExpires: {
      type: Date,
    },

    // ========================================================
    // LOGIN SECURITY
    // ========================================================

    loginAttempts: {
      type: Number,
      default: 0,
    },

    lockedUntil: {
      type: Date,
    },

    lastLoginIP: {
      type: String,
    },

    lastLoginLocation: {
      city: String,
      country: String,
      lat: Number,
      lng: Number,
    },

    // ========================================================
    // PASSWORD RESET
    // ========================================================

    resetPasswordToken: {
      type: String,
    },

    resetPasswordExpires: {
      type: Date,
    },

    // ========================================================
    // REFERRALS
    // ========================================================

    referralCode: {
      type: String,
      unique: true,
      sparse: true,
      index: true,
    },

    referredBy: {
      type: Schema.Types.ObjectId,
      ref: 'User',
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

    // ========================================================
    // RESPONSIBLE GAMBLING
    // ========================================================

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

    // ========================================================
    // NOTIFICATIONS
    // ========================================================

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

    // ========================================================
    // DEVICES
    // ========================================================

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

    // ========================================================
    // SESSIONS
    // ========================================================

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
      },
    ],

    // ========================================================
    // PAYMENT METHODS
    // ========================================================

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

        metadata: {
          type: Schema.Types.Mixed,
        },
      },
    ],

    // ========================================================
    // BETTING PREFERENCES
    // ========================================================

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

    // ========================================================
    // AFFILIATE
    // ========================================================

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

    // ========================================================
    // NOTES
    // ========================================================

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

    // ========================================================
    // TIMESTAMPS
    // ========================================================

    lastLogin: Date,

    lastActive: {
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

userSchema.index({ createdAt: -1 });
userSchema.index({ lastActive: -1 });
userSchema.index({ 'wallet.balance': -1 });
userSchema.index({ 'wallet.totalWagered': -1 });
userSchema.index({ 'vip.level': -1 });
userSchema.index({ 'vip.loyaltyPoints': -1 });
userSchema.index({ referredBy: 1 });
userSchema.index({ 'devices.deviceId': 1 });
userSchema.index({ 'sessions.sessionId': 1 });
userSchema.index({ kycStatus: 1 });

// ============================================================
// PASSWORD HASHING
// ============================================================

userSchema.pre(
  'save',
  async function (next) {
    try {
      if (!this.isModified('password')) {
        this.updatedAt = new Date();
        return next();
      }

      if (
        !this.isNew &&
        this.password &&
        Array.isArray(this.passwordHistory)
      ) {
        this.passwordHistory.unshift(this.password);
        this.passwordHistory =
          this.passwordHistory.slice(0, 5);
      }

      this.password = await bcrypt.hash(
        this.password,
        12
      );

      this.updatedAt = new Date();

      next();
    } catch (error) {
      next(error as Error);
    }
  }
);

// ============================================================
// REFERRAL CODE
// ============================================================

userSchema.pre(
  'save',
  function (next) {
    if (!this.referralCode && this.isNew) {
      this.referralCode =
        `SHB${crypto
          .randomBytes(4)
          .toString('hex')
          .toUpperCase()}`;
    }

    this.updatedAt = new Date();

    next();
  }
);

// ============================================================
// COMPARE PASSWORD
// ============================================================

userSchema.methods.comparePassword =
  async function (
    this: UserDocument,
    candidatePassword: string
  ): Promise<boolean> {
    if (!this.password) {
      return false;
    }

    return bcrypt.compare(
      candidatePassword,
      this.password
    );
  };

// ============================================================
// REFERRAL CODE
// ============================================================

userSchema.methods.generateReferralCode =
  function (
    this: UserDocument
  ): string {
    return (
      `SHB${crypto
        .randomBytes(4)
        .toString('hex')
        .toUpperCase()}`
    );
  };

// ============================================================
// 2FA SECRET
// ============================================================

userSchema.methods.generateTwoFactorSecret =
  function (
    this: UserDocument
  ) {
    const secret = speakeasy.generateSecret({
      length: 20,
      name: `SHEBAODDS (${this.email})`,
      issuer: 'SHEBAODDS',
    });

    this.twoFactorSecret = secret.base32;

    return secret;
  };

// ============================================================
// VERIFY TOTP
// ============================================================

userSchema.methods.verifyTwoFactorToken =
  function (
    this: UserDocument,
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
        secret: this.twoFactorSecret,
        encoding: 'base32',
        token,
        window: 2,
      })
    );
  };

// ============================================================
// BACKUP CODES
// ============================================================

userSchema.methods.generateBackupCodes =
  function (
    this: UserDocument
  ): string[] {
    const codes = Array.from(
      { length: 10 },
      () =>
        crypto
          .randomBytes(5)
          .toString('hex')
          .toUpperCase()
    );

    this.twoFactorBackupCodes =
      codes.map((code) =>
        bcrypt.hashSync(code, 12)
      );

    return codes;
  };

// ============================================================
// VERIFY BACKUP CODE
// ============================================================

userSchema.methods.verifyBackupCode =
  async function (
    this: UserDocument,
    code: string
  ): Promise<boolean> {
    if (
      !this.twoFactorBackupCodes ||
      this.twoFactorBackupCodes.length === 0
    ) {
      return false;
    }

    const normalized =
      String(code)
        .trim()
        .toUpperCase();

    for (
      let index = 0;
      index < this.twoFactorBackupCodes.length;
      index++
    ) {
      const valid =
        await bcrypt.compare(
          normalized,
          this.twoFactorBackupCodes[index]
        );

      if (valid) {
        this.twoFactorBackupCodes.splice(
          index,
          1
        );

        return true;
      }
    }

    return false;
  };

// ============================================================
// EMAIL VERIFICATION TOKEN
// ============================================================

userSchema.methods.generateEmailVerificationToken =
  function (
    this: UserDocument
  ): string {
    const token =
      crypto.randomBytes(32).toString('hex');

    this.emailVerificationToken =
      crypto
        .createHash('sha256')
        .update(token)
        .digest('hex');

    this.emailVerificationExpires =
      new Date(
        Date.now() +
          24 * 60 * 60 * 1000
      );

    return token;
  };

// ============================================================
// VIP
// ============================================================

userSchema.methods.updateVipLevel =
  function (
    this: UserDocument
  ): void {
    const totalWagered =
      this.wallet?.totalWagered ?? 0;

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
    this.vip.cashbackPercentage = cashback;

    this.vip.personalManager = level >= 6;
    this.vip.higherLimits = level >= 4;
    this.vip.exclusivePromotions = level >= 3;
    this.vip.fasterWithdrawals = level >= 5;
  };

// ============================================================
// CAN PLACE BET
// ============================================================

userSchema.methods.canPlaceBet =
  function (
    this: UserDocument,
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

    const now = new Date();

    if (
      this.responsibleGambling?.selfExcluded &&
      (
        !this.responsibleGambling
          .selfExclusionEndDate ||
        this.responsibleGambling
          .selfExclusionEndDate > now
      )
    ) {
      return false;
    }

    if (
      this.responsibleGambling
        ?.coolingOffPeriodEnd &&
      this.responsibleGambling
        .coolingOffPeriodEnd > now
    ) {
      return false;
    }

    return (
      (this.wallet?.balance ?? 0) >=
      amount
    );
  };

// ============================================================
// DEPOSIT LIMIT
// ============================================================

userSchema.methods.getDepositLimit =
  function (
    this: UserDocument
  ): number {
    let limit =
      this.responsibleGambling
        ?.depositLimit ?? 0;

    if (this.vip?.higherLimits) {
      limit *= 2;
    }

    if ((this.vip?.level ?? 0) >= 7) {
      limit *= 5;
    }

    return limit;
  };

// ============================================================
// SAFE JSON
// ============================================================

userSchema.methods.toJSON =
  function (
    this: UserDocument
  ): Record<string, unknown> {
    const obj =
      this.toObject() as Record<
        string,
        unknown
      >;

    delete obj.password;
    delete obj.passwordHistory;
    delete obj.twoFactorSecret;
    delete obj.twoFactorBackupCodes;
    delete obj.resetPasswordToken;
    delete obj.emailVerificationToken;
    delete obj.phoneVerificationCode;

    return obj;
  };

// ============================================================
// STATIC: FIND BY EMAIL OR USERNAME
// ============================================================

userSchema.statics.findByEmailOrUsername =
  function (
    this: IUserModel,
    identifier: string
  ): Promise<UserDocument | null> {
    const normalized =
      identifier
        .trim()
        .toLowerCase();

    return this.findOne({
      $or: [
        { email: normalized },
        { username: normalized },
      ],
    }).exec();
  };

// ============================================================
// STATIC: TOP WAGERED
// ============================================================

userSchema.statics.getTopWagered =
  function (
    this: IUserModel,
    limit = 100
  ): Promise<UserDocument[]> {
    const safeLimit =
      Math.min(
        Math.max(
          Math.floor(limit),
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

// ============================================================
// STATIC: ONLINE USERS
// ============================================================

userSchema.statics.getOnlineUsers =
  function (
    this: IUserModel
  ): Promise<number> {
    const fiveMinutesAgo =
      new Date(
        Date.now() -
          5 * 60 * 1000
      );

    return this.countDocuments({
      lastActive: {
        $gte: fiveMinutesAgo,
      },
    }).exec();
  };

// ============================================================
// MODEL
// ============================================================

export const User: IUserModel =
  (mongoose.models.User as IUserModel | undefined) ??
  mongoose.model<IUser, IUserModel>(
    'User',
    userSchema
  );

export default User;