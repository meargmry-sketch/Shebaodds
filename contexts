import React, { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const savedToken = localStorage.getItem("shebaodds_token");
      const savedUser = localStorage.getItem("shebaodds_user");

      if (savedToken) {
        setToken(savedToken);
      }

      if (savedUser) {
        setUser(JSON.parse(savedUser));
      }
    } catch (error) {
      console.error("Auth loading error:", error);
      localStorage.removeItem("shebaodds_token");
      localStorage.removeItem("shebaodds_user");
    } finally {
      setLoading(false);
    }
  }, []);

  const login = (userData, userToken) => {
    setUser(userData);
    setToken(userToken);

    if (userToken) {
      localStorage.setItem("shebaodds_token", userToken);
    }

    if (userData) {
      localStorage.setItem("shebaodds_user", JSON.stringify(userData));
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);

    localStorage.removeItem("shebaodds_token");
    localStorage.removeItem("shebaodds_user");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        logout,
        isAuthenticated: !!token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

export default AuthContext;