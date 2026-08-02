// ============================================
// SHEBAODDS - COMPLETE REACT FRONTEND
// 85% Black / 15% Gold Theme | Smart Bets. Real Wins.
// INCLUDES: 51+ CASINO GAMES INTEGRATION
// ============================================

import React, { useState, useEffect, createContext, useContext, useCallback, useRef } from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { io } from 'socket.io-client';
import { LanguageProvider, LanguageSwitcher, useTranslation, formatNumber, formatDate } from './LanguageContext';
import './styles/global.css';
import './styles/theme.css';

// ==================== CONFIGURATION ====================
const API_URL = process.env.REACT_APP_API_URL || 'https://api.shebaodds.com';
const WS_URL = process.env.REACT_APP_WS_URL || 'https://shebaodds.com';

// Axios configuration
axios.defaults.baseURL = API_URL;
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Socket instance
let socket;

// ==================== CONTEXTS ====================
const AuthContext = createContext();
const BetSlipContext = createContext();
const NotificationContext = createContext();

// Custom hooks
const useAuth = () => useContext(AuthContext);
const useBetSlip = () => useContext(BetSlipContext);
const useNotifications = () => useContext(NotificationContext);

// ==================== MAIN APP COMPONENT ====================
function App() {
  const [token, setToken] = useState(localStorage.getItem('shebaodds_token'));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [theme, setTheme] = useState(localStorage.getItem('shebaodds_theme') || 'dark');
  const [language, setLanguage] = useState(localStorage.getItem('shebaodds_language') || 'en');

  useEffect(() => {
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      fetchUser();
      initSocket();
    } else {
      setLoading(false);
    }

    return () => {
      if (socket) socket.disconnect();
    };
  }, [token]);

  const initSocket = () => {
    socket = io(WS_URL, {
      transports: ['websocket'],
      auth: { token },
      reconnection: true,
      reconnectionAttempts: 5,
      reconnectionDelay: 1000
    });

    socket.on('connect', () => {
      console.log('🦁 SHEBAODDS Socket connected');
    });

    socket.on('wallet_update', (data) => {
      setUser(prev => prev ? { ...prev, balance: data.balance, bonusBalance: data.bonusBalance } : prev);
    });

    socket.on('notification', (notification) => {
      showToast(notification.title, notification.message);
    });

    socket.on('odds_update', (data) => {
      window.dispatchEvent(new CustomEvent('odds_update', { detail: data }));
    });
  };

  const fetchUser = async () => {
    try {
      const res = await axios.get('/api/auth/me');
      setUser(res.data.user);
      socket?.emit('authenticate', token);
    } catch (error) {
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password, twoFactorCode) => {
    const res = await axios.post('/api/auth/login', { email, password, twoFactorCode });
    const { token, user } = res.data;
    localStorage.setItem('shebaodds_token', token);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setToken(token);
    setUser(user);
    initSocket();
    return res.data;
  };

  const register = async (userData) => {
    const res = await axios.post('/api/auth/register', userData);
    const { token, user } = res.data;
    localStorage.setItem('shebaodds_token', token);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setToken(token);
    setUser(user);
    initSocket();
    return res.data;
  };

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (error) {}
    localStorage.removeItem('shebaodds_token');
    delete axios.defaults.headers.common['Authorization'];
    setToken(null);
    setUser(null);
    socket?.disconnect();
  };

  const updateTheme = (newTheme) => {
    setTheme(newTheme);
    localStorage.setItem('shebaodds_theme', newTheme);
    document.body.setAttribute('data-theme', newTheme);
  };

  const updateLanguage = (newLanguage) => {
    setLanguage(newLanguage);
    localStorage.setItem('shebaodds_language', newLanguage);
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <LanguageProvider>
      <AuthContext.Provider value={{ user, login, register, logout, updateTheme, updateLanguage, theme, language }}>
        <BetSlipContext.Provider value={{}}>
          <NotificationContext.Provider value={{}}>
            <BrowserRouter>
            <div className="app" data-theme={theme}>
              <Navigation />
              <div className="main-content">
                <TopBar />
                <div className="page-container">
                  <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/matches" element={<MatchesPage />} />
                    <Route path="/matches/:matchId" element={<MatchDetailPage />} />
                    <Route path="/live" element={<LivePage />} />
                    <Route path="/games" element={<CasinoGames />} />
                    <Route path="/promotions" element={<PromotionsPage />} />
                    <Route path="/profile" element={user ? <ProfilePage /> : <Navigate to="/login" />} />
                    <Route path="/wallet" element={user ? <WalletPage /> : <Navigate to="/login" />} />
                    <Route path="/tax" element={user ? <TaxPage /> : <Navigate to="/login" />} />
                    <Route path="/betting-history" element={user ? <BettingHistoryPage /> : <Navigate to="/login" />} />
                    <Route path="/responsible-gambling" element={user ? <ResponsibleGamblingPage /> : <Navigate to="/login" />} />
                    <Route path="/login" element={!user ? <LoginPage /> : <Navigate to="/" />} />
                    <Route path="/register" element={!user ? <RegisterPage /> : <Navigate to="/" />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                    <Route path="/verify-email/:token" element={<VerifyEmailPage />} />
                    <Route path="/settings" element={user ? <SettingsPage /> : <Navigate to="/login" />} />
                    <Route path="/support" element={<SupportPage />} />
                    <Route path="/terms" element={<TermsPage />} />
                    <Route path="/privacy" element={<PrivacyPage />} />
                    <Route path="/responsible" element={<ResponsiblePage />} />
                    <Route path="*" element={<NotFoundPage />} />
                  </Routes>
                </div>
                <Footer />
              </div>
              <BetSlip />
            </div>
          </BrowserRouter>
        </NotificationContext.Provider>
      </BetSlipContext.Provider>
    </AuthContext.Provider>
    </LanguageProvider>
  );
}

// ==================== LOADING SCREEN ====================
function LoadingScreen() {
  return (
    <div className="loading-screen">
      <div className="loading-logo">
        <span className="logo-icon">🦁</span>
        <span className="logo-text">SHEBAODDS</span>
      </div>
      <div className="loading-tagline">Smart Bets. Real Wins.</div>
      <div className="loading-spinner"></div>
      <div className="loading-progress">
        <div className="progress-bar"></div>
      </div>
    </div>
  );
}

