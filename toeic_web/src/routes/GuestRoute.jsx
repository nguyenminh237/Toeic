import { Navigate, Outlet } from "react-router-dom";
import { Spinner } from "../components/common/Spinner";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

export function GuestRoute() {
  const { isAuthenticated, booting } = useAuth();
  if (booting) return <Spinner />;
  if (isAuthenticated) return <Navigate to={ROUTES.DASHBOARD} replace />;
  return <Outlet />;
}
