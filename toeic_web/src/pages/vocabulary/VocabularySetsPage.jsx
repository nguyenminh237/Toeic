import { Layers } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import { Card } from "../../components/common/Card";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { vocabularyService } from "../../services/vocabularyService";

export function VocabularySetsPage() {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const query = useAsync(async () => {
    const [category, sets] = await Promise.all([
      vocabularyService.getCategory(categoryId),
      vocabularyService.getSets(categoryId),
    ]);
    return { category, sets };
  }, [categoryId]);

  if (query.loading) return <Spinner label="Đang tải set từ vựng..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;

  return (
    <div>
      <PageHeader
        eyebrow="Category"
        title={query.data.category?.categoryName || "Bộ từ vựng"}
        description={query.data.category?.description}
      />
      {!query.data.sets?.length ? (
        <EmptyState title="Category này chưa có set" />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {query.data.sets.map((set) => (
            <Card key={set.id} onClick={() => navigate(path.vocabularyWords(set.id), { state: { set } })}>
              <Layers className="mb-3 h-5 w-5 text-primary" />
              <h3 className="text-lg font-semibold">{set.setName}</h3>
              <p className="mt-2 text-sm text-slate-500">{set.description || "Học các từ trong set này."}</p>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
