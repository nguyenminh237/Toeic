import { USER_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const userService = {
  getMe: () => http.get("/users/me"),
  changePassword: (currentPassword,newPassword) => http.put("/users/me/password", { currentPassword,newPassword }),
  register(payload) {
    return http.post(USER_ENDPOINTS.CREATE, payload, { auth: false });
  },
  getAll() {
    return http.get(USER_ENDPOINTS.LIST);
  },
  getById(userId) {
    return http.get(USER_ENDPOINTS.BY_ID(userId));
  },
  getByUsername(username) {
    return http.get(USER_ENDPOINTS.BY_USERNAME(username));
  },
};
