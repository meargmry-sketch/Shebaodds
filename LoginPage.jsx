import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, Fingerprint } from "lucide-react";
import { useAuth } from "./AuthContext";
import { useTranslation } from "./LanguageContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { t } = useTranslation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (event) => {
    event.preventDefault();

    setError("");

    if (!email.trim() || !password) {
      setError(
        t("fill_all_fields") || "Please enter your email and password."
      );
      return;
    }

    setLoading(true);

    try {
      const result = await login(email.trim(), password);

      if (result?.success) {
        navigate("/", { replace: true });
      } else {
        setError(
          result?.message ||
            t("invalid_credentials") ||
            "Invalid email or password."
        );
      }
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          t("something_wrong") ||
          "Something went wrong. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        {/* Logo / Brand */}
        <div className="login-brand">
          <div className="login-logo">S</div>

          <h1>ShebaOdds</h1>

          <p>
            {t("login_tagline") || "Smart Bets. Real Wins."}
          </p>
        </div>

        {/* Login Card */}
        <div className="login-card">
          <div className="login-card-header">
            <h2>
              {t("welcome_back") || "Welcome Back"}
            </h2>

            <p>
              {t("login_sub") ||
                "Sign in to access your ShebaOdds account"}
            </p>
          </div>

          {error && (
            <div className="login-error" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleLogin}>
            {/* Email */}
            <div className="form-group">
              <label htmlFor="email">
                {t("email") || "Email"}
              </label>

              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                autoComplete="email"
                disabled={loading}
              />
            </div>

            {/* Password */}
            <div className="form-group">
              <label htmlFor="password">
                {t("password") || "Password"}
              </label>

              <div className="password-input-wrapper">
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(event) =>
                    setPassword(event.target.value)
                  }
                  placeholder={
                    t("enter_password") || "Enter your password"
                  }
                  autoComplete="current-password"
                  disabled={loading}
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowPassword((current) => !current)
                  }
                  aria-label={
                    showPassword
                      ? "Hide password"
                      : "Show password"
                  }
                >
                  {showPassword ? (
                    <EyeOff size={20} />
                  ) : (
                    <Eye size={20} />
                  )}
                </button>
              </div>
            </div>

            {/* Forgot password */}
            <div className="login-options">
              <Link to="/forgot-password">
                {t("forgot_password") || "Forgot password?"}
              </Link>
            </div>

            {/* Login */}
            <button
              type="submit"
              className="login-button"
              disabled={loading}
            >
              {loading
                ? t("logging_in") || "Signing in..."
                : t("login") || "Login"}
            </button>
          </form>

          {/* Biometric */}
          <div className="biometric-section">
            <div className="divider">
              <span>OR</span>
            </div>

            <button
              type="button"
              className="biometric-button"
              onClick={() =>
                setError(
                  "Biometric login is not available in the web version."
                )
              }
            >
              <Fingerprint size={22} />

              <span>
                {t("login_with_biometric") ||
                  "Login with biometric"}
              </span>
            </button>
          </div>

          {/* Register */}
          <div className="register-section">
            <span>
              {t("dont_have_account") ||
                "Don't have an account?"}
            </span>

            <Link to="/register">
              {t("create_account") || "Create account"}
            </Link>
          </div>
        </div>

        {/* Security */}
        <div className="login-security">
          🔒{" "}
          {t("secure_login_note") ||
            "Your connection is secure and encrypted"}
        </div>
      </div>
    </div>
  );
}