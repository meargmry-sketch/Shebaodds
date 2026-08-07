import React, {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import axios from "axios";

const AuthContext = createContext(null);

const TOKEN_KEY = "shebaodds_token";
const USER_KEY = "shebaodds_user";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = () => {
    try {
      const storedToken = localStorage.getItem(TOKEN_KEY);
      const storedUser = localStorage.getItem(USER_KEY);

      if (storedToken && storedUser) {
        const parsedUser = JSON.parse(storedUser);

        setToken(storedToken);
        setUser(parsedUser);

        axios.defaults.headers.common.Authorization =
          `Bearer ${storedToken}`;
      }
    } catch (error) {
      console.error("Auth check error:", error);

      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      const response = await axios.post(
        "/api/auth/login",
        {
          email,
          password,
        }
      );

      const responseToken = response.data?.token;
      const responseUser = response.data?.user;

      if (!responseToken || !responseUser) {
        return {
          success: false,
          message: "Invalid server response.",
        };
      }

      localStorage.setItem(
        TOKEN_KEY,
        responseToken
      );

      localStorage.setItem(
        USER_KEY,
        JSON.stringify(responseUser)
      );

      setToken(responseToken);
      setUser(responseUser);

      axios.defaults.headers.common.Authorization =
        `Bearer ${responseToken}`;

      return {
        success: true,
        user: responseUser,
      };
    } catch (error) {
      return {
        success: false,
        message:
          error?.response?.data?.message ||
          "Login failed.",
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await axios.post(
        "/api/auth/register",
        userData
      );

      const responseToken = response.data?.token;
      const responseUser = response.data?.user;

      if (!responseToken || !responseUser) {
        return {
          success: false,
          message: "Invalid server response.",
        };
      }

      localStorage.setItem(
        TOKEN_KEY,
        responseToken
      );

      localStorage.setItem(
        USER_KEY,
        JSON.stringify(responseUser)
      );

      setToken(responseToken);
      setUser(responseUser);

      axios.defaults.headers.common.Authorization =
        `Bearer ${responseToken}`;

      return {
        success: true,
        user: responseUser,
      };
    } catch (error) {
      return {
        success: false,
        message:
          error?.response?.data?.message ||
          "Registration failed.",
      };
    }
  };

  const logout = async () => {
    try {
      await axios.post("/api/auth/logout");
    } catch (error) {
      console.warn("Logout request failed.");
    }

    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);

    delete axios.defaults.headers.common.Authorization;

    setToken(null);
    setUser(null);
  };

  const updateBiometric = async (enabled) => {
    if (!user) {
      return {
        success: false,
        message: "No authenticated user.",
      };
    }

    try {
      await axios.patch(
        "/api/users/biometric",
        { enabled }
      );

      const updatedUser = {
        ...user,
        biometricEnabled: enabled,
      };

      localStorage.setItem(
        USER_KEY,
        JSON.stringify(updatedUser)
      );

      setUser(updatedUser);

      return {
        success: true,
      };
    } catch (error) {
      return {
        success: false,
        message:
          error?.response?.data?.message ||
          "Update failed.",
      };
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
    setUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}