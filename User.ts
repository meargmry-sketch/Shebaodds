import mongoose, {
  CallbackWithoutResultAndOptionalError,
  HydratedDocument,
  Model,
  Schema,
} from "mongoose";
import bcrypt from "bcryptjs";
import crypto from "crypto";

export type UserRole =
  | "user"
  | "admin"
  | "superadmin"
  | "agent"
  | "manager";

export type UserStatus = "active" | "inactive" | "suspended" | "pending";

export type Platform = "web" | "android" | "ios" | "mobile";

export interface IUser {
  username: string;
  email: string;
  phone?: string;

  password: string;
  role: UserRole;
  status: UserStatus;

  firstName?: string;
  lastName?: string;
  avatar?: string;

  isEmailVerified: boolean;
  isPhoneVerified: boolean;

  twoFactorEnabled: boolean;
  twoFactorSecret?: string;
  backupCodes: string[];

  loginAttempts: number;
  lockUntil?: Date;

  lastLoginAt?: Date;
  lastLoginIp?: string;

  refreshTokens: Array<{
    tokenHash: string;
    deviceId?: string;
    platform?: Platform;
    ip?: string;
    userAgent?: string;
    createdAt: Date;
    expiresAt: Date;
    revokedAt?: Date;
  }>;

  passwordChangedAt?: Date;
  passwordHistory: string[];

  passwordResetTokenHash?: string;
  passwordResetExpires?: Date;

  emailVerificationTokenHash?: string;
  emailVerificationExpires?: Date;

  referralCode?: string;
  referredBy?: mongoose.Types.ObjectId;
  referralBonusPaid: boolean;

  balance: number;
  bonusBalance: number;

  createdAt: Date;
  updatedAt: Date;
}

export interface IUserMethods {
  comparePassword(candidatePassword: string): Promise<boolean>;

  setPassword(newPassword: string): Promise<void>;

  generatePasswordResetToken(): string;

  generateEmailVerificationToken(): string;

  revokeAllSessions(): Promise<void>;

  revokeSession(tokenHash: string): Promise<void>;

  addRefreshToken(
    tokenHash: string,
    options?: {
      deviceId?: string;
      platform?: Platform;
      ip?: string;
      userAgent?: string;
      expiresAt?: Date;
    }
  ): Promise<void>;

  hasActiveSession(tokenHash: string): boolean;

  generateReferralCode(): string;

  generateBackupCodes(count?: number): string[];
}

export type UserDocument = HydratedDocument<IUser, IUserMethods>;

type UserModel = Model<IUser, {}, IUserMethods>;

const refreshTokenSchema = new Schema(
  {
    tokenHash: {
      type: String,
      required: true,
      index: true,
    },

    deviceId: {
      type: String,
      trim: true,
    },

    platform: {
      type: String,
      enum: ["web", "android", "ios", "mobile"],
    },

    ip: {
      type: String,
      trim: true,
    },

    userAgent: {
      type: String,
      trim: true,
      maxlength: 1000,
    },

    createdAt: {
      type: Date,
      default: Date.now,
    },

    expiresAt: {
      type: Date,
      required: true,
      index: true,
    },

    revokedAt: {
      type: Date,
    },
  },
  {
    _id: true,
  }
);

