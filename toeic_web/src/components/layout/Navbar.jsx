import { BookOpen, Headphones, LayoutDashboard, Library, LogOut, Shield, Timer } from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";
import { BrandMark } from "../brand/BrandMark";
import { ROUTES } from "../../constants/routes";
import { useAuth } from "../../hooks/useAuth";
import { initials } from "../../utils/format";

const links = [
  { to: "/workspace", label: "Học liệu của tôi", icon: Library },
  { to: ROUTES.DASHBOARD, label: "Tổng quan", icon: LayoutDashboard, end: true },
  { to: ROUTES.LISTENING, label: "Nghe", icon: Headphones },
  { to: ROUTES.READING, label: "Đọc", icon: BookOpen },
  { to: ROUTES.VOCABULARY, label: "Từ vựng", icon: Library },
  { to: ROUTES.TESTS, label: "Đề thi", icon: Timer },
];

export function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <button
          type="button"
          className="flex items-center"
          onClick={() => navigate(ROUTES.DASHBOARD)}
        >
          <BrandMark />
        </button>
        <nav className="hidden items-center gap-1 xl:flex">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium ${
                  isActive ? "bg-blue-50 text-primary" : "text-slate-600 hover:bg-slate-100"
                }`
              }
            >
              <link.icon className="h-4 w-4" />
              {link.label}
            </NavLink>
          ))}
          {isAdmin ? (
            <NavLink
              to={ROUTES.ADMIN}
              className="inline-flex items-center gap-1.5 rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700"
            >
              <Shield className="h-4 w-4" />
              Quản trị
            </NavLink>
          ) : null}
        </nav>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate(ROUTES.PROFILE)}
            className="flex items-center gap-2 rounded-full pr-2 hover:bg-slate-100"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-xs font-bold text-white">
              {initials(user?.fullName || user?.username)}
            </span>
            <span className="hidden text-sm font-medium text-slate-700 sm:block">
              {user?.fullName || user?.username}
            </span>
          </button>
          <button
            type="button"
            onClick={logout}
            className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-800"
          >
            <LogOut className="h-4 w-4" />
            Thoát
          </button>
        </div>
      </div>
      <nav className="flex gap-1 overflow-x-auto border-t border-slate-100 px-4 py-2 xl:hidden">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) =>
              `inline-flex items-center gap-1 whitespace-nowrap rounded-full px-3 py-1.5 text-sm ${
                isActive ? "bg-blue-50 text-primary" : "text-slate-600"
              }`
            }
          >
            <link.icon className="h-3.5 w-3.5" />
            {link.label}
          </NavLink>
        ))}
      </nav>
    </header>
  );
}
