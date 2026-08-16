import mongoose from 'mongoose';
import * as dotenv from 'dotenv';

dotenv.config();

/*
|--------------------------------------------------------------------------
| REQUIRED ENVIRONMENT VARIABLES
|--------------------------------------------------------------------------
*/

const REQUIRED_ENV_VARIABLES = [
  'MONGODB_URI',
  'TAX_RATE',
  'WELCOME_BONUS',
  'TELE_BIRR_API_KEY',
  'TELE_BIRR_SECRET',
  'CBE_API_KEY',
  'CHAPA_API_KEY',
  'SPORTS_DATA_API_KEY',
  'BIOMETRIC_ENCRYPTION_KEY'
];

/*
|--------------------------------------------------------------------------
| ENVIRONMENT VALIDATION
|--------------------------------------------------------------------------
*/

function verifyEnvironmentMappings(): void {
  console.log('');
  console.log('🔄 [BOOTSTRAP] Checking environment configuration...');
  console.log('----------------------------------------------------');

  /*
   * Fallback mappings
   */

  if (
    !process.env.WELCOME_BONUS &&
    process.env.WELCOME_BONUS_AMOUNT
  ) {
    process.env.WELCOME_BONUS =
      process.env.WELCOME_BONUS_AMOUNT;
  }

  if (
    !process.env.BIOMETRIC_ENCRYPTION_KEY &&
    process.env.ENCRYPTION_KEY
  ) {
    process.env.BIOMETRIC_ENCRYPTION_KEY =
      process.env.ENCRYPTION_KEY;
  }

  /*
   * Redis is NOT required for initial startup.
   *
   * Your application can configure Redis separately when
   * you actually use Redis.
   */

  const missingVars: string[] = [];

  for (const variable of REQUIRED_ENV_VARIABLES) {
    const value = process.env[variable];

    if (
      !value ||
      value.trim() === '' ||
      value.includes('change_this') ||
      value.includes('your_') ||
      value.includes('YOUR_')
    ) {
      missingVars.push(variable);
    }
  }

  /*
   * Production
   */

  if (missingVars.length > 0) {
    console.error('');
    console.error(
      '❌ [BOOTSTRAP] Missing or unconfigured environment variables:'
    );

    for (const variable of missingVars) {
      console.error(`   ❌ ${variable}`);
    }

    console.error('');

    if (process.env.NODE_ENV === 'production') {
      console.error(
        '💥 Production environment validation failed.'
      );

      console.error(
        '👉 Add the missing variables in Render Environment Variables.'
      );

      throw new Error(
        `Missing environment variables: ${missingVars.join(', ')}`
      );
    }

    /*
     * Development mode
     */

    console.warn(
      '⚠️ Development mode: continuing despite missing variables.'
    );
  } else {
    console.log(
      '✅ All required environment variables are configured.'
    );
  }

  console.log('----------------------------------------------------');
}

/*
|--------------------------------------------------------------------------
| MONGODB CONNECTION
|--------------------------------------------------------------------------
*/

async function connectToMongoDB(): Promise<void> {
  const mongoUri = process.env.MONGODB_URI;

  if (!mongoUri) {
    throw new Error(
      'MONGODB_URI is not configured.'
    );
  }

  console.log(
    '🔄 [BOOTSTRAP] Connecting to MongoDB...'
  );

  try {
    await mongoose.connect(mongoUri, {
      serverSelectionTimeoutMS: 10000,
      connectTimeoutMS: 10000
    });

    console.log(
      '✅ [BOOTSTRAP] MongoDB connection established.'
    );

    console.log(
      `📦 Database: ${
        mongoose.connection.name || 'unknown'
      }`
    );

    /*
     * We intentionally do NOT require a replica set here.
     *
     * MongoDB Atlas normally supports transactions,
     * but this check should not prevent the HTTP server
     * from starting unnecessarily.
     */

    try {
      const adminDb =
        mongoose.connection.db?.admin();

      if (adminDb) {
        const status =
          await adminDb
            .command({
              hello: 1
            })
            .catch(() => null);

        if (status) {
          console.log(
            '✅ [BOOTSTRAP] MongoDB server responded successfully.'
          );

          if (status.setName) {
            console.log(
              `🔐 Replica Set: ${status.setName}`
            );
          }
        }
      }
    } catch (error) {
      console.warn(
        '⚠️ MongoDB server capability check skipped.'
      );
    }
  } catch (error: any) {
    console.error('');
    console.error(
      '❌ [BOOTSTRAP] MongoDB connection failed.'
    );

    console.error(
      error?.message || error
    );

    throw error;
  }
}

/*
|--------------------------------------------------------------------------
| BOOTSTRAP ENGINE
|--------------------------------------------------------------------------
*/

export async function bootstrapAppEngine(): Promise<void> {
  console.log('');
  console.log(
    '🚀 [BOOTSTRAP] Starting SHEBAODDS backend...'
  );

  console.log(
    '===================================================='
  );

  /*
   * Step 1
   */

  verifyEnvironmentMappings();

  /*
   * Step 2
   */

  await connectToMongoDB();

  /*
   * Success
   */

  console.log(
    '===================================================='
  );

  console.log(
    '✅ [BOOTSTRAP SUCCESS] SHEBAODDS backend initialized.'
  );

  console.log(
    '===================================================='
  );

  console.log('');
}

/*
|--------------------------------------------------------------------------
| DIRECT EXECUTION
|--------------------------------------------------------------------------
*/

if (require.main === module) {
  bootstrapAppEngine()
    .then(() => {
      console.log(
        '✅ Bootstrap process completed.'
      );
    })
    .catch((error) => {
      console.error('');
      console.error(
        '💥 [BOOTSTRAP PANIC] Startup failed.'
      );

      console.error(
        error?.message || error
      );

      process.exit(1);
    });
}