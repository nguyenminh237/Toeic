import { API_BASE, AUTH_ENDPOINTS, SUCCESS_CODE } from "../constants/api";
import { getUsernameFromToken } from "../utils/jwt";
import { storage } from "../utils/storage";

export class ApiError extends Error {
  constructor(message, code, status) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

let refreshPromise = null;

function buildUrl(path, query) {
  const url = new URL(`${API_BASE}${path}`, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, String(value));
      }
    });
  }
  return `${url.pathname}${url.search}`;
}

async function parseBody(response) {
  const text = await response.text();
  if (!text) return {};
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

async function refreshToken(currentToken) {
  const response = await fetch(`${API_BASE}${AUTH_ENDPOINTS.REFRESH}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token: currentToken }),
  });
  const data = await parseBody(response);
  if (!response.ok || data.code !== SUCCESS_CODE || !data.result?.token) {
    throw new ApiError(data.message || "Không thể làm mới phiên đăng nhập", data.code, response.status);
  }
  if (storage.getToken() !== currentToken) throw new ApiError("Phiên đăng nhập đã thay đổi", 1007, 401);
  storage.setToken(data.result.token);
  window.dispatchEvent(new CustomEvent("auth:refreshed", { detail: data.result.token }));
  return data.result.token;
}

async function request(path, { method = "GET", body, query, auth = true, retry = true, responseType = "json" } = {}) {
  const isForm = typeof FormData !== "undefined" && body instanceof FormData;
  const headers = isForm ? {} : { "Content-Type": "application/json" };
  const token = storage.getToken();
  if (auth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
  });

  if (response.status === 401 && auth && retry && token && !path.startsWith("/auth/")) {
    try {
      if (storage.getToken() && storage.getToken() !== token) {
        if (getUsernameFromToken(storage.getToken()) !== getUsernameFromToken(token)) throw new ApiError("Tài khoản đã thay đổi", 1007, 401);
        return request(path, { method, body, query, auth, retry: false, responseType });
      }
      refreshPromise ??= refreshToken(token).finally(() => {
        refreshPromise = null;
      });
      await refreshPromise;
      return request(path, { method, body, query, auth, retry: false, responseType });
    } catch {
      if (storage.getToken() === token) {
        storage.clear();
        window.dispatchEvent(new Event("auth:expired"));
      }
    }
  }

  if (response.ok && responseType === "blob") return response.blob();
  const data = await parseBody(response);
  if (!response.ok || (data.code != null && data.code !== SUCCESS_CODE)) {
    throw new ApiError(
      data.message || "Không thể kết nối máy chủ",
      data.code,
      response.status,
    );
  }

  return data.result;
}

export const http = {
  blob: (path) => request(path, { responseType: "blob" }),
  get: (path, options) => request(path, { ...options, method: "GET" }),
  post: (path, body, options) => request(path, { ...options, method: "POST", body }),
  put: (path, body, options) => request(path, { ...options, method: "PUT", body }),
  patch: (path, body, options) => request(path, { ...options, method: "PATCH", body }),
  delete: (path, options) => request(path, { ...options, method: "DELETE" }),
};
