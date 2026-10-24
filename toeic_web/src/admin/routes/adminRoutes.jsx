import { Route } from "react-router-dom";
import { ContentManagementPage } from "../pages/ContentManagementPage";
import { ROUTES } from "../../constants/routes";
import { AdminLayout } from "../layouts/AdminLayout";
import { AdminDashboardPage } from "../pages/DashboardPage";
import { ListeningManagementPage } from "../pages/ListeningManagementPage";
import { ReadingManagementPage } from "../pages/ReadingManagementPage";
import { TestManagementPage } from "../pages/TestManagementPage";
import { UserManagementPage } from "../pages/UserManagementPage";
import { VocabularyManagementPage } from "../pages/VocabularyManagementPage";
import { VocabularyImportPage } from "../pages/VocabularyImportPage";

export function adminRouteElements() {
  return (
    <Route element={<AdminLayout />}>
      <Route index element={<AdminDashboardPage />} />
      <Route path="users" element={<UserManagementPage />} />
      <Route path="listening" element={<ListeningManagementPage />} />
      <Route path="reading" element={<ReadingManagementPage />} />
      <Route path="vocabulary" element={<VocabularyManagementPage />} />
      <Route path="vocabulary/import" element={<VocabularyImportPage />} />
      <Route path="tests" element={<TestManagementPage />} />
      <Route path="content" element={<ContentManagementPage />} />
    </Route>
  );
}

export const ADMIN_PATHS = [
  ROUTES.ADMIN,
  ROUTES.ADMIN_USERS,
  ROUTES.ADMIN_LISTENING,
  ROUTES.ADMIN_READING,
  ROUTES.ADMIN_VOCABULARY,
  ROUTES.ADMIN_TESTS,
];
