// PromotionsScreen.jsx – Promotions and bonuses
import React, { useState, useEffect } from 'react';
import { useAuth } from './AuthContext.jsx';
import { useTranslation } from './LanguageContext';
import { Gift, Percent, Clock, ChevronRight, Sparkles, Fire, Star, Award, Zap, CheckCircle, XCircle } from 'lucide-react';

export default function PromotionsScreen() {
  const { user } = useAuth?.() || {};
  const { t } = useTranslation?.() || { t: (key) => key };
  const [loading, setLoading] = useState(true);
  const [claimed, setClaimed] = useState({});
  
  // Simulated promotions – replace with API call
  const promotions = [
    {
      id: 1,
      title: '🎉 Welcome Bonus',
      subtitle: '100% match up to 5,000 ETB',
      description: 'New players get a 100% bonus on their first deposit. Minimum deposit 50 ETB.',
      type: 'deposit',
      bonus: '100%',
      maxBonus: 5000,
      minDeposit: 50,
      wagering: '10x',
      expiry: '2026-09-30',
      image: '🎁',
      color: 'from-amber-500 to-orange-600',
      active: true,
    },
    {
      id: 2,
      title: '🔥 Free Bet Friday',
      subtitle: 'Get 100 ETB free bet every Friday',
      description: 'Place a bet of 200 ETB or more on any sport and get a 100 ETB free bet.',
      type: 'freebet',
      bonus: '100 ETB',
      minBet: 200,
      wagering: '1x',
      expiry: 'Ongoing',
      image: '🔥',
      color: 'from-red-500 to-rose-600',
      active: true,
    },
    {
      id: 3,
      title: '⚽ UEFA Champions League Special',
      subtitle: 'Enhanced odds on selected matches',
      description: 'Get boosted odds on all UEFA Champions League matches. Up to 2.5x your winnings.',
      type: 'odds_boost',
      bonus: 'Boosted odds',
      maxBonus: '2.5x',
      wagering: 'No wagering',
      expiry: '2026-06-01',
      image: '⚽',
      color: 'from-blue-500 to-indigo-600',
      active: true,
    },
    {
      id: 4,
      title: '🎰 Casino Cashback',
      subtitle: '10% cashback on all casino losses',
      description: 'Get 10% cashback on net losses in casino games every week. Paid every Monday.',
      type: 'cashback',
      bonus: '10%',
      maxBonus: 'Unlimited',
      minLoss: 100,
      wagering: '1x',
      expiry: 'Ongoing',
      image: '🎰',
      color: 'from-purple-500 to-violet-600',
      active: true,
    },
    {
      id: 5,
      title: '💰 Refer a Friend',
      subtitle: 'Earn 200 ETB for each friend you refer',
      description: 'Invite your friends to join SHEBAODDS. You both get 200 ETB when they deposit and bet.',
      type: 'referral',
      bonus: '200 ETB',
      maxBonus: 'Unlimited',
      wagering: '1x',
      expiry: 'Ongoing',
      image: '👥',
      color: 'from-green-500 to-emerald-600',
      active: true,
    },
    {
      id: 6,
      title: '📈 Loyalty Reward',
      subtitle: 'VIP players get exclusive bonuses',
      description: 'Earn loyalty points for every bet. Redeem points for free bets and cash.',
      type: 'loyalty',
      bonus: 'Points based',
      maxBonus: 'Varies',
      wagering: '1x',
      expiry: 'Ongoing',
      image: '👑',
      color: 'from-yellow-500 to-gold-600',
      active: true,
    },
    {
      id: 7,
      title: '🎯 Weekend Multiplier',
      subtitle: 'All winning bets get 20% extra',
      description: 'Get 20% extra winnings on all bets placed on Saturday and Sunday.',
      type: 'multiplier',
      bonus: '20% extra',
      maxBonus: 'Unlimited',
      wagering: 'No wagering',
      expiry: '2026-12-31',
      image: '📅',
      color: 'from-cyan-500 to-blue-600',
      active: true,
    },
  ];

  useEffect(() => {
    // Simulate loading
    setTimeout(() => setLoading(false), 500);
  }, []);

  // Claim promotion
  const claimPromotion = (id) => {
    if (!user) {
      alert('Please login to claim this promotion');
      return;
    }
    setClaimed(prev => ({ ...prev, [id]: true }));
    alert('✅ Promotion claimed successfully! Check your wallet.');
  };

  // Format expiry date
  const formatExpiry = (date) => {
    if (date === 'Ongoing') return 'Ongoing';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  if (loading) {
    return <div className="loading-spinner">Loading promotions...</div>;
  }

  return (
    <div className="promotions-screen">
      {/* Page header */}
      <div className="page-header">
        <h1>🎁 {t('promotions') || 'Promotions'}</h1>
        <span className="promo-count">{promotions.length} active offers</span>
      </div>

      {/* Featured banner */}
      <div className="promo-banner featured">
        <div className="banner-content">
          <div className="banner-icon">🎉</div>
          <div className="banner-text">
            <h2>Welcome to SHEBAODDS!</h2>
            <p>Get 100% up to 5,000 ETB on your first deposit</p>
            <button className="claim-btn-large" onClick={() => claimPromotion(1)}>
              Claim Now <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Promotions grid */}
      <div className="promotions-grid">
        {promotions.map((promo) => (
          <div key={promo.id} className="promo-card">
            <div className={`promo-header bg-gradient-to-r ${promo.color}`}>
              <span className="promo-icon">{promo.image}</span>
              <span className="promo-type">{promo.type.toUpperCase()}</span>
            </div>
            <div className="promo-body">
              <h3 className="promo-title">{promo.title}</h3>
              <p className="promo-subtitle">{promo.subtitle}</p>
              <p className="promo-description">{promo.description}</p>
              <div className="promo-details">
                <div className="detail-item">
                  <span className="detail-label">Bonus</span>
                  <span className="detail-value">{promo.bonus}</span>
                </div>
                {promo.maxBonus && (
                  <div className="detail-item">
                    <span className="detail-label">Max</span>
                    <span className="detail-value">{promo.maxBonus}</span>
                  </div>
                )}
                {promo.wagering && (
                  <div className="detail-item">
                    <span className="detail-label">Wagering</span>
                    <span className="detail-value">{promo.wagering}</span>
                  </div>
                )}
                <div className="detail-item">
                  <span className="detail-label">Expires</span>
                  <span className="detail-value">{formatExpiry(promo.expiry)}</span>
                </div>
              </div>
            </div>
            <div className="promo-footer">
              {claimed[promo.id] ? (
                <span className="claimed-badge">
                  <CheckCircle className="h-4 w-4" /> Claimed
                </span>
              ) : (
                <button
                  className="claim-btn"
                  onClick={() => claimPromotion(promo.id)}
                  disabled={!promo.active}
                >
                  {promo.active ? 'Claim' : 'Expired'}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Terms and conditions */}
      <div className="terms-section">
        <details>
          <summary>📋 Terms & Conditions apply</summary>
          <div className="terms-content">
            <ul>
              <li>All promotions are subject to the terms and conditions set by SHEBAODDS.</li>
              <li>Promotions are available to verified users only.</li>
              <li>Wagering requirements must be met before withdrawal of bonus funds.</li>
              <li>SHEBAODDS reserves the right to amend or cancel any promotion at any time.</li>
              <li>Promotions cannot be combined with other offers unless stated.</li>
              <li>Minimum deposit and bet requirements apply.</li>
              <li>Casino games contribute differently to wagering requirements.</li>
            </ul>
          </div>
        </details>
      </div>
    </div>
  );
}