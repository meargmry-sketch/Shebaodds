// ============================================
// SHEBAODDS - BETTING ROUTES
// Complete Bet Placement, Accumulators, System Bets, Cashout
// ============================================

import express, { Request, Response, NextFunction, Router } from 'express';
import mongoose from 'mongoose';
import { authenticate, canPlaceBet } from './authMiddleware';
import { checkResponsibleGambling, checkSelfExclusion } from './responsibleGamblingMiddleware';
import User from './User';
import Match, { MATCH_STATUS } from './Match';
import Bet, { BET_STATUS } from './Bet';
import { Transaction, TRANSACTION_TYPES, TRANSACTION_STATUS } from './Transaction';
import { sendNotification } from './walletRoutes';

const router = express.Router();

// ==================== VALIDATION MIDDLEWARE ====================
const validateBet = (req: Request, res: Response, next: NextFunction) => {
  const { matchId, marketType, selection, odds, stake } = req.body;
  if (!matchId) return res.status(400).json({ success: false, message: 'matchId is required' });
  if (!marketType) return res.status(400).json({ success: false, message: 'marketType is required' });
  if (!selection) return res.status(400).json({ success: false, message: 'selection is required' });
  if (odds === undefined || isNaN(Number(odds)) || Number(odds) <= 0) {
    return res.status(400).json({ success: false, message: 'Valid odds is required' });
  }
  if (stake === undefined || isNaN(Number(stake)) || Number(stake) <= 0) {
    return res.status(400).json({ success: false, message: 'Valid stake is required' });
  }
  return next();
};

const validateAccumulator = (req: Request, res: Response, next: NextFunction) => {
  const { selections, totalStake } = req.body;
  if (!selections || !Array.isArray(selections) || selections.length === 0) {
    return res.status(400).json({ success: false, message: 'selections array is required' });
  }
  if (totalStake === undefined || isNaN(Number(totalStake)) || Number(totalStake) <= 0) {
    return res.status(400).json({ success: false, message: 'Valid totalStake is required' });
  }
  for (const sel of selections) {
    if (!sel.matchId) return res.status(400).json({ success: false, message: 'matchId is required for each selection' });
    if (sel.odds === undefined || isNaN(Number(sel.odds)) || Number(sel.odds) <= 0) {
      return res.status(400).json({ success: false, message: 'Valid odds is required for each selection' });
    }
    if (!sel.selection) return res.status(400).json({ success: false, message: 'selection is required for each selection' });
  }
  return next();
};

const validateSystemBet = (req: Request, res: Response, next: NextFunction) => {
  const { selections, totalStake, systemBetType } = req.body;
  if (!selections || !Array.isArray(selections) || selections.length === 0) {
    return res.status(400).json({ success: false, message: 'selections array is required' });
  }
  if (totalStake === undefined || isNaN(Number(totalStake)) || Number(totalStake) <= 0) {
    return res.status(400).json({ success: false, message: 'Valid totalStake is required' });
  }
  if (!systemBetType) {
    return res.status(400).json({ success: false, message: 'systemBetType is required' });
  }
  for (const sel of selections) {
    if (!sel.matchId) return res.status(400).json({ success: false, message: 'matchId is required for each selection' });
    if (sel.odds === undefined || isNaN(Number(sel.odds)) || Number(sel.odds) <= 0) {
      return res.status(400).json({ success: false, message: 'Valid odds is required for each selection' });
    }
    if (!sel.selection) return res.status(400).json({ success: false, message: 'selection is required for each selection' });
  }
  return next();
};

// Combinations Generator Helper for System Bets
function getCombinations(arr: number[], k: number): number[][] {
  const result: number[][] = [];
  function helper(start: number, path: number[]) {
    if (path.length === k) {
      result.push([...path]);
      return;
    }
    for (let i = start; i < arr.length; i++) {
      path.push(arr[i]);
      helper(i + 1, path);
      path.pop();
    }
  }
  helper(0, []);
  return result;
}

