// ProfileScreen.jsx – User profile, settings, and responsible gambling
import React, { useState, useEffect } from 'react';
import { useAuth } from './contexts'; // adjust path if needed
import { useTranslation } from './LanguageContext';
import { User, Shield, Bell, Settings, LogOut, ChevronRight, Award, Wallet, Clock } from 'lucide-react';

export default function ProfileScreen() {
  const { user, logout, theme, toggleTheme } = useAuth?.() || {};
  const { t, language, setLanguage } = useTranslation?.() || { t: (key) => key, language: 'en', setLanguage: () => {} };
  
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);

  // Simulated user data – replace with real data from your auth context
  const profile = {
    username: user?.username || 'Player123',
    email: user?.email || 'player@example.com',
    vipLevel: user?.vip?.level || 1,
    joinedDate: user?.createdAt || '2024-01-15',
    balance: user?.wallet?.balance || 00.00,
    bonusBalance: user?.wallet?.bonusBalance || 500.00,
    totalDeposited: user?.wallet?.totalDeposited || 12500.00,
    totalWon: user?.wallet?.totalWon || 8750.00,
    totalTaxPaid: user?.wallet?.totalTaxPaid || 1312.50,
    betsPlaced: 342,
    betsWon: 178,
    winRate: 52,
  };

  // Handle logout
  const handleLogout = async () => {
    if (window.confirm(t('confirm_logout') || 'Are you sure you want to logout?')) {
      setLoading(true);
      await logout?.();
      setLoading(false);
    }
  };

  // Render tab content
  const renderTabContent = () => {
    switch (activeTab) {
      case 'profile':
        return (
          <div className="profile-tab">
            <div className="profile-header">
              <div className="avatar-container">
                <div className="avatar">
                  {profile.username?.[0]?.toUpperCase() || 'U'}
                </div>
                <div className="vip-badge">
                  <Award className="h-4 w-4" />
                  VIP {profile.vipLevel}
                </div>
              </div>
              <div className="profile-info">
                <h2>{profile.username}</h2>
                <p className="email">{profile.email}</p>
                <p className="joined">Joined: {new Date(profile.joinedDate).toLocaleDateString()}</p>
              </div>
            </div>

            <div className="stats-grid">
              <div className="stat-card">
                <span className="stat-label">💰 Balance</span>
                <span className="stat-value">{profile.balance.toLocaleString()} ETB</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">🎁 Bonus</span>
                <span className="stat-value">{profile.bonusBalance.toLocaleString()} ETB</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">📈 Total Deposited</span>
                <span className="stat-value">{profile.totalDeposited.toLocaleString()} ETB</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">🏆 Total Won</span>
                <span className="stat-value">{profile.totalWon.toLocaleString()} ETB</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">💰 Tax Paid</span>
                <span className="stat-value">{profile.totalTaxPaid.toLocaleString()} ETB</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">📊 Win Rate</span>
                <span className="stat-value">{profile.winRate}%</span>
              </div>
            </div>

            <div className="profile-actions">
              <button className="action-btn" onClick={() => window.location.href = '/wallet'}>
                <Wallet className="h-4 w-4" />
                Go to Wallet
              </button>
              <button className="action-btn" onClick={() => window.location.href = '/betting-history'}>
                <Clock className="h-4 w-4" />
                Betting History
              </button>
            </div>
          </div>
        );

      case 'settings':
        return (
          <div className="settings-tab">
            <div className="setting-group">
              <h3>Preferences</h3>
              <div className="setting-item">
                <div className="setting-label">
                  <span>🌙 Dark Theme</span>
                  <span className="setting-desc">Toggle dark/light mode</span>
                </div>
                <button
                  className={`toggle-btn ${theme === 'dark' ? 'active' : ''}`}
                  onClick={toggleTheme}
                >
                  {theme === 'dark' ? '🌙' : '☀️'}
                </button>
              </div>
              <div className="setting-item">
                <div className="setting-label">
                  <span>🌐 Language</span>
                  <span className="setting-desc">Select your preferred language</span>
                </div>
                <select
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  className="language-select"
                >
                  <option value="en">English</option>
                  <option value="am">አማርኛ</option>
                </select>
              </div>
            </div>

            <div className="setting-group">
              <h3>Security</h3>
              <div className="setting-item">
                <div className="setting-label">
                  <span>🔐 Change Password</span>
                  <span className="setting-desc">Update your password</span>
                </div>
                <button className="btn-outline-small">Change</button>
              </div>
              <div className="setting-item">
                <div className="setting-label">
                  <span>📱 Two-Factor Auth</span>
                  <span className="setting-desc">Add extra security layer</span>
                </div>
                <button className="btn-outline-small">Enable</button>
              </div>
            </div>
          </div>
        );

      case 'notifications':
        return (
          <div className="notifications-tab">
            <div className="notification-item">
              <div className="notif-content">
                <span className="notif-title">Bet Placed</span>
                <span className="notif-desc">You placed a bet on Real Madrid vs Barcelona</span>
                <span className="notif-time">2 min ago</span>
              </div>
              <span className="notif-dot unread"></span>
            </div>
            <div className="notification-item">
              <div className="notif-content">
                <span className="notif-title">Bet Won! 🎉</span>
                <span className="notif-desc">You won 250 ETB on Aviator</span>
                <span className="notif-time">1 hour ago</span>
              </div>
              <span className="notif-dot read"></span>
            </div>
            <div className="notification-item">
              <div className="notif-content">
                <span className="notif-title">Promotion</span>
                <span className="notif-desc">New 100% deposit bonus available!</span>
                <span className="notif-time">3 hours ago</span>
              </div>
              <span className="notif-dot read"></span>
            </div>
          </div>
        );

      case 'responsible':
        return (
          <div className="responsible-tab">
            <h3>Responsible Gambling</h3>
            <p className="responsible-desc">
              We are committed to providing a safe and enjoyable gaming experience. 
              Set your limits below to stay in control.
            </p>
            <div className="limit-card">
              <div className="limit-item">
                <span>💰 Deposit Limit</span>
                <span>5,000 ETB / day</span>
                <button className="btn-outline-small">Edit</button>
              </div>
              <div className="limit-item">
                <span>⏰ Time Limit</span>
                <span>2 hours / day</span>
                <button className="btn-outline-small">Edit</button>
              </div>
              <div className="limit-item">
                <span>📊 Loss Limit</span>
                <span>10,000 ETB / week</span>
                <button className="btn-outline-small">Edit</button>
              </div>
            </div>
            <div className="responsible-actions">
              <button className="btn-warning">⏸️ Take a Break (24h)</button>
              <button className="btn-danger">🔒 Self-Exclusion</button>
            </div>
            <div className="responsible-links">
              <a href="/terms">Terms & Conditions</a>
              <a href="/privacy">Privacy Policy</a>
              <a href="/responsible">Responsible Gambling Policy</a>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="profile-screen">
      {/* Tab navigation */}
      <div className="profile-tabs">
        <button
          className={`tab-btn ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          <User className="h-4 w-4" />
          Profile
        </button>
        <button
          className={`tab-btn ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('settings')}
        >
          <Settings className="h-4 w-4" />
          Settings
        </button>
        <button
          className={`tab-btn ${activeTab === 'notifications' ? 'active' : ''}`}
          onClick={() => setActiveTab('notifications')}
        >
          <Bell className="h-4 w-4" />
          Notifications
        </button>
        <button
          className={`tab-btn ${activeTab === 'responsible' ? 'active' : ''}`}
          onClick={() => setActiveTab('responsible')}
        >
          <Shield className="h-4 w-4" />
          Responsible
        </button>
      </div>

      {/* Tab content */}
      <div className="profile-content">
        {renderTabContent()}
      </div>

      {/* Logout button */}
      <div className="logout-container">
        <button
          className="logout-btn"
          onClick={handleLogout}
          disabled={loading}
        >
          <LogOut className="h-4 w-4" />
          {loading ? 'Logging out...' : 'Logout'}
        </button>
      </div>
    </div>
  );
}