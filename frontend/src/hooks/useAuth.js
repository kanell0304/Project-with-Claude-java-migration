import { useState, useEffect } from "react";

const TOKEN_KEY = "dh_token";

export function useAuth() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));

  // /auth/callback?token=xxx 처리
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const callbackToken = params.get("token");
    if (callbackToken) {
      localStorage.setItem(TOKEN_KEY, callbackToken);
      setToken(callbackToken);
      window.history.replaceState({}, "", "/");
    }
  }, []);

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
  };

  return { token, logout, isLoggedIn: !!token };
}
