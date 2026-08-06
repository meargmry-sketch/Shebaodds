// HomeScreen.jsx – Main sportsbook landing page
import React, { useState, useEffect } from 'react';
import LiveUpcomingMatches from './LiveUpcomingMatches';
import { useAuth } from './contexts'; // adjust if you have a different path
import { useTranslation } from './LanguageContext';

export default function HomeScreen() {
  const { user } = useAuth?.() || {};
  const { t } = useTranslation?.() || { t: (key) => key };
  const [balance, setBalance] = useState(0);
  const [activeCategory, setActiveCategory] = useState('all');

  // Fetch balance (simulated – replace with real data)
useEffect(() => {
  if (user) {
    // Example: fetch from API
    setBalance(0);
  }
}, [user]);

  // Categories
  const categories = [
    { id: 'all', label: 'All', icon: '🏆' },
    { id: 'live', label: 'Live', icon: '🔴' },
    { id: 'soccer', label: 'Soccer', icon: '⚽' },
    { id: 'basketball', label: 'Basketball', icon: '🏀' },
    { id: 'tennis', label: 'Tennis', icon: '🎾' },
    { id: 'volleyball', label: 'Volleyball', icon: '🏐' },
    { id: 'esports', label: 'Esports', icon: '🎮' },
  ];

  return (
    <div className="home-screen">
      {/* Search Bar – optional if header already has one, but included for design consistency */}
      <div className="search-bar">
        <span className="search-icon">🔍</span>
        <input
          type="text"
          placeholder={t('search_placeholder') || "Search matches, leagues, teams..."}
          className="search-input"
        />
      </div>

      {/* Balance Card */}
      <div className="balance-card">
        <div className="balance-left">
          <div className="balance-label">
            <span>AVAILABLE BALANCE</span>
            <span className="real-time-badge">REAL‑TIME</span>
          </div>
          <div className="balance-amount">
            {balance.toLocaleString()} <span className="currency">ETB</span>
          </div>
          <div className="security-badges">
            <span className="badge">🔒 SECURE LEDGER v2</span>
            <span className="badge">🔒 SSL ENCRYPTED</span>
          </div>
          <div className="action-buttons">
            <button className="btn-deposit">DEPOSIT FUNDS</button>
            <button className="btn-withdraw">WITHDRAW</button>
          </div>
        </div>
        <div className="balance-right">
          <div className="wallet-graphic">💳</div>
        </div>
      </div>

      {/* Category Row */}
      <div className="category-row">
        {categories.map((cat) => (
          <button
            key={cat.id}
            className={`category-btn ${activeCategory === cat.id ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat.id)}
          >
            <span className="cat-icon">{cat.icon}</span>
            <span className="cat-label">{cat.label}</span>
          </button>
        ))}
      </div>

      {/* Live Matches Section */}
      <div className="matches-section">
        <LiveUpcomingMatches
          type="live"        // if your component accepts a type prop
          limit={5}
          title="🔴 LIVE NOW"
        />
      </div>

      {/* Upcoming Matches Section */}
      <div className="matches-section">
        <LiveUpcomingMatches
          type="upcoming"    // if your component accepts a type prop
          limit={6}
          title="⚽ UPCOMING MATCHES"
        />
      </div>

      {/* Promo Banner (optional) */}
      <div className="promo-banner">
        <div className="promo-content">
          <span>🎉</span>
          <span>Get 100 ETB Welcome Bonus!</span>
          <button className="claim-btn">Claim Now</button>
        </div>
      </div>
    </div>
  );
}