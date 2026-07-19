// ============================================
// SHEBAODDS - MATCHES ROUTES
// Complete Match Data, Live Scores, Odds
// ============================================

import express, { Request, Response, NextFunction } from 'express';
import Match, { MATCH_STATUS } from './Match';
import { authenticate, isAdmin } from './authMiddleware';

const router = express.Router();

// Simple in-memory cache to replace Redis cleanly
class MemoryCache {
  private cache = new Map<string, { value: any; expiry: number }>();

  get(key: string): any {
    const item = this.cache.get(key);
    if (!item) return null;
    if (Date.now() > item.expiry) {
      this.cache.delete(key);
      return null;
    }
    return item.value;
  }

  set(key: string, value: any, ttlSeconds: number): void {
    this.cache.set(key, { value, expiry: Date.now() + ttlSeconds * 1000 });
  }

  del(key: string): void {
    this.cache.delete(key);
  }

  clear(): void {
    this.cache.clear();
  }
}

const cache = new MemoryCache();

// ==================== GET ALL MATCHES ====================
router.get('/', async (req: Request, res: Response) => {
  try {
    const { 
      league, 
      status, 
      featured, 
      date, 
      search,
      limit = '50', 
      page = '1',
      sortBy = 'matchDate',
      sortOrder = 'asc'
    } = req.query;
    
    const query: any = {};
    
    if (league) query.league = league;
    if (status) query.status = status;
    if (featured === 'true') query.isFeatured = true;
    if (date) {
      const startDate = new Date(date as string);
      startDate.setHours(0, 0, 0, 0);
      const endDate = new Date(date as string);
      endDate.setHours(23, 59, 59, 999);
      query.matchDate = { $gte: startDate, $lte: endDate };
    }
    if (search) {
      query.$or = [
        { homeTeam: { $regex: search as string, $options: 'i' } },
        { awayTeam: { $regex: search as string, $options: 'i' } },
        { league: { $regex: search as string, $options: 'i' } }
      ];
    }
    
    const limitNum = parseInt(limit as string, 10) || 50;
    const pageNum = parseInt(page as string, 10) || 1;
    const skip = (pageNum - 1) * limitNum;
    const sort = { [sortBy as string]: sortOrder === 'desc' ? -1 : 1 } as any;
    
    // Try cache
    const cacheKey = `matches:${JSON.stringify(req.query)}`;
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const [matches, total] = await Promise.all([
      Match.find(query)
        .select('-oddsHistory -playerProps')
        .sort(sort)
        .skip(skip)
        .limit(limitNum),
      Match.countDocuments(query)
    ]);
    
    const response = {
      success: true,
      matches,
      pagination: {
        total,
        page: pageNum,
        limit: limitNum,
        pages: Math.ceil(total / limitNum)
      }
    };
    
    // Cache for 30 seconds
    cache.set(cacheKey, response, 30);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET SINGLE MATCH ====================
router.get('/:matchId', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    
    // Try cache
    const cacheKey = `match:${matchId}`;
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const match = await Match.findOne({ matchId });
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    const response = { success: true, match };
    
    // Cache for 60 seconds
    cache.set(cacheKey, response, 60);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET LIVE MATCHES ====================
router.get('/live/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'live_matches';
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const matches = await Match.find({
      status: { $in: [MATCH_STATUS.LIVE, MATCH_STATUS.HALFTIME, MATCH_STATUS.SECOND_HALF, MATCH_STATUS.EXTRA_TIME] }
    }).select('matchId league homeTeam awayTeam homeScore awayScore minute status liveOdds statistics events');
    
    const response = { success: true, matches };
    cache.set(cacheKey, response, 5);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET UPCOMING MATCHES ====================
router.get('/upcoming/all', async (req: Request, res: Response) => {
  try {
    const { limit = '50' } = req.query;
    const limitNum = parseInt(limit as string, 10) || 50;
    
    const matches = await Match.find({
      status: MATCH_STATUS.UPCOMING,
      matchDate: { $gt: new Date() }
    })
      .sort({ matchDate: 1 })
      .limit(limitNum);
    
    return res.json({ success: true, matches });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET FEATURED MATCHES ====================
router.get('/featured/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'featured_matches';
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const matches = await Match.find({
      isFeatured: true,
      status: MATCH_STATUS.UPCOMING,
      matchDate: { $gt: new Date() }
    })
      .sort({ matchDate: 1 })
      .limit(10);
    
    const response = { success: true, matches };
    cache.set(cacheKey, response, 300);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET MATCH STATISTICS ====================
router.get('/:matchId/statistics', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    
    const match = await Match.findOne({ matchId }).select('statistics homeTeam awayTeam scores minute status');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    return res.json({ success: true, statistics: match.statistics, scores: match.scores, minute: match.minute, status: match.status });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET MATCH EVENTS ====================
router.get('/:matchId/events', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    
    const match = await Match.findOne({ matchId }).select('events homeTeam awayTeam scores minute');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    return res.json({ success: true, events: match.events, scores: match.scores, minute: match.minute });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET LIVE ODDS ====================
router.get('/:matchId/odds/live', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    
    const cacheKey = `live_odds:${matchId}`;
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const match = await Match.findOne({ matchId }).select('liveOdds prematchOdds homeTeam awayTeam scores minute status');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    const odds = {
      matchId: match.matchId,
      homeTeam: match.homeTeam,
      awayTeam: match.awayTeam,
      homeScore: match.scores.home,
      awayScore: match.scores.away,
      minute: match.minute,
      status: match.status,
      liveOdds: match.liveOdds,
      prematchOdds: match.prematchOdds
    };
    
    const response = { success: true, odds };
    cache.set(cacheKey, response, 3);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET ODDS HISTORY (for charts) ====================
router.get('/:matchId/odds/history', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    const { limit = '50' } = req.query;
    const limitNum = parseInt(limit as string, 10) || 50;
    
    const match = await Match.findOne({ matchId }).select('oddsHistory');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    const history = match.oddsHistory ? match.oddsHistory.slice(-limitNum) : [];
    
    return res.json({ success: true, history });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET LEAGUES ====================
router.get('/leagues/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'leagues';
    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }
    
    const leagues = await Match.distinct('league');
    const leaguesWithCount = await Promise.all(
      leagues.map(async (league) => ({
        name: league,
        upcomingCount: await Match.countDocuments({ league, status: MATCH_STATUS.UPCOMING, matchDate: { $gt: new Date() } }),
        liveCount: await Match.countDocuments({ league, status: { $in: [MATCH_STATUS.LIVE, MATCH_STATUS.HALFTIME, MATCH_STATUS.SECOND_HALF] } })
      }))
    );
    
    const response = { success: true, leagues: leaguesWithCount };
    cache.set(cacheKey, response, 3600);
    
    return res.json(response);
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== ADMIN: UPDATE LIVE ODDS ====================
router.post('/:matchId/odds/update', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { matchId } = req.params;
    const { odds } = req.body;
    
    const match = await Match.findOne({ matchId });
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    await match.updateLiveOdds(odds);
    
    // Broadcast to all connected clients
    req.io?.to(`match_${matchId}`).emit('odds_update', {
      matchId,
      odds: match.liveOdds,
      timestamp: new Date()
    });
    
    // Clear cache
    cache.del(`live_odds:${matchId}`);
    cache.del(`match:${matchId}`);
    
    return res.json({ success: true, liveOdds: match.liveOdds });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== ADMIN: UPDATE LIVE SCORE ====================
router.post('/:matchId/score/update', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { matchId } = req.params;
    const { homeScore, awayScore, minute } = req.body;
    
    const match = await Match.findOne({ matchId });
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    await match.updateLiveScore(homeScore, awayScore, minute);
    
    // Broadcast to all connected clients
    req.io?.to(`match_${matchId}`).emit('score_update', {
      matchId,
      homeScore: match.scores.home,
      awayScore: match.scores.away,
      minute: match.minute,
      status: match.status
    });
    
    // Clear cache
    cache.del(`match:${matchId}`);
    cache.del('live_matches');
    
    return res.json({ success: true, scores: match.scores, minute: match.minute, status: match.status });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== ADMIN: ADD MATCH EVENT ====================
router.post('/:matchId/events/add', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { matchId } = req.params;
    const event = req.body;
    
    const match = await Match.findOne({ matchId });
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    await match.addEvent(event);
    
    // Broadcast to all connected clients
    req.io?.to(`match_${matchId}`).emit('event_update', {
      matchId,
      event: match.events[match.events.length - 1],
      scores: match.scores,
      minute: match.minute
    });
    
    return res.json({ success: true, event: match.events[match.events.length - 1] });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== GET MATCH LINEUPS ====================
router.get('/:matchId/lineups', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    
    const match = await Match.findOne({ matchId }).select('lineups homeTeam awayTeam');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    
    return res.json({ success: true, lineups: match.lineups });
    
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

export default router;
