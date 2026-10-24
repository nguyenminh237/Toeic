import { useNavigate } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { Modal } from "../../components/common/Modal";
import { Spinner } from "../../components/common/Spinner";
import { formatDateTime, statusLabel } from "../../utils/format";
import { ContextHint } from "../components/ContextHint";
import { MediaField } from "../components/MediaField";
import { CrudToolbar } from "../components/CrudToolbar";
import { DataTable } from "../components/DataTable";
import { adminApi } from "../services/adminApi";

const TABS = [
  { id: "suites", label: "Suite" },
  { id: "parts", label: "Part" },
  { id: "questions", label: "Câu hỏi" },
  { id: "results", label: "Kết quả" },
];

export function TestManagementPage() {
  const api = adminApi.tests;
  const navigate = useNavigate();
  function openQuestionEditor(row) {
    const part = parts.find((p) => String(p.id) === String(partId))?.partNumber || 5;
    navigate(`/admin/content?part=${part}&testPartId=${partId}${row?.group?.id ? `&groupId=${row.group.id}` : ""}`);
  }
  const [tab, setTab] = useState("suites");
  const [suites, setSuites] = useState([]);
  const [parts, setParts] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [results, setResults] = useState([]);
  const [suiteId, setSuiteId] = useState("");
  const [partId, setPartId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({});

  const loadSuites = useCallback(async () => {
    const data = (await api.listSuites()) || [];
    setSuites(data);
    setSuiteId((current) => data.some((suite) => String(suite.id) === current) ? current : data[0] ? String(data[0].id) : "");
  }, [api]);

  useEffect(() => {
    Promise.all([loadSuites(), api.listResults().then((data) => setResults(data || []))])
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [api, loadSuites]);

  useEffect(() => {
    let active = true;
    setParts([]); setPartId("");
    if (suiteId) {
      api.listParts(suiteId).then((data) => { if (!active) return; setParts(data || []); setPartId(data?.[0] ? String(data[0].id) : ""); }).catch((err) => { if (active) setError(err.message); });
    }
    return () => { active = false; };
  }, [api, suiteId]);

  useEffect(() => {
    let active = true;
    setQuestions([]);
    if (partId) {
      api.listQuestions(partId).then((data) => { if (active) setQuestions(data || []); }).catch((err) => { if (active) setError(err.message); });
    }
    return () => { active = false; };
  }, [api, partId]);

  function change(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function openCreate() {
    if (tab === "questions") { openQuestionEditor(); return; }
    if (tab === "results") {
      api.listResults().then((data) => setResults(data || [])).catch((err) => setError(err.message));
      return;
    }
    setEditing(null);
    if (tab === "suites") setForm({ suiteName: "", description: "", source: "ETS" });
    if (tab === "parts") setForm({ suiteId, partNumber: 1, partType: "listening", questionCount: 6, timeLimit: 5, audioUrl: "" });
    setOpen(true);
  }

  async function onSubmit(event) {
    event.preventDefault();
    try {
      if (tab === "suites") {
        if (editing) await api.updateSuite(editing.id, form); else await api.createSuite(form);
        await loadSuites();
      }
      if (tab === "parts") {
        if (!suiteId) throw new Error("Hãy chọn suite ở bộ lọc trước khi thêm Part.");
        const payload = {
          partNumber: Number(form.partNumber),
          partType: form.partType,
          questionCount: Number(form.questionCount),
          timeLimit: Number(form.timeLimit),
          audioUrl: form.audioUrl || "",
          suiteId: Number(suiteId),
        };
        if (editing) await api.updatePart(editing.id, payload); else await api.createPart(payload);
        setParts((await api.listParts(suiteId)) || []);
      }

      setOpen(false);
    } catch (err) {
      setError(err.message);
    }
  }

  async function onDelete(row) {
    if (!window.confirm("Lưu trữ mục này? Lịch sử học viên được giữ lại.")) return;
    try {
      if (tab === "suites") { await api.deleteSuite(row.id); await loadSuites(); }
      if (tab === "parts") { await api.deletePart(row.id); setParts((await api.listParts(suiteId)) || []); }
      if (tab === "questions") { await api.deleteQuestion(row.id); setQuestions((await api.listQuestions(partId)) || []); }
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <Spinner />;
  const rows = { suites, parts, questions, results }[tab];
  const columns = {
    suites: [{ key: "suiteName", label: "Suite" }, { key: "source", label: "Nguồn" }, { key: "description", label: "Mô tả" }],
    parts: [
      { key: "partNumber", label: "Part" },
      { key: "partType", label: "Loại" },
      { key: "questionCount", label: "Số câu" },
      { key: "timeLimit", label: "Phút" },
      { key: "audioUrl", label: "Audio chung", render: (row) => (row.audioUrl ? "Có" : "") },
    ],
    questions: [{ key: "questionNumber", label: "#" }, { key: "questionText", label: "Câu hỏi" }, { key: "correctAnswer", label: "Đáp án" }],
    results: [
      { key: "id", label: "ID" },
      { key: "partId", label: "Part" },
      { key: "status", label: "Trạng thái", render: (row) => statusLabel(row.status) },
      { key: "score", label: "Điểm" },
      { key: "startedAt", label: "Bắt đầu", render: (row) => formatDateTime(row.startedAt) },
    ],
  }[tab];

  return (
    <div>
      {error ? <div className="mb-4"><Alert tone="error">{error}</Alert></div> : null}

      <CrudToolbar
        tabs={TABS}
        active={tab}
        onTab={setTab}
        onCreate={openCreate}
        createLabel={tab === "results" ? "Làm mới" : tab === "questions" ? "Thêm câu hỏi" : "Thêm mới"}
        extra={
          tab === "parts" || tab === "questions" ? (
            <select className="h-10 rounded-lg border border-slate-200 px-3" value={tab === "parts" ? suiteId : partId} onChange={(event) => (tab === "parts" ? setSuiteId(event.target.value) : setPartId(event.target.value))}>
              {(tab === "parts" ? suites : parts).map((item) => (
                <option key={item.id} value={item.id}>
                  {item.suiteName || `Part ${item.partNumber}`}
                </option>
              ))}
            </select>
          ) : null
        }
      />
      <DataTable
        rows={rows}
        columns={[
          ...columns,
          ...(tab === "results"
            ? []
            : [{
                key: "actions",
                label: "",
                render: (row) => (
                  <div className="flex gap-2">
                    <Button variant="secondary" onClick={() => { if (tab === "questions") { openQuestionEditor(row); return; } setEditing(row); setForm(row); setOpen(true); }}>Sửa</Button>
                    <Button variant="ghost" onClick={() => onDelete(row)}>Lưu trữ</Button>
                  </div>
                ),
              }]),
        ]}
      />
      <Modal open={open && tab !== "results"} title={editing ? "Cập nhật" : "Tạo mới"} onClose={() => setOpen(false)}>
        <form className="space-y-3" onSubmit={onSubmit}>
          {tab === "suites" ? (
            <>
              <Input label="Tên suite" name="suiteName" value={form.suiteName || ""} onChange={change} required />
              <Input label="Nguồn" name="source" value={form.source || ""} onChange={change} />
              <Input label="Mô tả" name="description" value={form.description || ""} onChange={change} />
            </>
          ) : null}
          {tab === "parts" ? (
            <>
              <ContextHint label="Suite" value={suites.find((item) => String(item.id) === String(suiteId))?.suiteName} />
              <Input label="Part" name="partNumber" type="number" value={form.partNumber || ""} onChange={change} required />
              <Input label="Loại" name="partType" value={form.partType || ""} onChange={change} required />
              <Input label="Số câu" name="questionCount" type="number" value={form.questionCount || ""} onChange={change} required />
              <Input label="Thời gian (phút)" name="timeLimit" type="number" value={form.timeLimit || ""} onChange={change} required />
              <MediaField
                label="Audio chung cho cả Part (listening)"
                name="audioUrl"
                kind="audio"
                value={form.audioUrl || ""}
                onChange={change}
              />
              <p className="text-xs text-slate-500">
                Chỉ upload 1 lần. Học viên nghe file này trên đầu trang, không lặp theo từng câu.
              </p>
            </>
          ) : null}

          <Button type="submit" className="w-full">Lưu</Button>
        </form>
      </Modal>
    </div>
  );
}
