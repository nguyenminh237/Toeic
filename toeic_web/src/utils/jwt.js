export function decodeJwt(token) {
  if (!token) return null;
  const parts = token.split(".");
  if (parts.length < 2) return null;
  try {
    const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(payload)
        .split("")
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(""),
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function getUsernameFromToken(token) {
  return decodeJwt(token)?.sub ?? null;
}

export function getRoleFromToken(token) {
  return decodeJwt(token)?.scope ?? null;
}

export function isTokenExpired(token, skewSeconds = 30) {
  const exp = decodeJwt(token)?.exp;
  if (!exp) return true;
  return Date.now() >= (exp - skewSeconds) * 1000;
}
