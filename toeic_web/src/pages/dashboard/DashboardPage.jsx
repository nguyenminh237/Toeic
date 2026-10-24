import { BookOpen, Headphones, Library, Timer } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Card } from "../../components/common/Card";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { ROUTES, path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { listeningService } from "../../services/listeningService";
import { readingService } from "../../services/readingService";
import { testService } from "../../services/testService";
import { formatDateTime, statusLabel } from "../../utils/format";

const modules = [
  {
    title: "Luyện nghe",
    description: "Part 1-4, 5 section mỗi Part. Nghe từng câu, xem lại đáp án ngay.",
    to: ROUTES.LISTENING,
    tone: "bg-blue-50 text-primary",
    icon: Headphones,
  },
  {
    title: "Luyện đọc",
    description: "Part 5-7. Làm từng câu, giữ nhịp học không bị áp lực thời gian.",
    to: ROUTES.READING,
    tone: "bg-emerald-50 text-emerald-700",
    icon: BookOpen,
  },
  {
    title: "Từ vựng",
    description: "Category → set → từ. Học nghĩa, ví dụ và đồng nghĩa.",
    to: ROUTES.VOCABULARY,
    tone: "bg-amber-50 text-amber-700",
    icon: Library,
  },
  {
    title: "Đề thi theo Part",
    description: "Bấm giờ theo từng Part. Xem lịch sử kết quả đã start.",
    to: ROUTES.TESTS,
    tone: "bg-slate-100 text-slate-700",
    icon: Timer,
  },
];

export function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const stats = useAsync(async () => {
    const [listening, reading, history] = await Promise.all([
      listeningService.getProgress(user.id),
      readingService.getProgress(user.id),
      testService.getResultPage(user.id, 0, 4),
    ]);
    return { listening, reading, results: history.items, testCount: history.totalElements };
  }, [user.id]);

  if (stats.loading) return <Spinner label="Đang tải tiến độ..." />;
  if (stats.error) return <ErrorState error={stats.error} onRetry={stats.reload} />;

  const listeningDone = stats.data.listening?.filter((p) => p.status === "CORRECT" || p.status === "WRONG").length || 0;
  const readingDone = stats.data.reading?.filter((p) => p.status === "CORRECT" || p.status === "WRONG").length || 0;
  const recentResults = (stats.data.results || []).slice(0, 4);

  return (
    <div>
      <PageHeader
        eyebrow="Xin chào"
        title={`${user.fullName || user.username}, tiếp tục luyện hôm nay`}
        description="Chọn một kỹ năng. Theo dõi tiến độ và chọn nội dung muốn luyện."
      />
      <div className="mb-8 grid gap-4 sm:grid-cols-3">
        <Card>
          <p className="inline-flex items-center gap-1.5 text-sm text-slate-500">
            <Headphones className="h-4 w-4" /> Câu nghe đã làm
          </p>
          <p className="mt-2 text-3xl font-bold">{listeningDone}</p>
        </Card>
        <Card>
          <p className="inline-flex items-center gap-1.5 text-sm text-slate-500">
            <BookOpen className="h-4 w-4" /> Câu đọc đã làm
          </p>
          <p className="mt-2 text-3xl font-bold">{readingDone}</p>
        </Card>
        <Card>
          <p className="inline-flex items-center gap-1.5 text-sm text-slate-500">
            <Timer className="h-4 w-4" /> Lượt test đã start
          </p>
          <p className="mt-2 text-3xl font-bold">{stats.data.testCount || 0}</p>
        </Card>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        {modules.map((item) => (
          <Card key={item.title} onClick={() => navigate(item.to)}>
            <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${item.tone}`}>
              <item.icon className="h-3.5 w-3.5" />
              Module
            </span>
            <h3 className="mt-4 text-xl font-semibold">{item.title}</h3>
            <p className="mt-2 text-sm text-slate-500">{item.description}</p>
          </Card>
        ))}
      </div>
      <section className="mt-10">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold">Lịch sử test gần đây</h2>
          <button
            type="button"
            className="text-sm font-semibold text-primary"
            onClick={() => navigate(ROUTES.TEST_RESULTS)}
          >
            Xem tất cả
          </button>
        </div>
        {recentResults.length === 0 ? (
          <p className="text-sm text-slate-500">Chưa có kết quả. Hãy start một Part đề thi.</p>
        ) : (
          <div className="space-y-3">
            {recentResults.map((item) => (
              <Card
                key={item.id}
                className="flex items-center justify-between"
                onClick={() => navigate(path.testResultDetail(item.id))}
              >
                <div>
                  <p className="font-semibold">Part {item.partNumber}</p>
                  <p className="text-sm text-slate-500">{formatDateTime(item.startedAt)}</p>
                </div>
                <p className="text-sm font-medium">{statusLabel(item.status)}</p>
              </Card>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
