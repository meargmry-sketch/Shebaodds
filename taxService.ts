// ============================================
// SHEBAODDS - TAX SERVICE
// 15% Ethiopian Withholding Tax Calculations
// Supports: Sportsbook & 51+ Casino Games
// ============================================

import mongoose from 'mongoose';
import PDFDocument from 'pdfkit';
import ExcelJS from 'exceljs';
import User from './User';
import { TaxTransaction, TaxSummary, UserTaxProfile } from './Tax';
import Bet from './Bet';
import { sendEmail } from './authRoutes';

export const TAX_CONFIG = {
  RATE: parseFloat(process.env.TAX_RATE || '0.15'),
  TAX_FREE_LIMIT: parseFloat(process.env.TAX_FREE_LIMIT || '100'),
  COLLECTION_METHOD: process.env.TAX_COLLECTION_METHOD || 'automatic',
  AUTHORITY_NAME: process.env.TAX_AUTHORITY_NAME || 'Ministry of Revenues - Ethiopia',
  AUTHORITY_ID: process.env.TAX_AUTHORITY_ID || 'TAX_SHEBAODDS_001',
  REPORTING_EMAIL: process.env.TAX_REPORTING_EMAIL || 'tax@shebaodds.com',
  PAYMENT_ACCOUNT: process.env.TAX_PAYMENT_ACCOUNT || '1000234567890',
  PAYMENT_BANK: process.env.TAX_PAYMENT_BANK || 'Commercial Bank of Ethiopia',
  PAYMENT_REFERENCE_PREFIX: process.env.TAX_PAYMENT_REFERENCE_PREFIX || 'SHEBAODDS_TAX',
  REPORTING_FREQUENCY: process.env.TAX_REPORTING_FREQUENCY || 'monthly'
};

// Calculate tax on winning
export function calculateTax(winningAmount: number, isExempt = false) {
  if (isExempt || winningAmount <= TAX_CONFIG.TAX_FREE_LIMIT) {
    return {
      taxAmount: 0,
      netWinning: winningAmount,
      isExempt: true,
      reason: isExempt ? 'User tax exempt' : 'Below tax-free limit'
    };
  }

  const taxAmount = winningAmount * TAX_CONFIG.RATE;
  const netWinning = winningAmount - taxAmount;

  return {
    taxAmount: Math.floor(taxAmount * 100) / 100,
    netWinning: Math.floor(netWinning * 100) / 100,
    isExempt: false,
    taxRate: TAX_CONFIG.RATE,
    reason: 'Withholding tax on gambling winnings'
  };
}

// Generate unique tax reference
export function generateTaxReference(): string {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 10).toUpperCase();
  return `${TAX_CONFIG.PAYMENT_REFERENCE_PREFIX}_${timestamp}_${random}`;
}

