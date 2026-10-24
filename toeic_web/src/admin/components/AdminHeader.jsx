import { LogOut } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";

export function AdminHeader({ title }) {
  const { user, logout } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between gap-3 border-b border-slate-200 bg-white px-3 md:px-6">
      <h1 className="min-w-0 truncate text-base font-semibold text-slate-900 md:text-lg">{title}</h1>
      <div className="flex shrink-0 items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-semibold">{user?.fullName || user?.username}</p>
          <p className="text-xs text-slate-500">{user?.role}</p>
        </div>
        <button
          type="button"
          title="Đăng xuất"
          onClick={logout}
          className="inline-flex items-center gap-1.5 rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-200"
        >
          <LogOut className="h-4 w-4" />
          <span className="sr-only sm:not-sr-only">Đăng xuất</span>
        </button>
      </div>
    </header>
  );
}
