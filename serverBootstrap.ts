import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

function verifyEnvironment(): void {
  console.log('🔄 [BOOTSTRAP] Checking environment...');

  const requiredVariables = [
    'MONGODB_URI'
  ];

  const missingVariables: string[] = [];

  for (const variable of requiredVariables) {
    const value = process.env[variable];

    if (!value || value.trim() === '') {
      missingVariables.push(variable);
    }
  }

  if (missingVariables.length > 0) {
    console.error(
      '❌ Missing environment variables:'
    );

    missingVariables.forEach((variable) => {
      console.error(`   - ${variable}`);
    });

    throw new Error(
      'Required environment variables are missing.'
    );
  }

  console.log(
    '✅ [BOOTSTRAP] Environment variables are configured.'
  );
}

async function connectMongoDB(): Promise<void> {
  const mongoUri = process.env.MONGODB_URI;

  if (!mongoUri) {
    throw new Error('MONGODB_URI is not configured.');
  }

  console.log(
    '🔄 [BOOTSTRAP] Connecting to MongoDB Atlas...'
  );

  await mongoose.connect(mongoUri, {
    serverSelectionTimeoutMS: 15000,
    connectTimeoutMS: 15000
  });

  console.log(
    '✅ [BOOTSTRAP] MongoDB Atlas connected successfully.'
  );

  console.log(
    `📦 Database: ${mongoose.connection.name}`
  );

  console.log(
    `🟢 MongoDB readyState: ${mongoose.connection.readyState}`
  );
}

export async function bootstrapAppEngine(): Promise<void> {
  console.log('');
  console.log('============================================');
  console.log('🦁 SHEBAODDS BOOTSTRAP');
  console.log('============================================');

  verifyEnvironment();

  await connectMongoDB();

  console.log('');
  console.log('============================================');
  console.log('✅ SHEBAODDS BOOTSTRAP SUCCESS');
  console.log('============================================');
}