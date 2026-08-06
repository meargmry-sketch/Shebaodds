import React, { createContext, useContext, useEffect, useState } from "react";
import axios from "axios";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("shebaodds_token"));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem("shebaodds_user");

    if (token) {
      axios.defaults.headers.common.Authorization = `Bearer ${token}`;
    }

    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem("shebaodds_user");
      }
    }

    setLoading(false);
  }, [token]);

  const login = async (email, password) => {
    const { data } = await axios.post("/api/auth/login", {
      email,
      password,
    });

    localStorage.setItem("shebaodds_token", data.token);
    localStorage.setItem("shebaodds_user", JSON.stringify(data.user));

    axios.defaults.headers.common.Authorization = `Bearer ${data.token}`;

    setToken(data.token);
    setUser(data.user);

    return data;
  };

  const register = async (userData) => {
    const { data } = await axios.post("/api/auth/register", userData);

    localStorage.setItem("shebaodds_token", data.token);
    localStorage.setItem("shebaodds_user", JSON.stringify(data.user));

    axios.defaults.headers.common.Authorization = `Bearer ${data.token}`;

    setToken(data.token);
    setUser(data.user);

    return data;
  };

  const logout = () => {
    localStorage.removeItem("shebaodds_token");
    localStorage.removeItem("shebaodds_user");

    delete axios.defaults.headers.common.Authorization;

    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        setUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}