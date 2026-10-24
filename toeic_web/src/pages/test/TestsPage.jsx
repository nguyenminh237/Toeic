import { ClipboardList, History } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "../../components/common/Button";
import { Card } from "../../components/common/Card";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { ROUTES, path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { testService } from "../../services/testService";

export function TestsPage() {
  const navigate = useNavigate();
  const query = useAsync(() => testService.getSuites(), []);

  if (query.loading) return <Spinner label="Đang tải đề thi..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  return (
    <div>
      <PageHeader
        eyebrow="Practice Tests"
        title="Đề thi theo Part"
        description="Chọn bộ đề, rồi chọn Part để bấm giờ. Không làm full TOEIC."
        actions={
          <Button variant="secondary" onClick={() => navigate(ROUTES.TEST_RESULTS)}>
            <History className="h-4 w-4" />
            Lịch sử
          </Button>
        }
      />
      {!query.data?.length ? (
        <EmptyState title="Chưa có đề luyện" description="Các đề luyện đang được cập nhật." />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {query.data.map((suite) => (
            <Card key={suite.id} onClick={() => navigate(path.testParts(suite.id))}>
              <ClipboardList className="mb-2 h-5 w-5 text-primary" />
              <p className="text-xs font-semibold uppercase text-primary">{suite.source || "TOEIC"}</p>
              <h3 className="mt-2 text-xl font-semibold">{suite.suiteName}</h3>
              <p className="mt-2 text-sm text-slate-500">{suite.description || "Gồm các Part luyện theo thời gian."}</p>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
