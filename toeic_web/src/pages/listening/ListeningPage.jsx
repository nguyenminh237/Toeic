import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { EmptyState } from "../../components/common/EmptyState";
import { Button } from "../../components/common/Button";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { SectionCard } from "../../components/practice/SectionCard";
import { PartTabs } from "../../components/practice/PartTabs";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { listeningService } from "../../services/listeningService";

export function ListeningPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const partsQuery = useAsync(() => listeningService.getParts(), []);
  const [partNumber, setPartNumber] = useState(null);

  const selectedPart = useMemo(() => {
    const parts = partsQuery.data || [];
    if (partNumber) return parts.find((item) => item.partNumber === Number(partNumber)) || parts[0];
    return parts[0];
  }, [partNumber, partsQuery.data]);

  const sectionsQuery = useAsync(
    () => (selectedPart ? listeningService.getSections(selectedPart.partNumber) : Promise.resolve([])),
    [selectedPart?.partNumber],
  );

  const statsQuery = useAsync(async () => {
    if (!selectedPart || !user?.id) return {};
    const summaries = await listeningService.getSummary(selectedPart.partNumber);
    return Object.fromEntries(summaries.map((row) => [row.sectionId, row]));
  }, [selectedPart?.partNumber, user?.id]);

  if (partsQuery.loading) return <Spinner label="Đang tải Part nghe..." />;
  if (partsQuery.error) return <ErrorState error={partsQuery.error} onRetry={partsQuery.reload} />;
  if (!partsQuery.data?.length) {
    return (
      <EmptyState
        title="Chưa có Part nghe"
        description="Nội dung luyện nghe đang được cập nhật."
      />
    );
  }

  return (
    <div>
      <PageHeader
        eyebrow="Listening"
        title="Luyện nghe theo Part"
        actions={<Button variant="secondary" onClick={() => navigate("/workspace/listening")}>Quản lý học liệu của tôi</Button>}
        description={selectedPart?.description || "Chọn Part, rồi chọn 1 mục luyện tập để luyện từng câu."}
      />
      <PartTabs
        parts={partsQuery.data}
        active={selectedPart.partNumber}
        onChange={setPartNumber}
      />
      {sectionsQuery.loading ? <Spinner label="Đang tải mục luyện tập..." /> : null}
      {sectionsQuery.error ? <ErrorState error={sectionsQuery.error} onRetry={sectionsQuery.reload} /> : null}
      {!sectionsQuery.loading && !sectionsQuery.error && !sectionsQuery.data?.length ? (
        <EmptyState title="Bạn chưa có mục luyện nghe trong Part này" description="Vào Học liệu của tôi để tạo mục, tải audio và thêm câu hỏi của bạn." />
      ) : null}
      {statsQuery.error ? <ErrorState error={statsQuery.error} onRetry={statsQuery.reload} /> : null}
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {(sectionsQuery.data || []).map((section) => (
          <SectionCard
            key={section.id}
            section={section}
            stats={statsQuery.data?.[section.id]}
            onStart={() => navigate(path.listeningPractice(selectedPart.partNumber, section.id))}
          />
        ))}
      </div>
    </div>
  );
}