// Get current tax period (YYYY-MM)
export function getCurrentTaxPeriod(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

// Process tax for a winning bet (Sportsbook or Casino)
export async function processTaxForWinning(betId: any, userId: any, winningAmount: number, matchId?: any) {
  try {
    // Check if user is tax exempt
    const userTaxProfile = await UserTaxProfile.findOne({ userId });
    const isExempt = userTaxProfile?.taxExempt || false;

    const taxCalculation = calculateTax(winningAmount, isExempt);
    const taxPeriod = getCurrentTaxPeriod();

    const taxTransaction = new TaxTransaction({
      userId,
      betId,
      matchId,
      grossWinning: winningAmount,
      taxAmount: taxCalculation.taxAmount,
      netWinning: taxCalculation.netWinning,
      taxRate: TAX_CONFIG.RATE,
      taxPeriod,
      taxReference: generateTaxReference(),
      isExempt: taxCalculation.isExempt,
      exemptionReason: taxCalculation.reason,
      status: taxCalculation.isExempt ? 'exempt' : 'deducted',
      deductedAt: taxCalculation.isExempt ? null : new Date()
    });

    await taxTransaction.save();

    // Update User Tax Profile
    await UserTaxProfile.findOneAndUpdate(
      { userId },
      {
        $inc: { 
          totalTaxPaid: taxCalculation.taxAmount, 
          totalWinningsTaxed: winningAmount 
        },
        $set: { 
          lastTaxCalculation: new Date(),
          updatedAt: new Date()
        }
      },
      { upsert: true, new: true }
    );

    // Update User wallet totalTaxPaid
    await User.findByIdAndUpdate(userId, {
      $inc: { 
        'wallet.totalTaxPaid': taxCalculation.taxAmount,
        'taxProfile.totalTaxPaid': taxCalculation.taxAmount,
        'taxProfile.totalWinningsTaxed': winningAmount
      }
    });

    // Update Tax Summary
    await TaxSummary.findOneAndUpdate(
      { taxPeriod },
      {
        $inc: { 
          totalWinnings: winningAmount, 
          totalTaxCollected: taxCalculation.taxAmount, 
          totalBets: 1 
        },
        $addToSet: { userTaxDetails: { userId, username: null } }
      },
      { upsert: true }
    );

    // Update bet with tax info (works for both sports and casino bets)
    await Bet.findByIdAndUpdate(betId, {
      taxAmount: taxCalculation.taxAmount,
      netWin: taxCalculation.netWinning,
      taxTransactionId: taxTransaction._id,
      isTaxExempt: taxCalculation.isExempt,
      taxExemptReason: taxCalculation.reason
    });

    return taxTransaction;

  } catch (error) {
    console.error('Tax processing error:', error);
    return null;
  }
}

// Generate monthly tax report (PDF & Excel)
export async function generateMonthlyTaxReport(taxPeriod: string) {
  try {
    let summary = await TaxSummary.findOne({ taxPeriod });
    if (!summary) {
      // Create a default / blank summary if none exists so generation doesn't crash
      summary = new TaxSummary({
        taxPeriod,
        totalWinnings: 0,
        totalTaxCollected: 0,
        totalBets: 0,
        totalUsers: 0,
        userTaxDetails: [],
        reported: false
      });
      await summary.save();
    }

    // Get all transactions for this period with user details
    const transactions = await TaxTransaction.find({ taxPeriod })
      .populate('userId', 'username email phone fullName')
      .populate('betId', 'matchId marketType stake odds isCasinoBet casinoGameId')
      .populate('matchId', 'homeTeam awayTeam league');

    // Update user count and usernames in summary
    const uniqueUsers = [...new Set(transactions.map(tx => tx.userId?._id?.toString()))];
    summary.totalUsers = uniqueUsers.length;

    // Update user details in summary
    const userMap = new Map();

    for (const tx of transactions) {
      if (tx.userId && !userMap.has(tx.userId._id.toString())) {
        userMap.set(tx.userId._id.toString(), {
          userId: tx.userId._id,
          username: (tx.userId as any).username,
          totalWinnings: 0,
          totalTax: 0
        });
      }
      if (tx.userId) {
        const userDetail = userMap.get(tx.userId._id.toString());
        userDetail.totalWinnings += tx.grossWinning;
        userDetail.totalTax += tx.taxAmount;
      }
    }

    summary.userTaxDetails = Array.from(userMap.values());
    await summary.save();

    // Generate PDF inside a promise to handle stream safely
    const pdfBuffer: Buffer = await new Promise((resolve, reject) => {
      const doc = new PDFDocument({ margin: 50, size: 'A4' });
      const buffers: Buffer[] = [];
      doc.on('data', (chunk: Buffer) => buffers.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(buffers)));
      doc.on('error', (err: any) => reject(err));

      // Header with SHEBAODDS branding
      doc.fontSize(24).font('Helvetica-Bold').fillColor('#FFD700').text('SHEBAODDS', { align: 'center' });
      doc.fontSize(12).font('Helvetica').fillColor('#0A0A0A').text('Smart Bets. Real Wins.', { align: 'center' });
      doc.moveDown();

      doc.fontSize(18).fillColor('#000000').text('MONTHLY TAX REPORT', { align: 'center' });
      doc.fontSize(12).text(`Period: ${taxPeriod}`, { align: 'center' });
      doc.moveDown();

      // Summary Section
      doc.fontSize(14).font('Helvetica-Bold').text('Summary', { underline: true });
      doc.fontSize(10).font('Helvetica')
        .text(`Total Winnings: ${summary.totalWinnings.toLocaleString()} ETB`)
        .text(`Total Tax Collected: ${summary.totalTaxCollected.toLocaleString()} ETB`)
        .text(`Total Bets: ${summary.totalBets.toLocaleString()}`)
        .text(`Total Users: ${summary.totalUsers.toLocaleString()}`);
      doc.moveDown();

      // Tax Rate Information
      doc.fontSize(14).font('Helvetica-Bold').text('Tax Rate Information', { underline: true });
      doc.fontSize(10).font('Helvetica')
        .text(`Tax Rate: ${TAX_CONFIG.RATE * 100}% (Withholding Tax)`)
        .text(`Tax-Free Limit: ${TAX_CONFIG.TAX_FREE_LIMIT} ETB per winning`)
        .text(`Collection Method: ${TAX_CONFIG.COLLECTION_METHOD}`);
      doc.moveDown();

      // Authority Information
      doc.fontSize(14).font('Helvetica-Bold').text('Tax Authority', { underline: true });
      doc.fontSize(10).font('Helvetica')
        .text(`Name: ${TAX_CONFIG.AUTHORITY_NAME}`)
        .text(`ID: ${TAX_CONFIG.AUTHORITY_ID}`)
        .text(`Reporting Email: ${TAX_CONFIG.REPORTING_EMAIL}`);
      doc.moveDown();

      // Payment Information
      doc.fontSize(14).font('Helvetica-Bold').text('Payment Information', { underline: true });
      doc.fontSize(10).font('Helvetica')
        .text(`Bank: ${TAX_CONFIG.PAYMENT_BANK}`)
        .text(`Account Number: ${TAX_CONFIG.PAYMENT_ACCOUNT}`)
        .text(`Reference Prefix: ${TAX_CONFIG.PAYMENT_REFERENCE_PREFIX}`);
      doc.moveDown();

      // User Tax Details Table
      doc.fontSize(12).font('Helvetica-Bold').text('User Tax Details');

      // Create table
      const tableTop = doc.y + 10;
      doc.fontSize(8).font('Helvetica-Bold');
      doc.text('Username', 50, tableTop);
      doc.text('Total Winnings (ETB)', 200, tableTop);
      doc.text('Tax Paid (ETB)', 350, tableTop);
      doc.text('Net Winnings (ETB)', 450, tableTop);

      doc.fontSize(8).font('Helvetica');
      let rowY = tableTop + 15;

      for (const userDetail of summary.userTaxDetails) {
        if (rowY > 700) {
          doc.addPage();
          rowY = 50;
        }
        doc.text(userDetail.username || 'Unknown', 50, rowY);
        doc.text(userDetail.totalWinnings.toLocaleString(), 200, rowY);
        doc.text(userDetail.totalTax.toLocaleString(), 350, rowY);
        doc.text((userDetail.totalWinnings - userDetail.totalTax).toLocaleString(), 450, rowY);
        rowY += 15;
      }

      doc.moveDown();

      // Footer
      doc.fontSize(8).font('Helvetica')
        .text('This is an official tax report generated by SHEBAODDS.', { align: 'center' })
        .text(`Generated: ${new Date().toLocaleString()}`, { align: 'center' })
        .text('SHEBAODDS - Smart Bets. Real Wins.', { align: 'center' });

      doc.end();
    });

    // Generate Excel
    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'SHEBAODDS';
    workbook.created = new Date();

    // Summary Sheet
    const summarySheet = workbook.addWorksheet('Tax Summary');
    summarySheet.columns = [
      { header: 'Metric', key: 'metric', width: 30 },
      { header: 'Value', key: 'value', width: 20 }
    ];

    summarySheet.addRow({ metric: 'Period', value: taxPeriod });
    summarySheet.addRow({ metric: 'Total Winnings (ETB)', value: summary.totalWinnings });
    summarySheet.addRow({ metric: 'Total Tax Collected (ETB)', value: summary.totalTaxCollected });
    summarySheet.addRow({ metric: 'Total Bets', value: summary.totalBets });
    summarySheet.addRow({ metric: 'Total Users', value: summary.totalUsers });
    summarySheet.addRow({ metric: 'Tax Rate', value: `${TAX_CONFIG.RATE * 100}%` });
    summarySheet.addRow({ metric: 'Tax-Free Limit (ETB)', value: TAX_CONFIG.TAX_FREE_LIMIT });

    // Transactions Sheet
    const transactionsSheet = workbook.addWorksheet('Tax Transactions');
    transactionsSheet.columns = [
      { header: 'Tax Reference', key: 'reference', width: 30 },
      { header: 'User', key: 'username', width: 20 },
      { header: 'Email', key: 'email', width: 30 },
      { header: 'Gross Winning', key: 'gross', width: 15 },
      { header: 'Tax Amount', key: 'tax', width: 15 },
      { header: 'Net Winning', key: 'net', width: 15 },
      { header: 'Match/Casino', key: 'match', width: 30 },
      { header: 'Date', key: 'date', width: 20 }
    ];

    for (const tx of transactions) {
      const bet = tx.betId as any;
      const matchLabel = bet?.isCasinoBet 
        ? `Casino: ${bet?.casinoGameId || 'Unknown'}`
        : tx.matchId ? `${(tx.matchId as any).homeTeam} vs ${(tx.matchId as any).awayTeam}` : 'N/A';

      transactionsSheet.addRow({
        reference: tx.taxReference,
        username: (tx.userId as any)?.username || 'Unknown',
        email: (tx.userId as any)?.email || 'Unknown',
        gross: tx.grossWinning,
        tax: tx.taxAmount,
        net: tx.netWinning,
        match: matchLabel,
        date: tx.calculatedAt ? tx.calculatedAt.toLocaleDateString() : new Date().toLocaleDateString()
      });
    }

    // User Details Sheet
    const userSheet = workbook.addWorksheet('User Details');
    userSheet.columns = [
      { header: 'Username', key: 'username', width: 20 },
      { header: 'Total Winnings (ETB)', key: 'winnings', width: 20 },
      { header: 'Tax Paid (ETB)', key: 'tax', width: 15 },
      { header: 'Net Winnings (ETB)', key: 'net', width: 15 }
    ];

    for (const userDetail of summary.userTaxDetails) {
      userSheet.addRow({
        username: userDetail.username || 'Unknown',
        winnings: userDetail.totalWinnings,
        tax: userDetail.totalTax,
        net: userDetail.totalWinnings - userDetail.totalTax
      });
    }

    const excelBuffer = await workbook.xlsx.writeBuffer() as Buffer;

    return { pdf: pdfBuffer, excel: excelBuffer, summary };

  } catch (error) {
    console.error('Generate tax report error:', error);
    throw error;
  }
}

