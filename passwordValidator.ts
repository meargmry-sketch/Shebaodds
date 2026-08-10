// ============================================
// SHEBAODDS - STRONG PASSWORD VALIDATION
// Enterprise-grade password requirements
// ============================================

import { Request, Response, NextFunction } from 'express';
import bcrypt from 'bcryptjs';

// Password strength levels
export const PASSWORD_STRENGTH = {
  WEAK: 'weak',
  FAIR: 'fair',
  GOOD: 'good',
  STRONG: 'strong',
  VERY_STRONG: 'very_strong'
};

// Password validation rules
export const PASSWORD_RULES = {
  minLength: 8,
  maxLength: 128,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,
  preventCommonPasswords: true,
  preventPersonalInfo: true,
  preventSequentialChars: true,
  preventRepeatedChars: true,
  maxHistory: 5 // Prevent password reuse
};

// Common passwords blacklist
export const COMMON_PASSWORDS = [
  'password', '12345678', 'qwerty123', 'admin123', 'letmein123',
  'welcome123', 'password123', 'abc123456', 'shebaodds', 'ethiopia123',
  '123456789', '11111111', '00000000', 'passw0rd', 'admin@123'
];

// Special characters allowed
export const SPECIAL_CHARS = '!@#$%^&*()_+-=[]{}|;:,.<>?';

export interface UserInfo {
  username?: string;
  email?: string;
  fullName?: string;
  phone?: string;
}

export interface ValidationResult {
  isValid: boolean;
  strength: string;
  score: number;
  errors: string[];
  warnings: string[];
  hasUppercase: boolean;
  hasLowercase: boolean;
  hasNumbers: boolean;
  hasSpecial: boolean;
  isLongEnough: boolean;
}

// Validate password strength
export function validatePasswordStrength(password: string, userInfo: UserInfo = {}): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  let strength = PASSWORD_STRENGTH.WEAK;
  let score = 0;

  // Check minimum length
  if (password.length < PASSWORD_RULES.minLength) {
    errors.push(`Password must be at least ${PASSWORD_RULES.minLength} characters`);
  } else if (password.length >= 12) {
    score += 2;
  } else if (password.length >= 10) {
    score += 1;
  } else if (password.length >= 8) {
    score += 0.5;
  }

  // Check maximum length
  if (password.length > PASSWORD_RULES.maxLength) {
    errors.push(`Password cannot exceed ${PASSWORD_RULES.maxLength} characters`);
  }

  // Check for uppercase letters
  const hasUppercase = /[A-Z]/.test(password);
  if (PASSWORD_RULES.requireUppercase && !hasUppercase) {
    errors.push('Password must contain at least one uppercase letter');
  } else if (hasUppercase) {
    score += 1;
  }

  // Check for lowercase letters
  const hasLowercase = /[a-z]/.test(password);
  if (PASSWORD_RULES.requireLowercase && !hasLowercase) {
    errors.push('Password must contain at least one lowercase letter');
  } else if (hasLowercase) {
    score += 1;
  }

  // Check for numbers
  const hasNumbers = /[0-9]/.test(password);
  if (PASSWORD_RULES.requireNumbers && !hasNumbers) {
    errors.push('Password must contain at least one number');
  } else if (hasNumbers) {
    score += 1;
  }

  // Check for special characters
  const escapedSpecialChars = SPECIAL_CHARS.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
  const hasSpecial = new RegExp(`[${escapedSpecialChars}]`).test(password);
  if (PASSWORD_RULES.requireSpecialChars && !hasSpecial) {
    errors.push(`Password must contain at least one special character (${SPECIAL_CHARS})`);
  } else if (hasSpecial) {
    score += 1.5;
  }

  // Check for common passwords
  if (PASSWORD_RULES.preventCommonPasswords) {
    const lowerPassword = password.toLowerCase();
    if (COMMON_PASSWORDS.includes(lowerPassword)) {
      errors.push('This password is too common. Please choose a more secure password');
    }
  }

  // Check for sequential characters (abc, 123, etc.)
  if (PASSWORD_RULES.preventSequentialChars) {
    const sequentialPatterns = [
      'abcdefghijklmnopqrstuvwxyz', 'qwertyuiop', 'asdfghjkl', 'zxcvbnm',
      '1234567890', '0123456789'
    ];
    
    for (const pattern of sequentialPatterns) {
      for (let i = 0; i < password.length - 3; i++) {
        const substr = password.substring(i, i + 4).toLowerCase();
        if (pattern.includes(substr)) {
          warnings.push('Password contains sequential characters which makes it easier to guess');
          break;
        }
      }
    }
  }

  // Check for repeated characters (aaaa, 1111, etc.)
  if (PASSWORD_RULES.preventRepeatedChars) {
    if (/(.)\1{3,}/.test(password)) {
      warnings.push('Password contains repeated characters which makes it easier to guess');
    }
  }

  // Check for personal information
  if (PASSWORD_RULES.preventPersonalInfo && userInfo) {
    const personalInfo = [
      userInfo.username,
      userInfo.email?.split('@')[0],
      userInfo.fullName,
      userInfo.phone
    ].filter(Boolean) as string[];
    
    for (const info of personalInfo) {
      if (info && password.toLowerCase().includes(info.toLowerCase())) {
        errors.push('Password should not contain personal information like username, email, or name');
        break;
      }
    }
  }

  // Determine strength
  if (score >= 6) {
    strength = PASSWORD_STRENGTH.VERY_STRONG;
  } else if (score >= 4.5) {
    strength = PASSWORD_STRENGTH.STRONG;
  } else if (score >= 3) {
    strength = PASSWORD_STRENGTH.GOOD;
  } else if (score >= 1.5) {
    strength = PASSWORD_STRENGTH.FAIR;
  } else {
    strength = PASSWORD_STRENGTH.WEAK;
  }

  return {
    isValid: errors.length === 0,
    strength,
    score,
    errors,
    warnings,
    hasUppercase,
    hasLowercase,
    hasNumbers,
    hasSpecial,
    isLongEnough: password.length >= PASSWORD_RULES.minLength
  };
}

