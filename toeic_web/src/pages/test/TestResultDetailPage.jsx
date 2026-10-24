import { PrivateImage } from "../../components/common/PrivateMedia";
import { useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Badge } from "../../components/common/Badge";
import { Button } from "../../components/common/Button";
import { Card } from "../../components/common/Card";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { QuestionDocuments } from "../../components/practice/QuestionDocuments";
import { OptionList } from "../../components/practice/OptionList";
import { ROUTES } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { testService } from "../../services/testService";
import { formatDateTime, formatDuration, statusLabel } from "../../utils/format";

export function TestResultDetailPage() {
  const { resultId } = useParams();
  const navigate = useNavigate();
  const query = useAsync(() => testService.getResultDetail(resultId), [resultId]);

  const answers = useMemo(() => {
    const items = query.data?.answers ? Array.from(query.data.answers) : [];
    return items.sort((left, right) => (left.questionNumber || 0) - (right.questionNumber || 0));
  }, [query.data]);

  if (query.loading) return <Spinner label="Đang tải chi tiết kết quả..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  const result = query.data;

  return (
    <div>
      <PageHeader
        eyebrow={`Result #${result.id}`}
        title="Xem lại bài làm"
        description={`${statusLabel(result.status)} · nộp lúc ${formatDateTime(result.submittedAt || result.startedAt)}`}
        actions={
          <Button variant="secondary" onClick={() => navigate(ROUTES.TEST_RESULTS)}>
            Lịch sử
          </Button>
        }
      />
      <div className="mb-6 grid gap-4 sm:grid-cols-4">
        <Card>
          <p className="text-sm text-slate-500">Đúng</p>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{result.correctCount ?? 0}</p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Sai / bỏ trống</p>
          <p className="mt-2 text-2xl font-bold text-red-600">
            {Math.max(0, (result.totalCount ?? 0) - (result.correctCount ?? 0))}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Điểm</p>
          <p className="mt-2 text-2xl font-bold">{result.accuracyPercent ?? result.score ?? "—"}%</p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Thời gian</p>
          <p className="mt-2 text-2xl font-bold">{formatDuration(result.timeSpent)}</p>
        </Card>
      </div>
      {!answers.length ? (
        <Alert tone="warning">Bài này chưa có danh sách câu trả lời.</Alert>
      ) : (
        <div className="space-y-6">
          {answers.map((answer, index) => (
            <article
              key={answer.id || answer.questionId}
              className={`rounded-2xl border bg-white p-6 shadow-[var(--shadow-card)] ${
                answer.isCorrect ? "border-emerald-200" : "border-red-200"
              }`}
            >
              <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
                <p className="text-sm font-semibold text-slate-500">
                  Câu {answer.questionNumber || index + 1}
                </p>
                <Badge tone={answer.isCorrect ? "green" : "red"}>
                  {answer.isCorrect ? "Đúng" : answer.selectedAnswer ? "Sai" : "Bỏ trống"}
                </Badge>
              </div>
              <QuestionDocuments group={answer.group} />
              {answer.imageUrl ? (
                <PrivateImage
                  src={answer.imageUrl}
                  alt=""
                  className="mb-4 max-h-72 w-full rounded-xl bg-slate-50 object-contain"
                />
              ) : null}
              {answer.questionText ? (
                <div className="mb-4 rounded-xl bg-slate-50 px-4 py-3">
                  <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    Script / câu hỏi
                  </p>
                  <p className="text-base font-medium text-slate-900">{answer.questionText}</p>
                </div>
              ) : null}
              <OptionList
                question={answer}
                selected={answer.selectedAnswer}
                revealed
                disabled
                onSelect={() => {}}
              />
              <p className="mt-3 text-sm text-slate-500">
                Bạn chọn <span className="font-semibold">{answer.selectedAnswer || "—"}</span>
                {" · "}
                Đáp án đúng <span className="font-semibold text-emerald-700">{answer.correctAnswer || "—"}</span>
              </p>
              {answer.explanation ? (
                <p className="mt-3 whitespace-pre-wrap text-sm text-slate-600">{answer.explanation}</p>
              ) : null}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