// System bet configuration lookup
const SYSTEM_CONFIGS: Record<string, { selectionsCount: number; betsCount: number; sizes: number[] }> = {
  trixie: { selectionsCount: 3, betsCount: 4, sizes: [2, 3] },
  yankee: { selectionsCount: 4, betsCount: 11, sizes: [2, 3, 4] },
  patent: { selectionsCount: 3, betsCount: 7, sizes: [1, 2, 3] },
  lucky15: { selectionsCount: 4, betsCount: 15, sizes: [1, 2, 3, 4] },
  lucky31: { selectionsCount: 5, betsCount: 31, sizes: [1, 2, 3, 4, 5] },
  lucky63: { selectionsCount: 6, betsCount: 63, sizes: [1, 2, 3, 4, 5, 6] },
  canadian: { selectionsCount: 5, betsCount: 26, sizes: [2, 3, 4, 5] },
  heinz: { selectionsCount: 6, betsCount: 57, sizes: [2, 3, 4, 5, 6] },
  super_heinz: { selectionsCount: 7, betsCount: 120, sizes: [2, 3, 4, 5, 6, 7] },
  goliath: { selectionsCount: 8, betsCount: 247, sizes: [2, 3, 4, 5, 6, 7, 8] }
};

// ==================== PLACE SINGLE BET ====================
router.post('/place', 
  authenticate, 
  canPlaceBet, 
  checkSelfExclusion, 
  checkResponsibleGambling, 
  validateBet,
  async (req: any, res: Response) => {
    try {
      const { 
        matchId, 
        marketType, 
        selection, 
        odds, 
        stake: inputStake, 
        isLive = false,
        period = 'full',
        useBonus = false
      } = req.body;
      
      const user = req.user;
      const stake = parseFloat(inputStake);
      
      // Get match
      const match = await Match.findOne({ matchId });
      if (!match) {
        return res.status(404).json({ success: false, message: 'Match not found' });
      }
      
      // Validate match status
      if (match.status === MATCH_STATUS.FINISHED) {
        return res.status(400).json({ success: false, message: 'Match already finished' });
      }
      
      if (isLive && !match.isLiveNow) {
        return res.status(400).json({ success: false, message: 'Match is not live' });
      }
      
      // Validate minimum bet
      const minBet = parseInt(process.env.MIN_BET_AMOUNT || '1', 10) || 1;
      if (stake < minBet) {
        return res.status(400).json({ success: false, message: `Minimum stake is ${minBet} ETB` });
      }
      
      // Validate maximum bet
      const maxBet = parseInt(process.env.MAX_BET_AMOUNT || '50000', 10) || 50000;
      if (stake > maxBet) {
        return res.status(400).json({ success: false, message: `Maximum stake is ${maxBet} ETB` });
      }
      
      // Check balance
      let availableBalance = user.wallet.balance;
      let usedRealBalance = stake;
      let usedBonusBalance = 0;
      
      if (useBonus && user.wallet.bonusBalance > 0) {
        usedBonusBalance = Math.min(stake, user.wallet.bonusBalance);
        usedRealBalance = stake - usedBonusBalance;
        availableBalance = user.wallet.balance + user.wallet.bonusBalance;
      }
      
      if (availableBalance < stake) {
        return res.status(400).json({ success: false, message: 'Insufficient balance' });
      }
      
      // Calculate potential win
      const potentialWin = stake * odds;
      
      // Create bet
      const bet = new Bet({
        userId: user._id,
        matchId: match._id,
        betType: 'single',
        marketType,
        selection,
        odds,
        stake,
        potentialWin,
        isLive,
        period,
        usedRealBalance,
        usedBonusBalance,
        ipAddress: req.ip,
        deviceInfo: {
          deviceId: req.headers['x-device-id'] || '',
          platform: req.headers['x-platform'] || '',
          browser: req.headers['user-agent'] || '',
          os: ''
        }
      });
      
      // Deduct stake
      const previousBalance = user.wallet.balance;
      const previousBonusBalance = user.wallet.bonusBalance;
      
      user.wallet.balance -= usedRealBalance;
      user.wallet.bonusBalance -= usedBonusBalance;
      user.wallet.totalWagered += stake;
      user.statistics.totalBets += 1;
      
      // Create transaction
      const transaction = new Transaction({
        userId: user._id,
        betId: bet._id,
        type: TRANSACTION_TYPES.BET_PLACE,
        amount: stake,
        netAmount: stake,
        paymentMethod: 'bonus', // fallback to bonus if using bonus or balance
        previousBalance,
        previousBonusBalance,
        newBalance: user.wallet.balance,
        newBonusBalance: user.wallet.bonusBalance,
        status: TRANSACTION_STATUS.COMPLETED,
        completedAt: new Date(),
        metadata: { betId: bet._id, matchId, marketType, selection, odds }
      });
      
      await Promise.all([bet.save(), user.save(), transaction.save()]);
      
      // Send real-time update
      req.io?.to(`user_${user._id}`).emit('wallet_update', {
        balance: user.wallet.balance,
        bonusBalance: user.wallet.bonusBalance
      });
      
      // Send confirmation
      await sendNotification({
        userId: user._id,
        title: 'Bet Placed! 🎯',
        message: `${stake} ETB on ${match.homeTeam} vs ${match.awayTeam} - ${selection} @ ${odds}`,
        type: 'bet_placed',
        data: { betId: bet._id, stake, odds, potentialWin }
      });
      
      return res.json({
        success: true,
        message: 'Bet placed successfully!',
        bet: {
          id: bet._id,
          match: `${match.homeTeam} vs ${match.awayTeam}`,
          selection,
          odds,
          stake,
          potentialWin,
          status: bet.status
        },
        newBalance: user.wallet.balance,
        bonusBalance: user.wallet.bonusBalance
      });
      
    } catch (error: any) {
      console.error('Place bet error:', error);
      return res.status(500).json({ success: false, message: error.message });
    }
});