// Password history tracking (prevent reuse)
export class PasswordHistory {
  userId: string;
  passwordHistory: string[];

  constructor(userId: string, passwordHistory: string[] = []) {
    this.userId = userId;
    this.passwordHistory = passwordHistory;
  }

  async isPasswordReused(newPassword: string): Promise<boolean> {
    if (!PASSWORD_RULES.maxHistory || this.passwordHistory.length === 0) {
      return false;
    }

    for (const oldHash of this.passwordHistory) {
      if (await bcrypt.compare(newPassword, oldHash)) {
        return true;
      }
    }
    return false;
  }

  async addToHistory(newPasswordHash: string): Promise<string[]> {
    this.passwordHistory.unshift(newPasswordHash);
    
    // Keep only last N passwords
    if (this.passwordHistory.length > PASSWORD_RULES.maxHistory) {
      this.passwordHistory = this.passwordHistory.slice(0, PASSWORD_RULES.maxHistory);
    }
    
    return this.passwordHistory;
  }
}

// Generate password strength meter data
export function getPasswordStrengthMeter(strength: string) {
  const meters: Record<string, any> = {
    [PASSWORD_STRENGTH.WEAK]: {
      label: 'Weak',
      labelAm: 'ደካማ',
      color: '#F44336',
      percentage: 20,
      suggestions: [
        'Use at least 8 characters',
        'Add uppercase letters',
        'Add numbers',
        'Add special characters'
      ]
    },
    [PASSWORD_STRENGTH.FAIR]: {
      label: 'Fair',
      labelAm: 'መጠነኛ',
      color: '#FF9800',
      percentage: 40,
      suggestions: [
        'Make it longer',
        'Add more variety of characters'
      ]
    },
    [PASSWORD_STRENGTH.GOOD]: {
      label: 'Good',
      labelAm: 'ጥሩ',
      color: '#2196F3',
      percentage: 60,
      suggestions: [
        'Add more special characters',
        'Make it longer for better security'
      ]
    },
    [PASSWORD_STRENGTH.STRONG]: {
      label: 'Strong',
      labelAm: 'ጠንካራ',
      color: '#4CAF50',
      percentage: 80,
      suggestions: []
    },
    [PASSWORD_STRENGTH.VERY_STRONG]: {
      label: 'Very Strong',
      labelAm: 'በጣም ጠንካራ',
      color: '#00E676',
      percentage: 100,
      suggestions: []
    }
  };
  
  return meters[strength] || meters[PASSWORD_STRENGTH.WEAK];
}

// Express middleware for password validation
export function validatePassword(req: Request, res: Response, next: NextFunction) {
  const { password, ...userInfo } = req.body;
  
  if (!password) {
    return res.status(400).json({
      success: false,
      message: 'Password is required'
    });
  }
  
  const validation = validatePasswordStrength(password, userInfo);
  
  if (!validation.isValid) {
    return res.status(400).json({
      success: false,
      message: 'Password does not meet security requirements',
      errors: validation.errors,
      strength: validation.strength
    });
  }
  
  // Add warnings/strength to request
  (req as any).passwordWarnings = validation.warnings;
  (req as any).passwordStrength = validation.strength;
  next();
}
