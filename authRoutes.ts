import { Router, Request, Response, NextFunction } from "express";
import jwt, { JwtPayload } from "jsonwebtoken";
import crypto from "crypto";
import User, {
  IUser,
  Platform,
  UserDocument,
} from "./User";

const router = Router();

/*
|--------------------------------------------------------------------------
| Environment
|--------------------------------------------------------------------------
*/

const JWT_ACCESS_SECRET = process.env.JWT_ACCESS_SECRET;
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET;

if (!JWT_ACCESS_SECRET) {
  throw new Error("JWT_ACCESS_SECRET is not configured");
}

if (!JWT_REFRESH_SECRET) {
  throw new Error("JWT_REFRESH_SECRET is not configured");
}

const ACCESS_TOKEN_EXPIRES_IN =
  process.env.JWT_ACCESS_EXPIRES_IN || "15m";

const REFRESH_TOKEN_EXPIRES_DAYS = Number(
  process.env.JWT_REFRESH_EXPIRES_DAYS || 30
);

const MAX_LOGIN_ATTEMPTS = 5;

const LOGIN_LOCK_MINUTES = 15;

/*
|--------------------------------------------------------------------------
| Types
|--------------------------------------------------------------------------
*/

interface AuthRequest extends Request {
  user?: UserDocument;
}

interface AccessTokenPayload extends JwtPayload {
  userId: string;
  role: string;
  type: "access";
}

interface RefreshTokenPayload extends JwtPayload {
  userId: string;
  type: "refresh";
}

/*
|--------------------------------------------------------------------------
| Helpers
|--------------------------------------------------------------------------
*/

function hashToken(token: string): string {
  return crypto
    .createHash("sha256")
    .update(token)
    .digest("hex");
}

function createAccessToken(user: UserDocument): string {
  return jwt.sign(
    {
      userId: user._id.toString(),
      role: user.role,
      type: "access",
    },
    JWT_ACCESS_SECRET as string,
    {
      expiresIn: ACCESS_TOKEN_EXPIRES_IN,
      issuer: "shebaodds",
      audience: "shebaodds-api",
    }
  );
}

function createRefreshToken(user: UserDocument): string {
  return jwt.sign(
    {
      userId: user._id.toString(),
      type: "refresh",
    },
    JWT_REFRESH_SECRET as string,
    {
      expiresIn: `${REFRESH_TOKEN_EXPIRES_DAYS}d`,
      issuer: "shebaodds",
      audience: "shebaodds-api",
    }
  );
}

function getRefreshExpiry(): Date {
  return new Date(
    Date.now() +
      REFRESH_TOKEN_EXPIRES_DAYS *
        24 *
        60 *
        60 *
        1000
  );
}

function sanitizeUser(user: UserDocument) {
  return {
    id: user._id.toString(),
    username: user.username,
    email: user.email,
    phone: user.phone,
    firstName: user.firstName,
    lastName: user.lastName,
    avatar: user.avatar,
    role: user.role,
    status: user.status,
    isEmailVerified: user.isEmailVerified,
    isPhoneVerified: user.isPhoneVerified,
    twoFactorEnabled: user.twoFactorEnabled,
    referralCode: user.referralCode,
    balance: user.balance,
    bonusBalance: user.bonusBalance,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  };
}

function getPlatform(value: unknown): Platform | undefined {
  if (
    value === "web" ||
    value === "android" ||
    value === "ios" ||
    value === "mobile"
  ) {
    return value;
  }

  return undefined;
}

function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

function normalizeUsername(username: string): string {
  return username.trim().toLowerCase();
}

function isStrongPassword(password: string): boolean {
  return (
    password.length >= 8 &&
    /[A-Z]/.test(password) &&
    /[a-z]/.test(password) &&
    /\d/.test(password)
  );
}

/*
|--------------------------------------------------------------------------
| Register
|--------------------------------------------------------------------------
*/

