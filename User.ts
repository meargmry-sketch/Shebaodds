// ============================================
// SHEBAODDS - USER MODEL
// Enterprise Grade User Schema
// ============================================

import mongoose, {
  Schema,
  Document,
  Model
} from 'mongoose';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';

const speakeasy = require('speakeasy');

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

export const createDefaultWallet = (): IUserWallet => ({
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
  totalCashbackReceived: 0
});

// ============================================
// TYPES
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
// USER INTERFACE
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

  statistics: {
    totalBets: number;
    totalWins: number;
    totalLosses: number;
    winningPercentage: number;
    currentWinStreak: number;
    longestWinStreak: number;
    biggestWin: number;
    biggestLoss: number;
    averageOdds: number;
  };

  vip: {
    level: number;
    name: string;
    loyaltyPoints: number;
    cashbackPercentage: number;
    personalManager: boolean;
    higherLimits: boolean;
    exclusivePromotions: boolean;
    fasterWithdrawals: boolean;
  };

  taxProfile: {
    taxExempt: boolean;
    taxId?: string;
    taxRegistrationNumber?: string;
    isTaxRegistered: boolean;
    totalTaxPaid: number;
    totalWinningsTaxed: number;
    lastTaxCalculation?: Date;
  };

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
  referredBy?: mongoose.Types.ObjectId;

  referralCount: number;
  referralEarnings: number;
  referralTier: number;

  responsibleGambling: {
    depositLimit: number;
    lossLimit: number;
    wagerLimit: number;
    sessionTimeout: number;
    realityCheckInterval: number;

    selfExcluded: boolean;
    selfExclusionEndDate?: Date;
    coolingOffPeriodEnd?: Date;
    lastRealityCheck?: Date;
  };

  notifications: {
    email: boolean;
    push: boolean;
    sms: boolean;
    betSettlements: boolean;
    promotions: boolean;
    aiTips: boolean;
    systemUpdates: boolean;
    securityAlerts: boolean;
  };

  devices: Array<{
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
  }>;

  sessions: Array<{
    sessionId: string;
    ipAddress?: string;
    userAgent?: string;
    deviceId?: string;

    loginAt: Date;
    lastActivity: Date;
    expiresAt?: Date;
  }>;

  savedPaymentMethods: Array<{
    type: SavedPaymentMethodType;
    identifier?: string;
    last4?: string;
    brand?: string;
    isDefault: boolean;
    addedAt: Date;
    metadata?: any;
  }>;

  bettingPreferences: {
    defaultStake: number;
    autoCashoutMultiplier: number;
    favoriteLeagues: string[];
    favoriteTeams: string[];
    excludedMarkets: string[];
  };

  affiliate: {
    partnerId?: string;
    commissionRate: number;
    totalCommission: number;
    paidCommission: number;
  };

  notes: Array<{
    note?: string;
    createdBy?: mongoose.Types.ObjectId;
    createdAt: Date;
  }>;

  lastLogin?: Date;
  lastActive: Date;

  createdAt: Date;
  updatedAt: Date;

  comparePassword(
    candidatePassword: string
  ): Promise<boolean>;

  generateReferralCode(): string;

  generateTwoFactorSecret(): any;

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
// MODEL INTERFACE
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
    username: {
      type: String,
      unique: true,
      required: [true, 'Username is required'],
      trim: true,
      lowercase: true,
      minlength: [3, 'Username must be at least 3 characters'],
      maxlength: [20, 'Username cannot exceed 20 characters'],
      match: [
        /^[a-zA-Z0-9_]+$/,
        'Username can only contain letters, numbers and underscore'
      ],
      index: true
    },

    email: {
      type: String,
      unique: true,
      required: [true, 'Email is required'],
      trim: true,
      lowercase: true,
      match: [
        /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
        'Please provide a valid email'
      ],
      index: true
    },

    password: {
      type: String,
      required: [true, 'Password is required'],
      minlength: [8, 'Password must be at least 8 characters'],
      select: false
    },

    passwordHistory: {
      type: [String],
      default: [],
      select: false
    },

    phone: {
      type: String,
      required: [true, 'Phone number is required'],
      unique: true,
      index: true,
      match: [
        /^\+?[0-9]{10,15}$/,
        'Please provide a valid phone number'
      ]
    },

    fullName: {
      type: String,
      trim: true,
      maxlength: [
        100,
        'Full name cannot exceed 100 characters'
      ]
    },

    dateOfBirth: {
      type: Date
    },

    country: {
      type: String,
      default: 'Ethiopia'
    },

    city: String,
    address: String,
    postalCode: String,

    language: {
      type: String,
      default: 'en',
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
        'zh'
      ]
    },

    theme: {
      type: String,
      enum: ['light', 'dark'],
      default: 'dark'
    },

    currency: {
      type: String,
      default: 'ETB',
      enum: [
        'ETB',
        'USD',
        'EUR',
        'GBP',
        'BTC',
        'ETH',
        'USDT'
      ]
    },

    timezone: {
      type: String,
      default: 'Africa/Addis_Ababa'
    },

    // ============================================
    // WALLET
    // ============================================

    wallet: {
      balance: {
        type: Number,
        default: 0,
        min: 0
      },

      bonusBalance: {
        type: Number,
        default: 0,
        min: 0
      },

      lockedBalance: {
        type: Number,
        default: 0,
        min: 0
      },

      currency: {
        type: String,
        default: 'ETB'
      },

      totalDeposited: {
        type: Number,
        default: 0
      },

      totalWithdrawn: {
        type: Number,
        default: 0
      },

      totalWagered: {
        type: Number,
        default: 0
      },

      totalWon: {
        type: Number,
        default: 0
      },

      totalLost: {
        type: Number,
        default: 0
      },

      totalTaxPaid: {
        type: Number,
        default: 0
      },

      totalBonusReceived: {
        type: Number,
        default: 0
      },

      totalCashbackReceived: {
        type: Number,
        default: 0
      }
    },

    // ============================================
    // STATISTICS
    // ============================================

    statistics: {
      totalBets: {
        type: Number,
        default: 0
      },

      totalWins: {
        type: Number,
        default: 0
      },

      totalLosses: {
        type: Number,
        default: 0
      },

      winningPercentage: {
        type: Number,
        default: 0
      },

      currentWinStreak: {
        type: Number,
        default: 0
      },

      longestWinStreak: {
        type: Number,
        default: 0
      },

      biggestWin: {
        type: Number,
        default: 0
      },

      biggestLoss: {
        type: Number,
        default: 0
      },

      averageOdds: {
        type: Number,
        default: 0
      }
    },

    // ============================================
    // VIP
    // ============================================

    vip: {
      level: {
        type: Number,
        default: 0,
        enum: [0, 1, 2, 3, 4, 5, 6, 7, 8]
      },

      name: {
        type: String,
        default: 'Bronze'
      },

      loyaltyPoints: {
        type: Number,
        default: 0
      },

      cashbackPercentage: {
        type: Number,
        default: 0
      },

      personalManager: {
        type: Boolean,
        default: false
      },

      higherLimits: {
        type: Boolean,
        default: false
      },

      exclusivePromotions: {
        type: Boolean,
        default: false
      },

      fasterWithdrawals: {
        type: Boolean,
        default: false
      }
    },

    // ============================================
    // TAX
    // ============================================

    taxProfile: {
      taxExempt: {
        type: Boolean,
        default: false
      },

      taxId: {
        type: String,
        sparse: true
      },

      taxRegistrationNumber: {
        type: String,
        sparse: true
      },

      isTaxRegistered: {
        type: Boolean,
        default: false
      },

      totalTaxPaid: {
        type: Number,
        default: 0
      },

      totalWinningsTaxed: {
        type: Number,
        default: 0
      },

      lastTaxCalculation: Date
    },

    // ============================================
    // STATUS
    // ============================================

    isActive: {
      type: Boolean,
      default: true,
      index: true
    },

    isAdmin: {
      type: Boolean,
      default: false,
      index: true
    },

    isVerified: {
      type: Boolean,
      default: false,
      index: true
    },

    isBlocked: {
      type: Boolean,
      default: false,
      index: true
    },

    isSuspended: {
      type: Boolean,
      default: false
    },

    suspensionReason: String,
    suspensionEndDate: Date,

    // ============================================
    // KYC
    // ============================================

    kycDocuments: [
      {
        type: {
          type: String,
          enum: [
            'national_id',
            'passport',
            'drivers_license',
            'proof_of_address',
            'selfie'
          ]
        },

        documentUrl: String,
        documentNumber: String,

        status: {
          type: String,
          enum: [
            'pending',
            'approved',
            'rejected'
          ],
          default: 'pending'
        },

        submittedAt: {
          type: Date,
          default: Date.now
        },

        reviewedAt: Date,
        reviewedBy: String,
        rejectionReason: String,
        verifiedAt: Date,
        verifiedBy: String
      }
    ],

    kycStatus: {
      type: String,
      enum: [
        'unverified',
        'pending',
        'verified',
        'rejected'
      ],
      default: 'unverified'
    },

    kycLevel: {
      type: Number,
      default: 0,
      enum: [0, 1, 2, 3]
    },

    // ============================================
    // 2FA
    // ============================================

    twoFactorEnabled: {
      type: Boolean,
      default: false
    },

    twoFactorSecret: {
      type: String,
      select: false
    },

    twoFactorBackupCodes: [
      {
        type: String,
        select: false
      }
    ],

    emailVerified: {
      type: Boolean,
      default: false
    },

    phoneVerified: {
      type: Boolean,
      default: false
    },

    emailVerificationToken: String,
    emailVerificationExpires: Date,

    phoneVerificationCode: String,
    phoneVerificationExpires: Date,

    // ============================================
    // LOGIN SECURITY
    // ============================================

    loginAttempts: {
      type: Number,
      default: 0
    },

    lockedUntil: Date,
    lastLoginIP: String,

    lastLoginLocation: {
      city: String,
      country: String,
      lat: Number,
      lng: Number
    },

    resetPasswordToken: String,
    resetPasswordExpires: Date,

    // ============================================
    // REFERRALS
    // ============================================

    referralCode: {
      type: String,
      unique: true,
      sparse: true
    },

    referredBy: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },

    referralCount: {
      type: Number,
      default: 0
    },

    referralEarnings: {
      type: Number,
      default: 0
    },

    referralTier: {
      type: Number,
      default: 1
    },

    // ============================================
    // RESPONSIBLE GAMBLING
    // ============================================

    responsibleGambling: {
      depositLimit: {
        type: Number,
        default: 10000
      },

      lossLimit: {
        type: Number,
        default: 5000
      },

      wagerLimit: {
        type: Number,
        default: 50000
      },

      sessionTimeout: {
        type: Number,
        default: 120
      },

      realityCheckInterval: {
        type: Number,
        default: 60
      },

      selfExcluded: {
        type: Boolean,
        default: false
      },

      selfExclusionEndDate: Date,
      coolingOffPeriodEnd: Date,
      lastRealityCheck: Date
    },

    // ============================================
    // NOTIFICATIONS
    // ============================================

    notifications: {
      email: {
        type: Boolean,
        default: true
      },

      push: {
        type: Boolean,
        default: true
      },

      sms: {
        type: Boolean,
        default: false
      },

      betSettlements: {
        type: Boolean,
        default: true
      },

      promotions: {
        type: Boolean,
        default: true
      },

      aiTips: {
        type: Boolean,
        default: true
      },

      systemUpdates: {
        type: Boolean,
        default: true
      },

      securityAlerts: {
        type: Boolean,
        default: true
      }
    },

    // ============================================
    // DEVICES
    // ============================================

    devices: [
      {
        deviceId: {
          type: String,
          required: true
        },

        deviceName: String,

        platform: {
          type: String,
          enum: [
            'web',
            'ios',
            'android',
            'admin'
          ]
        },

        browser: String,
        os: String,
        ipAddress: String,
        location: String,
        pushToken: String,

        biometricEnabled: {
          type: Boolean,
          default: false
        },

        biometricPublicKey: String,

        lastUsed: {
          type: Date,
          default: Date.now
        },

        isActive: {
          type: Boolean,
          default: true
        }
      }
    ],

    // ============================================
    // SESSIONS
    // ============================================

    sessions: [
      {
        sessionId: {
          type: String,
          required: true
        },

        ipAddress: String,
        userAgent: String,
        deviceId: String,

        loginAt: {
          type: Date,
          default: Date.now
        },

        lastActivity: {
          type: Date,
          default: Date.now
        },

        expiresAt: Date
      }
    ],

    // ============================================
    // PAYMENT METHODS
    // ============================================

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
            'bank_transfer'
          ]
        },

        identifier: String,
        last4: String,
        brand: String,

        isDefault: {
          type: Boolean,
          default: false
        },

        addedAt: {
          type: Date,
          default: Date.now
        },

        metadata: mongoose.Schema.Types.Mixed
      }
    ],

    // ============================================
    // BETTING PREFERENCES
    // ============================================

    bettingPreferences: {
      defaultStake: {
        type: Number,
        default: 10
      },

      autoCashoutMultiplier: {
        type: Number,
        default: 0
      },

      favoriteLeagues: [
        {
          type: String
        }
      ],

      favoriteTeams: [
        {
          type: String
        }
      ],

      excludedMarkets: [
        {
          type: String
        }
      ]
    },

    // ============================================
    // AFFILIATE
    // ============================================

    affiliate: {
      partnerId: String,

      commissionRate: {
        type: Number,
        default: 0
      },

      totalCommission: {
        type: Number,
        default: 0
      },

      paidCommission: {
        type: Number,
        default: 0
      }
    },

    // ============================================
    // NOTES
    // ============================================

    notes: [
      {
        note: String,

        createdBy: {
          type: mongoose.Schema.Types.ObjectId,
          ref: 'User'
        },

        createdAt: {
          type: Date,
          default: Date.now
        }
      }
    ],

    // Timestamps are handled by Mongoose.
    lastLogin: Date,

    lastActive: {
      type: Date,
      default: Date.now
    }
  },
  {
    timestamps: true,

    toJSON: {
      virtuals: true
    },

    toObject: {
      virtuals: true
    }
  }
);

