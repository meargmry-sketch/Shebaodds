import mongoose from 'mongoose';
import * as dotenv from 'dotenv';
import { Wallet, Wager } from './MongoDBWalletEngine';
import { Bet } from './Bet';
import { Transaction } from './Transaction';

dotenv.config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/shebaodds';
const TAX_RATE = parseFloat(process.env.TAX_RATE || '0.15');
const TAX_FREE_LIMIT = parseFloat(process.env.TAX_FREE_LIMIT || '100');

// Local representation of Wager Schema for query purposes
// (We already have Wager model from MongoDBWalletEngine)
// We also need to include the Bet model for sportsbook bets.

async function generateTaxReport() {
  console.log('🔄 [TAX AUDIT] Initializing Regional 15% Taxation Compliance Report...');
  console.log('🏛️  Tax Authority:', process.env.TAX_AUTHORITY_NAME || 'Ministry of Revenues - Ethiopia');
  console.log('🆔  Authority ID:', process.env.TAX_AUTHORITY_ID || 'TAX_SHEBAODDS_001');
  console.log('----------------------------------------------------------------------');

  try {
    await mongoose.connect(MONGODB_URI);

    // --------------------------------------------------------------
    // 1. Fetch all completed sportsbook bets (Won/Lost)
    // --------------------------------------------------------------
    const sportsBets = await Bet.find({ status: { $in: ['Won', 'Lost'] } });

    // --------------------------------------------------------------
    // 2. Fetch all casino wagers (Won/Lost) – using the Wager model
    // --------------------------------------------------------------
    const casinoWagers = await Wager.find({ status: { $in: ['Won', 'Lost'] } });

    // --------------------------------------------------------------
    // 3. Aggregate totals
    // --------------------------------------------------------------
    let totalVolume = 0;           // Total stake across all bets (sports + casino)
    let totalPayouts = 0;         // Total net payouts (including tax deduction)
    let totalTaxCollected = 0;    // Total tax deducted
    let taxableTicketsCount = 0;
    let exemptTicketsCount = 0;

    // --- Process sports bets ---
    for (const bet of sportsBets) {
      // For sports bets, we already have taxAmount and netWin fields.
      // We consider the stake as the volume.
      totalVolume += bet.stake;
      totalPayouts += bet.netWin || bet.actualWin || 0;
      totalTaxCollected += bet.taxAmount || 0;

      const netProfit = (bet.actualWin || 0) - bet.stake;
      if (netProfit > TAX_FREE_LIMIT) {
        taxableTicketsCount++;
      } else {
        exemptTicketsCount++;
      }
    }

    // --- Process casino wagers ---
    for (const wager of casinoWagers) {
      // For casino wagers, we have stake, payout (net after tax), and taxDeducted.
      totalVolume += wager.stake;
      totalPayouts += wager.payout || 0;
      totalTaxCollected += wager.taxDeducted || 0;

      const netProfit = wager.payout - wager.stake;
      if (netProfit > TAX_FREE_LIMIT) {
        taxableTicketsCount++;
      } else {
        exemptTicketsCount++;
      }
    }

    // --------------------------------------------------------------
    // 4. Output formatted ASCII Report
    // --------------------------------------------------------------
    console.log(`📊 STATUTORY TAX PERFORMANCE SHEET (Rate: ${(TAX_RATE * 100).toFixed(1)}%)`);
    console.log(`======================================================================`);
    console.log(`📅 Report Range          : Current Fiscal Ledger Block`);
    console.log(`📈 Gross Wagering Volume : ${totalVolume.toFixed(2)} ETB`);
    console.log(`💸 Total Cash Payouts    : ${totalPayouts.toFixed(2)} ETB`);
    console.log(`💰 Total Withheld Tax    : ${totalTaxCollected.toFixed(2)} ETB`);
    console.log(`🏷️  Taxable Tickets       : ${taxableTicketsCount}`);
    console.log(`🛡️  Exempt Tickets (<${TAX_FREE_LIMIT} ETB) : ${exemptTicketsCount}`);
    console.log(`📬 Contact Reporting     : ${process.env.TAX_REPORTING_EMAIL || 'tax@shebaodds.com'}`);
    console.log(`======================================================================`);
    console.log(`🏛️ Remit To Account     : ${process.env.TAX_PAYMENT_ACCOUNT || '1000234567890'} (${process.env.TAX_PAYMENT_BANK || 'CBE'})`);
    console.log(`🚀 Report Status: APPROVED FOR REMITTANCE`);

  } catch (err: any) {
    console.error('💥 [TAX REPORT ERROR] Failed to compile statutory data:', err.message || err);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
    console.log('🔌 [TAX AUDIT] Database connection pools released safely.');
  }
}

generateTaxReport();