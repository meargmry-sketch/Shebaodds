// MyBetsScreen.jsx – Betting history and active bets
import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthContext.jsx';
import { useTranslation } from './LanguageContext';
import { Filter, Calendar, ChevronDown, TrendingUp, TrendingDown, Clock, CheckCircle, XCircle, AlertCircle } from 'lucide-react';

export default function MyBetsScreen() {
  const { user } = useAuth?.() || {};
  const { t } = useTranslation?.() || { t: (key) => key };
  const [bets, setBets] = useState([]);
  const [filter, setFilter] = useState('all'); // all, active, won, lost, pending
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ total: 0, won: 0, lost: 0, pending: 0, profit: 0 });

  // Simulate fetching bets (replace with API call)
  useEffect(() => {
    if (user) {
      // Mock data – replace with axios.get('/api/bets/history')
      const mockBets = [
        {
          id: 1,
          match: 'Real Madrid vs Barcelona',
          market: '1X2',
          selection: 'Home Win',
          odds: 2.15,
          stake: 100,
          potentialWin: 215,
          status: 'won',
          profit: 115,
          placedAt: '2026-08-05T14:30:00Z',
          settledAt: '2026-08-05T16:45:00Z',
          league: 'La Liga',
        },
        {
          id: 2,
          match: 'Arsenal vs Chelsea',
          market: 'Over/Under 2.5',
          selection: 'Over 2.5',
          odds: 1.85,
          stake: 50,
          potentialWin: 92.5,
          status: 'lost',
          profit: -50,
          placedAt: '2026-08-05T12:00:00Z',
          settledAt: '2026-08-05T14:00:00Z',
          league: 'Premier League',
        },
        {
          id: 3,
          match: 'Aviator',
          market: 'Crash Game',
          selection: 'Cash Out @ 2.5x',
          odds: 2.5,
          stake: 200,
          potentialWin: 500,
          status: 'pending',
          profit: 0,
          placedAt: '2026-08-05T18:00:00Z',
          settledAt: null,
          league: 'Casino',
        },
        {
          id: 4,
          match: 'Barcelona vs Sevilla',
          market: 'Both Teams to Score',
          selection: 'Yes',
          odds: 1.65,
          stake: 75,
          potentialWin: 123.75,
          status: 'won',
          profit: 48.75,
          placedAt: '2026-08-04T20:00:00Z',
          settledAt: '2026-08-04T22:00:00Z',
          league: 'La Liga',
        },
        {
          id: 5,
          match: 'Mines',
          market: 'Mines',
          selection: '3 Mines',
          odds: 3.2,
          stake: 30,
          potentialWin: 96,
          status: 'lost',
          profit: -30,
          placedAt: '2026-08-04T15:30:00Z',
          settledAt: '2026-08-04T15:35:00Z',
          league: 'Casino',
        },
        {
          id: 6,
          match: 'Roulette',
          market: 'Color',
          selection: 'Red',
          odds: 1.9,
          stake: 10,
          potentialWin: 19,
          status: 'won',
          profit: 9,
          placedAt: '2026-08-03T22:00:00Z',
          settledAt: '2026-08-03T22:05:00Z',
          league: 'Casino',
        },
      ];
      setBets(mockBets);
      // Calculate stats
      const total = mockBets.length;
      const won = mockBets.filter(b => b.status === 'won').length;
      const lost = mockBets.filter(b => b.status === 'lost').length;
      const pending = mockBets.filter(b => b.status === 'pending').length;
      const profit = mockBets.reduce((acc, b) => acc + (b.profit || 0), 0);
      setStats({ total, won, lost, pending, profit });
      setLoading(false);
    } else {
      setLoading(false);
    }
  }, [user]);

  // Filter bets
  const filteredBets = bets.filter(bet => {
    if (filter === 'all') return true;
    return bet.status === filter;
  });

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Status badge component
  const StatusBadge = ({ status }) => {
    const config = {
      won: { label: 'Won', className: 'status-won', icon: <CheckCircle className="h-3 w-3" /> },
      lost: { label: 'Lost', className: 'status-lost', icon: <XCircle className="h-3 w-3" /> },
      pending: { label: 'Pending', className: 'status-pending', icon: <Clock className="h-3 w-3" /> },
    };
    const { label, className, icon } = config[status] || config.pending;
    return (
      <span className={`status-badge ${className}`}>
        {icon} {label}
      </span>
    );
  };

  if (loading) {
    return <div className="loading-spinner">Loading bets...</div>;
  }

  if (!user) {
    return (
      <div className="empty-state">
        <span>🔒 Please login to view your bets</span>
        <a href="/login" className="btn-primary">Login</a>
      </div>
    );
  }

  return (
    <div className="my-bets-screen">
      {/* Page header */}
      <div className="page-header">
        <h1>📋 {t('betting_history') || 'My Bets'}</h1>
        <div className="header-stats">
          <span className="stat-total">{stats.total} bets</span>
          <span className="stat-won">✅ {stats.won} won</span>
          <span className="stat-lost">❌ {stats.lost} lost</span>
          <span className="stat-pending">⏳ {stats.pending} pending</span>
          <span className={`stat-profit ${stats.profit >= 0 ? 'positive' : 'negative'}`}>
            {stats.profit >= 0 ? '📈' : '📉'} {stats.profit >= 0 ? '+' : ''}{stats.profit.toFixed(2)} ETB
          </span>
        </div>
      </div>

      {/* Filter bar */}
      <div className="filter-bar">
        <div className="filter-group">
          <Filter className="h-4 w-4 text-slate-400" />
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All
          </button>
          <button
            className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
            onClick={() => setFilter('pending')}
          >
            Pending
          </button>
          <button
            className={`filter-btn ${filter === 'won' ? 'active' : ''}`}
            onClick={() => setFilter('won')}
          >
            Won
          </button>
          <button
            className={`filter-btn ${filter === 'lost' ? 'active' : ''}`}
            onClick={() => setFilter('lost')}
          >
            Lost
          </button>
        </div>
        <div className="filter-right">
          <Calendar className="h-4 w-4 text-slate-400" />
          <select className="date-filter">
            <option value="7">Last 7 days</option>
            <option value="30">Last 30 days</option>
            <option value="90">Last 90 days</option>
            <option value="all">All time</option>
          </select>
        </div>
      </div>

      {/* Bets list */}
      {filteredBets.length === 0 ? (
        <div className="empty-state">
          <span>📭 No bets found</span>
          <p>Place your first bet and track it here!</p>
        </div>
      ) : (
        <div className="bets-list">
          {filteredBets.map((bet) => (
            <div key={bet.id} className="bet-card">
              <div className="bet-header">
                <div className="bet-league">
                  <span className="league-name">{bet.league}</span>
                  <span className="bet-id">#{bet.id}</span>
                </div>
                <StatusBadge status={bet.status} />
              </div>
              <div className="bet-match">
                <strong>{bet.match}</strong>
              </div>
              <div className="bet-details">
                <div className="detail">
                  <span>Market</span>
                  <span>{bet.market}</span>
                </div>
                <div className="detail">
                  <span>Selection</span>
                  <span>{bet.selection}</span>
                </div>
                <div className="detail">
                  <span>Odds</span>
                  <span>{bet.odds.toFixed(2)}</span>
                </div>
                <div className="detail">
                  <span>Stake</span>
                  <span>{bet.stake.toFixed(2)} ETB</span>
                </div>
                <div className="detail">
                  <span>Potential Win</span>
                  <span>{bet.potentialWin.toFixed(2)} ETB</span>
                </div>
                {bet.status !== 'pending' && (
                  <div className="detail">
                    <span>Profit</span>
                    <span className={bet.profit >= 0 ? 'positive' : 'negative'}>
                      {bet.profit >= 0 ? '+' : ''}{bet.profit.toFixed(2)} ETB
                    </span>
                  </div>
                )}
              </div>
              <div className="bet-timestamps">
                <span>Placed: {formatDate(bet.placedAt)}</span>
                {bet.settledAt && <span>Settled: {formatDate(bet.settledAt)}</span>}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination placeholder */}
      {filteredBets.length > 10 && (
        <div className="pagination">
          <button className="page-btn">Previous</button>
          <button className="page-btn active">1</button>
          <button className="page-btn">2</button>
          <button className="page-btn">3</button>
          <button className="page-btn">Next</button>
        </div>
      )}
    </div>
  );
}