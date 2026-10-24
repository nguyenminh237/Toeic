import { BookOpen, Headphones, Library, Timer, Users, ClipboardCheck } from "lucide-react";
import { ErrorState } from "../../components/common/ErrorState";
import { Spinner } from "../../components/common/Spinner";
import { useAsync } from "../../hooks/useAsync";
import { formatDateTime, statusLabel } from "../../utils/format";
import { DataTable } from "../components/DataTable";
import { StatCard } from "../components/StatCard";
import { adminApi } from "../services/adminApi";

export function AdminDashboardPage() {
  const query = useAsync(() => adminApi.getDashboard(), []);

  if (query.loading) return <Spinner label="Đang tải thống kê admin..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  const data = query.data;
  return (
    <div className="space-y-8">
      <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-6">
        <StatCard label="Users" value={data.userCount} icon={Users} />
        <StatCard label="Câu nghe" value={data.listeningQuestionCount} icon={Headphones} />
        <StatCard label="Câu đọc" value={data.readingQuestionCount} icon={BookOpen} />
        <StatCard label="Từ vựng" value={data.vocabularyWordCount} icon={Library} />
        <StatCard label="Bộ đề" value={data.testSuiteCount} icon={Timer} />
        <StatCard label="Lượt test" value={data.testResultCount} icon={ClipboardCheck} />
      </div>
      <section>
        <h2 className="mb-3 text-lg font-semibold">User mới</h2>
        <DataTable
          rows={data.recentUsers}
          columns={[
            { key: "username", label: "Username" },
            { key: "email", label: "Email" },
            { key: "role", label: "Role" },
            { key: "createdAt", label: "Tạo lúc", render: (row) => formatDateTime(row.createdAt) },
          ]}
        />
      </section>
      <section>
        <h2 className="mb-3 text-lg font-semibold">Activity test gần đây</h2>
        <DataTable
          rows={data.recentTestResults}
          columns={[
            { key: "id", label: "Result" },
            { key: "partId", label: "Part ID" },
            { key: "status", label: "Trạng thái", render: (row) => statusLabel(row.status) },
            { key: "startedAt", label: "Bắt đầu", render: (row) => formatDateTime(row.startedAt) },
          ]}
        />
      </section>
    </div>
  );
}
