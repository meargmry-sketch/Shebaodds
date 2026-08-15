// ============================================
// SHEBAODDS - SERVER BOOTSTRAP
// Development/Test Friendly Configuration
// ============================================

import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

const REQUIRED_ENV_VARIABLES = [
  'MONGODB_URI',
  'TAX_RATE',
  'WELCOME_BONUS',
  'TELE_BIRR_API_KEY',
  'TELE_BIRR_SECRET',
  'CBE_API_KEY',
  'CHAPA_API_KEY',
  'SPORTS_DATA_API_KEY',
  'BIOMETRIC_ENCRYPTION_KEY',
];

function verifyEnvironmentMappings(): void {
  console.log('🔄 [BOOTSTRAP] Checking environment variables...');

  // Development fallbacks
  if (!process.env.WELCOME_BONUS && process.env.WELCOME_BONUS_AMOUNT) {
    process.env.WELCOME_BONUS = process.env.WELCOME_BONUS_AMOUNT;
  }

  if (
    !process.env.BIOMETRIC_ENCRYPTION_KEY &&
    process.env.ENCRYPTION_KEY
  ) {
    process.env.BIOMETRIC_ENCRYPTION_KEY =
      process.env.ENCRYPTION_KEY;
  }

  const missingVars: string[] = [];

  for (const variable of REQUIRED_ENV_VARIABLES) {
    const value = process.env[variable];

    if (
      !value ||
      value.includes('change_this') ||
      value.includes('your_')
    ) {
      missingVars.push(variable);
    }
  }

  if (missingVars.length > 0) {
    console.warn('');
    console.warn(
      '⚠️ [BOOTSTRAP] The following environment variables are not configured:'
    );

    missingVars.forEach((variable) => {
      console.warn(`   - ${variable}`);
    });

    if (process.env.NODE_ENV === 'production') {
      console.error('');
      console.error(
        '❌ Production cannot start without required environment variables.'
      );

      process.exit(1);
    }

    console.warn('');
    console.warn(
      '⚠️ Development mode: continuing with test configuration.'
    );
  } else {
    console.log(
      '✅ [BOOTSTRAP] Environment variables configured.'
    );
  }
}

async function connectDatabase(): Promise<void> {
  const mongoUri = process.env.MONGODB_URI;

  console.log('🔄 [BOOTSTRAP] Connecting to MongoDB...');

  if (!mongoUri) {
    if (process.env.NODE_ENV === 'production') {
      console.error('❌ MONGODB_URI is missing.');
      process.exit(1);
    }

    console.warn(
      '⚠️ MONGODB_URI is missing. Database connection skipped in development.'
    );

    return;
  }

  try {
    await mongoose.connect(mongoUri);

    console.log(
      '✅ [BOOTSTRAP] MongoDB connection established.'
    );

    console.log(
      `📦 Database: ${mongoose.connection.name}`
    );

  } catch (error: any) {
    console.error(
      '❌ [BOOTSTRAP] MongoDB connection failed:',
      error?.message || error
    );

    if (process.env.NODE_ENV === 'production') {
      process.exit(1);
    }

    console.warn(
      '⚠️ Development mode: continuing without MongoDB.'
    );
  }
}

export async function bootstrapAppEngine(): Promise<void> {
  console.log('');
  console.log('============================================');
  console.log('🦁 SHEBAODDS BOOTSTRAP');
  console.log('============================================');

  verifyEnvironmentMappings();

  await connectDatabase();

  console.log('============================================');
  console.log('✅ SHEBAODDS BOOTSTRAP COMPLETE');
  console.log('============================================');
  console.log('');
}

if (require.main === module) {
  bootstrapAppEngine()
    .then(() => {
      console.log('🚀 Bootstrap finished successfully.');
    })
    .catch((error) => {
      console.error(
        '💥 Bootstrap failed:',
        error
      );

      process.exit(1);
    });
}