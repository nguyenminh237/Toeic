import { NavLink, Outlet } from "react-router-dom";

export function WorkspaceLayout() {
  return <div className="min-w-0 space-y-6">
    <div><h1 className="text-2xl font-bold text-slate-900">Học liệu của tôi</h1><p className="mt-2 text-sm text-slate-500">Tạo bài luyện từ tài liệu của bạn. Chỉ bạn có thể xem và quản lý học liệu này. Các Part TOEIC được hệ thống cung cấp sẵn.</p></div>
    <nav aria-label="Loại học liệu" className="flex flex-wrap gap-2 border-b border-slate-200 pb-3">
      {[["listening", "Listening"], ["reading", "Reading"], ["vocabulary", "Từ vựng"]].map(([path, label]) => <NavLink key={path} to={`/workspace/${path}`} className={({ isActive }) => `rounded-xl px-4 py-2 text-sm font-semibold ${isActive ? "bg-blue-600 text-white" : "bg-white text-slate-600 hover:bg-slate-100"}`}>{label}</NavLink>)}
    </nav>
    <Outlet />
  </div>;
}
