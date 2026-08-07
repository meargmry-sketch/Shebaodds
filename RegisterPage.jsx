import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { useAuth } from "./AuthContext";
import { useTranslation } from "./LanguageContext";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { t } = useTranslation();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const validatePassword = (password) => {
    return (
      password.length >= 8 &&
      /[A-Z]/.test(password) &&
      /[a-z]/.test(password) &&
      /[0-9]/.test(password) &&
      /[!@#$%^&*(),.?":{}|<>]/.test(password)
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (
      !form.name.trim() ||
      !form.email.trim() ||
      !form.password ||
      !form.confirmPassword
    ) {
      setError(
        t("fill_all_fields") ||
          "Please fill in all required fields."
      );
      return;
    }

    if (!validatePassword(form.password)) {
      setError(
        t("password_requirements") ||
          "Password must contain at least 8 characters, including uppercase, lowercase, number, and special character."
      );
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError(
        t("passwords_do_not_match") ||
          "Passwords do not match."
      );
      return;
    }

    setLoading(true);

    try {
      const result = await register({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
      });

      if (result?.success) {
        setSuccess(
          t("registration_success") ||
            "Account created successfully."
        );

        navigate("/", { replace: true });
      } else {
        setError(
          result?.message ||
            t("registration_failed") ||
            "Registration failed. Please try again."
        );
      }
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          "Registration failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">
      <div className="register-container">

        {/* Brand */}
        <div className="register-brand">
          <div className="register-logo">S</div>

          <h1>ShebaOdds</h1>

          <p>
            {t("register_tagline") ||
              "Create your account and start betting."}
          </p>
        </div>

        {/* Card */}
        <div className="register-card">

          <div className="register-card-header">
            <h2>
              {t("create_account") ||
                "Create Account"}
            </h2>

            <p>
              {t("register_sub") ||
                "Create your ShebaOdds account"}
            </p>
          </div>

          {error && (
            <div className="register-error" role="alert">
              {error}
            </div>
          )}

          {success && (
            <div className="register-success" role="status">
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit}>

            {/* Name */}
            <div className="form-group">
              <label htmlFor="name">
                {t("name") || "Full Name"}
              </label>

              <input
                id="name"
                name="name"
                type="text"
                value={form.name}
                onChange={handleChange}
                placeholder="Your full name"
                autoComplete="name"
                disabled={loading}
              />
            </div>

            {/* Email */}
            <div className="form-group">
              <label htmlFor="email">
                {t("email") || "Email"}
              </label>

              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
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
                  name="password"
                  type={
                    showPassword
                      ? "text"
                      : "password"
                  }
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Create a password"
                  autoComplete="new-password"
                  disabled={loading}
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowPassword(
                      (current) => !current
                    )
                  }
                >
                  {showPassword ? (
                    <EyeOff size={20} />
                  ) : (
                    <Eye size={20} />
                  )}
                </button>
              </div>

              <small>
                Minimum 8 characters, uppercase,
                lowercase, number and special
                character.
              </small>
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label htmlFor="confirmPassword">
                {t("confirm_password") ||
                  "Confirm Password"}
              </label>

              <div className="password-input-wrapper">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={
                    showConfirmPassword
                      ? "text"
                      : "password"
                  }
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Confirm your password"
                  autoComplete="new-password"
                  disabled={loading}
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowConfirmPassword(
                      (current) => !current
                    )
                  }
                >
                  {showConfirmPassword ? (
                    <EyeOff size={20} />
                  ) : (
                    <Eye size={20} />
                  )}
                </button>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              className="register-button"
              disabled={loading}
            >
              {loading
                ? t("creating_account") ||
                  "Creating Account..."
                : t("register") || "Create Account"}
            </button>
          </form>

          {/* Login */}
          <div className="login-link">
            <span>
              {t("already_have_account") ||
                "Already have an account?"}
            </span>

            <Link to="/login">
              {t("login") || "Login"}
            </Link>
          </div>
        </div>

        <div className="register-security">
          🔒 Your information is securely
          encrypted.
        </div>
      </div>
    </div>
  );
}