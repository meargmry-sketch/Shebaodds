// ============================================
// SHEBAODDS - TAX ROUTES
// User tax profile + Admin statutory reporting
// ============================================

import express, { Request, Response, Router } from 'express';
import { authenticate, isAdmin } from './authMiddleware';
import { TaxTransaction, UserTaxProfile } from './Tax';
import {
  TAX_CONFIG,
  getCurrentTaxPeriod,
  generateMonthlyTaxReport,
  submitTaxReport,
  registerUserForTax,
  exemptUserFromTax
} from './taxService';

const router: Router = express.Router();

// ==================== MY TAX PROFILE ====================
router.get('/my-profile', authenticate, async (req: any, res: Response) => {
  try {
    const profile = await UserTaxProfile.findOne({ userId: req.user._id });
    res.json({
      success: true,
      profile: profile || { userId: req.user._id, isTaxRegistered: false, taxExempt: false, totalTaxPaid: 0, totalWinningsTaxed: 0 },
      config: { rate: TAX_CONFIG.RATE, taxFreeLimit: TAX_CONFIG.TAX_FREE_LIMIT }
    });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load tax profile', error: error.message });
  }
});

// ==================== MY TAX TRANSACTIONS ====================
router.get('/my-transactions', authenticate, async (req: any, res: Response) => {
  try {
    const { limit = '50', page = '1', taxPeriod } = req.query as any;
    const limitNum = parseInt(limit, 10) || 50;
    const pageNum = parseInt(page, 10) || 1;
    const skip = (pageNum - 1) * limitNum;

    const query: any = { userId: req.user._id };
    if (taxPeriod) query.taxPeriod = taxPeriod;

    const [transactions, total] = await Promise.all([
      TaxTransaction.find(query).sort({ calculatedAt: -1 }).skip(skip).limit(limitNum),
      TaxTransaction.countDocuments(query)
    ]);

    res.json({ success: true, transactions, total, page: pageNum, pages: Math.ceil(total / limitNum) });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load tax transactions', error: error.message });
  }
});

// ==================== REGISTER FOR TAX ====================
router.post('/register', authenticate, async (req: any, res: Response) => {
  try {
    const { taxId, taxRegistrationNumber } = req.body;
    const profile = await registerUserForTax(req.user._id, taxId, taxRegistrationNumber);
    res.json({ success: true, profile });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to register for tax', error: error.message });
  }
});

// ==================== ADMIN: EXEMPT A USER ====================
router.post('/admin/exempt/:userId', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const { exemptionType, exemptionCertificate } = req.body;
    const profile = await exemptUserFromTax(req.params.userId, exemptionType, exemptionCertificate);
    res.json({ success: true, profile });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to exempt user from tax', error: error.message });
  }
});

// ==================== ADMIN: MONTHLY REPORT ====================
router.get('/admin/report/:taxPeriod?', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const taxPeriod = req.params.taxPeriod || getCurrentTaxPeriod();
    const { summary } = await generateMonthlyTaxReport(taxPeriod);
    res.json({ success: true, summary });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to generate tax report', error: error.message });
  }
});

// ==================== ADMIN: SUBMIT REPORT TO AUTHORITY ====================
router.post('/admin/report/:taxPeriod/submit', authenticate, isAdmin, async (req: Request, res: Response) => {
  try {
    const result = await submitTaxReport(req.params.taxPeriod);
    if (!result.success) {
      return res.status(500).json(result);
    }
    res.json(result);
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to submit tax report', error: error.message });
  }
});

export default router;
