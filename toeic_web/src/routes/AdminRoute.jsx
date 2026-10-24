import { Navigate, Outlet } from "react-router-dom";
import { Spinner } from "../components/common/Spinner";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

export function AdminRoute() {
  const { isAuthenticated, isAdmin, booting } = useAuth();
  if (booting) return <Spinner label="Đang kiểm tra quyền admin..." />;
  if (!isAuthenticated || !isAdmin) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }
  return <Outlet />;
}
