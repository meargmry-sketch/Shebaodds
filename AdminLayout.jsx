import React, { useState } from 'react';
import {
  Link,
  useLocation,
  Outlet,
  Navigate
} from 'react-router-dom';

import { useAuth } from './AuthContext.jsx';
import { useTranslation } from './LanguageContext';

import {
  LayoutDashboard,
  Users,
  Trophy,
  Briefcase,
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
} from 'lucide-react';