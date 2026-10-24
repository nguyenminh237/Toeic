import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Spinner } from "../components/common/Spinner";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

export function ProtectedRoute() {
  const { isAuthenticated, booting } = useAuth();
  const location = useLocation();

  if (booting) return <Spinner label="Đang kiểm tra phiên đăng nhập..." />;
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}
