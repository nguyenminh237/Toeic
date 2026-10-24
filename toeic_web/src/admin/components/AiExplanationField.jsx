import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/common/Button";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";
import { http } from "../../services/httpClient";

export function AiExplanationField({ question, partNumber, group, groupId, value, onChange }) {
  const { basePath } = useAuthoringScope();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const options = Object.fromEntries(
    (Number(partNumber) === 2 ? ["A", "B", "C"] : ["A", "B", "C", "D"])
      .map((label) => [label, question.choices?.[label] ?? question[`option${label}`] ?? ""]),
  );
  const payload = {
    partNumber: Number(partNumber), questionText: question.questionText || "",
    promptText: question.promptText || "", correctAnswer: question.correctAnswer?.trim().toUpperCase(),
    options, groupId: Number(groupId || question.group?.id || question.passageId) || null,
    transcript: group?.transcript, documents: group?.documents,
  };
  const signature = JSON.stringify(payload);
  const current = useRef(signature);
  current.current = signature;
  const mounted = useRef(false);
  useEffect(() => { mounted.current = true; return () => { mounted.current = false; }; }, []);

  async function generate() {
    setLoading(true); setError("");
    try {
      const result = await http.post(`${basePath}/content/explanations`, payload);
      if (!mounted.current) return;
      if (current.current !== signature) {
        setError("Nội dung đã thay đổi trong lúc tạo. Hãy tạo lại lời giải cho nội dung mới.");
        return;
      }
      onChange(result.explanation);
    } catch (err) { if (mounted.current) setError(err.message); }
    finally { if (mounted.current) setLoading(false); }
  }

  return <div className="space-y-2">
    <div className="flex flex-wrap items-center justify-between gap-2">
      <span className="text-sm font-medium">Giải thích chi tiết</span>
      <Button variant="secondary" loading={loading} disabled={!partNumber} onClick={generate}>
        {value ? "Tạo lại bằng AI" : "Tạo giải thích bằng AI"}
      </Button>
    </div>
    <p className="text-xs text-slate-500">AI giải thích ngắn gọn, chỉ dấu hiệu chọn đáp án và mẹo làm bài; chỉ dịch khi cần hiểu nghĩa. Nhấn Lưu để lưu cùng câu hỏi.</p>
    {error ? <p role="alert" className="text-sm text-red-600">{error}</p> : null}
    <textarea aria-label="Giải thích chi tiết" value={value || ""} onChange={(e) => onChange(e.target.value)}
      placeholder="Nhấn Tạo giải thích bằng AI để điền lời giải tự động."
      className="min-h-48 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" />
  </div>;
}
