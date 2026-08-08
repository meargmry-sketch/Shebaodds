// AdminLayout.jsx
import React, { useState } from "react";
import {
  Link,
  useLocation,
  Outlet,
  Navigate,
} from "react-router-dom";

import { useAuth } from "./AuthContext.jsx";
import { useTranslation } from "./LanguageContext.jsx";

import {
  LayoutDashboard,
  Users,
  Trophy,
  Briefcase,
  Gamepad2,
  Coins,
  ArrowUpRight,
  ArrowDownLeft,
  Receipt,
  BarChart3,
  Gift,
  Settings,
  Shield,
  FileText,
  MessageSquare,
  LogOut,
  ChevronDown,
  ChevronRight,
  Menu,
  X,
} from "lucide-react";

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const { t } = useTranslation();
  const location = useLocation();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  const [expandedSections, setExpandedSections] = useState({
    finance: true,
  });

  /*
   * Admin menu
   */
  const menuItems = [
    {
      path: "/admin",
      label: "Dashboard",
      icon: <LayoutDashboard size={20} />,
    },

    {
      path: "/admin/users",
      label: "Users",
      icon: <Users size={20} />,
    },

    {
      path: "/admin/matches",
      label: "Matches & Odds",
      icon: <Trophy size={20} />,
    },

    {
      path: "/admin/bets",
      label: "Bet Management",
      icon: <Briefcase size={20} />,
    },

    {
      path: "/admin/casino",
      label: "Casino",
      icon: <Gamepad2 size={20} />,
    },

    {
      label: "Finance",
      icon: <Coins size={20} />,
      section: "finance",
      children: [
        {
          path: "/admin/deposits",
          label: "Deposits",
          icon: <ArrowDownLeft size={16} />,
        },
        {
          path: "/admin/withdrawals",
          label: "Withdrawals",
          icon: <ArrowUpRight size={16} />,
        },
        {
          path: "/admin/transactions",
          label: "All Transactions",
          icon: <Receipt size={16} />,
        },
      ],
    },

    {
      path: "/admin/reports",
      label: "Reports",
      icon: <BarChart3 size={20} />,
    },

    {
      path: "/admin/bonuses",
      label: "Bonus Management",
      icon: <Gift size={20} />,
    },

    {
      path: "/admin/settings",
      label: "System Settings",
      icon: <Settings size={20} />,
    },

    {
      path: "/admin/admins",
      label: "Admin Management",
      icon: <Shield size={20} />,
    },

    {
      path: "/admin/logs",
      label: "Logs & Activity",
      icon: <FileText size={20} />,
    },

    {
      path: "/admin/support",
      label: "Support Messages",
      icon: <MessageSquare size={20} />,
    },
  ];

  /*
   * Toggle sidebar section
   */
  const toggleSection = (section) => {
    setExpandedSections((previous) => ({
      ...previous,
      [section]: !previous[section],
    }));
  };

  /*
   * Check active page
   */
  const isActive = (path) => {
    if (path === "/admin") {
      return location.pathname === "/admin";
    }

    return location.pathname === path;
  };

  /*
   * Check admin access
   */
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== "admin") {
    return <Navigate to="/" replace />;
  }

  return (
    <div
      className={`admin-layout ${
        sidebarCollapsed ? "collapsed" : ""
      }`}
    >

      {/* =====================================================
          SIDEBAR
          ===================================================== */}

      <aside className="admin-sidebar">

        {/* Sidebar Header */}
        <div className="sidebar-header">

          <div className="logo">
            <span className="logo-icon">
              🦁
            </span>

            {!sidebarCollapsed && (
              <span className="logo-text">
                SHEBAODDS
              </span>
            )}
          </div>

          <button
            type="button"
            className="collapse-btn"
            onClick={() =>
              setSidebarCollapsed(
                !sidebarCollapsed
              )
            }
            aria-label="Toggle sidebar"
          >
            {sidebarCollapsed ? (
              <Menu size={20} />
            ) : (
              <X size={20} />
            )}
          </button>

        </div>

        {/* =================================================
            NAVIGATION
            ================================================= */}

        <nav className="sidebar-nav">

          {menuItems.map((item, index) => {

            /*
             * Section with children
             */
            if (item.children) {

              const sectionName = item.section;

              const isExpanded =
                expandedSections[sectionName];

              return (
                <div
                  key={index}
                  className="nav-section"
                >

                  <button
                    type="button"
                    className="section-toggle"
                    onClick={() =>
                      toggleSection(
                        sectionName
                      )
                    }
                  >

                    <span className="section-icon">
                      {item.icon}
                    </span>

                    {!sidebarCollapsed && (
                      <>
                        <span className="section-label">
                          {item.label}
                        </span>

                        {isExpanded ? (
                          <ChevronDown size={16} />
                        ) : (
                          <ChevronRight size={16} />
                        )}
                      </>
                    )}

                  </button>

                  {isExpanded &&
                    !sidebarCollapsed && (
                      <div className="section-children">

                        {item.children.map(
                          (child) => (
                            <Link
                              key={child.path}
                              to={child.path}
                              className={`nav-link ${
                                isActive(
                                  child.path
                                )
                                  ? "active"
                                  : ""
                              }`}
                            >

                              <span className="nav-icon">
                                {child.icon}
                              </span>

                              <span className="nav-label">
                                {child.label}
                              </span>

                            </Link>
                          )
                        )}

                      </div>
                    )}

                </div>
              );
            }

            /*
             * Normal menu item
             */
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-link ${
                  isActive(item.path)
                    ? "active"
                    : ""
                }`}
              >

                <span className="nav-icon">
                  {item.icon}
                </span>

                {!sidebarCollapsed && (
                  <span className="nav-label">
                    {item.label}
                  </span>
                )}

              </Link>
            );
          })}

        </nav>

        {/* =================================================
            SIDEBAR FOOTER
            ================================================= */}

        <div className="sidebar-footer">

          <div className="user-info">

            <div className="avatar">
              {user.username
                ?.charAt(0)
                ?.toUpperCase() || "A"}
            </div>

            {!sidebarCollapsed && (
              <div className="user-details">

                <span className="user-name">
                  {user.username || "Admin"}
                </span>

                <span className="user-role">
                  Administrator
                </span>

              </div>
            )}

          </div>

          <button
            type="button"
            className="logout-btn"
            onClick={logout}
          >

            <LogOut size={20} />

            {!sidebarCollapsed && (
              <span>
                Logout
              </span>
            )}

          </button>

        </div>

      </aside>

      {/* =====================================================
          MAIN CONTENT
          ===================================================== */}

      <main className="admin-content">

        {/* Header */}
        <header className="admin-header">

          <div className="header-left">

            <h1>
              {t("admin_panel") ||
                "Admin Panel"}
            </h1>

          </div>

          <div className="header-right">

            <span className="online-status">
              ● Online
            </span>

            <span className="admin-time">
              {new Date().toLocaleString()}
            </span>

          </div>

        </header>

        {/* Page */}
        <div className="admin-body">
          <Outlet />
        </div>

      </main>

    </div>
  );
}