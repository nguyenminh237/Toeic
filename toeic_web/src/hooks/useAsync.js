import { useCallback, useEffect, useRef, useState } from "react";

// Callers declare the inputs that require a new query. A sequence prevents late responses
// from replacing newer data after navigation or a manual reload.
export function useAsync(asyncFn, deps = []) {
  const fn = useRef(asyncFn);
  fn.current = asyncFn;
  const sequence = useRef(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const run = useCallback(async () => {
    const request = ++sequence.current;
    setLoading(true);
    setError(null);
    try {
      const result = await fn.current();
      if (request === sequence.current) setData(result);
      return result;
    } catch (err) {
      if (request === sequence.current) setError(err);
      throw err;
    } finally {
      if (request === sequence.current) setLoading(false);
    }
  // The public hook accepts the caller's explicit query inputs.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  useEffect(() => {
    run().catch(() => {});
    return () => { sequence.current += 1; };
  }, [run]);
  return { data, error, loading, reload: run, setData };
}
