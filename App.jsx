import React, { lazy, Suspense } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Link,
  useLocation,
} from "react-router-dom";

import { AuthProvider, useAuth } from "./Contexts.jsx";
import { LanguageProvider, useTranslation } from "./LanguageContext";

import SportsbookHeader from "./SportsbookHeader";
import BetSlip from "./BetSlip";

import LoginPage from "./LoginPage";
import RegisterPage from "./RegisterPage";

import "./global.css";
import "./theme.css";


// ======================================================
// LAZY LOADED SCREENS
// ======================================================

const HomeScreen = lazy(() => import("./HomeScreen"));
const LiveScreen = lazy(() => import("./LiveScreen"));
const CasinoScreen = lazy(() => import("./CasinoScreen"));
const ProfileScreen = lazy(() => import("./ProfileScreen"));
const MyBetsScreen = lazy(() => import("./MyBetsScreen"));
const WalletScreen = lazy(() => import("./WalletScreen"));
const PromotionsScreen = lazy(() => import("./PromotionsScreen"));
const SupportScreen = lazy(() => import("./SupportScreen"));
const CalendarScreen = lazy(() => import("./CalendarScreen"));


// ======================================================
// LOADING SCREEN
// ======================================================

function LoadingFallback() {
  return (
    <div className="loading-spinner">
      Loading...
    </div>
  );
}


// ======================================================
// MAIN APP
// ======================================================

export default function App() {
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


// ======================================================
// APP LAYOUT
// ======================================================

function AppLayout() {
  const { user, logout, loading } = useAuth();
  const location = useLocation();
  const { t } = useTranslation();

  // Wait until authentication is checked
  if (loading) {
    return <LoadingFallback />;
  }


  // ====================================================
  // NOT LOGGED IN
  // ====================================================

  if (!user) {
    return (
      <div className="app-container auth-only">

        <main className="main-content">

          <Suspense fallback={<LoadingFallback />}>

            <Routes>

              <Route
                path="/login"
                element={<LoginPage />}
              />

              <Route
                path="/register"
                element={<RegisterPage />}
              />

              <Route
                path="*"
                element={
                  <Navigate
                    to="/login"
                    replace
                  />
                }
              />

            </Routes>

          </Suspense>

        </main>

      </div>
    );
  }


  // ====================================================
  // BOTTOM NAVIGATION
  // ====================================================

  const navItems = [
    {
      path: "/",
      label: "Sportsbook",
      icon: "🏠",
    },
    {
      path: "/my-bets",
      label: "My Bets",
      icon: "📋",
    },
    {
      path: "/wallet",
      label: "Wallet",
      icon: "💰",
    },
    {
      path: "/promotions",
      label: "Promotions",
      icon: "🎁",
    },
    {
      path: "/calendar",
      label: "Calendar",
      icon: "📅",
    },
  ];


  // ====================================================
  // LOGGED IN LAYOUT
  // ====================================================

  return (
    <div className="app-container">

      {/* HEADER */}

      <SportsbookHeader
        onLogout={logout}
      />


      {/* MAIN CONTENT */}

      <main className="main-content">

        <Suspense fallback={<LoadingFallback />}>

          <Routes>

            {/* HOME */}

            <Route
              path="/"
              element={<HomeScreen />}
            />


            {/* SPORTS */}

            <Route
              path="/live"
              element={<LiveScreen />}
            />


            {/* CASINO */}

            <Route
              path="/casino"
              element={<CasinoScreen />}
            />


            {/* PROFILE */}

            <Route
              path="/profile"
              element={<ProfileScreen />}
            />


            {/* MY BETS */}

            <Route
              path="/my-bets"
              element={<MyBetsScreen />}
            />


            {/* WALLET */}

            <Route
              path="/wallet"
              element={<WalletScreen />}
            />


            {/* PROMOTIONS */}

            <Route
              path="/promotions"
              element={<PromotionsScreen />}
            />


            {/* SUPPORT */}

            <Route
              path="/support"
              element={<SupportScreen />}
            />


            {/* ETHIOPIAN CALENDAR */}

            <Route
              path="/calendar"
              element={<CalendarScreen />}
            />


            {/* AUTH ROUTES WHEN ALREADY LOGGED IN */}

            <Route
              path="/login"
              element={
                <Navigate
                  to="/"
                  replace
                />
              }
            />

            <Route
              path="/register"
              element={
                <Navigate
                  to="/"
                  replace
                />
              }
            />


            {/* UNKNOWN PAGE */}

            <Route
              path="*"
              element={
                <Navigate
                  to="/"
                  replace
                />
              }
            />

          </Routes>

        </Suspense>

      </main>


      {/* BOTTOM NAVIGATION */}

      <nav className="bottom-nav">

        {navItems.map((item) => {

          const active =
            location.pathname === item.path;

          return (
            <Link
              key={item.path}
              to={item.path}
              className={
                `nav-item ${active ? "active" : ""}`
              }
            >

              <span className="nav-icon">
                {item.icon}
              </span>

              <span className="nav-label">
                {t(item.label) || item.label}
              </span>

            </Link>
          );

        })}

      </nav>


      {/* BET SLIP */}

      <BetSlip />

    </div>
  );
}