// contexts/AuthContext.jsx

import React, {
  createContext,
  useContext,
  useEffect,
  useState,
} from 'react';

const AuthContext = createContext(null);

const TOKEN_KEY = 'shebaodds_token';
const USER_KEY = 'shebaodds_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const storedToken = localStorage.getItem(TOKEN_KEY);
      const storedUser = localStorage.getItem(USER_KEY);

      if (storedToken) {
        setToken(storedToken);
      }

      if (storedUser) {
        setUser(JSON.parse(storedUser));
      }
    } catch (error) {
      console.error('Failed to restore authentication:', error);

      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } finally {
      setLoading(false);
    }
  }, []);

  const login = async (email, password) => {
    if (!email || !password) {
      return {
        success: false,
        message: 'Email and password are required.',
      };
    }

    /*
     * Temporary frontend authentication.
     *
     * Replace this section with your real API request
     * when the backend authentication endpoint is ready.
     */

    const fakeUser = {
      id: 'USR001',
      email,
      username: email.split('@')[0],
      role: 'Player',
      balance: 0,
      biometricEnabled: false,
    };

    const fakeToken = `demo-token-${Date.now()}`;

    localStorage.setItem(
      TOKEN_KEY,
      fakeToken
    );

    localStorage.setItem(
      USER_KEY,
      JSON.stringify(fakeUser)
    );

    setToken(fakeToken);
    setUser(fakeUser);

    return {
      success: true,
      user: fakeUser,
      token: fakeToken,
    };
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);

    setToken(null);
    setUser(null);
  };

  const updateUser = (updates) => {
    setUser((currentUser) => {
      if (!currentUser) {
        return currentUser;
      }

      const updatedUser = {
        ...currentUser,
        ...updates,
      };

      localStorage.setItem(
        USER_KEY,
        JSON.stringify(updatedUser)
      );

      return updatedUser;
    });
  };

  const value = {
    user,
    token,
    loading,
    isAuthenticated: Boolean(token && user),
    login,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error(
      'useAuth must be used inside AuthProvider'
    );
  }

  return context;
}

export default AuthContext;