// ==================== PLACE ACCUMULATOR ====================
router.post('/accumulator', 
  authenticate, 
  canPlaceBet, 
  checkSelfExclusion, 
  checkResponsibleGambling,
  validateAccumulator,
  async (req: any, res: Response) => {
    try {
      const { selections, totalStake } = req.body;
      const user = req.user;
      const stake = parseFloat(totalStake);
      
      // Validate number of selections
      const maxSelections = parseInt(process.env.MAX_ACCUMULATOR_SELECTIONS || '20', 10) || 20;
      if (selections.length < 2) {
        return res.status(400).json({ success: false, message: 'Accumulator requires at least 2 selections' });
      }
      if (selections.length > maxSelections) {
        return res.status(400).json({ success: false, message: `Maximum ${maxSelections} selections allowed` });
      }
      
      // Calculate combined odds
      let combinedOdds = 1;
      for (const sel of selections) {
        combinedOdds *= parseFloat(sel.odds);
      }
      
      // Validate max odds
      const maxOdds = parseInt(process.env.MAX_ACCUMULATOR_ODDS || '1000', 10) || 1000;
      if (combinedOdds > maxOdds) {
        return res.status(400).json({ success: false, message: `Combined odds cannot exceed ${maxOdds}` });
      }
      
      // Check balance
      if (user.wallet.balance < stake) {
        return res.status(400).json({ success: false, message: 'Insufficient balance' });
      }
      
      // Validate all matches
      const accumulatorId = new mongoose.Types.ObjectId().toString();
      const betSelections = [];
      
      for (const sel of selections) {
        const match = await Match.findOne({ matchId: sel.matchId });
        if (!match) {
          return res.status(404).json({ success: false, message: `Match ${sel.matchId} not found` });
        }
        if (match.status === MATCH_STATUS.FINISHED) {
          return res.status(400).json({ success: false, message: `Match ${match.homeTeam} vs ${match.awayTeam} is already finished` });
        }
        
        betSelections.push({
          matchId: match._id,
          marketType: sel.marketType || 'ft_1x2',
          selection: sel.selection,
          odds: parseFloat(sel.odds),
          status: BET_STATUS.PENDING
        });
      }
      
      const potentialWin = stake * combinedOdds;
      
      // Create accumulator bet
      const bet = new Bet({
        userId: user._id,
        matchId: betSelections[0].matchId, // schema requires matchId
        betType: 'accumulator',
        isAccumulator: true,
        isSingle: false,
        accumulatorId,
        accumulatorSelections: betSelections,
        combinedOdds,
        accumulatorType: 'accumulator',
        stake,
        potentialWin,
        ipAddress: req.ip
      });
      
      // Deduct stake
      const previousBalance = user.wallet.balance;
      user.wallet.balance -= stake;
      user.wallet.totalWagered += stake;
      user.statistics.totalBets += 1;
      
      // Create transaction
      const transaction = new Transaction({
        userId: user._id,
        betId: bet._id,
        type: TRANSACTION_TYPES.BET_PLACE,
        subType: 'accumulator',
        amount: stake,
        netAmount: stake,
        previousBalance,
        previousBonusBalance: user.wallet.bonusBalance,
        newBalance: user.wallet.balance,
        newBonusBalance: user.wallet.bonusBalance,
        status: TRANSACTION_STATUS.COMPLETED,
        completedAt: new Date(),
        metadata: { accumulatorId, selections: selections.length, combinedOdds }
      });
      
      await Promise.all([bet.save(), user.save(), transaction.save()]);
      
      req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });
      
      return res.json({
        success: true,
        message: 'Accumulator placed successfully!',
        accumulatorId,
        selections: selections.length,
        combinedOdds: combinedOdds.toFixed(2),
        totalStake: stake,
        potentialWin: potentialWin.toFixed(2),
        newBalance: user.wallet.balance
      });
      
    } catch (error: any) {
      console.error('Accumulator error:', error);
      return res.status(500).json({ success: false, message: error.message });
    }
});

