import { useCallback, useEffect, useState } from "react";
import { adminApi } from "../services/adminApi";

export function useAdminUsers() {
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const reload = useCallback(async (q) => {
    setLoading(true);
    setError(null);
    try {
      setUsers((await adminApi.searchUsers(q)) || []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload("");
  }, [reload]);

  async function toggleStatus(user) {
    const updated = await adminApi.setUserStatus(user.id, !user.enabled);
    setUsers((current) => current.map((item) => (item.id === user.id ? updated : item)));
  }

  async function changeRole(user, role) {
    const updated = await adminApi.setUserRole(user.id, role);
    setUsers((current) => current.map((item) => (item.id === user.id ? updated : item)));
  }

  return { users, query, setQuery, loading, error, reload, toggleStatus, changeRole };
}
