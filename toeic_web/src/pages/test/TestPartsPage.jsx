import { useNavigate, useParams } from "react-router-dom";
import { Badge } from "../../components/common/Badge";
import { Card } from "../../components/common/Card";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { testService } from "../../services/testService";

export function TestPartsPage() {
  const { suiteId } = useParams();
  const navigate = useNavigate();
  const query = useAsync(async () => {
    const [suite, parts] = await Promise.all([
      testService.getSuite(suiteId),
      testService.getParts(suiteId),
    ]);
    return { suite, parts };
  }, [suiteId]);

  if (query.loading) return <Spinner label="Đang tải Part đề thi..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  return (
    <div>
      <PageHeader
        eyebrow={query.data.suite?.source}
        title={query.data.suite?.suiteName || "Bộ đề"}
        description={query.data.suite?.description}
      />
      {!query.data.parts?.length ? (
        <EmptyState title="Bộ đề này chưa có Part" />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {query.data.parts.map((part) => (
            <Card key={part.id} onClick={() => navigate(path.testExam(part.id))}>
              <div className="flex items-center justify-between">
                <h3 className="text-xl font-semibold">Part {part.partNumber}</h3>
                <Badge tone="amber">{part.partType || "TEST"}</Badge>
              </div>
              <p className="mt-3 text-sm text-slate-500">
                {part.questionCount || 0} câu · {part.timeLimit || 0} phút
              </p>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
