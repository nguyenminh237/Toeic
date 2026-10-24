import { useState } from "react";
import { Button } from "../../components/common/Button";
import { useNavigate } from "react-router-dom";
import { Card } from "../../components/common/Card";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { testService } from "../../services/testService";
import { formatDateTime, statusLabel } from "../../utils/format";

export function TestResultsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const query = useAsync(() => testService.getResultPage(user.id, page, 20), [user.id, page]);

  if (query.loading) return <Spinner label="Đang tải lịch sử..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  return (
    <div>
      <PageHeader
        eyebrow="Kết quả"
        title="Lịch sử làm đề"
        description="Các lần đã nộp bài. Bấm vào một dòng để xem chi tiết đúng/sai."
      />
      {!query.data?.items?.length ? (
        <EmptyState title="Chưa có lịch sử" description="Hãy start một Part đề thi." />
      ) : (
        <div className="space-y-3">
          {query.data.items.map((item) => (
            <Card
              key={item.id}
              className="flex flex-wrap items-center justify-between gap-3"
              onClick={() => navigate(path.testResultDetail(item.id))}
            >
              <div>
                <p className="font-semibold">Result #{item.id} · Part {item.partNumber} · {item.suiteName}</p>
                <p className="text-sm text-slate-500">{formatDateTime(item.startedAt)}</p>
              </div>
              <div className="text-right text-sm">
                <p className="font-semibold">{statusLabel(item.status)}</p>
                <p className="text-slate-500">
                  {item.correctCount ?? 0}/{item.totalCount ?? 0} · {item.score ?? "—"} điểm
                </p>
              </div>
            </Card>
          ))}
        </div>
      )}
      <div className="mt-6 flex items-center gap-4">
        <Button variant="secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Trang trước</Button>
        <span className="text-sm">Trang {page + 1}/{Math.max(1, query.data.totalPages)} · {query.data.totalElements} lượt</span>
        <Button variant="secondary" disabled={page + 1 >= query.data.totalPages} onClick={() => setPage((p) => p + 1)}>Trang sau</Button>
      </div>
    </div>
  );
}
