import { Outlet, useLocation } from "react-router-dom";
import { AdminHeader } from "../components/AdminHeader";
import { AdminSidebar } from "../components/AdminSidebar";
import { ROUTES } from "../../constants/routes";

const titles = {
  "/admin/content": "Biên soạn câu hỏi",
  "/admin/vocabulary/import": "Thêm từ với AI",
  [ROUTES.ADMIN]: "Admin Dashboard",
  [ROUTES.ADMIN_USERS]: "Quản lý người dùng",
  [ROUTES.ADMIN_LISTENING]: "Quản lý Listening",
  [ROUTES.ADMIN_READING]: "Quản lý Reading",
  [ROUTES.ADMIN_VOCABULARY]: "Quản lý từ vựng",
  [ROUTES.ADMIN_TESTS]: "Quản lý đề thi",
};

export function AdminLayout() {
  const { pathname } = useLocation();
  return (
    <div className="flex min-h-screen bg-surface">
      <AdminSidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <AdminHeader title={titles[pathname] || "Admin"} />
        <main className="min-w-0 flex-1 p-3 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