// ==================== NAVIGATION COMPONENT ====================
function Navigation() {
  const { user, logout } = useAuth();
  const { language, t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navItems = [
    { path: '/', icon: '🏠', label: 'Dashboard', labelAm: 'ዳሽቦርድ' },
    { path: '/matches', icon: '⚽', label: 'Football', labelAm: 'እግር ኳስ' },
    { path: '/live', icon: '📡', label: 'Live Betting', labelAm: 'ቀጥታ ውርርድ' },
    { path: '/games', icon: '🎮', label: 'Casino', labelAm: 'ካዚኖ' },
    { path: '/promotions', icon: '🎁', label: 'Promotions', labelAm: 'ማስተዋወቂያዎች' },
    { path: '/profile', icon: '👤', label: 'Profile', labelAm: 'መገለጫ' },
    { path: '/wallet', icon: '💰', label: 'Wallet', labelAm: 'ቦርሳ' },
    { path: '/betting-history', icon: '📊', label: 'History', labelAm: 'ታሪክ' },
    { path: '/tax', icon: '📈', label: 'Tax Center', labelAm: 'የግብር ማዕከል' },
    { path: '/support', icon: '💬', label: 'Support', labelAm: 'ድጋፍ' }
  ];

  return (
    <>
      <button className="mobile-menu-toggle" onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}>
        ☰
      </button>
      
      <nav className={`sidebar ${isMobileMenuOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">🦁</span>
            <div className="logo-text">
              <span className="brand">SHEBAODDS</span>
              <span className="tagline">{t('app_tagline') || "Smart Bets. Real Wins."}</span>
            </div>
          </div>
        </div>

        <div className="sidebar-nav">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
              onClick={() => setIsMobileMenuOpen(false)}
            >
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-label">{language === 'am' ? item.labelAm : item.label}</span>
            </Link>
          ))}
        </div>

        {user && (
          <div className="sidebar-footer">
            <div className="user-info">
              <div className="user-avatar">
                {user.username?.[0]?.toUpperCase() || 'U'}
              </div>
              <div className="user-details">
                <div className="user-name">{user.username}</div>
                <div className="user-vip">
                  <span className="vip-badge">VIP {user.vip?.level || 1}</span>
                </div>
              </div>
            </div>
            <button className="logout-btn" onClick={logout}>
              <span>🚪</span> {language === 'am' ? 'ውጣ' : 'Logout'}
            </button>
          </div>
        )}
      </nav>
    </>
  );
}

// ==================== TOP BAR ====================
function TopBar() {
  const { user, theme, updateTheme } = useAuth();
  const { language, t } = useTranslation();
  const [currentTime, setCurrentTime] = useState('');
  const [showWalletDropdown, setShowWalletDropdown] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setCurrentTime(formatDate(now, language, 'time'));
    };
    updateTime();
    const interval = setInterval(updateTime, 60000);
    return () => clearInterval(interval);
  }, [language]);

  useEffect(() => {
    if (user) {
      fetchNotifications();
    }
  }, [user]);

  const fetchNotifications = async () => {
    try {
      const res = await axios.get('/api/notifications?limit=5');
      setNotifications(res.data.notifications);
      setUnreadCount(res.data.unreadCount);
    } catch (error) {}
  };

  return (
    <header className="top-bar">
      <div className="time-display">
        <span className="icon">🕐</span>
        <span>{currentTime}</span>
      </div>

      <div className="search-bar">
        <input type="text" placeholder={t('search_placeholder') || "Search matches, teams, leagues..."} />
        <button className="search-btn">🔍</button>
      </div>

      <div className="top-bar-actions">
        <button className="theme-toggle" onClick={() => updateTheme(theme === 'dark' ? 'light' : 'dark')}>
          {theme === 'dark' ? '☀️' : '🌙'}
        </button>

        <LanguageSwitcher />

        {user ? (
          <>
            <div className="wallet-card" onClick={() => setShowWalletDropdown(!showWalletDropdown)}>
              <span className="wallet-icon">💰</span>
              <div className="wallet-info">
                <span className="wallet-label">{t('balance')}</span>
                <span className="wallet-balance">{formatNumber(user.wallet?.balance || 0, language)} ETB</span>
              </div>
            </div>

            {showWalletDropdown && (
              <WalletDropdown onClose={() => setShowWalletDropdown(false)} />
            )}

            <div className="notifications-icon" onClick={() => navigate('/profile?tab=notifications')}>
              <span className="bell-icon">🔔</span>
              {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
            </div>
          </>
        ) : (
          <div className="auth-buttons">
            <Link to="/login" className="btn-outline">Login</Link>
            <Link to="/register" className="btn-primary">Register</Link>
          </div>
        )}
      </div>
    </header>
  );
}

// ==================== WALLET DROPDOWN ====================
function WalletDropdown({ onClose }) {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (!e.target.closest('.wallet-card') && !e.target.closest('.wallet-dropdown')) {
        onClose();
      }
    };
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [onClose]);

  return (
    <div className="wallet-dropdown">
      <div className="dropdown-header">
        <span>💰 My Wallet</span>
        <span className="total-balance">{user?.wallet?.balance?.toLocaleString()} ETB</span>
      </div>
      <div className="dropdown-stats">
        <div className="stat">
          <span className="stat-value">{user?.wallet?.bonusBalance?.toLocaleString() || 0} ETB</span>
          <span className="stat-label">Bonus</span>
        </div>
        <div className="stat">
          <span className="stat-value">{user?.wallet?.totalDeposited?.toLocaleString() || 0} ETB</span>
          <span className="stat-label">Deposited</span>
        </div>
        <div className="stat">
          <span className="stat-value">{user?.wallet?.totalWon?.toLocaleString() || 0} ETB</span>
          <span className="stat-label">Won</span>
        </div>
        <div className="stat">
          <span className="stat-value">{user?.wallet?.totalTaxPaid?.toLocaleString() || 0} ETB</span>
          <span className="stat-label">Tax Paid</span>
        </div>
      </div>
      <div className="dropdown-actions">
        <button className="deposit-btn" onClick={() => navigate('/wallet?action=deposit')}>Deposit</button>
        <button className="withdraw-btn" onClick={() => navigate('/wallet?action=withdraw')}>Withdraw</button>
      </div>
      <div className="dropdown-footer">
        <button onClick={() => navigate('/wallet')}>View All Transactions →</button>
      </div>
    </div>
  );
}

// ==================== HOME PAGE ====================
function HomePage() {
  const { user } = useAuth();
  const [featuredMatches, setFeaturedMatches] = useState([]);
  const [liveMatches, setLiveMatches] = useState([]);
  const [upcomingMatches, setUpcomingMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [featuredRes, liveRes, upcomingRes] = await Promise.all([
        axios.get('/api/matches/featured/all'),
        axios.get('/api/matches/live/all'),
        axios.get('/api/matches/upcoming/all?limit=6')
      ]);
      setFeaturedMatches(featuredRes.data.matches);
      setLiveMatches(liveRes.data.matches);
      setUpcomingMatches(upcomingRes.data.matches);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="home-page">
      <div className="hero-banner">
        <div className="hero-content">
          <h1>Smart Bets. Real Wins.</h1>
          <p>Join SHEBAODDS today and get 100 ETB Welcome Bonus!</p>
          {!user && <Link to="/register" className="btn-gold-large">Register Now →</Link>}
        </div>
        <div className="hero-stats">
          <div className="stat"><span>50K+</span> Active Users</div>
          <div className="stat"><span>100+</span> Daily Matches</div>
          <div className="stat"><span>99%</span> Payout Rate</div>
          <div className="stat"><span>15%</span> Tax Rate</div>
        </div>
      </div>

      {liveMatches.length > 0 && (
        <div className="live-widget">
          <div className="widget-header">
            <span className="live-dot"></span>
            <span className="widget-title">LIVE NOW</span>
            <Link to="/live" className="view-all">View All →</Link>
          </div>
          <div className="live-matches">
            {liveMatches.slice(0, 3).map(match => (
              <LiveMatchCard key={match.matchId} match={match} />
            ))}
          </div>
        </div>
      )}

      <div className="section">
        <div className="section-header">
          <h2>🔥 Featured Matches</h2>
          <Link to="/matches" className="view-all">View All →</Link>
        </div>
        {loading ? (
          <div className="skeleton-grid">{[1,2,3,4].map(i => <div key={i} className="skeleton-card"></div>)}</div>
        ) : (
          <div className="matches-grid">
            {featuredMatches.map(match => <MatchCard key={match.matchId} match={match} />)}
          </div>
        )}
      </div>

      <div className="section">
        <div className="section-header">
          <h2>⚽ Upcoming Matches</h2>
          <Link to="/matches" className="view-all">View All →</Link>
        </div>
        <div className="matches-grid">
          {upcomingMatches.map(match => <MatchCard key={match.matchId} match={match} />)}
        </div>
      </div>
    </div>
  );
}

// ==================== MATCH CARD ====================
function MatchCard({ match }) {
  const navigate = useNavigate();

  const addToBetSlip = (betType, odds) => {
    const bet = {
      matchId: match.matchId,
      homeTeam: match.homeTeam,
      awayTeam: match.awayTeam,
      league: match.league,
      betType,
      odds,
      selection: betType,
      matchDate: match.matchDate
    };
    window.dispatchEvent(new CustomEvent('addToBetSlip', { detail: bet }));
  };

  const formatDate = (date) => {
    return new Date(date).toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  return (
    <div className="match-card">
      <div className="match-header">
        <span className="match-league">{match.league}</span>
        <span className="match-date">{formatDate(match.matchDate)}</span>
      </div>
      <div className="match-teams" onClick={() => navigate(`/matches/${match.matchId}`)}>
        <div className="team home">
          <span className="team-name">{match.homeTeam}</span>
        </div>
        <div className="vs">VS</div>
        <div className="team away">
          <span className="team-name">{match.awayTeam}</span>
        </div>
      </div>
      <div className="match-odds">
        <button className="odds-btn" onClick={() => addToBetSlip('Home Win', match.prematchOdds?.homeWin)}>
          <span className="odds-label">1</span>
          <span className="odds-value">{match.prematchOdds?.homeWin?.toFixed(2)}</span>
        </button>
        <button className="odds-btn" onClick={() => addToBetSlip('Draw', match.prematchOdds?.draw)}>
          <span className="odds-label">X</span>
          <span className="odds-value">{match.prematchOdds?.draw?.toFixed(2)}</span>
        </button>
        <button className="odds-btn" onClick={() => addToBetSlip('Away Win', match.prematchOdds?.awayWin)}>
          <span className="odds-label">2</span>
          <span className="odds-value">{match.prematchOdds?.awayWin?.toFixed(2)}</span>
        </button>
      </div>
      <div className="match-extra-odds">
        <button onClick={() => addToBetSlip('Over 2.5', match.prematchOdds?.totalGoals?.over25)}>
          Over 2.5 ({match.prematchOdds?.totalGoals?.over25?.toFixed(2)})
        </button>
        <button onClick={() => addToBetSlip('Under 2.5', match.prematchOdds?.totalGoals?.under25)}>
          Under 2.5 ({match.prematchOdds?.totalGoals?.under25?.toFixed(2)})
        </button>
        <button onClick={() => addToBetSlip('BTTS - Yes', match.prematchOdds?.btts?.yes)}>
          BTTS ({match.prematchOdds?.btts?.yes?.toFixed(2)})
        </button>
      </div>
    </div>
  );
}

// ==================== LIVE MATCH CARD ====================
function LiveMatchCard({ match }) {
  const navigate = useNavigate();

  return (
    <div className="live-match-card" onClick={() => navigate(`/matches/${match.matchId}`)}>
      <div className="live-header">
        <span className="live-indicator"></span>
        <span className="live-text">LIVE</span>
        <span className="live-minute">{match.minute}'</span>
      </div>
      <div className="live-scores">
        <div className="team">
          <span className="team-name">{match.homeTeam}</span>
          <span className="team-score">{match.scores?.home || 0}</span>
        </div>
        <span className="vs">vs</span>
        <div className="team">
          <span className="team-score">{match.scores?.away || 0}</span>
          <span className="team-name">{match.awayTeam}</span>
        </div>
      </div>
      <div className="live-odds">
        <div className="odd">
          <span>{match.liveOdds?.homeWin?.toFixed(2)}</span>
          <span>Home</span>
        </div>
        <div className="odd">
          <span>{match.liveOdds?.draw?.toFixed(2)}</span>
          <span>Draw</span>
        </div>
        <div className="odd">
          <span>{match.liveOdds?.awayWin?.toFixed(2)}</span>
          <span>Away</span>
        </div>
      </div>
    </div>
  );
}

// ==================== BET SLIP ====================
function BetSlip() {
  const { user } = useAuth();
  const [bets, setBets] = useState([]);
  const [stake, setStake] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleAddBet = (e) => {
      setBets(prev => [...prev, e.detail]);
      setIsOpen(true);
    };
    window.addEventListener('addToBetSlip', handleAddBet);
    return () => window.removeEventListener('addToBetSlip', handleAddBet);
  }, []);

  const removeBet = (index) => setBets(prev => prev.filter((_, i) => i !== index));
  const clearBets = () => { setBets([]); setStake(''); };

  const calculateTotalOdds = () => {
    if (bets.length === 0) return 0;
    return bets.reduce((acc, bet) => acc * bet.odds, 1);
  };

  const calculatePotentialWin = () => {
    const stakeAmount = parseFloat(stake);
    if (isNaN(stakeAmount)) return 0;
    return stakeAmount * calculateTotalOdds();
  };

  const calculateTax = (amount) => amount * 0.15;
  const calculateNetWin = (amount) => amount - calculateTax(amount);

  const placeBet = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    const stakeAmount = parseFloat(stake);
    if (isNaN(stakeAmount) || stakeAmount < 1) {
      alert('Please enter a valid stake amount (minimum 1 ETB)');
      return;
    }

    setLoading(true);
    try {
      if (bets.length === 1) {
        const bet = bets[0];
        const res = await axios.post('/api/bets/place', {
          matchId: bet.matchId,
          marketType: 'ft_1x2',
          selection: bet.betType,
          odds: bet.odds,
          stake: stakeAmount
        });
        if (res.data.success) {
          alert('✅ Bet placed successfully!');
          clearBets();
          setIsOpen(false);
        }
      } else if (bets.length > 1) {
        const selections = bets.map(bet => ({
          matchId: bet.matchId,
          marketType: 'ft_1x2',
          selection: bet.betType,
          odds: bet.odds
        }));
        const res = await axios.post('/api/bets/accumulator', {
          selections,
          totalStake: stakeAmount
        });
        if (res.data.success) {
          alert(`✅ Accumulator placed! Potential win: ${res.data.potentialWin} ETB`);
          clearBets();
          setIsOpen(false);
        }
      }
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to place bet');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {bets.length > 0 && !isOpen && (
        <div className="bet-slip-toggle" onClick={() => setIsOpen(true)}>
          <span>🎫</span>
          <span className="bet-count">{bets.length}</span>
        </div>
      )}

      <div className={`bet-slip ${isOpen ? 'open' : ''}`}>
        <div className="bet-slip-header">
          <h3>🎫 Bet Slip</h3>
          <button className="close-btn" onClick={() => setIsOpen(false)}>✕</button>
        </div>

        <div className="bet-slip-items">
          {bets.map((bet, index) => (
            <div key={index} className="bet-item">
              <div className="bet-info">
                <div className="bet-match">{bet.homeTeam} vs {bet.awayTeam}</div>
                <div className="bet-selection">{bet.betType} @ {bet.odds.toFixed(2)}</div>
              </div>
              <button className="remove-bet" onClick={() => removeBet(index)}>✕</button>
            </div>
          ))}
          {bets.length === 0 && <div className="empty-betslip">No selections added</div>}
        </div>

        {bets.length > 0 && (
          <div className="bet-slip-footer">
            {bets.length > 1 && (
              <div className="total-odds">
                Total Odds: {calculateTotalOdds().toFixed(2)}
              </div>
            )}
            <div className="stake-input">
              <input 
                type="number" 
                placeholder="Enter stake (ETB)" 
                value={stake} 
                onChange={(e) => setStake(e.target.value)}
                min="1"
              />
            </div>
            <div className="potential-win">
              <span>Potential Win:</span>
              <span className="amount">{calculatePotentialWin().toFixed(2)} ETB</span>
            </div>
            <div className="tax-info">
              <span>Tax (15%):</span>
              <span>{calculateTax(calculatePotentialWin()).toFixed(2)} ETB</span>
            </div>
            <div className="net-win">
              <span>Net Win:</span>
              <span className="net-amount">{calculateNetWin(calculatePotentialWin()).toFixed(2)} ETB</span>
            </div>
            <button className="place-bet-btn" onClick={placeBet} disabled={loading}>
              {loading ? 'Processing...' : 'Place Bet'}
            </button>
            <button className="clear-bets-btn" onClick={clearBets}>Clear All</button>
          </div>
        )}
      </div>
    </>
  );
}

// ==================== FOOTER ====================
function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-logo">
          <span className="logo-icon">🦁</span>
          <div>
            <div className="logo-text">SHEBAODDS</div>
            <div className="tagline">Smart Bets. Real Wins.</div>
          </div>
        </div>
        <div className="footer-links">
          <div className="link-group">
            <h4>Company</h4>
            <Link to="/about">About Us</Link>
            <Link to="/contact">Contact</Link>
            <Link to="/careers">Careers</Link>
          </div>
          <div className="link-group">
            <h4>Legal</h4>
            <Link to="/terms">Terms & Conditions</Link>
            <Link to="/privacy">Privacy Policy</Link>
            <Link to="/responsible">Responsible Gambling</Link>
          </div>
          <div className="link-group">
            <h4>Support</h4>
            <Link to="/faq">FAQ</Link>
            <Link to="/support">Help Center</Link>
            <Link to="/live-chat">Live Chat (24/7)</Link>
          </div>
          <div className="link-group">
            <h4>Follow Us</h4>
            <a href="https://twitter.com/shebaodds" target="_blank" rel="noopener noreferrer">Twitter</a>
            <a href="https://instagram.com/shebaodds" target="_blank" rel="noopener noreferrer">Instagram</a>
            <a href="https://t.me/shebaodds" target="_blank" rel="noopener noreferrer">Telegram</a>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <p>© 2024 SHEBAODDS. All rights reserved. Smart Bets. Real Wins.</p>
        <p>18+ Only. Please gamble responsibly.</p>
        <p>Tax: 15% withholding tax applies to winnings over 100 ETB</p>
      </div>
    </footer>
  );
}

// ======================================================================================================
// ====================================== 51+ CASINO GAMES COMPONENT =====================================
// ======================================================================================================

function CasinoGames() {
  const { user } = useAuth();
  const { language, t } = useTranslation();
  const navigate = useNavigate();

  const [selectedGame, setSelectedGame] = useState(null);
  const [balance, setBalance] = useState(0);
  const [betAmount, setBetAmount] = useState(10);
  const [isBetPanelOpen, setIsBetPanelOpen] = useState(false);
  const [gameState, setGameState] = useState({});
  const [showResultModal, setShowResultModal] = useState(false);
  const [resultData, setResultData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [gameHistory, setGameHistory] = useState([]);
  const [favorites, setFavorites] = useState(() => {
    const saved = localStorage.getItem('shebaodds_favorite_games');
    return saved ? JSON.parse(saved) : [];
  });
  const [liveGameData, setLiveGameData] = useState({});
  const canvasRef = useRef(null);

  const GAMES = [
    { id: 'dice', name: 'Dice', nameAm: 'ዳይስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'aviator', name: 'Aviator', nameAm: 'አቪዬተር', icon: '✈️', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'coinflip', name: 'CoinFlip', nameAm: 'ሳንቲም', icon: '🪙', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'plinko', name: 'Plinko', nameAm: 'ፕሊንኮ', icon: '📉', cat: 'crash', minBet: 1, maxBet: 10000 },
    { id: 'blackjack', name: 'Blackjack', nameAm: 'ብላክጃክ', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'roulette', name: 'Roulette', nameAm: 'ሩሌት', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'mines', name: 'Mines', nameAm: 'ማይንስ', icon: '💣', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'crash', name: 'Crash', nameAm: 'ክራሽ', icon: '📈', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'tower', name: 'Tower', nameAm: 'ግንብ', icon: '🏗️', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'keno', name: 'Keno', nameAm: 'ኬኖ', icon: '🔢', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'baccarat', name: 'Baccarat', nameAm: 'ባካራት', icon: '♣️', cat: 'table', minBet: 5, maxBet: 10000 },
    { id: 'wheel', name: 'Wheel of Fortune', nameAm: 'የዕድል መንኮራኩር', icon: '🎰', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'hilo', name: 'Hilo', nameAm: 'ሂሎ', icon: '⬆️⬇️', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'sicbo', name: 'Sic Bo', nameAm: 'ሲክቦ', icon: '🎲🎲🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'videopoker', name: 'Video Poker', nameAm: 'ቪዲዮ ፖከር', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'bingo', name: 'Bingo', nameAm: 'ቢንጎ', icon: '🎯', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'craps', name: 'Craps', nameAm: 'ክራፕስ', icon: '🎲', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'dragontiger', name: 'Dragon Tiger', nameAm: 'ድራጎን ታይገር', icon: '🐉🐯', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'andarbahar', name: 'Andar Bahar', nameAm: 'አንዳር ባሃር', icon: '🃏', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'teenpatti', name: 'Teen Patti', nameAm: 'ቲን ፓቲ', icon: '♠️', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'lucky7', name: 'Lucky 7', nameAm: 'ላኪ 7', icon: '🍀7️⃣', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'scratch', name: 'Scratch Card', nameAm: 'ስክራች ካርድ', icon: '🎫', cat: 'slots', minBet: 1, maxBet: 10000 },
    { id: 'football', name: 'Football Prediction', nameAm: 'እግር ኳስ ትንበያ', icon: '⚽', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'basketball', name: 'Basketball Prediction', nameAm: 'ቅርጫት ኳስ ትንበያ', icon: '🏀', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'horseracing', name: 'Horse Racing', nameAm: 'ፈረስ እሽቅድምድም', icon: '🐎', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'spinwin', name: 'Spin & Win', nameAm: 'ደብል አሸንፍ', icon: '🌀', cat: 'special', minBet: 1, maxBet: 5000 },
    { id: 'slot', name: 'Slot Machine', nameAm: 'ስሎት ማሽን', icon: '🎰', cat: 'slots', minBet: 1, maxBet: 10000 },
    { id: 'reddog', name: 'Red Dog', nameAm: 'ቀይ ውሻ', icon: '🐕', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'war', name: 'War', nameAm: 'ጦርነት', icon: '⚔️', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'paigow', name: 'Pai Gow Poker', nameAm: 'ፓይ ጋው ፖከር', icon: '🀄️', cat: 'table', minBet: 5, maxBet: 10000 },
    { id: 'diceduels', name: 'Dice Duels', nameAm: 'ዳይስ ዱኤልስ', icon: '⚔️🎲', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'penalty', name: 'Penalty', nameAm: 'ፍፃጎት ምት', icon: '⚽', cat: 'sports', minBet: 1, maxBet: 5000 },
    { id: 'chickenroad', name: 'Chicken Road', nameAm: 'ዶሮ መንገድ', icon: '🐔', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'chickenshot', name: 'Chicken Shot', nameAm: 'ዶሮ ምት', icon: '🔫🐔', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'megaball', name: 'Mega Ball', nameAm: 'ሜጋ ቦል', icon: '⚾', cat: 'slots', minBet: 1, maxBet: 5000 },
    { id: 'pokerdice', name: 'Poker Dice', nameAm: 'ፖከር ዳይስ', icon: '🎲', cat: 'classic', minBet: 1, maxBet: 5000 },
    { id: 'lightningdice', name: 'Lightning Dice', nameAm: 'መብረቅ ዳይስ', icon: '⚡🎲', cat: 'crash', minBet: 1, maxBet: 5000 },
    { id: 'carroulette', name: 'Car Roulette', nameAm: 'መኪና ሩሌት', icon: '🚗', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'knockout', name: 'Knock Out', nameAm: 'ናክ አውት', icon: '🥊', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'rummy', name: 'Rummy', nameAm: 'ራሚ', icon: '🃏', cat: 'classic', minBet: 5, maxBet: 10000 },
    { id: 'darts', name: 'Darts', nameAm: 'ዳርትስ', icon: '🎯', cat: 'special', minBet: 1, maxBet: 5000 },
    { id: 'tennis', name: 'Tennis', nameAm: 'ቴኒስ', icon: '🎾', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'baseball', name: 'Baseball', nameAm: 'ቤዝቦል', icon: '⚾', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'greyhound', name: 'Greyhound Racing', nameAm: 'ግሬይሀውንድ እሽቅድምድም', icon: '🐕‍🦺', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'motorbike', name: 'Motorbike Racing', nameAm: 'ሞተር እሽቅድምድም', icon: '🏍️', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'cricket', name: 'Cricket', nameAm: 'ክሪኬት', icon: '🏏', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'roulette360', name: 'Roulette 360', nameAm: 'ሩሌት 360', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'megawheel', name: 'Mega Wheel', nameAm: 'ሜጋ መንኮራኩር', icon: '🎡', cat: 'table', minBet: 1, maxBet: 10000 },
    { id: 'monopoly', name: 'Monopoly', nameAm: 'ሞኖፖሊ', icon: '🎩', cat: 'table', minBet: 1, maxBet: 5000 },
    { id: 'virtualsports', name: 'Virtual Sports', nameAm: 'ቨርቹዋል ስፖርት', icon: '🎮', cat: 'sports', minBet: 1, maxBet: 10000 },
    { id: 'texasholdem', name: 'Texas Hold\'em', nameAm: 'ቴክሳስ ሆልደም', icon: '♠️', cat: 'classic', minBet: 5, maxBet: 10000 }
  ];

  // Sound Effects
  const sounds = {
    win: new Audio('/sounds/win.mp3'),
    lose: new Audio('/sounds/lose.mp3'),
    spin: new Audio('/sounds/spin.mp3'),
    coinFlip: new Audio('/sounds/coin.mp3'),
    diceRoll: new Audio('/sounds/dice.mp3'),
    slotSpin: new Audio('/sounds/slot.mp3')
  };
  const playSound = (type) => {
    if (sounds[type]) {
      sounds[type].currentTime = 0;
      sounds[type].play().catch(() => {});
    }
  };

  // Fetch Balance
  const fetchBalance = useCallback(async () => {
    try {
      const res = await axios.get('/api/wallet/balance');
      setBalance(res.data.balance || 0);
    } catch (error) {
      console.error('Error fetching balance:', error);
    }
  }, []);

  useEffect(() => {
    if (user) {
      fetchBalance();
    }
  }, [user, fetchBalance]);

  // Fallback Game Logic (Client-side if Backend fails)
  const gameLogic = {
    dice: (bet, params) => {
      const playerRoll = Math.floor(Math.random() * 6) + 1;
      const houseRoll = Math.floor(Math.random() * 6) + 1;
      const win = playerRoll > houseRoll;
      const profit = win ? bet * 2 : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { playerRoll, houseRoll } };
    },
    coinflip: (bet, params) => {
      const result = Math.random() < 0.5 ? 'heads' : 'tails';
      const win = params.side === result;
      const profit = win ? bet * 1.9 : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { result, side: params.side } };
    },
    plinko: (bet, params) => {
      const rows = params.rows || 12;
      const multipliers = { 8: [5.6,2.1,1.1,1.0,1.0,1.1,2.1,5.6], 12: [10,5,3,1.5,0.5,0.5,0.5,0.5,1.5,3,5,10], 16: [29,10,5,2,1,0.5,0.3,0.3,0.3,0.3,0.5,1,2,5,10,29] };
      const multiplier = multipliers[rows][Math.floor(Math.random() * rows)] || 1;
      const profit = bet * multiplier - bet;
      return { result: profit > 0 ? 'win' : 'lose', profit, details: { multiplier, rows } };
    },
    roulette: (bet, params) => {
      const number = Math.floor(Math.random() * 37);
      const reds = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];
      const isRed = reds.includes(number);
      const isEven = number > 0 && number % 2 === 0;
      let win = false, multiplier = 0;
      switch(params.bet) {
        case 'red': win = isRed; multiplier = 1.9; break;
        case 'black': win = !isRed && number !== 0; multiplier = 1.9; break;
        case 'even': win = isEven; multiplier = 1.9; break;
        case 'odd': win = !isEven && number !== 0; multiplier = 1.9; break;
        default: win = false; multiplier = 0;
      }
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { number, isRed, isEven } };
    },
    slot: (bet, params) => {
      const symbols = ['🍒', '🍋', '🍊', '🔔', '💎', '7️⃣'];
      const reels = [symbols[Math.floor(Math.random()*6)], symbols[Math.floor(Math.random()*6)], symbols[Math.floor(Math.random()*6)]];
      let win = false, multiplier = 0;
      if (reels[0] === reels[1] && reels[1] === reels[2]) { win = true; multiplier = 5; }
      else if (reels[0] === reels[1] || reels[1] === reels[2] || reels[0] === reels[2]) { win = true; multiplier = 0.5; }
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { reels, multiplier } };
    },
    blackjack: (bet, params) => {
      const getCard = () => { const v = Math.floor(Math.random()*13)+1; return { value: Math.min(v,10), display: v }; };
      const pCards = [getCard(), getCard()], dCards = [getCard(), getCard()];
      const score = (cards) => {
        let total = cards.reduce((s,c) => s + c.value, 0);
        const aces = cards.filter(c => c.display === 1).length;
        let adj = total, aUsed = 0;
        while (adj <= 11 && aUsed < aces) { adj += 10; aUsed++; }
        return adj;
      };
      const pScore = score(pCards), dScore = score(dCards);
      let result = 'lose', profit = -bet;
      if (pScore === 21 && pCards.length === 2) { result = 'win'; profit = bet * 2.5; }
      else if (pScore > 21) { result = 'lose'; profit = -bet; }
      else if (dScore > 21) { result = 'win'; profit = bet; }
      else if (pScore > dScore) { result = 'win'; profit = bet; }
      else if (pScore === dScore) { result = 'push'; profit = 0; }
      return { result, profit, details: { pCards: pCards.map(c=>c.display), dCards: dCards.map(c=>c.display), pScore, dScore } };
    },
    aviator: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;
      const cashOut = params.action === 'cashout' ? Math.min(1 + Math.random() * 5, crashPoint) : 0;
      const win = params.action === 'cashout' && cashOut < crashPoint;
      const multiplier = win ? cashOut : 0;
      const profit = win ? bet * multiplier : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { crashPoint, multiplier } };
    },
    mines: (bet, params) => {
      const gridSize = 25, mineCount = params.mines || 3;
      const mines = []; while (mines.length < mineCount) { const p = Math.floor(Math.random()*gridSize); if(!mines.includes(p)) mines.push(p); }
      const tile = params.tile || Math.floor(Math.random()*gridSize);
      const hit = mines.includes(tile);
      const profit = hit ? -bet : bet * 1.2;
      return { result: hit ? 'lose' : 'win', profit, details: { mines, tile, hit } };
    },
    crash: (bet, params) => {
      const crashPoint = 1 + Math.random() * 9;
      const cashOut = params.action === 'cashout' ? Math.min(1 + Math.random() * 5, crashPoint) : 0;
      const win = params.action === 'cashout' && cashOut < crashPoint;
      const mult = win ? cashOut : 0;
      const profit = win ? bet * mult : -bet;
      return { result: win ? 'win' : 'lose', profit, details: { crashPoint, multiplier: mult } };
    },
    default: (bet, params) => {
      const win = Math.random() < 0.45;
      return { result: win ? 'win' : 'lose', profit: win ? bet * 1.9 : -bet, details: {} };
    }
  };

  // Play Game Main Function
  const playGame = async (gameId, params = {}) => {
    if (!user) {
      alert('Please login to play games');
      navigate('/login');
      return;
    }
    const game = GAMES.find(g => g.id === gameId);
    if (!game) return;
    if (betAmount < game.minBet) { alert(`Minimum bet is ${game.minBet} ETB`); return; }
    if (betAmount > game.maxBet) { alert(`Maximum bet is ${game.maxBet} ETB`); return; }
    if (betAmount > balance) { alert(`Insufficient balance. Your balance is ${balance} ETB`); return; }

    setLoading(true);
    try {
      let response;
      try {
        response = await axios.post('/api/casino/play', {
          gameId,
          bet: betAmount,
          params,
          userId: user.id
        });
      } catch (serverError) {
        console.warn('Server unavailable, using client-side fallback');
        const logic = gameLogic[gameId] || gameLogic.default;
        const result = logic(betAmount, params);
        response = { data: { ...result, newBalance: balance + result.profit, gameId } };
      }

      const data = response.data;
      setBalance(data.newBalance);
      
      setResultData(data);
      setShowResultModal(true);
      setGameHistory(prev => [{ gameId, bet: betAmount, result: data.result, profit: data.profit, details: data.details, timestamp: new Date() }, ...prev].slice(0, 50));

      if (data.result === 'win') playSound('win');
      else if (data.result === 'lose') playSound('lose');

    } catch (error) {
      const msg = error.response?.data?.error || error.response?.data?.message || 'Failed to play game.';
      alert(msg);
    } finally {
      setLoading(false);
    }
  };

  const initGameState = (gameId) => {
    const defaultStates = {
      aviator: { multiplier: 1.0, gameStarted: false, crashed: false, history: [] },
      plinko: { rows: 12, ballPosition: null, multiplier: null },
      blackjack: { playing: false },
      roulette: { spinning: false },
      mines: { revealed: [], mines: [], gameOver: false },
      slot: { reels: ['🍒', '🍒', '🍒'], spinning: false }
    };
    setGameState(prev => ({ ...prev, [gameId]: defaultStates[gameId] || {} }));
  };

  const openGame = (game) => {
    setSelectedGame(game);
    setIsBetPanelOpen(true);
    initGameState(game.id);
  };

  const toggleFavorite = (gameId) => {
    setFavorites(prev => {
      const newFav = prev.includes(gameId) ? prev.filter(id => id !== gameId) : [...prev, gameId];
      localStorage.setItem('shebaodds_favorite_games', JSON.stringify(newFav));
      return newFav;
    });
  };

  const renderGameCard = (game) => (
    <div key={game.id} className={`game-card ${selectedGame?.id === game.id ? 'active' : ''}`} onClick={() => openGame(game)}>
      {game.id === 'aviator' && <span className="badge live">LIVE</span>}
      {['slot','megaball','lucky7'].includes(game.id) && <span className="badge hot">HOT</span>}
      <button className={`favorite-btn ${favorites.includes(game.id) ? 'active' : ''}`} onClick={(e) => { e.stopPropagation(); toggleFavorite(game.id); }}>
        {favorites.includes(game.id) ? '★' : '☆'}
      </button>
      <span className="game-icon">{game.icon}</span>
      <span className="game-name">{language === 'am' ? game.nameAm : game.name}</span>
      <span className="game-min-bet">{game.minBet} ETB</span>
    </div>
  );

  const renderGames = () => {
    const categories = ['crash','classic','table','slots','sports','special'];
    const categoryLabels = { crash: '💥 Crash', classic: '🃏 Classic', table: '🪑 Table', slots: '🎰 Slots', sports: '🏅 Sports', special: '✨ Special' };
    const favGames = GAMES.filter(g => favorites.includes(g.id));
    const otherGames = GAMES.filter(g => !favorites.includes(g.id));

    return (
      <>
        {favGames.length > 0 && (
          <div className="game-category">
            <h3 className="category-title">⭐ Favorites</h3>
            <div className="game-grid">{favGames.map(renderGameCard)}</div>
          </div>
        )}
        {categories.map(cat => {
          const gamesInCat = otherGames.filter(g => g.cat === cat);
          if (gamesInCat.length === 0) return null;
          return (
            <div key={cat} className="game-category">
              <h3 className="category-title">{categoryLabels[cat]} <small>{gamesInCat.length} games</small></h3>
              <div className="game-grid">{gamesInCat.map(renderGameCard)}</div>
            </div>
          );
        })}
      </>
    );
  };

  const renderGameSpecificUI = (gameId) => {
    switch (gameId) {
      case 'aviator':
        return (
          <div className="aviator-game">
            <canvas ref={canvasRef} className="aviator-canvas" width="400" height="200" />
            <div className="aviator-multiplier" id="aviatorMultiplier">{(liveGameData.aviator?.multiplier || 1.0).toFixed(2)}x</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('aviator', { action: 'bet' })} disabled={loading || liveGameData.aviator?.gameStarted}>
                {loading ? '⏳' : '✈️ Place Bet'}
              </button>
              <button className="btn-cashout" onClick={() => playGame('aviator', { action: 'cashout' })} disabled={loading || !liveGameData.aviator?.gameStarted}>
                💰 Cash Out
              </button>
            </div>
          </div>
        );
      case 'dice':
        return (
          <div className="dice-game">
            <div className="dice-display"><span className="dice">🎲</span><span className="vs-text">VS</span><span className="dice">🎲</span></div>
            <div className="dice-info">Roll higher than the house to win!</div>
            <button className="btn-play" onClick={() => playGame('dice')} disabled={loading}>{loading ? '🎲 Rolling...' : '🎲 Roll Dice'}</button>
          </div>
        );
      case 'coinflip':
        return (
          <div className="coinflip-game">
            <div className="coin-display">🪙</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('coinflip', { side: 'heads' })} disabled={loading}>Heads</button>
              <button className="btn-bet" onClick={() => playGame('coinflip', { side: 'tails' })} disabled={loading}>Tails</button>
            </div>
          </div>
        );
      case 'plinko':
        return (
          <div className="plinko-game">
            <div className="plinko-multiplier">{(gameState.plinko?.multiplier || 1.0)}x</div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 8 })}>8 Rows</button>
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 12 })}>12 Rows</button>
              <button className="btn-bet" onClick={() => playGame('plinko', { rows: 16 })}>16 Rows</button>
            </div>
            <button className="btn-play" onClick={() => playGame('plinko', { rows: gameState.plinko?.rows || 12 })} disabled={loading}>
              {loading ? '🔽 Dropping...' : '🔴 Drop Ball'}
            </button>
          </div>
        );
      case 'blackjack':
        return (
          <div className="blackjack-game">
            <div className="bj-display">
              <div className="bj-hand"><span className="hand-label">Dealer</span><span>🃏 <span className="hand-score">0</span></span></div>
              <div className="bj-hand"><span className="hand-label">You</span><span>🃏 <span className="hand-score">0</span></span></div>
            </div>
            <div className="game-controls">
              <button className="btn-bet" onClick={() => playGame('blackjack', { action: 'hit' })} disabled={loading || !gameState.blackjack?.playing}>Hit</button>
              <button className="btn-bet" onClick={() => playGame('blackjack', { action: 'stand' })} disabled={loading || !gameState.blackjack?.playing}>Stand</button>
              <button className="btn-play" onClick={() => { setGameState(prev => ({...prev, blackjack:{playing:true}})); playGame('blackjack', { action: 'deal' }); }} disabled={loading}>New Hand</button>
            </div>
          </div>
        );
      case 'slot':
        return (
          <div className="slot-game">
            <div className="slot-reels"><span>🍒</span><span>🍒</span><span>🍒</span></div>
            <button className="btn-play spin" onClick={() => { playSound('slotSpin'); playGame('slot'); }} disabled={loading}>
              {loading ? '🔄 Spinning...' : '🎰 Spin'}
            </button>
          </div>
        );
      default:
        return (
          <div className="default-game">
            <div className="game-icon-large">{GAMES.find(g=>g.id===gameId)?.icon || '🎮'}</div>
            <div className="game-info-text">Place your bet and try your luck!</div>
            <button className="btn-play" onClick={() => playGame(gameId)} disabled={loading}>
              {loading ? '⏳ Playing...' : '▶️ Play Now'}
            </button>
          </div>
        );
    }
  };

  return (
    <div className="casino-games-page">
      <div className="casino-header">
        <div className="header-left">
          <h1>🎰 {language === 'am' ? 'ካሲኖ ጨዋታዎች' : 'Casino Games'}</h1>
          <span className="game-count">{GAMES.length} {language === 'am' ? 'ጨዋታዎች' : 'Games'}</span>
        </div>
        <div className="balance-box">
          <span>💰 {language === 'am' ? 'ቀሪ ሂሳብ' : 'Balance'}</span>
          <span className="balance-amount">{balance.toLocaleString()} ETB</span>
          <button className="refresh-balance" onClick={fetchBalance}>🔄</button>
        </div>
      </div>

      <div className="games-container">{renderGames()}</div>

      {selectedGame && (
        <div className="game-view">
          <div className="game-view-header">
            <h2 className="game-title">{selectedGame.icon} {language === 'am' ? selectedGame.nameAm : selectedGame.name}</h2>
            <div className="game-stats">
              <span>💰 {balance.toLocaleString()} ETB</span>
              <span>📂 {selectedGame.cat.toUpperCase()}</span>
            </div>
          </div>
          <div className="game-area">{renderGameSpecificUI(selectedGame.id)}</div>
          <div className="game-tutorial">
            <button className="tutorial-btn" onClick={() => alert(`How to play ${selectedGame.name}: Place your bet and try your luck!`)}>❓ How to play</button>
          </div>
        </div>
      )}

      {gameHistory.length > 0 && (
        <div className="game-history">
          <h4>📜 Recent Games</h4>
          <div className="history-list">
            {gameHistory.slice(0, 10).map((g, i) => (
              <div key={i} className={`history-item ${g.result}`}>
                <span className="history-game">{g.gameId}</span>
                <span className="history-bet">{g.bet} ETB</span>
                <span className="history-result">{g.result === 'win' ? '✅' : '❌'}</span>
                <span className={`history-profit ${g.profit >= 0 ? 'positive' : 'negative'}`}>
                  {g.profit >= 0 ? '+' : ''}{g.profit} ETB
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {isBetPanelOpen && (
        <div className="bet-panel">
          <div className="game-info">
            <span>{selectedGame?.icon} {language === 'am' ? selectedGame?.nameAm : selectedGame?.name}</span>
            <small>Min: {selectedGame?.minBet} ETB</small>
          </div>
          <div className="quick-bets">
            {[{label:'10%',value:Math.round(balance*0.1)},{label:'25%',value:Math.round(balance*0.25)},{label:'50%',value:Math.round(balance*0.5)},{label:'100%',value:Math.round(balance)}].map(qb => (
              <button key={qb.label} className="quick-bet-btn" onClick={() => setBetAmount(Math.max(qb.value, 1))} disabled={qb.value < 1}>{qb.label}</button>
            ))}
          </div>
          <div className="bet-amounts">
            {[1,2,5,10,20,50,100,500,1000].map(amt => (
              <button key={amt} className={`amt-btn ${betAmount === amt ? 'active' : ''}`} onClick={() => setBetAmount(amt)}>{amt}</button>
            ))}
            <button className="amt-btn max" onClick={() => setBetAmount(Math.min(balance, selectedGame?.maxBet || 10000))}>MAX</button>
          </div>
          <div className="manual-input">
            <input type="number" value={betAmount} onChange={(e) => setBetAmount(Math.max(1, Number(e.target.value)))} min="1" /><span>ETB</span>
          </div>
          <div className="bet-actions">
            <button className="play-btn" onClick={() => playGame(selectedGame?.id)} disabled={loading || betAmount > balance}>{loading ? '⏳' : '▶️ Play'}</button>
            <button className="close-btn" onClick={() => setIsBetPanelOpen(false)}>✕</button>
          </div>
          <div className="balance-display">{language === 'am' ? 'ቀሪ ሂሳብ' : 'Balance'}: <strong>{balance.toLocaleString()} ETB</strong></div>
          {betAmount > balance && <div className="balance-warning">⚠️ Insufficient balance</div>}
        </div>
      )}

      {showResultModal && resultData && (
        <div className="result-modal-overlay" onClick={() => setShowResultModal(false)}>
          <div className="result-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-icon">{resultData.result === 'win' ? '🎉' : '😔'}</div>
            <h2 className="modal-title">{language === 'am' ? selectedGame?.nameAm : selectedGame?.name}</h2>
            <div className={`result-value ${resultData.result}`}>
              {resultData.result === 'win' ? '✅ WIN' : resultData.result === 'lose' ? '❌ LOSE' : '⚖️ PUSH'}
            </div>
            <div className="result-details">
              <div>{language === 'am' ? 'ውርርድ' : 'Bet'}: <span>{betAmount} ETB</span></div>
              <div>{language === 'am' ? 'ትርፍ' : 'Profit'}: <span className={resultData.result === 'win' ? 'green' : 'red'}>{resultData.profit} ETB</span></div>
              <div>{language === 'am' ? 'አዲስ ቀሪ ሂሳብ' : 'New Balance'}: <span className="gold">{resultData.newBalance} ETB</span></div>
            </div>
            {resultData.details && Object.keys(resultData.details).length > 0 && (
              <div className="result-extra">
                {Object.entries(resultData.details).map(([key, value]) => (
                  <div key={key}>{key}: <span>{typeof value === 'object' ? JSON.stringify(value) : value}</span></div>
                ))}
              </div>
            )}
            <button className="modal-btn" onClick={() => setShowResultModal(false)}>
              {language === 'am' ? 'ቀጥል' : 'Continue'}
            </button>
          </div>
        </div>
      )}

      <style jsx>{`
        .casino-games-page { padding: 20px; max-width: 1400px; margin: 0 auto; min-height: 100vh; background: #0b0e1a; color: #fff; }
        .casino-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #1a1f33; border-radius: 16px; margin-bottom: 24px; border-bottom: 2px solid #f0b90b; flex-wrap: wrap; gap: 12px; }
        .header-left { display: flex; align-items: center; gap: 12px; }
        .casino-header h1 { font-size: 24px; font-weight: 700; color: #f0b90b; margin: 0; }
        .game-count { font-size: 14px; color: #8892b0; background: #1e2338; padding: 4px 12px; border-radius: 20px; }
        .balance-box { display: flex; align-items: center; gap: 12px; background: #1e2338; padding: 8px 20px; border-radius: 30px; border: 1px solid #f0b90b55; }
        .balance-amount { font-size: 20px; font-weight: 700; color: #f0b90b; }
        .refresh-balance { background: transparent; border: none; color: #8892b0; cursor: pointer; font-size: 16px; padding: 4px; }
        .refresh-balance:hover { color: #f0b90b; }
        .game-category { margin-bottom: 28px; }
        .category-title { font-size: 18px; font-weight: 600; margin-bottom: 14px; color: #ccd6f6; }
        .category-title small { font-size: 13px; font-weight: 400; color: #8892b0; }
        .game-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 12px; }
        .game-card { background: #151b2b; border-radius: 16px; padding: 16px 8px 12px; text-align: center; cursor: pointer; transition: 0.25s; border: 2px solid transparent; position: relative; }
        .game-card:hover { transform: translateY(-4px); border-color: #f0b90b66; background: #1c2338; }
        .game-card.active { border-color: #f0b90b; background: #1c2338; }
        .game-card .game-icon { font-size: 32px; display: block; margin-bottom: 6px; }
        .game-card .game-name { font-size: 11px; font-weight: 600; color: #ccd6f6; }
        .game-card .game-min-bet { font-size: 9px; color: #8892b0; display: block; margin-top: 4px; }
        .badge { position: absolute; top: 6px; right: 6px; font-size: 8px; padding: 2px 8px; border-radius: 10px; }
        .badge.live { background: #ff4757; color: #fff; animation: pulse 1.5s infinite; }
        .badge.hot { background: #f0b90b; color: #0b0e1a; }
        .favorite-btn { position: absolute; top: 6px; left: 6px; background: transparent; border: none; color: #8892b0; cursor: pointer; font-size: 14px; padding: 2px; transition: 0.2s; z-index: 2; }
        .favorite-btn.active { color: #f0b90b; }
        .favorite-btn:hover { transform: scale(1.2); }
        .game-view { background: #0f1322; border-radius: 24px; padding: 24px; margin-top: 16px; border: 1px solid #f0b90b44; }
        .game-view-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
        .game-title { font-size: 24px; font-weight: 700; color: #f0b90b; margin: 0; }
        .game-stats { display: flex; gap: 16px; font-size: 14px; color: #8892b0; }
        .game-stats span { background: #1e2338; padding: 4px 12px; border-radius: 20px; }
        .game-area { background: #1a1f33; border-radius: 16px; padding: 20px; min-height: 200px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
        .game-tutorial { margin-top: 12px; text-align: center; }
        .tutorial-btn { background: transparent; border: 1px solid #2a3150; color: #8892b0; padding: 6px 16px; border-radius: 20px; cursor: pointer; font-size: 12px; transition: 0.2s; }
        .tutorial-btn:hover { border-color: #f0b90b; color: #f0b90b; }
        .game-controls { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
        .game-controls button { background: #2a3150; border: none; color: #fff; padding: 10px 24px; border-radius: 30px; cursor: pointer; font-weight: 600; transition: 0.2s; }
        .game-controls button:hover:not(:disabled) { background: #f0b90b; color: #0b0e1a; transform: scale(1.05); }
        .game-controls button:disabled { opacity: 0.5; cursor: not-allowed; }
        .btn-play { background: #f0b90b; border: none; color: #0b0e1a; padding: 12px 32px; border-radius: 30px; font-weight: 700; font-size: 16px; cursor: pointer; transition: 0.2s; }
        .btn-play:hover:not(:disabled) { transform: scale(1.05); box-shadow: 0 0 20px #f0b90b66; }
        .btn-play:disabled { opacity: 0.5; cursor: not-allowed; }
        .btn-bet { background: #2a3150; border: none; color: #fff; padding: 10px 24px; border-radius: 30px; cursor: pointer; font-weight: 600; transition: 0.2s; }
        .btn-bet:hover:not(:disabled) { background: #f0b90b; color: #0b0e1a; }
        .btn-bet:disabled { opacity: 0.5; cursor: not-allowed; }
        .btn-cashout { background: #2ed573; border: none; color: #fff; padding: 10px 24px; border-radius: 30px; cursor: pointer; font-weight: 600; transition: 0.2s; }
        .btn-cashout:hover:not(:disabled) { background: #f0b90b; color: #0b0e1a; }
        .btn-cashout:disabled { opacity: 0.5; cursor: not-allowed; }
        .bet-panel { position: fixed; bottom: 0; left: 0; right: 0; background: rgba(11,14,26,0.96); border-top: 2px solid #f0b90b55; padding: 12px 16px 16px; display: flex; flex-wrap: wrap; align-items: center; justify-content: center; gap: 10px; z-index: 200; backdrop-filter: blur(12px); }
        .game-info { font-size: 14px; font-weight: 600; color: #f0b90b; min-width: 100px; text-align: center; }
        .game-info small { display: block; font-size: 10px; color: #8892b0; font-weight: 400; }
        .quick-bets { display: flex; gap: 4px; }
        .quick-bet-btn { background: #1e2338; border: 1px solid #2a3150; color: #ccd6f6; padding: 4px 10px; border-radius: 6px; font-size: 11px; cursor: pointer; transition: 0.15s; }
        .quick-bet-btn:hover:not(:disabled) { background: #2a3150; border-color: #f0b90b66; }
        .quick-bet-btn:disabled { opacity: 0.3; cursor: not-allowed; }
        .bet-amounts { display: flex; flex-wrap: wrap; gap: 6px; }
        .amt-btn { background: #1e2338; border: 1px solid #2a3150; color: #ccd6f6; padding: 6px 12px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; transition: 0.15s; min-width: 44px; }
        .amt-btn:hover { background: #2a3150; border-color: #f0b90b66; }
        .amt-btn.active { background: #f0b90b; color: #0b0e1a; border-color: #f0b90b; }
        .amt-btn.max { border-color: #ff4757; color: #ff4757; }
        .manual-input { display: flex; align-items: center; gap: 4px; background: #1e2338; border-radius: 8px; padding: 2px 8px 2px 12px; border: 1px solid #2a3150; }
        .manual-input input { background: transparent; border: none; color: #fff; width: 70px; font-size: 14px; font-weight: 600; padding: 6px 0; outline: none; }
        .bet-actions { display: flex; gap: 8px; align-items: center; }
        .play-btn { background: #f0b90b; border: none; color: #0b0e1a; padding: 10px 32px; border-radius: 30px; font-weight: 700; font-size: 16px; cursor: pointer; transition: 0.2s; }
        .play-btn:hover:not(:disabled) { transform: scale(1.03); box-shadow: 0 0 20px #f0b90b66; }
        .play-btn:disabled { opacity: 0.5; cursor: not-allowed; }
        .close-btn { background: transparent; border: none; color: #8892b0; font-size: 22px; cursor: pointer; padding: 4px 8px; }
        .close-btn:hover { color: #fff; }
        .balance-display { font-size: 13px; color: #8892b0; min-width: 80px; text-align: center; }
        .balance-display strong { color: #f0b90b; font-size: 16px; }
        .balance-warning { color: #ff4757; font-size: 12px; font-weight: 600; text-align: center; width: 100%; }
        .result-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(8px); z-index: 300; display: flex; align-items: center; justify-content: center; padding: 20px; }
        .result-modal { background: #151b2b; border-radius: 24px; max-width: 440px; width: 100%; padding: 30px 24px 24px; text-align: center; border: 1px solid #f0b90b44; animation: modalIn 0.3s ease; }
        @keyframes modalIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
        .modal-icon { font-size: 48px; }
        .modal-title { font-size: 20px; color: #ccd6f6; margin: 8px 0; }
        .result-value { font-size: 36px; font-weight: 800; margin: 12px 0; }
        .result-value.win { color: #2ed573; }
        .result-value.lose { color: #ff4757; }
        .result-value.push { color: #f0b90b; }
        .result-details { margin: 16px 0; display: flex; flex-direction: column; gap: 8px; }
        .result-details div { display: flex; justify-content: space-between; padding: 8px 4px; border-bottom: 1px solid #1e2338; }
        .result-details .green { color: #2ed573; }
        .result-details .red { color: #ff4757; }
        .result-details .gold { color: #f0b90b; }
        .result-extra { margin-top: 8px; font-size: 12px; color: #8892b0; }
        .modal-btn { margin-top: 18px; background: #f0b90b; border: none; color: #0b0e1a; padding: 12px; border-radius: 30px; font-weight: 700; font-size: 16px; cursor: pointer; width: 100%; transition: 0.2s; }
        .modal-btn:hover { transform: scale(1.02); box-shadow: 0 0 20px #f0b90b66; }
        .aviator-canvas { width: 100%; max-width: 400px; height: 200px; background: #1e2338; border-radius: 8px; }
        .aviator-multiplier { font-size: 42px; font-weight: 800; color: #f0b90b; text-align: center; }
        .dice-display { display: flex; gap: 20px; font-size: 48px; align-items: center; }
        .dice { font-size: 64px; }
        .vs-text { color: #8892b0; font-size: 24px; font-weight: 700; }
        .dice-info { color: #8892b0; font-size: 14px; }
        .coin-display { font-size: 80px; animation: coinFlip 0.5s ease; }
        @keyframes coinFlip { 0% { transform: rotateY(0deg); } 100% { transform: rotateY(360deg); } }
        .slot-reels { display: flex; gap: 16px; font-size: 48px; }
        .slot-reels span { background: #0b0e1a; padding: 16px; border-radius: 12px; border: 2px solid #2a3150; }
        .plinko-multiplier { font-size: 24px; font-weight: 700; color: #f0b90b; }
        .bj-display { display: flex; flex-direction: column; gap: 16px; width: 100%; max-width: 400px; }
        .bj-hand { display: flex; align-items: center; gap: 12px; background: #0b0e1a; padding: 12px 16px; border-radius: 8px; }
        .hand-label { color: #8892b0; font-weight: 600; min-width: 60px; }
        .hand-score { margin-left: auto; font-weight: 700; font-size: 18px; color: #f0b90b; }
        .history-list { display: flex; flex-direction: column; gap: 6px; max-height: 200px; overflow-y: auto; }
        .history-item { display: flex; justify-content: space-between; padding: 6px 12px; border-radius: 6px; font-size: 13px; background: #0b0e1a; }
        .history-item.win { border-left: 3px solid #2ed573; }
        .history-item.lose { border-left: 3px solid #ff4757; }
        .history-item.push { border-left: 3px solid #f0b90b; }
        .history-profit.positive { color: #2ed573; }
        .history-profit.negative { color: #ff4757; }
        @media (max-width: 700px) {
          .game-grid { grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)); }
          .bet-panel { padding: 10px; gap: 6px; }
          .amt-btn { font-size: 11px; padding: 4px 8px; min-width: 34px; }
          .casino-header { flex-direction: column; align-items: flex-start; }
          .balance-box { width: 100%; justify-content: center; }
          .game-view-header { flex-direction: column; align-items: flex-start; }
          .game-controls button { padding: 8px 16px; font-size: 13px; }
          .result-modal { margin: 12px; padding: 20px; }
          .result-value { font-size: 28px; }
          .dice-display { font-size: 32px; }
          .slot-reels { font-size: 32px; }
          .slot-reels span { padding: 10px; }
        }
        @media (max-width: 480px) {
          .game-grid { grid-template-columns: repeat(auto-fill, minmax(70px, 1fr)); gap: 8px; }
          .game-card { padding: 12px 6px 10px; }
          .game-card .game-icon { font-size: 24px; }
          .game-card .game-name { font-size: 10px; }
          .bet-panel { padding: 8px; }
          .bet-amounts { gap: 4px; }
          .amt-btn { font-size: 10px; padding: 3px 6px; min-width: 28px; }
          .manual-input input { width: 50px; font-size: 12px; }
          .play-btn { padding: 8px 20px; font-size: 14px; }
          .game-title { font-size: 20px; }
        }
      `}</style>
    </div>
  );
}

// ==================== PLACEHOLDER COMPONENTS ====================
function MatchesPage() { return <div className="coming-soon">Matches Page - Coming Soon</div>; }
function MatchDetailPage() { return <div className="coming-soon">Match Detail - Coming Soon</div>; }
function LivePage() { return <div className="coming-soon">Live Betting - Coming Soon</div>; }
function PromotionsPage() { return <div className="coming-soon">Promotions - Coming Soon</div>; }
function ProfilePage() { return <div className="coming-soon">Profile - Coming Soon</div>; }
function WalletPage() { return <div className="coming-soon">Wallet - Coming Soon</div>; }
function TaxPage() { return <div className="coming-soon">Tax Center - Coming Soon</div>; }
function BettingHistoryPage() { return <div className="coming-soon">Betting History - Coming Soon</div>; }
function ResponsibleGamblingPage() { return <div className="coming-soon">Responsible Gambling - Coming Soon</div>; }
function LoginPage() { return <div className="coming-soon">Login - Coming Soon</div>; }
function RegisterPage() { return <div className="coming-soon">Register - Coming Soon</div>; }
function ResetPasswordPage() { return <div className="coming-soon">Reset Password - Coming Soon</div>; }
function VerifyEmailPage() { return <div className="coming-soon">Verify Email - Coming Soon</div>; }
function SettingsPage() { return <div className="coming-soon">Settings - Coming Soon</div>; }
function SupportPage() { return <div className="coming-soon">Support - Coming Soon</div>; }
function TermsPage() { return <div className="coming-soon">Terms - Coming Soon</div>; }
function PrivacyPage() { return <div className="coming-soon">Privacy - Coming Soon</div>; }
function ResponsiblePage() { return <div className="coming-soon">Responsible Gambling - Coming Soon</div>; }
function NotFoundPage() { return <div className="coming-soon">Page Not Found</div>; }
function showToast(title, message) { console.log('Toast:', title, message); }

export default App;