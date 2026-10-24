import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import { AppLayout } from "../layouts/AppLayout";
import { WorkspaceLayout } from "../layouts/WorkspaceLayout";
import { ListeningManagementPage } from "../admin/pages/ListeningManagementPage";
import { ReadingManagementPage } from "../admin/pages/ReadingManagementPage";
import { VocabularyManagementPage } from "../admin/pages/VocabularyManagementPage";
import { VocabularyImportPage } from "../admin/pages/VocabularyImportPage";
import { ContentManagementPage } from "../admin/pages/ContentManagementPage";
import { AuthLayout } from "../layouts/AuthLayout";
import { adminRouteElements } from "../admin/routes/adminRoutes";
import { ForgotPasswordPage } from "../pages/auth/ForgotPasswordPage";
import { LoginPage } from "../pages/auth/LoginPage";
import { RegisterPage } from "../pages/auth/RegisterPage";
import { ResetPasswordPage } from "../pages/auth/ResetPasswordPage";
import { DashboardPage } from "../pages/dashboard/DashboardPage";
import { ListeningPage } from "../pages/listening/ListeningPage";
import { ListeningPracticePage } from "../pages/listening/ListeningPracticePage";
import { ProfilePage } from "../pages/profile/ProfilePage";
import { ReadingPage } from "../pages/reading/ReadingPage";
import { ReadingPracticePage } from "../pages/reading/ReadingPracticePage";
import { TestExamPage } from "../pages/test/TestExamPage";
import { TestPartsPage } from "../pages/test/TestPartsPage";
import { TestResultDetailPage } from "../pages/test/TestResultDetailPage";
import { TestResultsPage } from "../pages/test/TestResultsPage";
import { TestsPage } from "../pages/test/TestsPage";
import { VocabularyPage } from "../pages/vocabulary/VocabularyPage";
import { VocabularySetsPage } from "../pages/vocabulary/VocabularySetsPage";
import { VocabularyWordsPage } from "../pages/vocabulary/VocabularyWordsPage";
import { AdminRoute } from "./AdminRoute";
import { GuestRoute } from "./GuestRoute";
import { ProtectedRoute } from "./ProtectedRoute";

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<GuestRoute />}>
          <Route element={<AuthLayout />}>
            <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            <Route path={ROUTES.REGISTER} element={<RegisterPage />} />
            <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
            <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
          </Route>
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
            <Route path={ROUTES.LISTENING} element={<ListeningPage />} />
            <Route path={ROUTES.LISTENING_PRACTICE} element={<ListeningPracticePage />} />
            <Route path={ROUTES.READING} element={<ReadingPage />} />
            <Route path={ROUTES.READING_PRACTICE} element={<ReadingPracticePage />} />
            <Route path={ROUTES.VOCABULARY} element={<VocabularyPage />} />
            <Route path={ROUTES.VOCABULARY_SETS} element={<VocabularySetsPage />} />
            <Route path={ROUTES.VOCABULARY_WORDS} element={<VocabularyWordsPage />} />
            <Route path={ROUTES.TESTS} element={<TestsPage />} />
            <Route path={ROUTES.TEST_PARTS} element={<TestPartsPage />} />
            <Route path={ROUTES.TEST_EXAM} element={<TestExamPage />} />
            <Route path={ROUTES.TEST_RESULTS} element={<TestResultsPage />} />
            <Route path={ROUTES.TEST_RESULT_DETAIL} element={<TestResultDetailPage />} />
            <Route path="/workspace" element={<WorkspaceLayout />}>
              <Route index element={<Navigate to="listening" replace />} />
              <Route path="listening" element={<ListeningManagementPage />} />
              <Route path="reading" element={<ReadingManagementPage />} />
              <Route path="vocabulary" element={<VocabularyManagementPage />} />
              <Route path="vocabulary/import" element={<VocabularyImportPage />} />
              <Route path="content" element={<ContentManagementPage />} />
            </Route>
            <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
          </Route>
        </Route>

        <Route element={<AdminRoute />}>
          <Route path={ROUTES.ADMIN}>
            {adminRouteElements()}
          </Route>
        </Route>

        <Route path="*" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
      </Routes>
    </BrowserRouter>
  );
}
