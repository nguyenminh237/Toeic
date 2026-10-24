import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { Modal } from "../../components/common/Modal";
import { Spinner } from "../../components/common/Spinner";
import { MediaField } from "../components/MediaField";
import { CrudToolbar } from "../components/CrudToolbar";
import { DataTable } from "../components/DataTable";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";

const tabs = [{ id: "parts", label: "Part" }, { id: "sections", label: "Mục luyện tập" }, { id: "questions", label: "Câu hỏi" }];
export function PracticeManagementPage({ listening }) {
  const { api: authoringApi, basePath, canManageParts } = useAuthoringScope();
  const api = listening ? authoringApi.listening : authoringApi.reading;
  const navigate = useNavigate();
  const [tab, setTab] = useState("questions");
  const [parts, setParts] = useState([]), [part, setPart] = useState("");
  const [sections, setSections] = useState([]), [sectionId, setSectionId] = useState("");
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true), [error, setError] = useState("");
  const [open, setOpen] = useState(false), [editing, setEditing] = useState(null), [form, setForm] = useState({});
  const loadParts = useCallback(async () => {
    const list = await api.listParts(); setParts(list || []);
    setPart((id) => list?.some((p) => String(p.partNumber) === id) ? id : String(list?.[0]?.partNumber || ""));
  }, [api]);
  useEffect(() => { loadParts().catch((e) => setError(e.message)).finally(() => setLoading(false)); }, [loadParts]);
  useEffect(() => {
    let active = true; setSections([]); setSectionId("");
    if (part) api.listSections(part).then((list) => { if (active) { setSections(list || []); setSectionId(String(list?.[0]?.id || "")); } }).catch((e) => { if (active) setError(e.message); });
    return () => { active = false; };
  }, [api, part]);
  useEffect(() => {
    let active = true; setQuestions([]);
    if (sectionId) api.listQuestions(sectionId).then((list) => { if (active) setQuestions(list || []); }).catch((e) => { if (active) setError(e.message); });
    return () => { active = false; };
  }, [api, sectionId]);
  function edit(row) {
    if (tab === "questions") {
      if (!row.group?.id) { setError("Không tìm thấy ngữ cảnh của câu hỏi."); return; }
      navigate(`${basePath}/content?part=${part}&groupId=${row.group.id}&sectionId=${sectionId}`); return;
    }
    setEditing(row || null); setForm(row || (tab === "parts" ? { partNumber: Number(part) || (listening ? 1 : 5), description: "" } : { name: "", description: "", audioUrl: "" })); setOpen(true);
  }
  function create() {
    if (tab === "questions") {
      if (!sectionId && !canManageParts) {
        setTab("sections"); setEditing(null); setForm({ name: "", description: "", audioUrl: "" }); setOpen(true); return;
      }
      navigate(`${basePath}/content?part=${part}&sectionId=${sectionId}`); return;
    }
    edit(null);
  }
  async function save(e) {
    e.preventDefault(); setError("");
    try {
      if (tab === "parts") { const body = { partNumber: Number(form.partNumber), description: form.description }; if (editing) await api.updatePart(editing.id, body); else await api.createPart(body); await loadParts(); }
      else { if (!part) throw new Error("Chọn Part trước khi thêm mục."); const body = { partId: Number(part), name: form.name, description: form.description, audioUrl: form.audioUrl || "" }; const saved = editing ? await api.updateSection(editing.id, body) : await api.createSection(body); setSections(await api.listSections(part)); setSectionId(String(saved.id)); }
      setOpen(false);
    } catch (err) { setError(err.message); }
  }
  async function archive(row) {
    if (!window.confirm("Lưu trữ mục này? Lịch sử học viên được giữ lại.")) return;
    try {
      if (tab === "parts") { await api.deletePart(row.id); await loadParts(); }
      else if (tab === "sections") { await api.deleteSection(row.id); const list = await api.listSections(part); setSections(list); setSectionId(String(list?.[0]?.id || "")); }
      else { await api.deleteQuestion(row.id); setQuestions(await api.listQuestions(sectionId)); }
    } catch (err) { setError(err.message); }
  }
  const columns = tab === "parts" ? [{ key: "partNumber", label: "Part" }, { key: "description", label: "Mô tả" }] : tab === "sections" ? [{ key: "name", label: "Tên" }, { key: "description", label: "Mô tả" }] : [{ key: "questionNumber", label: "Câu" }, { key: "questionText", label: "Nội dung" }, { key: "context", label: "Ngữ cảnh", render: (q) => q.group?.title || "Không có tiêu đề" }, { key: "correctAnswer", label: "Đáp án" }];
  if (loading) return <Spinner />;
  return <div className="space-y-4">
    {error ? <Alert tone="error">{error}</Alert> : null}
    <CrudToolbar tabs={canManageParts ? tabs : tabs.filter(t => t.id !== "parts")} active={tab} onTab={setTab} onCreate={create} createLabel={tab === "questions" ? "Thêm câu hỏi" : "Thêm mới"} extra={tab !== "parts" ? <div className="flex flex-wrap gap-2"><select aria-label="Part" className="rounded-lg border p-2" value={part} onChange={(e) => setPart(e.target.value)}>{parts.map((p) => <option key={p.id} value={p.partNumber}>Part {p.partNumber}</option>)}</select>{tab === "questions" ? <select aria-label="Mục luyện tập" className="rounded-lg border p-2" value={sectionId} onChange={(e) => setSectionId(e.target.value)}><option value="">Chọn mục luyện tập</option>{sections.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}</select> : null}</div> : null} />
    <DataTable rows={tab === "parts" ? parts : tab === "sections" ? sections : questions} columns={[...columns, { key: "actions", label: "", render: (row) => <div className="flex gap-2"><Button variant="secondary" onClick={() => edit(row)}>Sửa</Button><Button variant="ghost" onClick={() => archive(row)}>Lưu trữ</Button></div> }]} />
    <Modal open={open} title={editing ? "Cập nhật" : "Tạo mới"} onClose={() => setOpen(false)}><form className="space-y-4" onSubmit={save}>{tab === "parts" ? <><Input label="Part" type="number" value={form.partNumber || ""} onChange={(e) => setForm((f) => ({ ...f, partNumber: e.target.value }))} required /><Input label="Mô tả" value={form.description || ""} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} /></> : <><Input label="Tên mục" value={form.name || ""} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required /><label className="block text-sm">Mô tả<textarea className="mt-2 min-h-24 w-full rounded-lg border p-3" value={form.description || ""} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} /></label>{listening ? <MediaField label="Audio chung (tùy chọn)" kind="audio" value={form.audioUrl || ""} onChange={(e) => setForm((f) => ({ ...f, audioUrl: e.target.value }))} /> : null}</>}<Button type="submit">Lưu</Button></form></Modal>
  </div>;
}
