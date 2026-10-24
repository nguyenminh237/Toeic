import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { FileUp, Check, ArrowLeft } from "lucide-react";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { http } from "../../services/httpClient";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";
import { useAsync } from "../../hooks/useAsync";
import { vocabularyService } from "../../services/vocabularyService";
import { WordLearningEditor } from "../components/WordLearningEditor";
import { AIM_OPTIONS, editableWord, wordPayload } from "../../utils/vocabulary";

export function VocabularyImportPage() {
  const { api, basePath } = useAuthoringScope();
  const root = `${basePath}/vocabulary/import`;
  const [mode, setMode] = useState("manual");
  const [manual, setManual] = useState(() => editableWord());
  async function saveManual(event) {
    event.preventDefault(); if (busy) return; setError(""); setBusy(true);
    try {
      await api.vocabulary.createWord(wordPayload(manual, setId));
      navigate(`${basePath}/vocabulary?setId=${setId}`);
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  const [search] = useSearchParams(); const navigate = useNavigate();
  const setId = search.get("setId");
  const [step, setStep] = useState(1), [file, setFile] = useState(null), [fromPage, setFromPage] = useState(1), [toPage, setToPage] = useState(3);
  const [aim, setAim] = useState(650), [terms, setTerms] = useState([]), [typed, setTyped] = useState(""), [extraction, setExtraction] = useState(null);
  const [drafts, setDrafts] = useState([]), [active, setActive] = useState(0), [busy, setBusy] = useState(false), [progress, setProgress] = useState("");
  const [error, setError] = useState(""), [saved, setSaved] = useState(false);
  const metadata = useAsync(() => setId ? vocabularyService.getSet(setId) : Promise.resolve(null), [setId]);
  const existing = useAsync(() => setId ? vocabularyService.getWords(setId) : Promise.resolve([]), [setId]);
  const duplicate = term => existing.data?.some(w => w.word.trim().toLowerCase() === term.trim().toLowerCase());
  const selected = terms.filter(t => t.selected); const approved = drafts.filter(d => d.selected && d.reviewed);
  useEffect(() => {
    if (!drafts.length || saved) return;
    const warn = event => { event.preventDefault(); event.returnValue = ""; };
    window.addEventListener("beforeunload", warn); return () => window.removeEventListener("beforeunload", warn);
  }, [drafts.length, saved]);
  function chooseTerms(list) {
    const normalized = [...new Set(list.map(t => t.trim().toLowerCase()).filter(Boolean))];
    let count = 0;
    setTerms(normalized.map(term => ({ term, selected: !duplicate(term) && count++ < 30 }))); setStep(2); setError("");
  }
  async function extract() {
    setError(""); if (!file) { setError("Chọn ảnh, PDF hoặc TXT trước."); return; }
    if (file.size > 10 * 1024 * 1024) { setError("File tối đa 10 MB."); return; }
    setBusy(true); setProgress("Đang đọc tài liệu và lọc từ tiếng Anh…");
    try {
      const body = new FormData(); body.append("file", file); body.append("fromPage", fromPage); body.append("toPage", toPage);
      const result = await http.post(`${root}/extract`, body); setExtraction(result);
      if (!result.terms.length) throw new Error("Không tìm thấy từ tiếng Anh rõ ràng. Hãy thử ảnh nét hơn hoặc trang khác.");
      chooseTerms(result.terms);
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  async function generate() {
    if (!selected.length || selected.length > 30) { setError("Chọn từ 1 đến 30 từ mỗi lần tạo nội dung."); return; }
    if (new Set(selected.map(t => t.term.trim().toLowerCase())).size !== selected.length) { setError("Có từ bị trùng sau khi chỉnh sửa. Hãy bỏ chọn bản trùng."); return; }
    if (drafts.length && !window.confirm("Tạo lại sẽ thay nội dung AI và các chỉnh sửa đang có. Tiếp tục?")) return;
    setBusy(true); setError(""); setDrafts([]);
    const created = [];
    try {
      for (let index = 0; index < selected.length; index += 3) {
        setProgress(`Đang soạn ${Math.min(index + 3, selected.length)}/${selected.length} từ theo aim ${aim}…`);
        const batch = await http.post(`${root}/generate`, { targetScore: Number(aim), terms: selected.slice(index, index + 3).map(t => t.term) });
        created.push(...batch.map(word => ({ value: editableWord(word), selected: true, reviewed: false })));
        setDrafts([...created]);
      }
      setActive(0); setStep(3);
    } catch (e) { setError(`${e.message}${created.length ? ` Đã giữ ${created.length} từ tạo thành công để bạn duyệt.` : ""}`); if (created.length) { setActive(0); setStep(3); } }
    finally { setBusy(false); }
  }
  function update(value) { setDrafts(list => list.map((d, i) => i === active ? { ...d, value, reviewed: false } : d)); }
  async function save(event) {
    event.preventDefault(); setError("");
    const chosen = drafts.filter(d => d.selected);
    if (!chosen.length || chosen.some(d => !d.reviewed)) { setError("Hãy duyệt từng từ đã chọn trước khi lưu."); return; }
    setBusy(true); setProgress("Đang lưu các từ đã duyệt…");
    try {
      await http.post(`${root}/confirm`, { setId: Number(setId), reviewed: true, words: chosen.map(d => wordPayload(d.value, setId)) });
      setSaved(true);
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  if (saved) return <div className="space-y-4"><Alert tone="success">Đã thêm {approved.length} từ vào {metadata.data?.setName || "bộ từ vựng"}.</Alert><Button onClick={() => navigate(`${basePath}/vocabulary?setId=${setId}`)}>Về bộ từ vựng</Button></div>;
  return <div className="min-w-0 space-y-5">
    <Button variant="ghost" onClick={() => { if (!drafts.length || window.confirm("Nội dung đang duyệt chưa lưu. Rời màn hình?")) navigate(`${basePath}/vocabulary?setId=${setId || ""}`); }}><ArrowLeft className="h-4 w-4" />Quay lại</Button>
    <div><h1 className="text-2xl font-semibold">Thêm từ</h1><p className="mt-2 text-sm text-slate-500">{metadata.data?.setName ? `Bộ nhận: ${metadata.data.setName}. ` : ""}Bạn xem và sửa nội dung trước khi thêm vào bộ.</p></div>
    {mode === "ai" ? <ol className="flex flex-wrap gap-2 text-sm">{["Nguồn từ", "Chọn từ & aim", "Duyệt & thêm"].map((label, i) => <li key={label} className={`rounded-full px-4 py-2 ${step === i + 1 ? "bg-blue-600 text-white" : "bg-slate-100 text-slate-500"}`}>{i + 1}. {label}</li>)}</ol> : null}
    {!setId ? <Alert tone="error">Hãy chọn bộ từ vựng trước khi thêm từ.</Alert> : null}
    {error || metadata.error || existing.error ? <Alert tone="error">{error || metadata.error?.message || existing.error?.message}</Alert> : null}
    {busy ? <p role="status" className="rounded-xl bg-blue-50 p-4 text-sm text-blue-800">{progress}</p> : null}
    <div role="group" aria-label="Cách thêm từ" className="flex flex-wrap gap-2">
      <Button variant={mode === "manual" ? "primary" : "secondary"} disabled={busy} onClick={() => { setMode("manual"); setError(""); }}>Nhập thủ công</Button>
      <Button variant={mode === "ai" ? "primary" : "secondary"} disabled={busy} onClick={() => { setMode("ai"); setError(""); }}>Nhập bằng AI</Button>
    </div>
    {mode === "manual" ? <form onSubmit={saveManual} className="rounded-2xl border border-slate-200 bg-white p-5">
      <fieldset disabled={busy || !setId || metadata.loading || Boolean(metadata.error)} className="space-y-5">
        <p className="text-sm text-slate-500">Tự nhập từ và nghĩa muốn học. Ví dụ, họ từ, đồng nghĩa, trái nghĩa và collocation đều có thể để trống.</p>
        <WordLearningEditor value={manual} onChange={setManual} />
        <Button type="submit" loading={busy}>Lưu từ</Button>
      </fieldset>
    </form> : <fieldset disabled={busy || !setId || existing.loading || Boolean(existing.error)} className="min-w-0 space-y-5">
      {step === 1 ? <section className="space-y-5 rounded-2xl border border-slate-200 bg-white p-5">
        <label className="block cursor-pointer rounded-xl border-2 border-dashed border-blue-200 bg-blue-50 p-6 text-center"><FileUp className="mx-auto mb-3 h-8 w-8 text-blue-600" /><span className="block font-semibold">Chọn ảnh hoặc tài liệu</span><span className="mt-1 block text-sm text-slate-500">PDF, PNG, JPEG, TXT · tối đa 10 MB</span><input aria-label="Tài liệu từ vựng" type="file" accept=".pdf,.png,.jpg,.jpeg,.txt" className="mt-4 w-full min-w-0 text-sm" onChange={e => { setFile(e.target.files[0] || null); setExtraction(null); }} /></label>
        {file?.name.toLowerCase().endsWith(".pdf") ? <div><div className="grid gap-3 sm:grid-cols-2"><Input label="Từ trang" type="number" min={1} value={fromPage} onChange={e => setFromPage(Number(e.target.value))} /><Input label="Đến trang" type="number" min={fromPage} max={Number(fromPage) + 4} value={toPage} onChange={e => setToPage(Number(e.target.value))} /></div><p className="mt-2 text-xs text-slate-500">Mỗi lần tối đa 5 trang. PDF scan được đọc bằng AI nhận diện ảnh.</p></div> : null}
        <p className="text-sm text-slate-500">Phần cần nhận diện sẽ được gửi tới Groq để lọc từ tiếng Anh. Tài liệu gốc không được lưu vào kho media.</p>
        <Button onClick={extract} disabled={!file}>Trích từ tiếng Anh</Button>
        <details className="border-t border-slate-100 pt-4"><summary className="cursor-pointer text-sm text-slate-600">Hoặc nhập danh sách từ có sẵn</summary><textarea aria-label="Danh sách từ tiếng Anh" value={typed} onChange={e => setTyped(e.target.value)} placeholder={"invoice\ncharge\napply for"} maxLength={10000} className="my-3 min-h-32 w-full rounded-lg border p-3" /><Button variant="secondary" onClick={() => chooseTerms(typed.split(/[\n,;]+/))} disabled={!typed.trim()}>Tiếp tục với danh sách này</Button></details>
      </section> : null}
      {step === 2 ? <section className="space-y-5 rounded-2xl border border-slate-200 bg-white p-5">
        <div className="flex flex-wrap items-center justify-between gap-3"><h2 className="font-semibold">Chọn từ cần học · {selected.length}/{terms.length}</h2><Button variant="ghost" onClick={() => setStep(1)}>Đổi nguồn</Button></div>
        {extraction ? <p className="text-sm text-slate-500">Đã đọc trang {extraction.fromPage}–{extraction.toPage}/{extraction.totalPages}.{extraction.toPage < extraction.totalPages ? " Có thể đổi khoảng trang để đọc phần còn lại." : ""}{extraction.limited ? " Đã đạt giới hạn 80 từ; hãy chọn ít trang hơn để đọc kỹ từng phần." : ""}</p> : null}
        <p className="text-sm text-slate-500">Bỏ từ không cần học hoặc sửa lỗi nhận diện. Từ đã có trong bộ được bỏ chọn sẵn.</p>
        <div className="flex flex-wrap gap-2"><Button variant="secondary" onClick={() => { let count = 0; setTerms(list => list.map(t => ({ ...t, selected: !duplicate(t.term) && count++ < 30 }))); }}>Chọn tối đa 30 từ đầu</Button><Button variant="ghost" onClick={() => setTerms(list => list.map(t => ({ ...t, selected: false })))}>Bỏ chọn tất cả</Button></div>
        <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">{terms.map((item, index) => <label key={index} className={`flex min-w-0 items-center gap-2 rounded-xl border p-3 ${item.selected ? "border-blue-300 bg-blue-50" : "border-slate-200"}`}><input type="checkbox" aria-label={`Chọn từ ${item.term}`} checked={item.selected} disabled={duplicate(item.term)} onChange={e => setTerms(list => list.map((t, i) => i === index ? { ...t, selected: e.target.checked } : t))} /><input aria-label={`Từ trích xuất ${index + 1}`} className="min-w-0 w-full bg-transparent outline-none" maxLength={100} value={item.term} onChange={e => setTerms(list => list.map((t, i) => i === index ? { ...t, term: e.target.value, selected: duplicate(e.target.value) ? false : t.selected } : t))} />{duplicate(item.term) ? <span className="shrink-0 text-xs text-slate-500">Đã có</span> : null}</label>)}</div>
        <div className="space-y-2 border-t border-slate-100 pt-4"><label className="block text-sm font-medium">Mục tiêu TOEIC<select aria-label="Aim TOEIC" value={aim} onChange={e => setAim(Number(e.target.value))} className="ml-3 rounded-lg border p-3">{AIM_OPTIONS.map(score => <option key={score} value={score}>{score} điểm</option>)}</select></label><p className="text-sm text-slate-500">AI đề xuất những nghĩa nên ưu tiên cho mục tiêu này. Đây là định hướng học tập, không phải phân loại điểm chính thức.</p></div>
        <Button onClick={generate} disabled={!selected.length || selected.length > 30}>Tạo nội dung cho {selected.length} từ</Button>
      </section> : null}
      {step === 3 && drafts.length ? <form onSubmit={save} className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3"><p className="text-sm text-slate-500">Aim {aim} · Đã duyệt {approved.length}/{drafts.filter(d => d.selected).length} từ được chọn</p><Button variant="ghost" onClick={() => setStep(2)}>Quay lại chọn từ</Button></div>
        <div className="grid min-w-0 items-start gap-5 lg:grid-cols-[220px_minmax(0,1fr)]">
          <nav aria-label="Từ đang duyệt" className="flex flex-wrap gap-2 lg:sticky lg:top-4 lg:flex-col">{drafts.map((draft, index) => <button type="button" key={index} onClick={() => setActive(index)} className={`flex items-center justify-between gap-2 rounded-xl border px-4 py-3 text-left text-sm ${index === active ? "border-blue-300 bg-blue-50 text-blue-800" : "border-slate-200 bg-white"}`}><span className={!draft.selected ? "line-through text-slate-400" : ""}>{draft.value.word}</span>{draft.reviewed ? <Check className="h-4 w-4 shrink-0 text-emerald-600" /> : null}</button>)}</nav>
          <section className="min-w-0 space-y-5 rounded-2xl border border-slate-200 bg-white p-4 sm:p-5">
            <div className="flex flex-wrap items-center justify-between gap-3"><h2 className="font-semibold">Nội dung AI đề xuất</h2><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={drafts[active].selected} onChange={e => setDrafts(list => list.map((d, i) => i === active ? { ...d, selected: e.target.checked } : d))} />Thêm từ này</label></div>
            <fieldset disabled={!drafts[active].selected} className="min-w-0"><WordLearningEditor value={drafts[active].value} onChange={update} /></fieldset>
            <label className="flex items-start gap-3 rounded-xl bg-emerald-50 p-4 text-sm font-medium text-emerald-900"><input type="checkbox" aria-label="Đã duyệt từ này" disabled={!drafts[active].selected} checked={drafts[active].reviewed} onChange={e => { if (e.target.checked && !e.target.form.reportValidity()) return; setDrafts(list => list.map((d, i) => i === active ? { ...d, reviewed: e.target.checked } : d)); }} className="mt-1" />Tôi đã xem và duyệt nội dung của từ này</label>
            {active < drafts.length - 1 ? <Button variant="secondary" onClick={() => setActive(active + 1)}>Duyệt từ tiếp theo</Button> : null}
          </section>
        </div>
        <div className="sticky bottom-0 flex flex-wrap items-center justify-between gap-3 rounded-xl border bg-white p-4 shadow-lg"><span className="text-sm">Chỉ lưu sau khi tất cả từ được chọn đã duyệt</span><Button type="submit" disabled={!approved.length || drafts.some(d => d.selected && !d.reviewed)}>Thêm {approved.length} từ đã duyệt</Button></div>
      </form> : null}
    </fieldset>}
  </div>;
}