// ==================== PLACE SYSTEM BET (Trixie, Yankee, Patent, Lucky 15, etc.) ====================
router.post('/system-bet', 
  authenticate, 
  canPlaceBet, 
  checkSelfExclusion, 
  checkResponsibleGambling,
  validateSystemBet,
  async (req: any, res: Response) => {
    try {
      const { selections, totalStake, systemBetType } = req.body;
      const user = req.user;
      const stake = parseFloat(totalStake);
      
      const config = SYSTEM_CONFIGS[systemBetType];
      if (!config) {
        return res.status(400).json({ success: false, message: `Invalid system bet type: ${systemBetType}` });
      }
      
      if (selections.length !== config.selectionsCount) {
        return res.status(400).json({ 
          success: false, 
          message: `${systemBetType} requires exactly ${config.selectionsCount} selections, but got ${selections.length}` 
        });
      }
      
      // Check balance
      if (user.wallet.balance < stake) {
        return res.status(400).json({ success: false, message: 'Insufficient balance' });
      }
      
      // Validate each selection
      const validatedSelections = [];
      for (const sel of selections) {
        const match = await Match.findOne({ matchId: sel.matchId });
        if (!match) {
          return res.status(404).json({ success: false, message: `Match ${sel.matchId} not found` });
        }
        if (match.status === MATCH_STATUS.FINISHED) {
          return res.status(400).json({ success: false, message: `Match ${match.homeTeam} vs ${match.awayTeam} is already finished` });
        }
        validatedSelections.push({
          matchId: match._id,
          selection: sel.selection,
          odds: parseFloat(sel.odds)
        });
      }
      
      // Generate all combination bets
      const stakePerBet = stake / config.betsCount;
      const indices = Array.from({ length: selections.length }, (_, i) => i);
      const systemBets: any[] = [];
      let totalPotentialWin = 0;
      
      for (const size of config.sizes) {
        const combos = getCombinations(indices, size);
        for (const combo of combos) {
          let combinedOdds = 1;
          for (const idx of combo) {
            combinedOdds *= validatedSelections[idx].odds;
          }
          const potentialWin = stakePerBet * combinedOdds;
          totalPotentialWin += potentialWin;
          systemBets.push({
            selections: combo,
            combinedOdds,
            stake: stakePerBet,
            potentialWin,
            status: BET_STATUS.PENDING
          });
        }
      }
      
      // Create system bet
      const bet = new Bet({
        userId: user._id,
        matchId: validatedSelections[0].matchId, // schema requires matchId
        betType: 'system',
        isSystemBet: true,
        isSingle: false,
        systemBetType,
        systemSelections: validatedSelections,
        systemBets,
        totalSystemStake: stake,
        numberOfBets: config.betsCount,
        stake,
        potentialWin: totalPotentialWin,
        ipAddress: req.ip
      });
      
      // Deduct stake
      const previousBalance = user.wallet.balance;
      user.wallet.balance -= stake;
      user.wallet.totalWagered += stake;
      user.statistics.totalBets += 1;
      
      const transaction = new Transaction({
        userId: user._id,
        betId: bet._id,
        type: TRANSACTION_TYPES.BET_PLACE,
        subType: systemBetType,
        amount: stake,
        netAmount: stake,
        previousBalance,
        previousBonusBalance: user.wallet.bonusBalance,
        newBalance: user.wallet.balance,
        newBonusBalance: user.wallet.bonusBalance,
        status: TRANSACTION_STATUS.COMPLETED,
        completedAt: new Date(),
        metadata: { systemBetType, selections: selections.length, numberOfBets: config.betsCount }
      });
      
      await Promise.all([bet.save(), user.save(), transaction.save()]);
      
      return res.json({
        success: true,
        message: `${systemBetType.toUpperCase()} placed successfully!`,
        systemBetType,
        numberOfBets: config.betsCount,
        stakePerBet: stakePerBet.toFixed(2),
        totalStake: stake,
        totalPotentialWin: totalPotentialWin.toFixed(2),
        newBalance: user.wallet.balance
      });
      
    } catch (error: any) {
      console.error('System bet error:', error);
      return res.status(500).json({ success: false, message: error.message });
    }
});

