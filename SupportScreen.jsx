// SupportScreen.jsx – Help center, FAQ, and contact support
import React, { useState } from 'react';
import { useTranslation } from './LanguageContext';
import { 
  HelpCircle, MessageCircle, Mail, Phone, 
  ChevronDown, ChevronUp, Send, Clock, 
  CheckCircle, ExternalLink, FileText, 
  Users, Shield, Smartphone, Headphones,
  AlertCircle, X
} from 'lucide-react';

export default function SupportScreen() {
  const { t } = useTranslation?.() || { t: (key) => key };
  const [openFaq, setOpenFaq] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });
  const [formStatus, setFormStatus] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // FAQ data
  const faqs = [
    {
      id: 1,
      question: 'How do I create an account?',
      answer: 'Click on the "Register" button at the top right corner. Fill in your details, verify your email, and you\'re ready to start betting!'
    },
    {
      id: 2,
      question: 'What payment methods are accepted?',
      answer: 'We accept Telebirr, bank transfers, and cryptocurrency (Bitcoin, Ethereum, and USDT). All deposits are processed instantly.'
    },
    {
      id: 3,
      question: 'How long do withdrawals take?',
      answer: 'Withdrawals are processed within 24 hours. Telebirr withdrawals are instant, bank transfers take 1-3 business days.'
    },
    {
      id: 4,
      question: 'What is the minimum deposit?',
      answer: 'The minimum deposit is 50 ETB for Telebirr and 100 ETB for bank transfers. Cryptocurrency deposits require a minimum equivalent of 500 ETB.'
    },
    {
      id: 5,
      question: 'How do I claim a bonus?',
      answer: 'Visit the Promotions page and click "Claim" on any active offer. Some bonuses are automatically credited upon deposit.'
    },
    {
      id: 6,
      question: 'What is the tax rate on winnings?',
      answer: 'A 15% withholding tax applies to winnings over 100 ETB. Tax is automatically deducted before payout.'
    },
    {
      id: 7,
      question: 'How do I set responsible gambling limits?',
      answer: 'Go to your Profile → Responsible Gambling to set deposit limits, time limits, and loss limits. You can also self-exclude at any time.'
    },
    {
      id: 8,
      question: 'Can I cancel a bet?',
      answer: 'Once a bet is placed, it cannot be cancelled. Please double-check your selections before confirming.'
    },
  ];

  const toggleFaq = (id) => {
    setOpenFaq(openFaq === id ? null : id);
  };

  // Handle form submission
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
        <h1>💬 {t('support') || 'Support'}</h1>
        <span className="support-status">
          <span className="status-dot"></span>
          Online
        </span>
      </div>

      {/* Quick support channels */}
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

      {/* FAQ section */}
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

      {/* Contact form */}
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
            {submitting ? (
              <>⏳ Sending...</>
            ) : (
              <>
                <Send className="h-4 w-4" />
                Send Message
              </>
            )}
          </button>
          {formStatus && (
            <div className={`form-status ${formStatus.type}`}>
              {formStatus.type === 'success' ? (
                <CheckCircle className="h-4 w-4" />
              ) : (
                <AlertCircle className="h-4 w-4" />
              )}
              {formStatus.message}
              {formStatus.type === 'success' && (
                <button className="status-close" onClick={() => setFormStatus(null)}>
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>
          )}
        </form>
      </div>

      {/* Additional resources */}
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