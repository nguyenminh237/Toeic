import { FolderOpen } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { vocabularyService } from "../../services/vocabularyService";

export function VocabularyPage() {
  const navigate = useNavigate();
  const query = useAsync(() => vocabularyService.getCategories(), []);

  if (query.loading) return <Spinner label="Đang tải danh mục từ vựng..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  return (
    <div>
      <PageHeader
        eyebrow="Vocabulary"
        title="Chọn loại từ vựng"
        actions={<Button variant="secondary" onClick={() => navigate("/workspace/vocabulary")}>Quản lý từ vựng của tôi</Button>}
        description="Chọn một loại từ vựng để xem các set theo chủ đề."
      />
      {!query.data?.length ? (
        <EmptyState title="Bạn chưa có danh mục từ vựng" description="Vào Học liệu của tôi để tạo Category, Set và thêm từ thủ công hoặc từ ảnh/PDF bằng AI." />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {query.data.map((category) => (
            <Card key={category.id} onClick={() => navigate(path.vocabularySets(category.id))}>
              <FolderOpen className="mb-3 h-5 w-5 text-primary" />
              <h3 className="text-xl font-semibold">{category.categoryName}</h3>
              <p className="mt-2 text-sm text-slate-500">
                {category.description || "Bộ từ vựng theo chủ đề."}
              </p>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
