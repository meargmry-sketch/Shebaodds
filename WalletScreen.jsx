// WalletScreen.jsx – Wallet management: balance, deposit, withdraw, transactions
import React, { useState, useEffect } from 'react';
import { useAuth } from './contexts';
import { useTranslation } from './LanguageContext';
import { 
  Wallet, ArrowUpRight, ArrowDownLeft, Clock, 
  Filter, ChevronDown, RefreshCw, Copy, Check,
  TrendingUp, TrendingDown, CreditCard, Banknote, 
  Phone, Smartphone, AlertCircle, X, CheckCircle,
  History, Receipt, Percent
} from 'lucide-react';

export default function WalletScreen() {
  const { user } = useAuth?.() || {};
  const { t } = useTranslation?.() || { t: (key) => key };
  
  // State
  const [balance, setBalance] = useState(0);
  const [bonusBalance, setBonusBalance] = useState(0);
  const [transactions, setTransactions] = useState([]);
  const [filter, setFilter] = useState('all'); // all, deposit, withdraw, bet, bonus
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('transactions'); // transactions, deposit, withdraw
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState('telebirr');
  const [copySuccess, setCopySuccess] = useState(false);
  const [showDepositModal, setShowDepositModal] = useState(false);
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  // Simulated data – replace with real API calls
  useEffect(() => {
    if (user) {
      // Mock balance
      setBalance(523600.00);
      setBonusBalance(500.00);
      
      // Mock transactions
      const mockTransactions = [
        { id: 1, type: 'deposit', amount: 1000, method: 'Telebirr', status: 'completed', date: '2026-08-05T10:30:00Z', description: 'Deposit via Telebirr' },
        { id: 2, type: 'bet', amount: -100, method: 'Wallet', status: 'completed', date: '2026-08-05T09:15:00Z', description: 'Bet on Real Madrid vs Barcelona' },
        { id: 3, type: 'win', amount: 215, method: 'Wallet', status: 'completed', date: '2026-08-05T08:45:00Z', description: 'Won bet on Real Madrid vs Barcelona' },
        { id: 4, type: 'withdraw', amount: -500, method: 'Telebirr', status: 'pending', date: '2026-08-04T22:00:00Z', description: 'Withdraw request' },
        { id: 5, type: 'bonus', amount: 50, method: 'Bonus', status: 'completed', date: '2026-08-04T18:00:00Z', description: 'Welcome bonus credited' },
        { id: 6, type: 'bet', amount: -50, method: 'Wallet', status: 'completed', date: '2026-08-04T16:20:00Z', description: 'Bet on Arsenal vs Chelsea' },
        { id: 7, type: 'deposit', amount: 500, method: 'Bank Transfer', status: 'completed', date: '2026-08-04T14:00:00Z', description: 'Deposit via Bank Transfer' },
        { id: 8, type: 'tax', amount: -32.25, method: 'Tax', status: 'completed', date: '2026-08-04T12:00:00Z', description: 'Tax on winnings' },
        { id: 9, type: 'win', amount: 123.75, method: 'Wallet', status: 'completed', date: '2026-08-03T22:30:00Z', description: 'Won bet on Barcelona vs Sevilla' },
        { id: 10, type: 'withdraw', amount: -200, method: 'Telebirr', status: 'completed', date: '2026-08-03T20:00:00Z', description: 'Withdraw to Telebirr' },
      ];
      setTransactions(mockTransactions);
      setLoading(false);
    } else {
      setLoading(false);
    }
  }, [user]);

  // Filter transactions
  const filteredTransactions = transactions.filter(tx => {
    if (filter === 'all') return true;
    return tx.type === filter;
  });

  // Calculate totals
  const totalDeposited = transactions.filter(tx => tx.type === 'deposit').reduce((sum, tx) => sum + tx.amount, 0);
  const totalWithdrawn = transactions.filter(tx => tx.type === 'withdraw').reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  const totalWon = transactions.filter(tx => tx.type === 'win').reduce((sum, tx) => sum + tx.amount, 0);
  const totalTax = transactions.filter(tx => tx.type === 'tax').reduce((sum, tx) => sum + Math.abs(tx.amount), 0);
  const netProfit = totalWon - totalTax - totalWithdrawn;

  // Format date
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Status badge
  const StatusBadge = ({ status }) => {
    const config = {
      completed: { label: 'Completed', className: 'status-completed', icon: <CheckCircle className="h-3 w-3" /> },
      pending: { label: 'Pending', className: 'status-pending', icon: <Clock className="h-3 w-3" /> },
      failed: { label: 'Failed', className: 'status-failed', icon: <XCircle className="h-3 w-3" /> },
    };
    const { label, className, icon } = config[status] || config.completed;
    return (
      <span className={`status-badge ${className}`}>
        {icon} {label}
      </span>
    );
  };

  // Type icon
  const TypeIcon = ({ type }) => {
    const icons = {
      deposit: <ArrowDownLeft className="h-4 w-4 text-green-400" />,
      withdraw: <ArrowUpRight className="h-4 w-4 text-rose-400" />,
      bet: <Clock className="h-4 w-4 text-slate-400" />,
      win: <TrendingUp className="h-4 w-4 text-emerald-400" />,
      bonus: <Percent className="h-4 w-4 text-amber-400" />,
      tax: <Percent className="h-4 w-4 text-rose-400" />,
    };
    return icons[type] || <Clock className="h-4 w-4 text-slate-400" />;
  };

  // Quick deposit amounts
  const quickAmounts = [100, 500, 1000, 2000, 5000];

  // Handle deposit
  const handleDeposit = () => {
    const amt = parseFloat(amount);
    if (!amt || amt < 10) {
      alert('Minimum deposit is 10 ETB');
      return;
    }
    // Simulate deposit
    setBalance(prev => prev + amt);
    setTransactions(prev => [
      { id: Date.now(), type: 'deposit', amount: amt, method, status: 'completed', date: new Date().toISOString(), description: `Deposit via ${method}` },
      ...prev
    ]);
    setAmount('');
    setShowDepositModal(false);
    alert(`✅ Deposit of ${amt} ETB successful!`);
  };

  // Handle withdraw
  const handleWithdraw = () => {
    const amt = parseFloat(amount);
    if (!amt || amt < 20) {
      alert('Minimum withdrawal is 20 ETB');
      return;
    }
    if (amt > balance) {
      alert('Insufficient balance');
      return;
    }
    setBalance(prev => prev - amt);
    setTransactions(prev => [
      { id: Date.now(), type: 'withdraw', amount: -amt, method, status: 'pending', date: new Date().toISOString(), description: `Withdraw via ${method}` },
      ...prev
    ]);
    setAmount('');
    setShowWithdrawModal(false);
    alert(`⏳ Withdrawal request of ${amt} ETB submitted for processing.`);
  };

  // Copy address (for crypto/deposit)
  const copyAddress = (address) => {
    navigator.clipboard.writeText(address);
    setCopySuccess(true);
    setTimeout(() => setCopySuccess(false), 2000);
  };

  if (loading) {
    return <div className="loading-spinner">Loading wallet...</div>;
  }

  if (!user) {
    return (
      <div className="empty-state">
        <span>🔒 Please login to view your wallet</span>
        <a href="/login" className="btn-primary">Login</a>
      </div>
    );
  }

  return (
    <div className="wallet-screen">
      {/* Page header */}
      <div className="page-header">
        <h1>💰 {t('wallet') || 'Wallet'}</h1>
        <button className="refresh-btn" onClick={() => window.location.reload()}>
          <RefreshCw className="h-4 w-4" />
        </button>
      </div>

      {/* Balance summary cards */}
      <div className="balance-summary">
        <div className="balance-card-main">
          <div className="balance-label">
            <Wallet className="h-5 w-5 text-amber-400" />
            <span>Available Balance</span>
          </div>
          <div className="balance-amount">{balance.toLocaleString()} ETB</div>
          <div className="balance-sub">
            <span>Bonus: {bonusBalance.toLocaleString()} ETB</span>
            <span>💳 Secure Ledger</span>
          </div>
        </div>
        <div className="balance-stats">
          <div className="stat">
            <span className="stat-label">Total Deposited</span>
            <span className="stat-value">{totalDeposited.toLocaleString()} ETB</span>
          </div>
          <div className="stat">
            <span className="stat-label">Total Withdrawn</span>
            <span className="stat-value">{totalWithdrawn.toLocaleString()} ETB</span>
          </div>
          <div className="stat">
            <span className="stat-label">Total Won</span>
            <span className="stat-value">{totalWon.toLocaleString()} ETB</span>
          </div>
          <div className="stat">
            <span className="stat-label">Tax Paid</span>
            <span className="stat-value">{totalTax.toLocaleString()} ETB</span>
          </div>
          <div className="stat">
            <span className="stat-label">Net Profit</span>
            <span className={`stat-value ${netProfit >= 0 ? 'positive' : 'negative'}`}>
              {netProfit >= 0 ? '+' : ''}{netProfit.toLocaleString()} ETB
            </span>
          </div>
        </div>
      </div>

      {/* Quick actions */}
      <div className="wallet-actions">
        <button className="action-btn deposit" onClick={() => setShowDepositModal(true)}>
          <ArrowDownLeft className="h-5 w-5" />
          Deposit
        </button>
        <button className="action-btn withdraw" onClick={() => setShowWithdrawModal(true)}>
          <ArrowUpRight className="h-5 w-5" />
          Withdraw
        </button>
        <button className="action-btn history" onClick={() => setActiveTab('transactions')}>
          <History className="h-5 w-5" />
          History
        </button>
        <button className="action-btn tax" onClick={() => window.location.href = '/tax'}>
          <Percent className="h-5 w-5" />
          Tax Center
        </button>
      </div>

      {/* Transactions section */}
      <div className="transactions-section">
        <div className="section-header">
          <h2>📜 {t('transaction_history') || 'Transaction History'}</h2>
          <div className="filter-group">
            <Filter className="h-4 w-4 text-slate-400" />
            <button
              className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
              onClick={() => setFilter('all')}
            >
              All
            </button>
            <button
              className={`filter-btn ${filter === 'deposit' ? 'active' : ''}`}
              onClick={() => setFilter('deposit')}
            >
              Deposits
            </button>
            <button
              className={`filter-btn ${filter === 'withdraw' ? 'active' : ''}`}
              onClick={() => setFilter('withdraw')}
            >
              Withdrawals
            </button>
            <button
              className={`filter-btn ${filter === 'bet' ? 'active' : ''}`}
              onClick={() => setFilter('bet')}
            >
              Bets
            </button>
            <button
              className={`filter-btn ${filter === 'win' ? 'active' : ''}`}
              onClick={() => setFilter('win')}
            >
              Wins
            </button>
            <button
              className={`filter-btn ${filter === 'bonus' ? 'active' : ''}`}
              onClick={() => setFilter('bonus')}
            >
              Bonus
            </button>
          </div>
        </div>

        {filteredTransactions.length === 0 ? (
          <div className="empty-state">
            <span>📭 No transactions found</span>
            <p>Your transactions will appear here.</p>
          </div>
        ) : (
          <div className="transaction-list">
            {filteredTransactions.map((tx) => (
              <div key={tx.id} className="transaction-item">
                <div className="tx-left">
                  <div className="tx-icon">
                    <TypeIcon type={tx.type} />
                  </div>
                  <div className="tx-info">
                    <div className="tx-description">{tx.description}</div>
                    <div className="tx-meta">
                      <span className="tx-date">{formatDate(tx.date)}</span>
                      <span className="tx-method">{tx.method}</span>
                    </div>
                  </div>
                </div>
                <div className="tx-right">
                  <div className={`tx-amount ${tx.amount >= 0 ? 'positive' : 'negative'}`}>
                    {tx.amount >= 0 ? '+' : ''}{tx.amount.toFixed(2)} ETB
                  </div>
                  <StatusBadge status={tx.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Deposit Modal */}
      {showDepositModal && (
        <div className="modal-overlay" onClick={() => setShowDepositModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>💰 Deposit Funds</h3>
              <button className="close-btn" onClick={() => setShowDepositModal(false)}>
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="modal-body">
              <div className="method-selector">
                <button
                  className={`method-btn ${method === 'telebirr' ? 'active' : ''}`}
                  onClick={() => setMethod('telebirr')}
                >
                  <Phone className="h-5 w-5" />
                  Telebirr
                </button>
                <button
                  className={`method-btn ${method === 'bank' ? 'active' : ''}`}
                  onClick={() => setMethod('bank')}
                >
                  <CreditCard className="h-5 w-5" />
                  Bank Transfer
                </button>
                <button
                  className={`method-btn ${method === 'crypto' ? 'active' : ''}`}
                  onClick={() => setMethod('crypto')}
                >
                  <Smartphone className="h-5 w-5" />
                  Crypto
                </button>
              </div>
              <div className="quick-amounts">
                {quickAmounts.map((amt) => (
                  <button
                    key={amt}
                    className={`quick-amt ${parseFloat(amount) === amt ? 'active' : ''}`}
                    onClick={() => setAmount(String(amt))}
                  >
                    {amt}
                  </button>
                ))}
              </div>
              <div className="amount-input">
                <label>Amount (ETB)</label>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="Enter amount"
                  min="10"
                />
              </div>
              {method === 'telebirr' && (
                <div className="method-details">
                  <p>📱 Telebirr Number: <strong>09XXXXXXXX</strong></p>
                  <p>Please send the exact amount to this number.</p>
                </div>
              )}
              {method === 'bank' && (
                <div className="method-details">
                  <p>🏦 Bank: <strong>Commercial Bank of Ethiopia</strong></p>
                  <p>Account: <strong>1234567890</strong></p>
                  <p>Name: <strong>SHEBAODDS</strong></p>
                </div>
              )}
              {method === 'crypto' && (
                <div className="method-details">
                  <p>💳 Bitcoin Address:</p>
                  <div className="address-box">
                    <code>1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa</code>
                    <button onClick={() => copyAddress('1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa')}>
                      {copySuccess ? <Check className="h-4 w-4 text-green-400" /> : <Copy className="h-4 w-4" />}
                    </button>
                  </div>
                </div>
              )}
              <button className="btn-primary full-width" onClick={handleDeposit}>
                Confirm Deposit
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Withdraw Modal */}
      {showWithdrawModal && (
        <div className="modal-overlay" onClick={() => setShowWithdrawModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>💸 Withdraw Funds</h3>
              <button className="close-btn" onClick={() => setShowWithdrawModal(false)}>
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="modal-body">
              <div className="method-selector">
                <button
                  className={`method-btn ${method === 'telebirr' ? 'active' : ''}`}
                  onClick={() => setMethod('telebirr')}
                >
                  <Phone className="h-5 w-5" />
                  Telebirr
                </button>
                <button
                  className={`method-btn ${method === 'bank' ? 'active' : ''}`}
                  onClick={() => setMethod('bank')}
                >
                  <CreditCard className="h-5 w-5" />
                  Bank Transfer
                </button>
              </div>
              <div className="balance-info">
                <span>Available: <strong>{balance.toLocaleString()} ETB</strong></span>
                <span>Min: 20 ETB</span>
              </div>
              <div className="amount-input">
                <label>Amount (ETB)</label>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="Enter amount"
                  min="20"
                  max={balance}
                />
              </div>
              {method === 'telebirr' && (
                <div className="method-details">
                  <p>📱 Telebirr Number: <strong>09XXXXXXXX</strong></p>
                  <p>We will send funds to this number.</p>
                </div>
              )}
              {method === 'bank' && (
                <div className="method-details">
                  <p>🏦 Bank Account Details</p>
                  <input type="text" placeholder="Account Number" className="input-field" />
                  <input type="text" placeholder="Account Name" className="input-field" />
                  <select className="input-field">
                    <option>Commercial Bank of Ethiopia</option>
                    <option>Awash Bank</option>
                    <option>Dashen Bank</option>
                  </select>
                </div>
              )}
              <button className="btn-primary full-width" onClick={handleWithdraw}>
                Submit Withdrawal
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}