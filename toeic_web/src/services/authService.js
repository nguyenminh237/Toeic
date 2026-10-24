import { AUTH_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const authService = {
  login(payload) {
    return http.post(AUTH_ENDPOINTS.LOGIN, payload, { auth: false });
  },
  introspect(token) {
    return http.post(AUTH_ENDPOINTS.INTROSPECT, { token }, { auth: false });
  },
  logout(token) {
    return http.post(AUTH_ENDPOINTS.LOGOUT, { token }, { auth: false });
  },
  refresh(token) {
    return http.post(AUTH_ENDPOINTS.REFRESH, { token }, { auth: false });
  },
  forgotPassword(email) {
    return http.post(AUTH_ENDPOINTS.FORGOT_PASSWORD, { email }, { auth: false });
  },
  resetPassword(userId, newPassword) {
    return http.post(AUTH_ENDPOINTS.RESET_PASSWORD(userId), { token: userId, newPassword }, { auth: false });
  },
};
