import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Plus } from "lucide-react";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { VoicePicker } from "../../components/vocabulary/VoicePicker";
import { VocabularyStudyWorkspace } from "../../components/vocabulary/VocabularyStudyWorkspace";
import { useAsync } from "../../hooks/useAsync";
import { vocabularyService } from "../../services/vocabularyService";

export function VocabularyWordsPage() {
  const { setId } = useParams();
  const { state } = useLocation();
  const navigate = useNavigate();
  const query = useAsync(() => vocabularyService.getWords(setId), [setId]);
  const metadata = useAsync(() => vocabularyService.getSet(setId), [setId]);
  if (query.loading) return <Spinner label="Đang tải từ vựng…" />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;
  return <div className="mx-auto max-w-5xl">
    <Link to={metadata.data?.categoryId ? `/vocabulary/categories/${metadata.data.categoryId}` : "/vocabulary"} className="mb-5 inline-flex items-center gap-2 text-sm text-slate-500 hover:text-primary"><ArrowLeft className="h-4 w-4" />Các bộ từ vựng</Link>
    <PageHeader eyebrow={`Bộ từ vựng · ${query.data?.length || 0} từ`} title={metadata.data?.setName || state?.set?.setName || "Từ vựng"} description="Lật thẻ để làm quen, luyện nhớ để ghi nhớ lâu hơn." actions={<><VoicePicker />{<Button variant="secondary" onClick={() => navigate(`/workspace/vocabulary/import?setId=${setId}`)}><Plus className="h-4 w-4" />Thêm từ</Button>}</>} />
    {metadata.error ? <div className="mb-5"><Alert tone="error">Không tải được thông tin bộ từ. Bạn vẫn có thể học các từ bên dưới.</Alert></div> : null}
    {query.data?.length ? <VocabularyStudyWorkspace key={setId} words={query.data} /> : <EmptyState title="Bộ này chưa có từ" description="Các từ sẽ xuất hiện ở đây sau khi được thêm vào bộ." />}
  </div>;
}