// ============================================
// INDEXES
// ============================================

userSchema.index({ createdAt: -1 });
userSchema.index({ lastActive: -1 });
userSchema.index({ 'wallet.balance': -1 });
userSchema.index({ 'vip.level': -1 });
userSchema.index({ 'vip.loyaltyPoints': -1 });
userSchema.index({ referralCode: 1 });
userSchema.index({ referredBy: 1 });
userSchema.index({ 'devices.deviceId': 1 });
userSchema.index({ 'sessions.sessionId': 1 });
userSchema.index({ kycStatus: 1 });

// ============================================
// PASSWORD HASH
// ============================================

userSchema.pre('save', async function(next) {
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

    if (!this.passwordHistory) {
      this.passwordHistory = [];
    }

    /*
     * Avoid adding the same hash twice.
     * The history contains hashes, not plaintext.
     */
    this.passwordHistory.unshift(hashedPassword);

    if (this.passwordHistory.length > 5) {
      this.passwordHistory =
        this.passwordHistory.slice(0, 5);
    }

    this.password = hashedPassword;

    next();
  } catch (error) {
    next(error as Error);
  }
});

// ============================================
// NORMALIZATION
// ============================================

userSchema.pre('save', function(next) {
  try {
    if (!this.wallet) {
      this.wallet = createDefaultWallet();
    }

    if (!this.referralCode && this.isNew) {
      this.referralCode =
        this.generateReferralCode();
    }

    next();
  } catch (error) {
    next(error as Error);
  }
});

