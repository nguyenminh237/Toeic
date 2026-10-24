import { ArrowLeft, BookOpen, Headphones, LayoutDashboard, Library, Timer, Users } from "lucide-react";
import { NavLink } from "react-router-dom";
import { BrandMark } from "../../components/brand/BrandMark";
import { ROUTES } from "../../constants/routes";

const items = [
  { to: "/admin/content", label: "Biên soạn câu hỏi", icon: Library },
  { to: ROUTES.ADMIN, label: "Tổng quan", icon: LayoutDashboard, end: true },
  { to: ROUTES.ADMIN_USERS, label: "Người dùng", icon: Users },
  { to: ROUTES.ADMIN_LISTENING, label: "Listening", icon: Headphones },
  { to: ROUTES.ADMIN_READING, label: "Reading", icon: BookOpen },
  { to: ROUTES.ADMIN_VOCABULARY, label: "Từ vựng", icon: Library },
  { to: ROUTES.ADMIN_TESTS, label: "Đề thi", icon: Timer },
];

export function AdminSidebar() {
  return (
    <aside className="flex w-16 shrink-0 flex-col border-r border-slate-200 bg-slate-950 text-white md:w-64">
      <div className="px-3 py-5 md:px-6">
        <span className="md:hidden"><BrandMark light size="sm" showName={false} /></span>
        <span className="hidden md:inline"><BrandMark light /></span>
        <p className="mt-1 hidden text-xs text-slate-400 md:block">Admin Console</p>
      </div>
      <nav className="flex-1 space-y-1 px-3">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            title={item.label}
            end={item.end}
            className={({ isActive }) =>
              `flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium ${
                isActive ? "bg-primary text-white" : "text-slate-300 hover:bg-slate-800"
              }`
            }
          >
            <item.icon className="h-4 w-4 shrink-0" />
            <span className="sr-only md:not-sr-only">{item.label}</span>
          </NavLink>
        ))}
      </nav>
      <NavLink to={ROUTES.DASHBOARD} title="Về trang học" className="flex items-center gap-2 px-6 py-4 text-sm text-slate-400 hover:text-white">
        <ArrowLeft className="h-4 w-4 shrink-0" />
        <span className="sr-only md:not-sr-only">Về trang học</span>
      </NavLink>
    </aside>
  );
}
