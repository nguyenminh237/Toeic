import { PrivateImage, PrivateAudio } from "../../components/common/PrivateMedia";
import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { QuestionDocuments } from "../../components/practice/QuestionDocuments";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";
import { useAsync } from "../../hooks/useAsync";
import { http } from "../../services/httpClient";
import { listeningService } from "../../services/listeningService";
import { readingService } from "../../services/readingService";
import { testService } from "../../services/testService";
import { MediaField } from "../components/MediaField";
import { AiExplanationField } from "../components/AiExplanationField";
import { parseQuestions } from "../utils/parseQuestions";

const expectedCount = (part) => part === 6 ? 4 : [3, 4].includes(part) ? 3 : null;
const blankGroup = (part) => ({ title: "", audioUrl: "", imageUrl: "", transcript: "", audioStartMs: "", audioEndMs: "", documents: part >= 6 ? [{ title: "", content: "", imageUrl: "" }] : [] });
const blankQuestion = (part, position) => ({ key: crypto.randomUUID(), id: null, position, questionText: part === 6 ? `Chỗ trống (${position})` : "", promptText: "", explanation: "", correctAnswer: "", hideQuestion: part === 2, choices: { A: "", B: "", C: "", D: "" } });
const initialQuestions = (part) => [blankQuestion(part, 1)];
const fromView = (q) => ({ ...q, key: crypto.randomUUID(), position: q.questionNumber, choices: Object.fromEntries(q.options.map((o) => [o.label, o.content || ""])) });
const descriptions = {
  1: "Một ảnh và audio mô tả dùng chung cho câu hỏi.",
  2: "Nhập câu hỏi và ba phương án đáp. Các câu trong cụm dùng chung file audio.",
  3: "Một hội thoại dùng chung cho ba câu hỏi. Không cần tải lại audio ở từng câu.",
  4: "Một bài nói dùng chung cho ba câu hỏi.",
  5: "Nhập nhiều câu độc lập trong cùng một lần lưu.",
  6: "Một bài đọc, bốn chỗ trống, bốn câu trắc nghiệm. Đánh dấu trong bài bằng (1) ____, (2) ____, (3) ____, (4) ____ để khớp với các câu bên phải.",
  7: "Một cụm có thể chứa nhiều tài liệu đọc. Các câu hỏi bên phải cùng sử dụng toàn bộ cụm tài liệu.",
};

