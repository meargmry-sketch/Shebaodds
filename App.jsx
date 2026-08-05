// App.jsx – Main entry point
// Uses existing components: SportsbookHeader, BetSlip, LiveUpcomingMatches, LanguageContext
// Adds bottom navigation and removes the old sidebar.

import React, { useState, useEffect, lazy, Suspense } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Link,
  useLocation,
  useNavigate
} from 'react-router-dom';

// ----- Existing components (from your codebase) -----
import SportsbookHeader from './SportsbookHeader';
import BetSlip from './BetSlip';
import LiveUpcomingMatches from './LiveUpcomingMatches'; // will be used on Home page
import { LanguageProvider, useTranslation, formatNumber, formatDate } from './LanguageContext';

// ----- Styles (must be present) -----
import './global.css';
import './theme.css';

// ----- Contexts (if you have them in separate files, import them; otherwise define them here) -----
// Assuming you already have AuthContext, BetSlipContext, NotificationContext.
// If not, you can keep the ones from your previous code.
// For now, we'll import them from your existing files (adjust paths if needed).
// If they don't exist, we'll define them inline.
import { AuthContext, BetSlipContext, NotificationContext } from './contexts'; // adjust path

// ----- Lazy load page components (we'll create them later) -----
const HomeScreen = lazy(() => import('./HomeScreen'));
const LiveScreen = lazy(() => import('./LiveScreen'));
const CasinoScreen = lazy(() => import('./CasinoScreen'));
const ProfileScreen = lazy(() => import('./ProfileScreen'));
const MyBetsScreen = lazy(() => import('./MyBetsScreen'));
const WalletScreen = lazy(() => import('./WalletScreen'));
const PromotionsScreen = lazy(() => import('./PromotionsScreen'));
const SupportScreen = lazy(() => import('./SupportScreen'));

// ----- Fallback for lazy loading -----
const LoadingFallback = () => <div className="loading-spinner">Loading...</div>;

// ==================== MAIN APP ====================
function App() {
  // Auth state – you already have this in your existing App.
  // We'll keep it as is; for now we just wrap the router.
  // We'll reuse your existing context providers.
  // Assuming AuthContext etc. are defined in separate files.
  // If not, we can define them here.

  return (
    <LanguageProvider>
      {/* Your existing contexts */}
      <AuthContext.Provider value={{ user: null, login: () => {}, logout: () => {} /* etc. */ }}>
        <BetSlipContext.Provider value={{ bets: [], addBet: () => {}, clearBets: () => {} }}>
          <NotificationContext.Provider value={{ notifications: [], markRead: () => {} }}>
            <BrowserRouter>
              <AppLayout />
            </BrowserRouter>
          </NotificationContext.Provider>
        </BetSlipContext.Provider>
      </AuthContext.Provider>
    </LanguageProvider>
  );
}

// ==================== LAYOUT WITH BOTTOM NAVIGATION ====================
function AppLayout() {
  const location = useLocation();
  const { t } = useTranslation();

  // Bottom navigation items
  const navItems = [
    { path: '/', label: 'Sportsbook', icon: '🏠' },
    { path: '/my-bets', label: 'My Bets', icon: '📋' },
    { path: '/wallet', label: 'Wallet', icon: '💰' },
    { path: '/promotions', label: 'Promotions', icon: '🎁' },
    { path: '/support', label: 'Support', icon: '❓' },
  ];

  return (
    <div className="app-container">
      {/* Top Header – your existing SportsbookHeader */}
      <SportsbookHeader />

      {/* Main content area – renders current route */}
      <main className="main-content">
        <Suspense fallback={<LoadingFallback />}>
          <Routes>
            <Route path="/" element={<HomeScreen />} />
            <Route path="/live" element={<LiveScreen />} />
            <Route path="/casino" element={<CasinoScreen />} />
            <Route path="/profile" element={<ProfileScreen />} />
            <Route path="/my-bets" element={<MyBetsScreen />} />
            <Route path="/wallet" element={<WalletScreen />} />
            <Route path="/promotions" element={<PromotionsScreen />} />
            <Route path="/support" element={<SupportScreen />} />
            {/* Fallback – redirect to home */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>

      {/* Bottom Navigation – new component */}
      <nav className="bottom-nav">
        {navItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{t(item.label) || item.label}</span>
          </Link>
        ))}
      </nav>

      {/* Bet Slip – your existing floating component */}
      <BetSlip />
    </div>
  );
}

export default App;