// ============================================
// PASSWORD
// ============================================

userSchema.methods.comparePassword =
  async function(
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

// ============================================
// REFERRAL CODE
// ============================================

userSchema.methods.generateReferralCode =
  function(): string {
    const random = crypto
      .randomBytes(4)
      .toString('hex')
      .toUpperCase();

    return `SHB${random}`;
  };

// ============================================
// 2FA SECRET
// ============================================

userSchema.methods.generateTwoFactorSecret =
  function(): any {
    const secret =
      speakeasy.generateSecret({
        length: 20,
        name: `SHEBAODDS (${this.email})`
      });

    this.twoFactorSecret =
      secret.base32;

    return secret;
  };

// ============================================
// VERIFY 2FA
// ============================================

userSchema.methods.verifyTwoFactorToken =
  function(token: string): boolean {
    if (!this.twoFactorSecret) {
      return false;
    }

    return Boolean(
      speakeasy.totp.verify({
        secret: this.twoFactorSecret,
        encoding: 'base32',
        token,
        window: 2
      })
    );
  };

// ============================================
// BACKUP CODES
// ============================================

userSchema.methods.generateBackupCodes =
  function(): string[] {
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
      codes.map(code =>
        bcrypt.hashSync(code, 10)
      );

    return codes;
  };

// ============================================
// VERIFY BACKUP CODE
// ============================================

userSchema.methods.verifyBackupCode =
  async function(
    code: string
  ): Promise<boolean> {
    if (
      !this.twoFactorBackupCodes ||
      this.twoFactorBackupCodes.length === 0
    ) {
      return false;
    }

    for (
      let i = 0;
      i < this.twoFactorBackupCodes.length;
      i++
    ) {
      const hash =
        this.twoFactorBackupCodes[i];

      if (
        await bcrypt.compare(code, hash)
      ) {
        /*
         * Backup codes are one-time-use.
         */
        this.twoFactorBackupCodes.splice(i, 1);

        await this.save();

        return true;
      }
    }

    return false;
  };

// ============================================
// EMAIL VERIFICATION
// ============================================

userSchema.methods.generateEmailVerificationToken =
  function(): string {
    const token =
      crypto.randomBytes(32).toString('hex');

    this.emailVerificationToken =
      token;

    this.emailVerificationExpires =
      new Date(
        Date.now() +
        24 * 60 * 60 * 1000
      );

    return token;
  };

// ============================================
// VIP
// ============================================

userSchema.methods.updateVipLevel =
  function(): void {
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
    this.vip.cashbackPercentage = cashback;

    this.vip.personalManager = level >= 6;
    this.vip.higherLimits = level >= 4;
    this.vip.exclusivePromotions = level >= 3;
    this.vip.fasterWithdrawals = level >= 5;
  };

// ============================================
// CAN PLACE BET
// ============================================

userSchema.methods.canPlaceBet =
  function(amount: number): boolean {
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
      this.responsibleGambling.selfExclusionEndDate &&
      this.responsibleGambling.selfExclusionEndDate > now
    ) {
      return false;
    }

    if (
      this.responsibleGambling?.coolingOffPeriodEnd &&
      this.responsibleGambling.coolingOffPeriodEnd > now
    ) {
      return false;
    }

    return (
      (this.wallet?.balance || 0) >= amount
    );
  };

