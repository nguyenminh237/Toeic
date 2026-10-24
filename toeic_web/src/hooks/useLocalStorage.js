import { useCallback, useState } from "react";

export function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    const raw = localStorage.getItem(key);
    if (raw == null) return initialValue;
    try {
      return JSON.parse(raw);
    } catch {
      return initialValue;
    }
  });

  const update = useCallback(
    (next) => {
      setValue((current) => {
        const resolved = typeof next === "function" ? next(current) : next;
        if (resolved === undefined) localStorage.removeItem(key);
        else localStorage.setItem(key, JSON.stringify(resolved));
        return resolved;
      });
    },
    [key],
  );

  return [value, update];
}
