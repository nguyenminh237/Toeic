import { useLocation } from "react-router-dom";
import { adminApi } from "../admin/services/adminApi";
import { workspaceApi } from "../services/workspaceApi";

export function useAuthoringScope() {
  const { pathname } = useLocation();
  const admin = pathname.startsWith("/admin");
  return { api: admin ? adminApi : workspaceApi, basePath: admin ? "/admin" : "/workspace", canManageParts: admin };
}