// ============================================
// DEPOSIT LIMIT
// ============================================

userSchema.methods.getDepositLimit =
  function(): number {
    let limit =
      this.responsibleGambling
        ?.depositLimit || 0;

    if (this.vip?.higherLimits) {
      limit *= 2;
    }

    if ((this.vip?.level || 0) >= 7) {
      limit *= 5;
    }

    return limit;
  };

// ============================================
// SAFE JSON
// ============================================

userSchema.methods.toJSON =
  function(): any {
    const obj = this.toObject();

    delete obj.password;
    delete obj.passwordHistory;
    delete obj.twoFactorSecret;
    delete obj.twoFactorBackupCodes;
    delete obj.resetPasswordToken;
    delete obj.emailVerificationToken;
    delete obj.phoneVerificationCode;

    return obj;
  };

// ============================================
// STATIC: FIND USER
// ============================================

userSchema.statics.findByEmailOrUsername =
  function(
    identifier: string
  ): Promise<IUser | null> {
    const normalized =
      identifier.trim().toLowerCase();

    return this.findOne({
      $or: [
        { email: normalized },
        { username: normalized }
      ]
    }).exec();
  };

// ============================================
// STATIC: TOP WAGERED
// ============================================

userSchema.statics.getTopWagered =
  function(
    limit = 100
  ): Promise<IUser[]> {
    const safeLimit =
      Math.min(Math.max(limit, 1), 1000);

    return this.find({
      isActive: true
    })
      .sort({
        'wallet.totalWagered': -1
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
  function(): Promise<number> {
    const fiveMinutesAgo =
      new Date(
        Date.now() -
        5 * 60 * 1000
      );

    return this.countDocuments({
      lastActive: {
        $gte: fiveMinutesAgo
      }
    }).exec();
  };

// ============================================
// MODEL
// ============================================

const existingUserModel =
  mongoose.models.User as
    | IUserModel
    | undefined;

export const User: IUserModel =
  existingUserModel ||
  mongoose.model<IUser, IUserModel>(
    'User',
    userSchema
  );

export default User;