// Submit tax report to authority
export async function submitTaxReport(taxPeriod: string) {
  try {
    const { pdf, excel, summary } = await generateMonthlyTaxReport(taxPeriod);

    await sendEmail({
      to: TAX_CONFIG.REPORTING_EMAIL,
      subject: `Tax Report - ${taxPeriod} - SHEBAODDS`,
      template: 'tax_report',
      data: {
        period: taxPeriod,
        totalWinnings: summary.totalWinnings,
        totalTaxCollected: summary.totalTaxCollected,
        totalBets: summary.totalBets,
        totalUsers: summary.totalUsers,
        taxRate: TAX_CONFIG.RATE * 100,
        taxFreeLimit: TAX_CONFIG.TAX_FREE_LIMIT,
        authorityName: TAX_CONFIG.AUTHORITY_NAME,
        reportDate: new Date().toLocaleDateString()
      },
      attachments: [
        { filename: `tax_report_${taxPeriod}.pdf`, content: pdf },
        { filename: `tax_report_${taxPeriod}.xlsx`, content: excel }
      ]
    });

    await TaxSummary.findOneAndUpdate(
      { taxPeriod },
      {
        reported: true,
        reportedAt: new Date(),
        reportReference: `REP_${taxPeriod}_${Date.now()}`
      }
    );

    return { success: true, message: 'Tax report submitted successfully' };

  } catch (error: any) {
    console.error('Submit tax report error:', error);
    return { success: false, error: error.message };
  }
}

