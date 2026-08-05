// contexts/AuthContext.js
import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const storage = window.localStorage;

const { BiometricAuthHelper } = NativeModules;

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(null);
  const [biometricAvailable, setBiometricAvailable] = useState(false);
  const [biometricType, setBiometricType] = useState('');

  // Check for stored token on mount
  useEffect(() => {
    checkAuth();
    checkBiometric();
  }, []);

  const checkAuth = async () => {
    try {
      const storedToken = await AsyncStorage.getItem('shebaodds_token');
      const storedUser = await AsyncStorage.getItem('shebaodds_user');
      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
        axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
      }
    } catch (error) {
      console.error('Auth check error:', error);
    } finally {
      setLoading(false);
    }
  };

  const checkBiometric = async () => {
    if (BiometricAuthHelper) {
      try {
        const result = await BiometricAuthHelper.isBiometricAvailable();
        setBiometricAvailable(result.available);
        setBiometricType(result.biometryType || 'biometric');
      } catch (error) {
        console.warn('Biometric check error:', error);
      }
    }
  };

  const login = async (email, password, options = {}) => {
    try {
      const response = await axios.post('/api/auth/login', { email, password });
      const { token, user } = response.data;
      await AsyncStorage.setItem('shebaodds_token', token);
      await AsyncStorage.setItem('shebaodds_user', JSON.stringify(user));
      setToken(token);
      setUser(user);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      return { success: true, user };
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Login failed' };
    }
  };

  const register = async (userData) => {
    try {
      const response = await axios.post('/api/auth/register', userData);
      const { token, user } = response.data;
      await AsyncStorage.setItem('shebaodds_token', token);
      await AsyncStorage.setItem('shebaodds_user', JSON.stringify(user));
      setToken(token);
      setUser(user);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      return { success: true, user };
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Registration failed' };
    }
  };

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (error) {}
    await AsyncStorage.removeItem('shebaodds_token');
    await AsyncStorage.removeItem('shebaodds_user');
    delete axios.defaults.headers.common['Authorization'];
    setToken(null);
    setUser(null);
  };

  const updateBiometric = async (enabled) => {
    if (!user) return { success: false };
    try {
      const response = await axios.patch('/api/users/biometric', { enabled });
      const updatedUser = { ...user, biometricEnabled: enabled };
      await AsyncStorage.setItem('shebaodds_user', JSON.stringify(updatedUser));
      setUser(updatedUser);
      return { success: true };
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Update failed' };
    }
  };

  const value = {
    user,
    token,
    loading,
    login,
    register,
    logout,
    updateBiometric,
    biometricAvailable,
    biometricType,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);