// ==================== CHECK CASHOUT VALUE ====================
router.get('/:betId/cashout-value', authenticate, async (req: any, res: Response) => {
  try {
    const { betId } = req.params;
    const bet = await Bet.findById(betId).populate('matchId');
    
    if (!bet) {
      return res.status(404).json({ success: false, message: 'Bet not found' });
    }
    
    if (bet.userId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ success: false, message: 'Unauthorized' });
    }
    
    const match = bet.matchId as any;
    const currentMinute = match ? match.minute : 45;
    const currentLiveOdds = match ? match.liveOdds : null;
    
    const cashOutAvailable = bet.checkCashOutAvailability(currentMinute, currentLiveOdds);
    
    return res.json({
      success: true,
      betId: bet._id,
      cashOutAvailable,
      cashOutValue: bet.cashOutAmount || 0,
      originalStake: bet.stake,
      potentialWin: bet.potentialWin,
      cashoutPercentage: bet.cashOutPercentage?.toFixed(1) || '0.0'
    });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== EXECUTE CASHOUT ====================
router.post('/:betId/cashout', authenticate, async (req: any, res: Response) => {
  try {
    const { betId } = req.params;
    const bet = await Bet.findById(betId).populate('matchId');
    const user = req.user;
    
    if (!bet) {
      return res.status(404).json({ success: false, message: 'Bet not found' });
    }
    
    if (bet.userId.toString() !== user._id.toString()) {
      return res.status(403).json({ success: false, message: 'Unauthorized' });
    }
    
    if (bet.status !== BET_STATUS.PENDING && bet.status !== BET_STATUS.RUNNING) {
      return res.status(400).json({ success: false, message: 'Bet cannot be cashed out' });
    }
    
    const match = bet.matchId as any;
    const currentMinute = match ? match.minute : 45;
    const currentLiveOdds = match ? match.liveOdds : null;
    
    bet.checkCashOutAvailability(currentMinute, currentLiveOdds);
    const cashOutValue = bet.cashOutAmount || 0;
    
    if (cashOutValue <= 0) {
      return res.status(400).json({ success: false, message: 'Cashout currently unavailable for this bet' });
    }
    
    // Update balance
    const previousBalance = user.wallet.balance;
    bet.status = BET_STATUS.CASHED_OUT;
    bet.cashedOutAt = new Date();
    bet.actualWin = cashOutValue;
    
    // Calculate and apply tax
    const taxInfo = bet.calculateTax();
    const finalCredit = taxInfo.netWin;
    
    user.wallet.balance = previousBalance + finalCredit;
    user.wallet.totalWon += finalCredit;
    user.statistics.totalWins += 1;
    if (taxInfo.taxAmount > 0) {
      user.wallet.totalTaxPaid += taxInfo.taxAmount;
    }
    
    bet.statusHistory.push({
      status: BET_STATUS.CASHED_OUT,
      timestamp: new Date(),
      reason: 'User cashed out'
    });
    
    // Create transaction
    const transaction = new Transaction({
      userId: user._id,
      betId: bet._id,
      type: TRANSACTION_TYPES.BET_WIN,
      subType: 'cashout',
      amount: cashOutValue,
      taxAmount: taxInfo.taxAmount,
      netAmount: finalCredit,
      previousBalance,
      previousBonusBalance: user.wallet.bonusBalance,
      newBalance: user.wallet.balance,
      newBonusBalance: user.wallet.bonusBalance,
      status: TRANSACTION_STATUS.COMPLETED,
      completedAt: new Date(),
      metadata: { betId: bet._id, originalStake: bet.stake, taxAmount: taxInfo.taxAmount, netWin: finalCredit }
    });
    
    await Promise.all([bet.save(), user.save(), transaction.save()]);
    
    req.io?.to(`user_${user._id}`).emit('wallet_update', { balance: user.wallet.balance });
    
    await sendNotification({
      userId: user._id,
      title: 'Bet Cashed Out! 💰',
      message: `You cashed out ${cashOutValue.toFixed(2)} ETB on ${match ? match.homeTeam + ' vs ' + match.awayTeam : 'Match'}. Net win credited: ${finalCredit.toFixed(2)} ETB (Tax: ${taxInfo.taxAmount.toFixed(2)} ETB).`,
      type: 'bet_cashed_out',
      data: { betId: bet._id, amount: cashOutValue, netAmount: finalCredit }
    });
    
    return res.json({
      success: true,
      message: 'Bet cashed out successfully!',
      cashOutValue,
      netWin: finalCredit,
      taxAmount: taxInfo.taxAmount,
      newBalance: user.wallet.balance
    });
    
  } catch (error: any) {
    console.error('Cashout execution error:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET BET HISTORY ====================
router.get('/history', authenticate, async (req: any, res: Response) => {
  try {
    const { 
      status, 
      limit = '50', 
      page = '1', 
      from, 
      to, 
      marketType,
      betType,
      sortBy = 'createdAt',
      sortOrder = 'desc'
    } = req.query;
    
    const query: any = { userId: req.user._id };
    
    if (status) query.status = status;
    if (marketType) query.marketType = marketType;
    if (betType) query.betType = betType;
    if (from || to) {
      query.createdAt = {};
      if (from) query.createdAt.$gte = new Date(from as string);
      if (to) query.createdAt.$lte = new Date(to as string);
    }
    
    const limitNum = parseInt(limit as string, 10) || 50;
    const pageNum = parseInt(page as string, 10) || 1;
    const skip = (pageNum - 1) * limitNum;
    const sort = { [sortBy as string]: sortOrder === 'desc' ? -1 : 1 } as any;
    
    const [bets, total] = await Promise.all([
      Bet.find(query)
        .populate('matchId', 'homeTeam awayTeam league matchDate scores status')
        .sort(sort)
        .skip(skip)
        .limit(limitNum),
      Bet.countDocuments(query)
    ]);
    
    // Calculate statistics
    const stats = await (Bet as any).getUserBetStats(req.user._id);
    
    return res.json({
      success: true,
      bets,
      statistics: stats,
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        pages: Math.ceil(total / limitNum)
      }
    });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET LIVE BETS ====================
router.get('/live', authenticate, async (req: any, res: Response) => {
  try {
    const liveBets = await Bet.find({
      userId: req.user._id,
      status: { $in: [BET_STATUS.PENDING, BET_STATUS.RUNNING] },
      isLive: true
    }).populate('matchId', 'homeTeam awayTeam league minute scores liveOdds status');
    
    // Check cashout availability for each bet
    const betsWithCashout = await Promise.all(liveBets.map(async (bet) => {
      const match = bet.matchId as any;
      if (match && match.isLiveNow) {
        bet.checkCashOutAvailability(match.minute, match.liveOdds);
        return {
          ...bet.toJSON(),
          cashOutAvailable: bet.cashOutAvailable,
          cashOutValue: bet.cashOutAmount
        };
      }
      return bet;
    }));
    
    return res.json({ success: true, bets: betsWithCashout });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET BET STATISTICS ====================
router.get('/statistics', authenticate, async (req: any, res: Response) => {
  try {
    const userId = req.user._id;
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    
    const weekStart = new Date();
    weekStart.setDate(weekStart.getDate() - weekStart.getDay());
    weekStart.setHours(0, 0, 0, 0);
    
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    
    const [overall, today, weekly, monthly, marketStats, hourlyDistribution] = await Promise.all([
      (Bet as any).getUserBetStats(userId),
      
      Bet.aggregate([
        { $match: { userId: new mongoose.Types.ObjectId(userId.toString()), createdAt: { $gte: todayStart } } },
        { $group: { _id: null, stake: { $sum: '$stake' }, win: { $sum: '$actualWin' }, count: { $sum: 1 } } }
      ]),
      
      Bet.aggregate([
        { $match: { userId: new mongoose.Types.ObjectId(userId.toString()), createdAt: { $gte: weekStart } } },
        { $group: { _id: null, stake: { $sum: '$stake' }, win: { $sum: '$actualWin' }, count: { $sum: 1 } } }
      ]),
      
      Bet.aggregate([
        { $match: { userId: new mongoose.Types.ObjectId(userId.toString()), createdAt: { $gte: monthStart } } },
        { $group: { _id: null, stake: { $sum: '$stake' }, win: { $sum: '$actualWin' }, count: { $sum: 1 } } }
      ]),
      
      Bet.aggregate([
        { $match: { userId: new mongoose.Types.ObjectId(userId.toString()) } },
        { $group: { _id: '$marketType', count: { $sum: 1 }, stake: { $sum: '$stake' }, win: { $sum: '$actualWin' } } },
        { $sort: { count: -1 } },
        { $limit: 10 }
      ]),
      
      Bet.aggregate([
        { $match: { userId: new mongoose.Types.ObjectId(userId.toString()) } },
        { $group: { _id: { $hour: '$createdAt' }, count: { $sum: 1 }, stake: { $sum: '$stake' } } },
        { $sort: { _id: 1 } }
      ])
    ]);
    
    return res.json({
      success: true,
      overall,
      today: {
        stake: today[0]?.stake || 0,
        win: today[0]?.win || 0,
        count: today[0]?.count || 0,
        profit: (today[0]?.win || 0) - (today[0]?.stake || 0)
      },
      weekly: {
        stake: weekly[0]?.stake || 0,
        win: weekly[0]?.win || 0,
        count: weekly[0]?.count || 0,
        profit: (weekly[0]?.win || 0) - (weekly[0]?.stake || 0)
      },
      monthly: {
        stake: monthly[0]?.stake || 0,
        win: monthly[0]?.win || 0,
        count: monthly[0]?.count || 0,
        profit: (monthly[0]?.win || 0) - (monthly[0]?.stake || 0)
      },
      popularMarkets: marketStats,
      hourlyDistribution: hourlyDistribution.map((h: any) => ({ hour: h._id, bets: h.count, stake: h.stake }))
    });
    
  } catch (error: any) {
    console.error('Get statistics error:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});

export default router;
