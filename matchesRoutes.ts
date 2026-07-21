// ============================================
// SHEBAODDS - MATCHES ROUTES
// Complete Match Data, Live Scores, Odds
// ============================================

import express, { Request, Response, NextFunction } from 'express';
import Match, { MATCH_STATUS, CasinoGame, CASINO_GAMES_DATA } from './Match';
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

// ==================== SPORTS MATCHES ROUTES ====================

// ... (existing sports routes remain unchanged)

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

// ... (GET /:matchId, GET /live/all, GET /upcoming/all, GET /featured/all, GET /:matchId/statistics, GET /:matchId/events, GET /:matchId/odds/live, GET /:matchId/odds/history, GET /leagues/all, and the admin POST routes remain exactly as they were)
// We'll not repeat them for brevity, but they should be kept.

// ==================== NEW: CASINO GAMES ROUTES ====================

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
    if (cached) {
      return res.json(cached);
    }

    const games = await CasinoGame.find(query).sort({ name: 1 });
    const response = { success: true, games };

    // Cache for 60 seconds
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
    if (cached) {
      return res.json(cached);
    }

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
    const { isFavorite } = req.body; // boolean

    if (typeof isFavorite !== 'boolean') {
      return res.status(400).json({ success: false, message: 'isFavorite must be a boolean' });
    }

    const game = await CasinoGame.findOne({ gameId: id });
    if (!game) {
      return res.status(404).json({ success: false, message: 'Casino game not found' });
    }

    game.isFavorite = isFavorite;
    await game.save();

    // Clear cache
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
    if (cached) {
      return res.json(cached);
    }

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
    cache.set(cacheKey, response, 300); // cache 5 minutes

    return res.json(response);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * POST /api/casino/admin/games/:id/stats
 * Admin: manually update a game's stats (e.g., after a batch settlement)
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

    // Clear caches
    cache.del(`casino_game:${id}`);
    cache.del('casino_games:*');
    cache.del('casino_stats');

    return res.json({ success: true, game });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// ==================== EXPORT ====================
export default router;