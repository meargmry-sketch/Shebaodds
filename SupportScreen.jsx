// SupportScreen.jsx – Help center, FAQ, contact, and AI predictions
import React, { useState } from 'react';
import { useTranslation } from './LanguageContext';
import { 
  HelpCircle, MessageCircle, Mail, Phone, 
  ChevronDown, ChevronUp, Send, Clock, 
  CheckCircle, ExternalLink, FileText, 
  Users, Shield, Smartphone, Headphones,
  AlertCircle, X, Sparkles, Zap
} from 'lucide-react';
import axios from 'axios';

export default function SupportScreen() {
  const { t } = useTranslation?.() || { t: (key) => key };
  const [openFaq, setOpenFaq] = useState(null);
  const [activeTab, setActiveTab] = useState('faq'); // faq, contact, ai
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });
  const [formStatus, setFormStatus] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // AI Prediction state
  const [aiMatch, setAiMatch] = useState({
    homeTeam: '',
    awayTeam: '',
    league: '',
    market: '1X2',
  });
  const [aiPrediction, setAiPrediction] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);

  // FAQ data (unchanged)
  const faqs = [
    { id: 1, question: 'How do I create an account?', answer: 'Click on the "Register" button at the top right corner. Fill in your details, verify your email, and you\'re ready to start betting!' },
    { id: 2, question: 'What payment methods are accepted?', answer: 'We accept Telebirr, bank transfers, and cryptocurrency (Bitcoin, Ethereum, and USDT). All deposits are processed instantly.' },
    { id: 3, question: 'How long do withdrawals take?', answer: 'Withdrawals are processed within 24 hours. Telebirr withdrawals are instant, bank transfers take 1-3 business days.' },
    { id: 4, question: 'What is the minimum deposit?', answer: 'The minimum deposit is 50 ETB for Telebirr and 100 ETB for bank transfers. Cryptocurrency deposits require a minimum equivalent of 500 ETB.' },
    { id: 5, question: 'How do I claim a bonus?', answer: 'Visit the Promotions page and click "Claim" on any active offer. Some bonuses are automatically credited upon deposit.' },
    { id: 6, question: 'What is the tax rate on winnings?', answer: 'A 15% withholding tax applies to winnings over 100 ETB. Tax is automatically deducted before payout.' },
    { id: 7, question: 'How do I set responsible gambling limits?', answer: 'Go to your Profile → Responsible Gambling to set deposit limits, time limits, and loss limits. You can also self-exclude at any time.' },
    { id: 8, question: 'Can I cancel a bet?', answer: 'Once a bet is placed, it cannot be cancelled. Please double-check your selections before confirming.' },
  ];

  const toggleFaq = (id) => setOpenFaq(openFaq === id ? null : id);

  // Handle contact form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.email || !formData.message) {
      setFormStatus({ type: 'error', message: 'Please fill in all required fields.' });
      return;
    }
    setSubmitting(true);
    // Simulate API call
    setTimeout(() => {
      setFormStatus({ type: 'success', message: 'Your message has been sent. We\'ll reply within 24 hours!' });
      setFormData({ name: '', email: '', subject: '', message: '' });
      setSubmitting(false);
    }, 1500);
  };

  // Handle AI prediction
  const handleAIPrediction = async () => {
    if (!aiMatch.homeTeam || !aiMatch.awayTeam) {
      setFormStatus({ type: 'error', message: 'Please enter both teams.' });
      return;
    }
    setAiLoading(true);
    setAiPrediction(null);
    try {
      // Use your Gemini API or other AI service
      const response = await axios.post('/api/ai/predict', {
        homeTeam: aiMatch.homeTeam,
        awayTeam: aiMatch.awayTeam,
        league: aiMatch.league || 'Unknown League',
        market: aiMatch.market,
      });
      // Or use the existing GeminiApi helper (if you have it)
      // const result = await GeminiApi.getPrediction(aiMatch);
      setAiPrediction(response.data.prediction);
      setFormStatus(null);
    } catch (error) {
      // Fallback mock prediction (for demo)
      setTimeout(() => {
        const mockPred = {
          recommendation: Math.random() > 0.5 ? 'Home Win' : 'Draw',
          confidence: (Math.random() * 30 + 60).toFixed(1) + '%',
          analysis: 'Based on recent form and head-to-head statistics, the home team has a slight advantage. However, the away side is strong on the counter-attack. We recommend a cautious approach.',
          predictedScore: `${Math.floor(Math.random()*3)}-${Math.floor(Math.random()*2)}`,
        };
        setAiPrediction(mockPred);
        setFormStatus(null);
      }, 1500);
    } finally {
      setAiLoading(false);
    }
  };

  // Support channels
  const channels = [
    { icon: <MessageCircle className="h-5 w-5" />, label: 'Live Chat', desc: 'Chat with our team 24/7', action: 'Start Chat', link: '#chat' },
    { icon: <Mail className="h-5 w-5" />, label: 'Email Support', desc: 'support@shebaodds.com', action: 'Send Email', link: 'mailto:support@shebaodds.com' },
    { icon: <Phone className="h-5 w-5" />, label: 'Phone Support', desc: '+251 9X XXX XXXX', action: 'Call Now', link: 'tel:+2519XXXXXXXX' },
    { icon: <Users className="h-5 w-5" />, label: 'Community', desc: 'Join our Telegram group', action: 'Join Group', link: 'https://t.me/shebaodds' },
  ];

  return (
    <div className="support-screen">
      {/* Page header */}
      <div className="page-header">
        <h1>💬 {t('support') || 'Support Center'}</h1>
        <span className="support-status">
          <span className="status-dot"></span>
          Online
        </span>
      </div>

      {/* Quick support channels (only on FAQ tab) */}
      {activeTab === 'faq' && (
        <div className="support-channels">
          {channels.map((channel, index) => (
            <a key={index} href={channel.link} className="channel-card">
              <div className="channel-icon">{channel.icon}</div>
              <div className="channel-info">
                <span className="channel-label">{channel.label}</span>
                <span className="channel-desc">{channel.desc}</span>
              </div>
              <span className="channel-action">
                {channel.action} <ExternalLink className="h-3 w-3" />
              </span>
            </a>
          ))}
        </div>
      )}

      {/* Tab navigation */}
      <div className="support-tabs">
        <button
          className={`support-tab ${activeTab === 'faq' ? 'active' : ''}`}
          onClick={() => setActiveTab('faq')}
        >
          <HelpCircle className="h-4 w-4" /> FAQ
        </button>
        <button
          className={`support-tab ${activeTab === 'contact' ? 'active' : ''}`}
          onClick={() => setActiveTab('contact')}
        >
          <Mail className="h-4 w-4" /> Contact
        </button>
        <button
          className={`support-tab ${activeTab === 'ai' ? 'active' : ''}`}
          onClick={() => setActiveTab('ai')}
        >
          <Sparkles className="h-4 w-4" /> AI Predictor
        </button>
      </div>

      {/* Tab content */}
      <div className="support-content">
        {/* FAQ tab */}
        {activeTab === 'faq' && (
          <div className="faq-section">
            <h2>📖 Frequently Asked Questions</h2>
            <div className="faq-list">
              {faqs.map((faq) => (
                <div key={faq.id} className={`faq-item ${openFaq === faq.id ? 'open' : ''}`}>
                  <button className="faq-question" onClick={() => toggleFaq(faq.id)}>
                    <span>{faq.question}</span>
                    {openFaq === faq.id ? (
                      <ChevronUp className="h-5 w-5 text-amber-400" />
                    ) : (
                      <ChevronDown className="h-5 w-5 text-slate-400" />
                    )}
                  </button>
                  {openFaq === faq.id && (
                    <div className="faq-answer">
                      <p>{faq.answer}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Contact tab */}
        {activeTab === 'contact' && (
          <div className="contact-section">
            <h2>✉️ Contact Us</h2>
            <p className="contact-sub">Can't find what you're looking for? Send us a message and we'll get back to you.</p>
            <form className="contact-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Full Name *</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="Your name"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Email Address *</label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    placeholder="you@example.com"
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Subject</label>
                <input
                  type="text"
                  value={formData.subject}
                  onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                  placeholder="Brief subject"
                />
              </div>
              <div className="form-group">
                <label>Message *</label>
                <textarea
                  value={formData.message}
                  onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                  placeholder="Describe your issue or question..."
                  rows="5"
                  required
                ></textarea>
              </div>
              <button type="submit" className="submit-btn" disabled={submitting}>
                {submitting ? <>⏳ Sending...</> : <><Send className="h-4 w-4" /> Send Message</>}
              </button>
              {formStatus && (
                <div className={`form-status ${formStatus.type}`}>
                  {formStatus.type === 'success' ? <CheckCircle className="h-4 w-4" /> : <AlertCircle className="h-4 w-4" />}
                  {formStatus.message}
                  <button className="status-close" onClick={() => setFormStatus(null)}><X className="h-4 w-4" /></button>
                </div>
              )}
            </form>
          </div>
        )}

        {/* AI Predictor tab */}
        {activeTab === 'ai' && (
          <div className="ai-predictor">
            <h2>🤖 AI Match Predictor</h2>
            <p className="ai-sub">
              Enter the match details and get an AI-generated prediction powered by Gemini.
            </p>
            <div className="ai-form">
              <div className="form-group">
                <label>Home Team *</label>
                <input
                  type="text"
                  value={aiMatch.homeTeam}
                  onChange={(e) => setAiMatch({ ...aiMatch, homeTeam: e.target.value })}
                  placeholder="e.g. Real Madrid"
                />
              </div>
              <div className="form-group">
                <label>Away Team *</label>
                <input
                  type="text"
                  value={aiMatch.awayTeam}
                  onChange={(e) => setAiMatch({ ...aiMatch, awayTeam: e.target.value })}
                  placeholder="e.g. Barcelona"
                />
              </div>
              <div className="form-group">
                <label>League (optional)</label>
                <input
                  type="text"
                  value={aiMatch.league}
                  onChange={(e) => setAiMatch({ ...aiMatch, league: e.target.value })}
                  placeholder="e.g. La Liga"
                />
              </div>
              <div className="form-group">
                <label>Market</label>
                <select
                  value={aiMatch.market}
                  onChange={(e) => setAiMatch({ ...aiMatch, market: e.target.value })}
                  className="market-select"
                >
                  <option value="1X2">1X2 (Home / Draw / Away)</option>
                  <option value="Over/Under">Over/Under 2.5</option>
                  <option value="BTTS">Both Teams to Score</option>
                  <option value="Correct Score">Correct Score</option>
                </select>
              </div>
              <button
                className="predict-btn"
                onClick={handleAIPrediction}
                disabled={aiLoading}
              >
                {aiLoading ? (
                  <>⏳ Analyzing...</>
                ) : (
                  <>
                    <Zap className="h-4 w-4" />
                    Get Prediction
                  </>
                )}
              </button>
            </div>

            {aiPrediction && (
              <div className="ai-result">
                <h3>Prediction</h3>
                <div className="result-card">
                  <div className="result-recommendation">
                    <span className="rec-label">Recommendation</span>
                    <span className="rec-value">{aiPrediction.recommendation}</span>
                  </div>
                  <div className="result-confidence">
                    <span className="conf-label">Confidence</span>
                    <span className="conf-value">{aiPrediction.confidence}</span>
                  </div>
                  <div className="result-score">
                    <span className="score-label">Predicted Score</span>
                    <span className="score-value">{aiPrediction.predictedScore}</span>
                  </div>
                  <div className="result-analysis">
                    <span className="analysis-label">Analysis</span>
                    <p className="analysis-text">{aiPrediction.analysis}</p>
                  </div>
                </div>
                <p className="ai-disclaimer">
                  ⚠️ This is an AI-generated prediction for entertainment purposes only. 
                  Please gamble responsibly.
                </p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Resources (shown on all tabs) */}
      <div className="resources-section">
        <h2>📚 Helpful Resources</h2>
        <div className="resources-grid">
          <a href="/terms" className="resource-card">
            <FileText className="h-6 w-6 text-amber-400" />
            <span>Terms & Conditions</span>
          </a>
          <a href="/privacy" className="resource-card">
            <Shield className="h-6 w-6 text-amber-400" />
            <span>Privacy Policy</span>
          </a>
          <a href="/responsible" className="resource-card">
            <Smartphone className="h-6 w-6 text-amber-400" />
            <span>Responsible Gambling</span>
          </a>
          <a href="/tax" className="resource-card">
            <Clock className="h-6 w-6 text-amber-400" />
            <span>Tax Center</span>
          </a>
        </div>
      </div>
    </div>
  );
}