const userSchema = new Schema<IUser, UserModel, IUserMethods>(
  {
    username: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      minlength: 3,
      maxlength: 50,
      lowercase: true,
      index: true,
    },

    email: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      lowercase: true,
      maxlength: 255,
      index: true,
    },

    phone: {
      type: String,
      trim: true,
      sparse: true,
      index: true,
    },

    password: {
      type: String,
      required: true,
      minlength: 8,
      select: false,
    },

    role: {
      type: String,
      enum: ["user", "admin", "superadmin", "agent", "manager"],
      default: "user",
      index: true,
    },

    status: {
      type: String,
      enum: ["active", "inactive", "suspended", "pending"],
      default: "active",
      index: true,
    },

    firstName: {
      type: String,
      trim: true,
      maxlength: 100,
    },

    lastName: {
      type: String,
      trim: true,
      maxlength: 100,
    },

    avatar: {
      type: String,
      trim: true,
    },

    isEmailVerified: {
      type: Boolean,
      default: false,
      index: true,
    },

    isPhoneVerified: {
      type: Boolean,
      default: false,
    },

    twoFactorEnabled: {
      type: Boolean,
      default: false,
    },

    twoFactorSecret: {
      type: String,
      select: false,
    },

    backupCodes: {
      type: [String],
      default: [],
      select: false,
    },

    loginAttempts: {
      type: Number,
      default: 0,
      min: 0,
    },

    lockUntil: {
      type: Date,
    },

    lastLoginAt: {
      type: Date,
    },

    lastLoginIp: {
      type: String,
    },

    refreshTokens: {
      type: [refreshTokenSchema],
      default: [],
      select: false,
    },

    passwordChangedAt: {
      type: Date,
    },

    passwordHistory: {
      type: [String],
      default: [],
      select: false,
    },

    passwordResetTokenHash: {
      type: String,
      select: false,
      index: true,
    },

    passwordResetExpires: {
      type: Date,
      select: false,
    },

    emailVerificationTokenHash: {
      type: String,
      select: false,
      index: true,
    },

    emailVerificationExpires: {
      type: Date,
      select: false,
    },

    referralCode: {
      type: String,
      unique: true,
      sparse: true,
      uppercase: true,
      trim: true,
      index: true,
    },

    referredBy: {
      type: Schema.Types.ObjectId,
      ref: "User",
    },

    referralBonusPaid: {
      type: Boolean,
      default: false,
    },

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
  },
  {
    timestamps: true,
    versionKey: false,
    collection: "users",
  }
);

/*
|--------------------------------------------------------------------------
| Password hashing
|--------------------------------------------------------------------------
*/

userSchema.pre(
  "save",
  async function (
    next: CallbackWithoutResultAndOptionalError
  ): Promise<void> {
    if (!this.isModified("password")) {
      next();
      return;
    }

    const currentPassword = this.password;

    if (!currentPassword) {
      next(new Error("Password is required"));
      return;
    }

    const hashedPassword = await bcrypt.hash(currentPassword, 12);

    if (!this.isNew && this.passwordHistory) {
      const previousPassword = this.password;

      if (previousPassword && previousPassword !== hashedPassword) {
        this.passwordHistory = [
          previousPassword,
          ...this.passwordHistory,
        ].slice(0, 5);
      }
    }

    this.password = hashedPassword;
    this.passwordChangedAt = new Date();

    next();
  }
);

/*
|--------------------------------------------------------------------------
| Compare password
|--------------------------------------------------------------------------
*/

userSchema.methods.comparePassword = async function (
  candidatePassword: string
): Promise<boolean> {
  if (!candidatePassword || !this.password) {
    return false;
  }

  return bcrypt.compare(candidatePassword, this.password);
};

/*
|--------------------------------------------------------------------------
| Set password
|--------------------------------------------------------------------------
*/

userSchema.methods.setPassword = async function (
  newPassword: string
): Promise<void> {
  if (!newPassword || newPassword.length < 8) {
    throw new Error("Password must be at least 8 characters long");
  }

  const isSame = await bcrypt.compare(newPassword, this.password);

  if (isSame) {
    throw new Error("New password must be different from the old password");
  }

  const history = this.passwordHistory || [];

  for (const oldPassword of history) {
    const reused = await bcrypt.compare(newPassword, oldPassword);

    if (reused) {
      throw new Error(
        "You cannot reuse one of your previous passwords"
      );
    }
  }

  this.passwordHistory = [
    this.password,
    ...history,
  ].slice(0, 5);

  this.password = newPassword;
  this.passwordChangedAt = new Date();

  await this.save();
};

/*
|--------------------------------------------------------------------------
| Password reset token
|--------------------------------------------------------------------------
*/

