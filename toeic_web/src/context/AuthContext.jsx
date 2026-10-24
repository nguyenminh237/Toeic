import { useCallback, useEffect, useMemo, useState } from "react";
import { authService } from "../services/authService";
import { userService } from "../services/userService";
import { storage } from "../utils/storage";
import { AuthContext } from "./auth-context";

async function hydrateUser() { return userService.getMe(); }

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => storage.getToken());
  const [user, setUser] = useState(() => storage.getUser());
  const [booting, setBooting] = useState(true);

  const persist = useCallback((nextToken, nextUser) => {
    storage.setToken(nextToken);
    storage.setUser(nextUser);
    setToken(nextToken);
    setUser(nextUser);
  }, []);

  const logout = useCallback(async () => {
    const current = storage.getToken();
    if (current) {
      try {
        await authService.logout(current);
      } catch {
        // Token hết hạn vẫn cho phép thoát phiên local.
      }
    }
    persist(null, null);
  }, [persist]);

  useEffect(() => {
    const onExpired = () => persist(null, null);
    const onRefreshed = (event) => setToken(event.detail);
    window.addEventListener("auth:expired", onExpired);
    window.addEventListener("auth:refreshed", onRefreshed);
    return () => {
      window.removeEventListener("auth:expired", onExpired);
      window.removeEventListener("auth:refreshed", onRefreshed);
    };
  }, [persist]);

  useEffect(() => {
    let active = true;
    async function boot() {
      if (!token) {
        setBooting(false);
        return;
      }
      try {
        const introspect = await authService.introspect(token);
        if (!introspect?.valid) {
          const refreshed = await authService.refresh(token);
          if (active) persist(refreshed.token, await hydrateUser());
          return;
        }
        const profile = await hydrateUser(token);
        if (active) persist(storage.getToken(), profile);
      } catch {
        if (active) persist(null, null);
      } finally {
        if (active) setBooting(false);
      }
    }
    boot();
    return () => {
      active = false;
    };
  }, [logout, persist, token]);

  const login = useCallback(
    async ({ username, password }) => {
      const result = await authService.login({ username, password });
      storage.setToken(result.token);
      const profile = await hydrateUser(result.token);
      persist(result.token, profile);
      return profile;
    },
    [persist],
  );

  const register = useCallback(async (payload) => {
    return userService.register(payload);
  }, []);

  const refreshProfile = useCallback(async () => {
    if (!user?.id) return null;
    const profile = await userService.getById(user.id);
    persist(storage.getToken(), profile);
    return profile;
  }, [persist, user?.id]);

  const value = useMemo(
    () => ({
      token,
      user,
      booting,
      isAuthenticated: Boolean(token && user),
      isAdmin: user?.role === "ADMIN",
      login,
      register,
      logout,
      refreshProfile,
    }),
    [booting, login, logout, refreshProfile, register, token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
