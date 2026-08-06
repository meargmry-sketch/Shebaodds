import React, { useState, useEffect } from "react";
import LiveUpcomingMatches from "./LiveUpcomingMatches";
import { useAuth } from "./AuthContext";
import { useTranslation } from "./LanguageContext";

export default function HomeScreen() {
  const { user } = useAuth();
  const { t } = useTranslation();

  const [balance, setBalance] = useState(0);
  const [activeCategory, setActiveCategory] = useState("all");

  useEffect(() => {
    if (user) {
      setBalance(0);
    }
  }, [user]);

  const categories = [
    { id: "all", label: "All", icon: "🏆" },
    { id: "live", label: "Live", icon: "🔴" },
    { id: "soccer", label: "Soccer", icon: "⚽" },
    { id: "basketball", label: "Basketball", icon: "🏀" },
    { id: "tennis", label: "🎾", icon: "🎾" },
    { id: "volleyball", label: "Volleyball", icon: "🏐" },
    { id: "esports", label: "Esports", icon: "🎮" },
  ];

  return (
    <div className="home-screen">
      <div className="search-bar">
        <span className="search-icon">🔍</span>

        <input
          type="text"
          className="search-input"
          placeholder={t("search_placeholder") || "Search matches..."}
        />
      </div>

      <div className="balance-card">
        <div className="balance-left">
          <div className="balance-label">
            <span>AVAILABLE BALANCE</span>
            <span className="real-time-badge">REAL-TIME</span>
          </div>

          <div className="balance-amount">
            {balance.toLocaleString()}{" "}
            <span className="currency">ETB</span>
          </div>

          <div className="security-badges">
            <span className="badge">🔒 SECURE</span>
            <span className="badge">🔒 SSL</span>
          </div>

          <div className="action-buttons">
            <button className="btn-deposit">Deposit</button>
            <button className="btn-withdraw">Withdraw</button>
          </div>
        </div>

        <div className="balance-right">
          <div className="wallet-graphic">💳</div>
        </div>
      </div>

      <div className="category-row">
        {categories.map((category) => (
          <button
            key={category.id}
            className={`category-btn ${
              activeCategory === category.id ? "active" : ""
            }`}
            onClick={() => setActiveCategory(category.id)}
          >
            <span className="cat-icon">{category.icon}</span>
            <span className="cat-label">{category.label}</span>
          </button>
        ))}
      </div>

      <div className="matches-section">
        <LiveUpcomingMatches
          type="live"
          limit={5}
          title="🔴 LIVE NOW"
        />
      </div>

      <div className="matches-section">
        <LiveUpcomingMatches
          type="upcoming"
          limit={6}
          title="⚽ UPCOMING MATCHES"
        />
      </div>

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