router.post(
  "/register",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const {
        username,
        email,
        password,
        phone,
        firstName,
        lastName,
        referralCode,
      } = req.body;

      if (!username || !email || !password) {
        return res.status(400).json({
          success: false,
          message:
            "Username, email and password are required",
        });
      }

      const normalizedUsername =
        normalizeUsername(username);

      const normalizedEmail =
        normalizeEmail(email);

      if (!isStrongPassword(password)) {
        return res.status(400).json({
          success: false,
          message:
            "Password must be at least 8 characters and contain uppercase, lowercase and a number",
        });
      }

      const existingUser = await User.findOne({
        $or: [
          { username: normalizedUsername },
          { email: normalizedEmail },
        ],
      }).lean();

      if (existingUser) {
        return res.status(409).json({
          success: false,
          message:
            existingUser.username === normalizedUsername
              ? "Username already exists"
              : "Email already exists",
        });
      }

      let referredBy: IUser["referredBy"];

      if (referralCode) {
        const referrer = await User.findOne({
          referralCode: String(referralCode)
            .trim()
            .toUpperCase(),
        }).select("_id");

        if (referrer) {
          referredBy = referrer._id;
        }
      }

      const user = new User({
        username: normalizedUsername,
        email: normalizedEmail,
        password,
        phone,
        firstName,
        lastName,
        referredBy,
        status: "active",
      });

      user.generateReferralCode();

      const verificationToken =
        user.generateEmailVerificationToken();

      await user.save();

      /*
       * Send verification email from your email service here.
       *
       * Example:
       *
       * await sendVerificationEmail(
       *   user.email,
       *   verificationToken
       * );
       */

      return res.status(201).json({
        success: true,
        message:
          "Registration successful. Please verify your email.",
        user: sanitizeUser(user),
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Login
|--------------------------------------------------------------------------
*/

router.post(
  "/login",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const {
        email,
        username,
        password,
        deviceId,
        platform,
      } = req.body;

      const loginIdentifier =
        email || username;

      if (!loginIdentifier || !password) {
        return res.status(400).json({
          success: false,
          message:
            "Email/username and password are required",
        });
      }

      const identifier =
        String(loginIdentifier).trim().toLowerCase();

      const user = await User.findOne({
        $or: [
          { email: identifier },
          { username: identifier },
        ],
      })
        .select(
          "+password +refreshTokens +twoFactorSecret +backupCodes"
        )
        .exec();

      if (!user) {
        return res.status(401).json({
          success: false,
          message: "Invalid credentials",
        });
      }

      if (
        user.lockUntil &&
        user.lockUntil.getTime() > Date.now()
      ) {
        return res.status(423).json({
          success: false,
          message:
            "Account temporarily locked. Please try again later.",
        });
      }

      if (user.status === "suspended") {
        return res.status(403).json({
          success: false,
          message: "Account is suspended",
        });
      }

      if (user.status === "inactive") {
        return res.status(403).json({
          success: false,
          message: "Account is inactive",
        });
      }

      const passwordValid =
        await user.comparePassword(password);

      if (!passwordValid) {
        user.loginAttempts =
          (user.loginAttempts || 0) + 1;

        if (
          user.loginAttempts >=
          MAX_LOGIN_ATTEMPTS
        ) {
          user.lockUntil = new Date(
            Date.now() +
              LOGIN_LOCK_MINUTES *
                60 *
                1000
          );

          user.loginAttempts = 0;
        }

        await user.save();

        return res.status(401).json({
          success: false,
          message: "Invalid credentials",
        });
      }

      user.loginAttempts = 0;
      user.lockUntil = undefined;
      user.lastLoginAt = new Date();
      user.lastLoginIp = req.ip;

      /*
       * If 2FA is enabled, stop here and require OTP.
       */
      if (user.twoFactorEnabled) {
        await user.save();

        return res.status(200).json({
          success: true,
          requiresTwoFactor: true,
          userId: user._id.toString(),
          message:
            "Two-factor authentication required",
        });
      }

      const accessToken =
        createAccessToken(user);

      const refreshToken =
        createRefreshToken(user);

      const refreshTokenHash =
        hashToken(refreshToken);

      await user.addRefreshToken(
        refreshTokenHash,
        {
          deviceId:
            typeof deviceId === "string"
              ? deviceId
              : undefined,

          platform:
            getPlatform(platform),

          ip: req.ip,

          userAgent:
            req.get("user-agent"),

          expiresAt:
            getRefreshExpiry(),
        }
      );

      return res.status(200).json({
        success: true,
        message: "Login successful",
        accessToken,
        refreshToken,
        user: sanitizeUser(user),
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Verify email
|--------------------------------------------------------------------------
*/

router.get(
  "/verify-email",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const token =
        typeof req.query.token === "string"
          ? req.query.token
          : "";

      if (!token) {
        return res.status(400).json({
          success: false,
          message: "Verification token is required",
        });
      }

      const tokenHash = hashToken(token);

      const user = await User.findOne({
        emailVerificationTokenHash: tokenHash,
        emailVerificationExpires: {
          $gt: new Date(),
        },
      }).exec();

      if (!user) {
        return res.status(400).json({
          success: false,
          message:
            "Invalid or expired verification token",
        });
      }

      user.isEmailVerified = true;
      user.emailVerificationTokenHash = undefined;
      user.emailVerificationExpires = undefined;

      await user.save();

      return res.status(200).json({
        success: true,
        message: "Email verified successfully",
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Refresh token
|--------------------------------------------------------------------------
*/

router.post(
  "/refresh",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const { refreshToken } = req.body;

      if (!refreshToken) {
        return res.status(401).json({
          success: false,
          message: "Refresh token is required",
        });
      }

      let decoded: RefreshTokenPayload;

      try {
        decoded =
          jwt.verify(
            refreshToken,
            JWT_REFRESH_SECRET,
            {
              issuer: "shebaodds",
              audience: "shebaodds-api",
            }
          ) as RefreshTokenPayload;
      } catch {
        return res.status(401).json({
          success: false,
          message: "Invalid or expired refresh token",
        });
      }

      if (
        decoded.type !== "refresh" ||
        !decoded.userId
      ) {
        return res.status(401).json({
          success: false,
          message: "Invalid refresh token",
        });
      }

      const tokenHash =
        hashToken(refreshToken);

      const user = await User.findById(
        decoded.userId
      )
        .select("+refreshTokens")
        .exec();

      if (!user) {
        return res.status(401).json({
          success: false,
          message: "User not found",
        });
      }

      if (
        user.status !== "active"
      ) {
        return res.status(403).json({
          success: false,
          message: "Account is not active",
        });
      }

      if (
        !user.hasActiveSession(tokenHash)
      ) {
        return res.status(401).json({
          success: false,
          message:
            "Refresh session is invalid or revoked",
        });
      }

      /*
       * Rotate refresh token.
       */
      await user.revokeSession(tokenHash);

      const newAccessToken =
        createAccessToken(user);

      const newRefreshToken =
        createRefreshToken(user);

      await user.addRefreshToken(
        hashToken(newRefreshToken),
        {
          ip: req.ip,
          userAgent:
            req.get("user-agent"),
          expiresAt:
            getRefreshExpiry(),
        }
      );

      return res.status(200).json({
        success: true,
        accessToken: newAccessToken,
        refreshToken: newRefreshToken,
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Logout
|--------------------------------------------------------------------------
*/

router.post(
  "/logout",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const { refreshToken } = req.body;

      if (refreshToken) {
        try {
          const decoded =
            jwt.verify(
              refreshToken,
              JWT_REFRESH_SECRET
            ) as RefreshTokenPayload;

          if (decoded.userId) {
            const user = await User.findById(
              decoded.userId
            )
              .select("+refreshTokens")
              .exec();

            if (user) {
              await user.revokeSession(
                hashToken(refreshToken)
              );
            }
          }
        } catch {
          /*
           * Logout should remain idempotent.
           */
        }
      }

      return res.status(200).json({
        success: true,
        message: "Logged out successfully",
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Logout all devices
|--------------------------------------------------------------------------
*/

router.post(
  "/logout-all",
  authenticate,
  async (
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message: "Authentication required",
        });
      }

      await req.user.revokeAllSessions();

      return res.status(200).json({
        success: true,
        message:
          "Logged out from all devices",
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Forgot password
|--------------------------------------------------------------------------
*/

router.post(
  "/forgot-password",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const { email } = req.body;

      /*
       * Always return the same response to avoid
       * revealing whether an account exists.
       */
      const genericResponse = {
        success: true,
        message:
          "If the account exists, a password reset link has been sent.",
      };

      if (!email) {
        return res.status(200).json(
          genericResponse
        );
      }

      const user = await User.findOne({
        email: normalizeEmail(email),
      })
        .select(
          "+passwordResetTokenHash +passwordResetExpires"
        )
        .exec();

      if (!user) {
        return res.status(200).json(
          genericResponse
        );
      }

      const resetToken =
        user.generatePasswordResetToken();

      await user.save();

      /*
       * Send reset email here.
       *
       * await sendPasswordResetEmail(
       *   user.email,
       *   resetToken
       * );
       */

      return res.status(200).json(
        genericResponse
      );
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Reset password
|--------------------------------------------------------------------------
*/

router.post(
  "/reset-password",
  async (
    req: Request,
    res: Response,
    next: NextFunction
  ) => {
    try {
      const {
        token,
        password,
      } = req.body;

      if (!token || !password) {
        return res.status(400).json({
          success: false,
          message:
            "Reset token and password are required",
        });
      }

      if (!isStrongPassword(password)) {
        return res.status(400).json({
          success: false,
          message:
            "Password must be at least 8 characters and contain uppercase, lowercase and a number",
        });
      }

      const tokenHash =
        hashToken(token);

      const user = await User.findOne({
        passwordResetTokenHash: tokenHash,
        passwordResetExpires: {
          $gt: new Date(),
        },
      })
        .select(
          "+password +passwordHistory +refreshTokens"
        )
        .exec();

      if (!user) {
        return res.status(400).json({
          success: false,
          message:
            "Invalid or expired reset token",
        });
      }

      await user.setPassword(password);

      user.passwordResetTokenHash = undefined;
      user.passwordResetExpires = undefined;

      await user.revokeAllSessions();

      return res.status(200).json({
        success: true,
        message:
          "Password reset successfully",
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Current user
|--------------------------------------------------------------------------
*/

router.get(
  "/me",
  authenticate,
  async (
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ) => {
    try {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message: "Authentication required",
        });
      }

      return res.status(200).json({
        success: true,
        user: sanitizeUser(req.user),
      });
    } catch (error) {
      next(error);
    }
  }
);

/*
|--------------------------------------------------------------------------
| Authentication middleware
|--------------------------------------------------------------------------
*/

export async function authenticate(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const authorization =
      req.headers.authorization;

    if (!authorization) {
      res.status(401).json({
        success: false,
        message: "Authentication required",
      });
      return;
    }

    const [scheme, token] =
      authorization.split(" ");

    if (
      scheme !== "Bearer" ||
      !token
    ) {
      res.status(401).json({
        success: false,
        message:
          "Invalid authorization header",
      });
      return;
    }

    let decoded: AccessTokenPayload;

    try {
      decoded =
        jwt.verify(
          token,
          JWT_ACCESS_SECRET,
          {
            issuer: "shebaodds",
            audience: "shebaodds-api",
          }
        ) as AccessTokenPayload;
    } catch {
      res.status(401).json({
        success: false,
        message:
          "Invalid or expired access token",
      });
      return;
    }

    if (
      decoded.type !== "access" ||
      !decoded.userId
    ) {
      res.status(401).json({
        success: false,
        message: "Invalid access token",
      });
      return;
    }

    const user = await User.findById(
      decoded.userId
    ).exec();

    if (!user) {
      res.status(401).json({
        success: false,
        message: "User not found",
      });
      return;
    }

    if (user.status !== "active") {
      res.status(403).json({
        success: false,
        message: "Account is not active",
      });
      return;
    }

    req.user = user;

    next();
  } catch (error) {
    next(error);
  }
}

/*
|--------------------------------------------------------------------------
| Role middleware
|--------------------------------------------------------------------------
*/

export function requireRole(
  ...roles: string[]
) {
  return (
    req: AuthRequest,
    res: Response,
    next: NextFunction
  ): void => {
    if (!req.user) {
      res.status(401).json({
        success: false,
        message: "Authentication required",
      });
      return;
    }

    if (!roles.includes(req.user.role)) {
      res.status(403).json({
        success: false,
        message: "Insufficient permissions",
      });
      return;
    }

    next();
  };
}

export default router;