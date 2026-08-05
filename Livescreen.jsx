// LiveScreen.jsx – Dedicated live betting page
import React, { useState, useEffect } from 'react';
import LiveUpcomingMatches from './LiveUpcomingMatches';
import { useTranslation } from './LanguageContext';

export default function LiveScreen() {
  const { t } = useTranslation?.() || { t: (key) => key };
  const [liveCount, setLiveCount] = useState(0);

  // Simulate counting live matches (optional)
  useEffect(() => {
    // You can fetch real count from API
    setLiveCount(12);
  }, []);

  return (
    <div className="live-screen">
      {/* Page header */}
      <div className="page-header">
        <h1>📡 {t('live_betting') || 'Live Betting'}</h1>
        <span className="live-count">
          <span className="live-dot"></span>
          {liveCount} {t('live_matches') || 'live matches'}
        </span>
      </div>

      {/* Quick filters (optional) */}
      <div className="filter-row">
        <button className="filter-btn active">All Sports</button>
        <button className="filter-btn">⚽ Football</button>
        <button className="filter-btn">🏀 Basketball</button>
        <button className="filter-btn">🎾 Tennis</button>
      </div>

      {/* Live matches feed – reusing your component */}
      <div className="matches-section">
        <LiveUpcomingMatches
          type="live"
          limit={20}
          title="🔴 LIVE NOW"
        />
      </div>

      {/* Optional: No matches fallback */}
      {liveCount === 0 && (
        <div className="empty-state">
          <span>⏳ No live matches at the moment</span>
          <p>Check back soon!</p>
        </div>
      )}
    </div>
  );
}