// Register user for tax (with Tax ID)
export async function registerUserForTax(userId: any, taxId: string, taxRegistrationNumber: string) {
  try {
    const userTaxProfile = await UserTaxProfile.findOneAndUpdate(
      { userId },
      {
        taxId,
        taxRegistrationNumber,
        isTaxRegistered: true,
        updatedAt: new Date()
      },
      { upsert: true, new: true }
    );

    await User.findByIdAndUpdate(userId, {
      'taxProfile.taxId': taxId,
      'taxProfile.taxRegistrationNumber': taxRegistrationNumber,
      'taxProfile.isTaxRegistered': true
    });

    return userTaxProfile;

  } catch (error) {
    console.error('Tax registration error:', error);
    throw error;
  }
}

// Exempt user from tax
export async function exemptUserFromTax(userId: any, exemptionType: string, exemptionCertificate: string) {
  try {
    const userTaxProfile = await UserTaxProfile.findOneAndUpdate(
      { userId },
      {
        taxExempt: true,
        exemptionType,
        exemptionCertificate,
        updatedAt: new Date()
      },
      { upsert: true, new: true }
    );

    await User.findByIdAndUpdate(userId, {
      'taxProfile.taxExempt': true,
      'taxProfile.exemptionType': exemptionType
    });

    return userTaxProfile;

  } catch (error) {
    console.error('Tax exemption error:', error);
    throw error;
  }
}