export function ContentManagementPage() {
  const { basePath, canManageParts } = useAuthoringScope();
  const root = `${basePath}/content`;
  const [search] = useSearchParams();
  const initialPart = [1, 2, 3, 4, 5, 6, 7].includes(Number(search.get("part"))) ? Number(search.get("part")) : 6;
  const [part, setPart] = useState(initialPart);
  const [groupId, setGroupId] = useState(search.get("groupId") || "");
  const [drafts, setDrafts] = useState([]);
  const [activeKey, setActiveKey] = useState(() => crypto.randomUUID());
  const [activeOrder, setActiveOrder] = useState(0);
  const [addMenu, setAddMenu] = useState(false);
  const skipLoad = useRef(false);
  const [group, setGroup] = useState(() => blankGroup(initialPart));
  const [rows, setRows] = useState(() => initialQuestions(initialPart));
  const [dirty, setDirty] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [preview, setPreview] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [paste, setPaste] = useState("");
  const [targetType, setTargetType] = useState(search.get("sectionId") || !canManageParts ? "sets" : search.get("testPartId") ? "test-parts" : "");
  const [targetId, setTargetId] = useState(search.get("sectionId") || (canManageParts ? search.get("testPartId") : "") || "");
  const [start, setStart] = useState("");
  const targets = useAsync(async () => {
    if (!targetType) return [];
    if (targetType === "sets") return (part <= 4 ? listeningService : readingService).getSections(part);
    const suites = await testService.getSuites();
    const lists = await Promise.all(suites.map(async (s) => (await testService.getParts(s.id)).map((p) => ({ ...p, name: `${s.suiteName} · Part ${p.partNumber}` }))));
    return lists.flat().filter((p) => p.partNumber === part);
  }, [part, targetType]);

  useEffect(() => {
    if (skipLoad.current) { skipLoad.current = false; return; }
    let active = true;
    setError(""); setMessage(""); setPaste(""); setPreview(false);
    if (!groupId) { setGroup(blankGroup(part)); setRows(initialQuestions(part)); setLoading(false); return; }
    setLoading(true);
    setGroup(blankGroup(part)); setRows([]);
    Promise.all([http.get(`${root}/groups`, { query: { partNumber: part } }), http.get(`${root}/groups/${groupId}/questions`)])
      .then(([list, questions]) => {
        if (!active) return;
        const selected = list.find((g) => String(g.id) === groupId);
        if (!selected) throw new Error("Cụm đã được lưu trữ hoặc không thuộc Part này.");
        setGroup({ ...blankGroup(part), ...selected, documents: selected.documents?.length ? selected.documents : blankGroup(part).documents }); setRows(questions.length ? questions.map(fromView) : initialQuestions(part));
      }).catch((err) => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [part, groupId, root]);
  useEffect(() => {
    if (!dirty) return;
    const warn = (e) => { e.preventDefault(); e.returnValue = ""; };
    window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [dirty]);

  const labels = part === 2 ? ["A", "B", "C"] : ["A", "B", "C", "D"];
  const expected = expectedCount(part);
  function switchContext(nextPart, nextId) {
    if (saving || (dirty && !window.confirm("Bạn có nội dung chưa lưu. Chuyển sang cụm khác?"))) return;
    setDirty(false); setDrafts([]); setActiveKey(crypto.randomUUID()); setActiveOrder(0); setPart(nextPart); setGroupId(nextId); setTargetId("");
  }
  function snapshot() { return { key: activeKey, order: activeOrder, id: groupId, group, rows }; }
  function restore(entry) {
    if (entry.id !== groupId) skipLoad.current = true;
    setActiveKey(entry.key); setActiveOrder(entry.order); setGroupId(entry.id); setGroup(entry.group); setRows(entry.rows); setAddMenu(false); setPaste(""); setError(""); setPreview(false);
  }
  function switchDraft(entry) {
    if (entry.key === activeKey || saving) return;
    setDrafts((list) => [...list.filter((d) => d.key !== entry.key), snapshot()]); restore(entry);
  }
  function addIndependent() {
    if (drafts.length >= 19) { setError("Mỗi lần soạn tối đa 20 ngữ cảnh."); return; }
    setDrafts((list) => [...list, snapshot()]);
    const order = Math.max(activeOrder, ...drafts.map((d) => d.order)) + 1;
    restore({ key: crypto.randomUUID(), order, id: "", group: blankGroup(part), rows: initialQuestions(part) }); setDirty(true);
  }
  function removeActiveContext() {
    if (groupId || !drafts.length) return;
    if (!window.confirm("Bỏ ngữ cảnh chưa lưu này và các câu trong đó?")) return;
    const next = [...drafts].sort((a, b) => a.order - b.order)[0];
    setDrafts((list) => list.filter((entry) => entry.key !== next.key)); restore(next); setDirty(true);
  }
  function groupField(name, value) { setDirty(true); setGroup((g) => ({ ...g, [name]: value })); }
  function rowField(key, changes) { setDirty(true); setRows((list) => list.map((q) => q.key === key ? { ...q, ...changes } : q)); }
  function documentField(index, name, value) { groupField("documents", group.documents.map((d, i) => i === index ? { ...d, [name]: value } : d)); }
  function addQuestion() { setDirty(true); setRows((list) => [...list, blankQuestion(part, Math.max(0, ...list.map((q) => q.position)) + 1)]); }
  function importQuestions() {
    try {
      const imported = parseQuestions(paste, part);
      const saved = rows.filter((q) => q.id);
      if (saved.length + imported.length > 50) throw new Error("Một cụm chứa tối đa 50 câu trong một lần soạn.");
      if (expected && imported.length + saved.length !== expected) throw new Error(`Part ${part} cần đúng ${expected} câu trong một cụm.`);
      if (rows.some((q) => !q.id && Object.values(q.choices).some(Boolean)) && !window.confirm("Thay các câu chưa lưu bằng nội dung vừa dán?")) return;
      let position = Math.max(0, ...saved.map((q) => q.position));
      setRows([...saved, ...imported.map((q) => ({ ...blankQuestion(part, ++position), ...q }))]); setDirty(true); setPaste(""); setError("");
    } catch (err) { setError(err.message); }
  }
  async function archive() {
    if (!window.confirm("Lưu trữ cả cụm? Cụm sẽ ngừng được sử dụng, lịch sử học viên vẫn được giữ.")) return;
    setSaving(true); setError("");
    try { await http.delete(`${root}/groups/${groupId}`); setDirty(false); setGroupId(""); }
    catch (err) { setError(err.message); } finally { setSaving(false); }
  }
  async function save(event) {
    event.preventDefault(); setSaving(true); setError(""); setMessage("");
    try {
      const entries = [...drafts, snapshot()].sort((a, b) => a.order - b.order);
      if (targetType && !targetId) throw new Error("Chọn mục luyện tập hoặc Part đề để gắn câu hỏi.");
      const payloads = entries.map((entry, index) => {
        if (expected && entry.rows.length !== expected) throw new Error(`Ngữ cảnh ${index + 1}: Part ${part} cần đúng ${expected} câu. Dùng dấu + để bổ sung câu cùng ngữ cảnh.`);
        const missing = entry.rows.findIndex((q) => !labels.includes(q.correctAnswer));
        if (missing >= 0) throw new Error(`Ngữ cảnh ${index + 1}, câu ${missing + 1}: hãy chọn đáp án đúng.`);
        return { id: entry.id ? Number(entry.id) : null, bundle: {
          version: entry.group.version,
          group: { ...entry.group, partNumber: part, audioStartMs: entry.group.audioStartMs === "" || entry.group.audioStartMs == null ? null : Number(entry.group.audioStartMs), audioEndMs: entry.group.audioEndMs === "" || entry.group.audioEndMs == null ? null : Number(entry.group.audioEndMs) },
          questions: entry.rows.map((q) => ({ id: q.id, version: q.version, position: q.position, questionText: q.questionText, promptText: q.promptText, explanation: q.explanation, hideQuestion: q.hideQuestion, options: labels.map((label) => ({ label, content: q.choices[label], correct: q.correctAnswer === label })) })),
          destination: targetType && targetId ? { type: targetType, id: Number(targetId), position: start === "" ? null : Number(start) + entries.slice(0, index).reduce((n, e) => n + e.rows.length, 0) } : null,
        } };
      });
      const result = await http.post(`${root}/batches`, { groups: payloads });
      const saved = entries.map((entry, i) => ({ ...entry, id: String(result[i].id), group: { ...entry.group, version: result[i].version }, rows: result[i].questions.map(fromView) }));
      setDrafts(saved.filter((d) => d.key !== activeKey)); restore(saved.find((d) => d.key === activeKey)); setDirty(false);
      setMessage(`Đã lưu ${saved.reduce((n, e) => n + e.rows.length, 0)} câu trong ${saved.length} ngữ cảnh.`);
    } catch (err) { setError(err.message); } finally { setSaving(false); }
  }

  return <div className="space-y-5">
    <div><h1 className="text-2xl font-semibold">Thêm câu hỏi</h1><p className="mt-2 text-sm text-slate-600">Bắt đầu với một câu. Nhấn + để thêm câu dùng chung bài đọc/audio hoặc bắt đầu ngữ cảnh mới.</p></div>
    <div className="flex flex-wrap items-center gap-3">
      <select aria-label="Part" disabled={saving} value={part} onChange={(e) => switchContext(Number(e.target.value), "")} className="rounded-lg border p-3">{[1, 2, 3, 4, 5, 6, 7].map((n) => <option key={n} value={n}>Part {n}</option>)}</select>

      <Button variant="secondary" onClick={() => setPreview((v) => !v)}>{preview ? "Tiếp tục soạn" : "Xem trước cả cụm"}</Button>
      {groupId && !drafts.length ? <Button variant="ghost" disabled={saving || loading} onClick={archive}>Lưu trữ ngữ cảnh</Button> : null}
      {!groupId && drafts.length ? <Button variant="ghost" disabled={saving || loading} onClick={removeActiveContext}>Bỏ ngữ cảnh chưa lưu</Button> : null}
      <span className="text-sm text-slate-500">{rows.length}{expected ? `/${expected}` : ""} câu · {dirty || !groupId ? "Chưa lưu" : "Đã lưu"}</span>
    </div>
    <nav aria-label="Các ngữ cảnh đang soạn" className="flex flex-wrap gap-2">{[...drafts, snapshot()].sort((a, b) => a.order - b.order).map((entry) => <Button key={entry.key} variant={entry.key === activeKey ? "primary" : "secondary"} disabled={saving || loading} onClick={() => switchDraft(entry)}>{entry.group.title || `Ngữ cảnh ${entry.order + 1}`} · {entry.rows.length} câu</Button>)}</nav>
    <p className="rounded-xl bg-blue-50 p-4 text-sm text-blue-900">{descriptions[part]}</p>
    {error || targets.error ? <Alert tone="error">{error || targets.error?.message}</Alert> : null}
    {message ? <Alert tone="success">{message}</Alert> : null}
    {loading ? <p role="status">Đang tải cụm và các câu hỏi…</p> : <form onSubmit={save}>
      <fieldset disabled={saving} className="min-w-0 space-y-5">
        <div className={`grid min-w-0 items-start gap-6 ${part !== 5 ? "lg:grid-cols-2" : ""}`}>
          {part !== 5 ? <section className="min-w-0 space-y-4 rounded-2xl border bg-white p-4 lg:sticky lg:top-4 lg:max-h-[80vh] lg:overflow-auto lg:p-5">
            <h2 className="font-semibold">{part >= 6 ? "Bài đọc chung" : "Audio và ngữ liệu chung"}</h2>
            {preview ? <><h3 className="font-medium">{group.title}</h3>{group.audioUrl ? <PrivateAudio controls src={group.audioUrl} className="w-full" /> : null}<QuestionDocuments group={group} />{group.imageUrl ? <PrivateImage src={group.imageUrl} alt="Ngữ liệu chung" className="max-h-72 object-contain" /> : null}</> : <>
              <Input label="Tên ngữ cảnh (tùy chọn)" value={group.title || ""} onChange={(e) => groupField("title", e.target.value)} placeholder={part === 6 ? "Email thông báo lịch đào tạo" : "Tên ngữ liệu hoặc nhóm câu"} />
              {part <= 4 ? <><MediaField label="Audio chung" kind="audio" value={group.audioUrl || ""} onChange={(e) => groupField("audioUrl", e.target.value)} />
                <div className="grid grid-cols-2 gap-3"><Input label="Bắt đầu (ms, tùy chọn)" type="number" min="0" value={group.audioStartMs ?? ""} onChange={(e) => groupField("audioStartMs", e.target.value)} /><Input label="Kết thúc (ms)" type="number" min="1" value={group.audioEndMs ?? ""} onChange={(e) => groupField("audioEndMs", e.target.value)} /></div>
                <label className="block text-sm">Transcript<textarea value={group.transcript || ""} onChange={(e) => groupField("transcript", e.target.value)} className="mt-2 min-h-32 w-full rounded-lg border p-3" /></label></> : null}
              {part === 1 ? <MediaField label="Ảnh của câu hỏi" value={group.imageUrl || ""} onChange={(e) => groupField("imageUrl", e.target.value)} /> : null}
              {group.documents.map((doc, i) => <div key={i} className="space-y-3 rounded-xl bg-slate-50 p-3">
                <Input label={part === 6 ? "Tiêu đề bài đọc" : `Tiêu đề tài liệu ${i + 1}`} value={doc.title || ""} onChange={(e) => documentField(i, "title", e.target.value)} />
                <label className="block text-sm">Nội dung bài đọc<textarea value={doc.content || ""} onChange={(e) => documentField(i, "content", e.target.value)} placeholder={part === 6 ? "Dear team,\nOur next training session will be held (1) ____ Monday…" : "Nhập nội dung tài liệu…"} className="mt-2 min-h-64 w-full rounded-lg border p-3 leading-7" /></label>
                <details><summary className="cursor-pointer text-sm text-slate-500">Ảnh tài liệu (tùy chọn)</summary><div className="mt-2"><MediaField label="Ảnh tài liệu" value={doc.imageUrl || ""} onChange={(e) => documentField(i, "imageUrl", e.target.value)} /></div></details>
                {part === 7 || group.documents.length > 1 ? <Button variant="ghost" onClick={() => groupField("documents", group.documents.filter((_, n) => i !== n))}>Bỏ tài liệu này</Button> : null}
              </div>)}
              {part === 7 && group.documents.length < 5 ? <Button variant="secondary" onClick={() => groupField("documents", [...group.documents, { title: "", content: "", imageUrl: "" }])}>+ Thêm tài liệu đọc</Button> : null}
              {part !== 1 ? <details><summary className="cursor-pointer text-sm text-slate-500">Ảnh / biểu đồ bổ sung (tùy chọn)</summary><div className="mt-2"><MediaField label="Ảnh / biểu đồ chung" value={group.imageUrl || ""} onChange={(e) => groupField("imageUrl", e.target.value)} /></div></details> : null}
            </>}
          </section> : null}
          <section className="min-w-0 space-y-4">
            <h2 className="font-semibold">Câu hỏi</h2>
            {!preview ? <details className="rounded-xl border bg-white p-4"><summary className="cursor-pointer font-medium">Dán nhiều câu từ tài liệu</summary><p className="my-2 text-sm text-slate-500">Mỗi câu có nội dung, các dòng A. / B. / C. / D. và dòng “Đáp án: B”. Câu đã lưu được giữ nguyên; nội dung dán thay các câu mới chưa lưu.</p><textarea aria-label="Dán nhiều câu" value={paste} maxLength={50000} onChange={(e) => setPaste(e.target.value)} placeholder={"Câu 1: The team responded _____.\nA. quick\nB. quickly\nC. quickness\nD. quicker\nĐáp án: B"} className="min-h-40 w-full rounded-lg border p-3" /><Button variant="secondary" onClick={importQuestions}>Đưa vào danh sách để chỉnh sửa</Button></details> : null}
            {rows.map((q, index) => <article key={q.key} className="space-y-3 rounded-2xl border bg-white p-5">
              <div className="flex items-center justify-between"><h3 className="font-semibold">{part === 6 ? `Chỗ trống (${q.position})` : `Câu ${index + 1}`}</h3>{!preview && !expected && !q.id ? <Button variant="ghost" onClick={() => { setRows((list) => list.filter((r) => r.key !== q.key)); setDirty(true); }}>Bỏ câu chưa lưu</Button> : null}</div>
              {preview ? <><p className="whitespace-pre-wrap">{q.questionText}</p>{labels.map((label) => <p key={label} className="rounded-lg bg-slate-50 px-3 py-2">{label}. {q.choices[label]}</p>)}</> : <>
                <Input label={part === 6 ? "Nhãn câu / chỗ trống trong bài" : "Nội dung câu hỏi"} value={q.questionText || ""} onChange={(e) => rowField(q.key, { questionText: e.target.value })} required={[3, 4, 5, 7].includes(part)} />
                <details open={Boolean(q.promptText) || undefined}><summary className="cursor-pointer text-sm text-slate-500">Chỉ dẫn bổ sung (tùy chọn)</summary><Input className="mt-2" label="Chỉ dẫn" value={q.promptText || ""} onChange={(e) => rowField(q.key, { promptText: e.target.value })} /></details>
                <div className="space-y-2">{labels.map((label) => <label key={label} className={`flex items-start gap-3 rounded-xl border p-3 ${q.correctAnswer === label ? "border-emerald-400 bg-emerald-50" : "border-slate-200"}`}><input type="radio" aria-label={`Đáp án ${label} của câu ${index + 1}`} name={`answer-${q.key}`} checked={q.correctAnswer === label} onChange={() => rowField(q.key, { correctAnswer: label })} className="mt-3" /><span className="mt-2 font-medium">{label}.</span><textarea aria-label={`Lựa chọn ${label} của câu ${index + 1}`} value={q.choices[label] || ""} onChange={(e) => rowField(q.key, { choices: { ...q.choices, [label]: e.target.value } })} required={part >= 3} rows={2} className="w-full resize-y rounded-lg border bg-white p-2 text-sm" /></label>)}</div>
                {part <= 4 ? <label className="flex gap-2 text-sm"><input type="checkbox" checked={Boolean(q.hideQuestion)} onChange={(e) => rowField(q.key, { hideQuestion: e.target.checked })} />Ẩn câu hỏi trước khi học viên trả lời</label> : null}
                <details><summary className="cursor-pointer text-sm text-blue-700">Lời giải / Tạo bằng AI</summary><div className="mt-3"><AiExplanationField question={q} partNumber={part} group={group} value={q.explanation} onChange={(explanation) => rowField(q.key, { explanation })} /></div></details>
              </>}
            </article>)}
            {!preview ? <div className="space-y-2 rounded-xl border border-dashed border-blue-300 p-3">
              <Button variant="secondary" aria-expanded={addMenu} onClick={() => setAddMenu((v) => !v)}>+ Thêm câu</Button>
              {addMenu ? <div className="grid gap-2 sm:grid-cols-2">
                <Button variant="secondary" disabled={rows.length >= (expected || 50)} onClick={() => { addQuestion(); setAddMenu(false); }}>Cùng ngữ cảnh · dùng lại bài đọc/audio</Button>
                <Button variant="secondary" onClick={addIndependent}>Ngữ cảnh mới · câu độc lập</Button>
                {expected && rows.length >= expected ? <p className="text-xs text-slate-500 sm:col-span-2">Ngữ cảnh Part {part} đã đủ {expected} câu. Câu tiếp theo cần ngữ cảnh mới.</p> : null}
              </div> : null}
            </div> : null}
          </section>
        </div>
        <section className="space-y-3 rounded-2xl border bg-white p-5">
          <h2 className="font-semibold">Lưu vào đâu?</h2>
          <p className="text-sm text-slate-500">Gắn nguyên cụm theo thứ tự. Nếu có lỗi, toàn bộ lần lưu sẽ được hủy để tránh lưu thiếu câu.</p>
          <div className="flex flex-wrap gap-3"><select aria-label="Loại bộ nhận" value={targetType} onChange={(e) => { setTargetType(e.target.value); setTargetId(""); }} className="rounded-lg border p-3">{canManageParts ? <option value="">Chỉ lưu vào ngân hàng</option> : null}<option value="sets">Mục luyện tập</option>{canManageParts ? <option value="test-parts">Part của đề thi</option> : null}</select>{targetType ? <><select required aria-label="Bộ nhận nội dung" value={targetId} onChange={(e) => setTargetId(e.target.value)} className="rounded-lg border p-3"><option value="">Chọn bộ nhận</option>{targets.data?.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}</select><Input label="Số câu bắt đầu (để trống: thêm vào cuối)" type="number" min="1" value={start} onChange={(e) => setStart(e.target.value)} /></> : null}</div>
        </section>
        <div className="sticky bottom-0 flex flex-wrap items-center justify-between gap-3 rounded-xl border bg-white/95 p-4 shadow-lg"><span className="text-sm">{drafts.length + 1} ngữ cảnh · {[...drafts, snapshot()].reduce((n, e) => n + e.rows.length, 0)} câu đang soạn</span><Button type="submit" loading={saving} disabled={!rows.length}>Lưu câu hỏi</Button></div>
      </fieldset>
    </form>}
  </div>;
}
