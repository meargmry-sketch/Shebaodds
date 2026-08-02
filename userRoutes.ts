// ============================================
// SHEBAODDS - USER ROUTES
// Profile, preferences, and account settings
// ============================================

import express, { Request, Response, Router } from 'express';
import { authenticate } from './authMiddleware';
import User from './User';

const router: Router = express.Router();

// ==================== MY PROFILE ====================
router.get('/me', authenticate, async (req: any, res: Response) => {
  try {
    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, user });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load profile', error: error.message });
  }
});

// ==================== UPDATE MY PROFILE ====================
router.put('/me', authenticate, async (req: any, res: Response) => {
  try {
    const allowedFields = [
      'fullName', 'city', 'address', 'postalCode', 'language', 'theme', 'currency', 'timezone'
    ];
    const updates: any = {};
    for (const field of allowedFields) {
      if (req.body[field] !== undefined) updates[field] = req.body[field];
    }

    const user = await User.findByIdAndUpdate(req.user._id, { $set: updates }, { new: true, runValidators: true });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, user });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to update profile', error: error.message });
  }
});

// ==================== NOTIFICATION PREFERENCES ====================
router.put('/me/notifications', authenticate, async (req: any, res: Response) => {
  try {
    const updates: any = {};
    for (const [key, value] of Object.entries(req.body || {})) {
      updates[`notifications.${key}`] = value;
    }
    const user = await User.findByIdAndUpdate(req.user._id, { $set: updates }, { new: true, runValidators: true });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, notifications: user.notifications });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to update notification preferences', error: error.message });
  }
});

// ==================== BETTING PREFERENCES ====================
router.put('/me/betting-preferences', authenticate, async (req: any, res: Response) => {
  try {
    const updates: any = {};
    for (const [key, value] of Object.entries(req.body || {})) {
      updates[`bettingPreferences.${key}`] = value;
    }
    const user = await User.findByIdAndUpdate(req.user._id, { $set: updates }, { new: true, runValidators: true });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, bettingPreferences: user.bettingPreferences });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to update betting preferences', error: error.message });
  }
});

// ==================== RESPONSIBLE GAMBLING LIMITS ====================
router.put('/me/responsible-gambling', authenticate, async (req: any, res: Response) => {
  try {
    const allowedFields = ['depositLimit', 'lossLimit', 'wagerLimit', 'sessionTimeout', 'realityCheckInterval'];
    const updates: any = {};
    for (const field of allowedFields) {
      if (req.body[field] !== undefined) updates[`responsibleGambling.${field}`] = req.body[field];
    }
    const user = await User.findByIdAndUpdate(req.user._id, { $set: updates }, { new: true, runValidators: true });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, responsibleGambling: user.responsibleGambling });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to update responsible gambling limits', error: error.message });
  }
});

// ==================== SELF-EXCLUSION ====================
router.post('/me/self-exclude', authenticate, async (req: any, res: Response) => {
  try {
    const { days } = req.body as { days: number };
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + (days || 30));

    const user = await User.findByIdAndUpdate(
      req.user._id,
      { $set: { 'responsibleGambling.selfExcluded': true, 'responsibleGambling.selfExclusionEndDate': endDate } },
      { new: true }
    );
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, selfExcludedUntil: endDate });
  } catch (error: any) {
    res.status(400).json({ success: false, message: 'Failed to set self-exclusion', error: error.message });
  }
});

// ==================== PUBLIC PROFILE (limited fields) ====================
router.get('/:userId/public', async (req: Request, res: Response) => {
  try {
    const user = await User.findById(req.params.userId).select('username vip.level vip.name statistics.totalWins statistics.winningPercentage');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, user });
  } catch (error: any) {
    res.status(500).json({ success: false, message: 'Failed to load public profile', error: error.message });
  }
});

export default router;
