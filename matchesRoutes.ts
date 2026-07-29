// ============================================
// SHEBAODDS - MATCHES ROUTES
// Complete Match Data, Live Scores, Odds & Casino Games
// ============================================

import express, { Request, Response, NextFunction } from 'express';
import Match, { MATCH_STATUS, CasinoGame, CASINO_GAMES_DATA } from './Match';
import { authenticate, isAdmin } from './authMiddleware';

const router = express.Router();

// ---------- In‑memory cache ----------
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

// ============================================
// SPORTS MATCHES ROUTES
// ============================================

/**
 * GET /api/matches
 * Fetch all sports matches with filtering and pagination.
 */
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

    const cacheKey = `matches:${JSON.stringify(req.query)}`;
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

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

    cache.set(cacheKey, response, 30);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/:matchId
 * Fetch a single match by ID
 */
router.get('/:matchId', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;

    const cacheKey = `match:${matchId}`;
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const match = await Match.findById(matchId);
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }

    const response = { success: true, match };
    cache.set(cacheKey, response, 30);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/live/all
 * Fetch all live matches
 */
router.get('/live/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'live_matches';
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const matches = await Match.find({
      status: MATCH_STATUS.LIVE
    }).sort({ matchDate: -1 });

    const response = { success: true, matches };
    cache.set(cacheKey, response, 10); // short cache for live
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/upcoming/all
 * Fetch all upcoming matches (status = SCHEDULED)
 */
router.get('/upcoming/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'upcoming_matches';
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const matches = await Match.find({
      status: MATCH_STATUS.SCHEDULED,
      matchDate: { $gte: new Date() }
    }).sort({ matchDate: 1 });

    const response = { success: true, matches };
    cache.set(cacheKey, response, 60);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/featured/all
 * Fetch featured matches
 */
router.get('/featured/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'featured_matches';
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const matches = await Match.find({ isFeatured: true })
      .sort({ matchDate: 1 })
      .limit(10);

    const response = { success: true, matches };
    cache.set(cacheKey, response, 60);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/:matchId/statistics
 * Fetch match statistics (head‑to‑head, form, etc.)
 */
router.get('/:matchId/statistics', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    const match = await Match.findById(matchId).select('statistics');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    return res.json({ success: true, statistics: match.statistics || {} });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/:matchId/events
 * Fetch live events (goals, cards, substitutions)
 */
router.get('/:matchId/events', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    const match = await Match.findById(matchId).select('events');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    return res.json({ success: true, events: match.events || [] });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/:matchId/odds/live
 * Fetch live odds for a match
 */
router.get('/:matchId/odds/live', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    const match = await Match.findById(matchId).select('odds');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    return res.json({ success: true, odds: match.odds || {} });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/:matchId/odds/history
 * Fetch historical odds changes for a match
 */
router.get('/:matchId/odds/history', async (req: Request, res: Response) => {
  try {
    const { matchId } = req.params;
    const match = await Match.findById(matchId).select('oddsHistory');
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    return res.json({ success: true, history: match.oddsHistory || [] });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/matches/leagues/all
 * Fetch all unique leagues
 */
router.get('/leagues/all', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'leagues_all';
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const leagues = await Match.distinct('league');
    const response = { success: true, leagues };
    cache.set(cacheKey, response, 3600);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ============================================
// ADMIN ROUTES (Sports)
// ============================================

/**
 * POST /api/matches/admin/create
 * Admin: create a new match
 */
router.post('/admin/create', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const matchData = req.body;
    const newMatch = new Match(matchData);
    await newMatch.save();
    cache.clear(); // clear all cache
    return res.status(201).json({ success: true, match: newMatch });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * PUT /api/matches/admin/:matchId
 * Admin: update a match
 */
router.put('/admin/:matchId', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { matchId } = req.params;
    const updateData = req.body;
    const match = await Match.findByIdAndUpdate(matchId, updateData, { new: true });
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    cache.clear();
    return res.json({ success: true, match });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * DELETE /api/matches/admin/:matchId
 * Admin: delete a match
 */
router.delete('/admin/:matchId', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { matchId } = req.params;
    const match = await Match.findByIdAndDelete(matchId);
    if (!match) {
      return res.status(404).json({ success: false, message: 'Match not found' });
    }
    cache.clear();
    return res.json({ success: true });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ============================================
// CASINO GAMES ROUTES
// ============================================

/**
 * GET /api/casino/games
 * Fetch all casino games (optionally filtered by category)
 */
router.get('/casino/games', async (req: Request, res: Response) => {
  try {
    const { category } = req.query;
    const query: any = {};
    if (category) query.category = category;

    const cacheKey = `casino_games:${JSON.stringify(req.query)}`;
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const games = await CasinoGame.find(query).sort({ name: 1 });
    const response = { success: true, games };

    cache.set(cacheKey, response, 60);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/casino/games/:id
 * Fetch a single casino game by ID
 */
router.get('/casino/games/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;

    const cacheKey = `casino_game:${id}`;
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const game = await CasinoGame.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, message: 'Casino game not found' });
    }

    const response = { success: true, game };
    cache.set(cacheKey, response, 60);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * PUT /api/casino/games/:id/favorite
 * Toggle favorite status for a casino game
 */
router.put('/casino/games/:id/favorite', authenticate, async (req: any, res: Response) => {
  try {
    const { id } = req.params;
    const { isFavorite } = req.body;

    if (typeof isFavorite !== 'boolean') {
      return res.status(400).json({ success: false, message: 'isFavorite must be a boolean' });
    }

    const game = await CasinoGame.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, message: 'Casino game not found' });
    }

    game.isFavorite = isFavorite;
    await game.save();

    cache.del(`casino_game:${id}`);
    cache.del('casino_games:*');
    return res.json({ success: true, game });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/casino/stats
 * Get platform‑wide casino statistics
 */
router.get('/casino/stats', async (req: Request, res: Response) => {
  try {
    const cacheKey = 'casino_stats';
    const cached = cache.get(cacheKey);
    if (cached) return res.json(cached);

    const [totalGames, totalWagered, totalWon, mostPlayed] = await Promise.all([
      CasinoGame.countDocuments(),
      CasinoGame.aggregate([{ $group: { _id: null, total: { $sum: '$totalWagered' } } }]),
      CasinoGame.aggregate([{ $group: { _id: null, total: { $sum: '$totalWon' } } }]),
      CasinoGame.find().sort({ timesPlayed: -1 }).limit(5).select('gameId name icon timesPlayed')
    ]);

    const stats = {
      totalGames,
      totalWagered: totalWagered[0]?.total || 0,
      totalWon: totalWon[0]?.total || 0,
      mostPlayed
    };

    const response = { success: true, stats };
    cache.set(cacheKey, response, 300);
    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * POST /api/casino/admin/games/:id/stats
 * Admin: manually update a game's stats
 */
router.post('/casino/admin/games/:id/stats', authenticate, isAdmin, async (req: any, res: Response) => {
  try {
    const { id } = req.params;
    const { timesPlayed, totalWagered, totalWon } = req.body;

    const game = await CasinoGame.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, message: 'Casino game not found' });
    }

    if (timesPlayed !== undefined) game.timesPlayed = timesPlayed;
    if (totalWagered !== undefined) game.totalWagered = totalWagered;
    if (totalWon !== undefined) game.totalWon = totalWon;

    await game.save();

    cache.del(`casino_game:${id}`);
    cache.del('casino_games:*');
    cache.del('casino_stats');

    return res.json({ success: true, game });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

export default router;