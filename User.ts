// ============================================
// SHEBAODDS - USER MODEL
// Enterprise Grade User Schema
// Fixed TypeScript + Mongoose 8 Version
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
    // ==========================================
    // BASIC INFORMATION
    // ==========================================

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
      index: true,
      match: [
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        'Please provide a valid email'
      ]
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
      maxlength: [100, 'Full name cannot exceed 100 characters']
    },

    dateOfBirth: {
      type: Date
    },

    country: {
      type: String,
      default: 'Ethiopia',
      trim: true
    },

    city: {
      type: String,
      trim: true
    },

    address: {
      type: String,
      trim: true
    },

    postalCode: {
      type: String,
      trim: true
    },

    // ==========================================
    // PREFERENCES
    // ==========================================

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
        'zh'
      ],
      default: 'en'
    },

    theme: {
      type: String,
      enum: ['light', 'dark'],
      default: 'dark'
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
        'USDT'
      ],
      default: 'ETB'
    },

    timezone: {
      type: String,
      default: 'Africa/Addis_Ababa'
    },

    // ==========================================
    // WALLET
    // ==========================================

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

    // ==========================================
    // STATISTICS
    // ==========================================

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

    // ==========================================
    // VIP
    // ==========================================

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

    // ==========================================
    // TAX PROFILE
    // ==========================================

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

      lastTaxCalculation: {
        type: Date
      }
    },

    // ==========================================
    // ACCOUNT STATUS
    // ==========================================

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
      enum: [0, 1, 2, 3],
      default: 0
    },

    // ==========================================
    // TWO FACTOR AUTHENTICATION
    // ==========================================

    twoFactorEnabled: {
      type: Boolean,
      default: false
    },

    twoFactorSecret: {
      type: String,
      select: false
    },

    twoFactorBackupCodes: {
      type: [String],
      default: [],
      select: false
    },

    // ==========================================
    // VERIFICATION
    // ==========================================

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

    // ==========================================
    // LOGIN SECURITY
    // ==========================================

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

    // ==========================================
    // PASSWORD RESET
    // ==========================================

    resetPasswordToken: String,

    resetPasswordExpires: Date,

    // ==========================================
    // REFERRALS
    // ==========================================

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

    // ==========================================
    // RESPONSIBLE GAMBLING
    // ==========================================

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

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

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

    // ==========================================
    // DEVICES
    // ==========================================

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

    // ==========================================
    // SESSIONS
    // ==========================================

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

    // ==========================================
    // BETTING PREFERENCES
    // ==========================================

    bettingPreferences: {
      defaultStake: {
        type: Number,
        default: 10
      },

      autoCashoutMultiplier: {
        type: Number,
        default: 0
      },

      favoriteLeagues: {
        type: [String],
        default: []
      },

      favoriteTeams: {
        type: [String],
        default: []
      },

      excludedMarkets: {
        type: [String],
        default: []
      }
    },

    // ==========================================
    // AFFILIATE
    // ==========================================

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

    // ==========================================
    // ADMIN NOTES
    // ==========================================

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

    // ==========================================
    // LAST ACTIVITY
    // ==========================================

    lastLogin: Date,

    lastActive: {
      type: Date,
      default: Date.now,
      index: true
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
userSchema.index({ 'wallet.totalWagered': -1 });
userSchema.index({ 'vip.level': -1 });
userSchema.index({ 'vip.loyaltyPoints': -1 });
userSchema.index({ referralCode: 1 });
userSchema.index({ referredBy: 1 });
userSchema.index({ 'devices.deviceId': 1 });
userSchema.index({ 'sessions.sessionId': 1 });
userSchema.index({ kycStatus: 1 });

// ============================================
// PASSWORD HASHING
// ============================================

userSchema.pre('save', async function(next) {
  try {
    const user = this as IUser;

    if (!user.isModified('password')) {
      return next();
    }

    if (!user.password) {
      return next();
    }

    /*
     * IMPORTANT:
     *
     * passwordHistory contains bcrypt hashes.
     * We store the OLD/current hash before replacing
     * the password with the new hash.
     */

    const oldPasswordHash =
      user.isNew
        ? undefined
        : user.get('password', null);

    const salt = await bcrypt.genSalt(12);

    const newPasswordHash =
      await bcrypt.hash(
        user.password,
        salt
      );

    if (!user.passwordHistory) {
      user.passwordHistory = [];
    }

    /*
     * Do not duplicate the same hash.
     */
    if (
      oldPasswordHash &&
      typeof oldPasswordHash === 'string'
    ) {
      const alreadyStored =
        user.passwordHistory.includes(
          oldPasswordHash
        );

      if (!alreadyStored) {
        user.passwordHistory.unshift(
          oldPasswordHash
        );
      }
    }

    /*
     * Keep only the last 5 password hashes.
     */
    if (user.passwordHistory.length > 5) {
      user.passwordHistory =
        user.passwordHistory.slice(0, 5);
    }

    user.password = newPasswordHash;

    next();
  } catch (error) {
    next(error as Error);
  }
});

// ============================================
// PRE-SAVE NORMALIZATION
// ============================================

userSchema.pre('save', function(next) {
  try {
    const user = this as IUser;

    if (!user.wallet) {
      user.wallet = createDefaultWallet();
    }

    if (!user.wallet.currency) {
      user.wallet.currency =
        user.currency || 'ETB';
    }

    if (!user.referralCode && user.isNew) {
      user.referralCode =
        user.generateReferralCode();
    }

    user.updatedAt = new Date();

    next();
  } catch (error) {
    next(error as Error);
  }
});

// ============================================
// PASSWORD COMPARISON
// ============================================

userSchema.methods.comparePassword =
  async function(
    candidatePassword: string
  ): Promise<boolean> {
    const user = this as IUser;

    if (
      !candidatePassword ||
      !user.password
    ) {
      return false;
    }

    return bcrypt.compare(
      candidatePassword,
      user.password
    );
  };

// ============================================
// REFERRAL CODE
// ============================================

userSchema.methods.generateReferralCode =
  function(): string {
    const random =
      crypto
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
    const user = this as IUser;

    const secret =
      speakeasy.generateSecret({
        length: 20,
        name: `SHEBAODDS (${user.email})`,
        issuer: 'SHEBAODDS'
      });

    user.twoFactorSecret =
      secret.base32;

    return secret;
  };

// ============================================
// VERIFY 2FA
// ============================================

userSchema.methods.verifyTwoFactorToken =
  function(
    token: string
  ): boolean {
    const user = this as IUser;

    if (
      !user.twoFactorSecret ||
      !token
    ) {
      return false;
    }

    return Boolean(
      speakeasy.totp.verify({
        secret: user.twoFactorSecret,
        encoding: 'base32',
        token: String(token),
        window: 2
      })
    );
  };

// ============================================
// GENERATE BACKUP CODES
// ============================================

userSchema.methods.generateBackupCodes =
  function(): string[] {
    const user = this as IUser;

    const codes: string[] = [];

    for (let i = 0; i < 10; i++) {
      const code =
        crypto
          .randomBytes(6)
          .toString('hex')
          .toUpperCase();

      codes.push(code);
    }

    /*
     * Store only bcrypt hashes.
     * Plain-text codes are returned once to the user.
     */
    user.twoFactorBackupCodes =
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
    const user = this as IUser;

    if (
      !code ||
      !user.twoFactorBackupCodes ||
      user.twoFactorBackupCodes.length === 0
    ) {
      return false;
    }

    for (
      let i = 0;
      i < user.twoFactorBackupCodes.length;
      i++
    ) {
      const valid =
        await bcrypt.compare(
          code.trim().toUpperCase(),
          user.twoFactorBackupCodes[i]
        );

      if (valid) {
        /*
         * Backup codes are single-use.
         */
        user.twoFactorBackupCodes.splice(
          i,
          1
        );

        await user.save();

        return true;
      }
    }

    return false;
  };

// ============================================
// EMAIL VERIFICATION TOKEN
// ============================================

userSchema.methods.generateEmailVerificationToken =
  function(): string {
    const user = this as IUser;

    const token =
      crypto
        .randomBytes(32)
        .toString('hex');

    user.emailVerificationToken =
      token;

    user.emailVerificationExpires =
      new Date(
        Date.now() +
        24 * 60 * 60 * 1000
      );

    return token;
  };

// ============================================
// VIP LEVEL
// ============================================

userSchema.methods.updateVipLevel =
  function(): void {
    const user = this as IUser;

    const totalWagered =
      user.wallet?.totalWagered || 0;

    let newLevel = 1;
    let newName = 'Bronze';
    let cashback = 2;

    if (totalWagered >= 1_000_000) {
      newLevel = 8;
      newName = 'Ambassador';
      cashback = 25;
    } else if (totalWagered >= 500_000) {
      newLevel = 7;
      newName = 'President';
      cashback = 20;
    } else if (totalWagered >= 250_000) {
      newLevel = 6;
      newName = 'Elite';
      cashback = 15;
    } else if (totalWagered >= 100_000) {
      newLevel = 5;
      newName = 'Diamond';
      cashback = 10;
    } else if (totalWagered >= 50_000) {
      newLevel = 4;
      newName = 'Platinum';
      cashback = 7;
    } else if (totalWagered >= 20_000) {
      newLevel = 3;
      newName = 'Gold';
      cashback = 5;
    } else if (totalWagered >= 5_000) {
      newLevel = 2;
      newName = 'Silver';
      cashback = 3;
    }

    user.vip.level = newLevel;
    user.vip.name = newName;
    user.vip.cashbackPercentage = cashback;

    user.vip.personalManager =
      newLevel >= 6;

    user.vip.higherLimits =
      newLevel >= 4;

    user.vip.exclusivePromotions =
      newLevel >= 3;

    user.vip.fasterWithdrawals =
      newLevel >= 5;
  };

// ============================================
// CAN PLACE BET
// ============================================

userSchema.methods.canPlaceBet =
  function(
    amount: number
  ): boolean {
    const user = this as IUser;

    if (
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      return false;
    }

    if (
      !user.isActive ||
      user.isBlocked ||
      user.isSuspended
    ) {
      return false;
    }

    const now = new Date();

    if (
      user.responsibleGambling?.selfExcluded &&
      user.responsibleGambling.selfExclusionEndDate &&
      user.responsibleGambling.selfExclusionEndDate > now
    ) {
      return false;
    }

    if (
      user.responsibleGambling?.coolingOffPeriodEnd &&
      user.responsibleGambling.coolingOffPeriodEnd > now
    ) {
      return false;
    }

    const availableBalance =
      user.wallet?.balance || 0;

    if (
      availableBalance < amount
    ) {
      return false;
    }

    return true;
  };

// ============================================
// DEPOSIT LIMIT
// ============================================

userSchema.methods.getDepositLimit =
  function(): number {
    const user = this as IUser;

    let limit =
      user.responsibleGambling
        ?.depositLimit || 0;

    if (
      user.vip?.higherLimits
    ) {
      limit *= 2;
    }

    if (
      (user.vip?.level || 0) >= 7
    ) {
      limit *= 5;
    }

    return limit;
  };

// ============================================
// SAFE JSON
// ============================================

userSchema.methods.toJSON =
  function(): any {
    const user = this as IUser;

    const obj =
      user.toObject();

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
// STATIC: FIND BY EMAIL OR USERNAME
// ============================================

userSchema.statics.findByEmailOrUsername =
  function(
    identifier: string
  ): Promise<IUser | null> {
    const normalized =
      String(identifier)
        .trim()
        .toLowerCase();

    return this.findOne({
      $or: [
        {
          email: normalized
        },
        {
          username: normalized
        }
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
      Math.min(
        Math.max(
          Number(limit) || 100,
          1
        ),
        500
      );

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
// MODEL CREATION
// ============================================

/*
 * IMPORTANT:
 *
 * In development, ts-node/hot reload can execute
 * this file more than once.
 *
 * Reusing mongoose.models.User prevents:
 *
 * OverwriteModelError:
 * Cannot overwrite `User` model once compiled.
 */

const existingModel =
  mongoose.models.User as
    | IUserModel
    | undefined;

const User: IUserModel =
  existingModel ??
  mongoose.model<IUser, IUserModel>(
    'User',
    userSchema
  );

export { User };

export default User;