userSchema.methods.generatePasswordResetToken = function (): string {
  const token = crypto.randomBytes(32).toString("hex");

  this.passwordResetTokenHash = crypto
    .createHash("sha256")
    .update(token)
    .digest("hex");

  this.passwordResetExpires = new Date(
    Date.now() + 15 * 60 * 1000
  );

  return token;
};

/*
|--------------------------------------------------------------------------
| Email verification token
|--------------------------------------------------------------------------
*/

userSchema.methods.generateEmailVerificationToken = function (): string {
  const token = crypto.randomBytes(32).toString("hex");

  this.emailVerificationTokenHash = crypto
    .createHash("sha256")
    .update(token)
    .digest("hex");

  this.emailVerificationExpires = new Date(
    Date.now() + 24 * 60 * 60 * 1000
  );

  return token;
};

/*
|--------------------------------------------------------------------------
| Refresh sessions
|--------------------------------------------------------------------------
*/

userSchema.methods.addRefreshToken = async function (
  tokenHash: string,
  options = {}
): Promise<void> {
  const expiresAt =
    options.expiresAt ||
    new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

  this.refreshTokens = this.refreshTokens || [];

  this.refreshTokens = this.refreshTokens.filter(
    (session) =>
      !session.revokedAt &&
      session.expiresAt.getTime() > Date.now()
  );

  this.refreshTokens.push({
    tokenHash,
    deviceId: options.deviceId,
    platform: options.platform,
    ip: options.ip,
    userAgent: options.userAgent,
    createdAt: new Date(),
    expiresAt,
  });

  /*
   * Prevent unbounded session growth.
   */
  if (this.refreshTokens.length > 10) {
    this.refreshTokens = this.refreshTokens.slice(-10);
  }

  await this.save();
};

userSchema.methods.hasActiveSession = function (
  tokenHash: string
): boolean {
  if (!this.refreshTokens) {
    return false;
  }

  return this.refreshTokens.some(
    (session) =>
      session.tokenHash === tokenHash &&
      !session.revokedAt &&
      session.expiresAt.getTime() > Date.now()
  );
};

userSchema.methods.revokeSession = async function (
  tokenHash: string
): Promise<void> {
  if (!this.refreshTokens) {
    return;
  }

  const session = this.refreshTokens.find(
    (item) =>
      item.tokenHash === tokenHash &&
      !item.revokedAt
  );

  if (session) {
    session.revokedAt = new Date();
    await this.save();
  }
};

userSchema.methods.revokeAllSessions = async function (): Promise<void> {
  if (!this.refreshTokens) {
    return;
  }

  const now = new Date();

  for (const session of this.refreshTokens) {
    if (!session.revokedAt) {
      session.revokedAt = now;
    }
  }

  await this.save();
};

/*
|--------------------------------------------------------------------------
| Referral code
|--------------------------------------------------------------------------
*/

userSchema.methods.generateReferralCode = function (): string {
  const prefix = this.username
    .replace(/[^a-zA-Z0-9]/g, "")
    .slice(0, 5)
    .toUpperCase();

  const random = crypto
    .randomBytes(4)
    .toString("hex")
    .toUpperCase();

  const code = `${prefix || "USER"}${random}`;

  this.referralCode = code;

  return code;
};

/*
|--------------------------------------------------------------------------
| 2FA backup codes
|--------------------------------------------------------------------------
*/

userSchema.methods.generateBackupCodes = function (
  count = 10
): string[] {
  const codes: string[] = [];

  for (let i = 0; i < count; i++) {
    const code = crypto
      .randomBytes(5)
      .toString("hex")
      .toUpperCase();

    codes.push(code);
  }

  this.backupCodes = codes.map((code) =>
    crypto
      .createHash("sha256")
      .update(code)
      .digest("hex")
  );

  return codes;
};

/*
|--------------------------------------------------------------------------
| Export
|--------------------------------------------------------------------------
*/

const User =
  (mongoose.models.User as UserModel | undefined) ||
  mongoose.model<IUser, UserModel>("User", userSchema);

export default User;