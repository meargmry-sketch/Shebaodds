// App.jsx – Main entry with authentication, protected routes, and Calendar nav
import React, { lazy, Suspense } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Link,
  useLocation,
} from 'react-router-dom';
import { AuthProvider, useAuth } from "./AuthContext";
import { LanguageProvider, useTranslation } from './LanguageContext';
import SportsbookHeader from './SportsbookHeader';
import BetSlip from './BetSlip';
import LoginPage from './LoginPage';
import RegisterPage from './RegisterPage';
import './global.css';
import './theme.css';

// Lazy load protected pages
const HomeScreen = lazy(() => import('./HomeScreen'));
const Livescreen = lazy(() => import('./Livescreen'));
const Casinoscreen = lazy(() => import('./Casinoscreen'));
const Profilescreen = lazy(() => import('./Profilescreen'));
const MyBetsscreen = lazy(() => import('./MyBetsscreen'));
const Walletscreen = lazy(() => import('./Walletscreen'));
const Promotionsscreen = lazy(() => import('./Promotionsscreen'));
const Supportscreen = lazy(() => import('./Supportscreen'));
const Calendarscreen = lazy(() => import('./Calendarscreen')); // NEW

const LoadingFallback = () => <div className="loading-spinner">Loading...</div>;

// ==================== MAIN APP ====================
function App() {
  return (
    <LanguageProvider>
      <AuthProvider>
        <BrowserRouter>
          <AppLayout />
        </BrowserRouter>
      </AuthProvider>
    </LanguageProvider>
  );
}

// ==================== LAYOUT WITH AUTH AWARENESS ====================
function AppLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const { t } = useTranslation();

  // Bottom navigation items – Calendar added, Support moved to Profile
  const navItems = [
    { path: '/', label: 'Sportsbook', icon: '🏠' },
    { path: '/my-bets', label: 'My Bets', icon: '📋' },
    { path: '/wallet', label: 'Wallet', icon: '💰' },
    { path: '/promotions', label: 'Promotions', icon: '🎁' },
    { path: '/calendar', label: 'Calendar', icon: '📅' }, // NEW
  ];

  // If user is not logged in, only show auth routes
  if (!user) {
    return (
      <div className="app-container auth-only">
        <main className="main-content">
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Suspense>
        </main>
      </div>
    );
  }

  // Logged-in layout
  return (
    <div className="app-container">
      <SportsbookHeader onLogout={logout} />

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
            <Route path="/support" element={<SupportScreen />} /> {/* kept for direct access */}
            <Route path="/calendar" element={<CalendarScreen />} /> {/* NEW */}
            <Route path="/login" element={<Navigate to="/" replace />} />
            <Route path="/register" element={<Navigate to="/" replace />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>

      {/* Bottom Navigation */}
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

      <BetSlip />
    </div>
  );
}

export default App;