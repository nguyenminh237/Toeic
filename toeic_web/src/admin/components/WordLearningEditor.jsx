import { Input } from "../../components/common/Input";
import { Button } from "../../components/common/Button";
import { PARTS_OF_SPEECH, getPartOfSpeech } from "../../constants/partOfSpeech";
import { blankSense } from "../../utils/vocabulary";

const fieldClass = "mt-1 min-h-20 w-full rounded-lg border border-slate-200 bg-white p-3 text-sm";
function Pos({ value, onChange }) {
  const selected = getPartOfSpeech(value || "noun");
  const options = PARTS_OF_SPEECH.some(p => p.value === selected.value) ? PARTS_OF_SPEECH : [...PARTS_OF_SPEECH, selected];
  return <label className="block text-sm font-medium">Từ loại<select value={selected.value} onChange={e => onChange(e.target.value)} className="mt-1 h-12 w-full rounded-lg border border-slate-200 bg-white px-3">{options.map(p => <option key={p.value} value={p.value}>{p.label}</option>)}</select></label>;
}
export function WordLearningEditor({ value, onChange }) {
  function senseField(index, changes) { onChange({ ...value, senses: value.senses.map((s, i) => i === index ? { ...s, ...changes } : s) }); }
  return <div className="min-w-0 space-y-5">
    <div className="grid gap-3 sm:grid-cols-2"><Input label="Từ / cụm từ tiếng Anh" required maxLength={100} value={value.word} onChange={e => onChange({ ...value, word: e.target.value })} /><Input label="Phiên âm (tùy chọn)" maxLength={255} value={value.pronunciation || ""} onChange={e => onChange({ ...value, pronunciation: e.target.value })} /></div>
    {value.senses.map((sense, index) => <section key={index} className="space-y-3 rounded-xl border border-slate-200 bg-slate-50 p-4">
      <div className="flex items-center justify-between gap-2"><h3 className="font-semibold">Nghĩa {index + 1}</h3>{value.senses.length > 1 ? <Button variant="ghost" onClick={() => onChange({ ...value, senses: value.senses.filter((_, i) => i !== index) })}>Bỏ nghĩa</Button> : null}</div>
      <Input label="Nghĩa tiếng Việt" required maxLength={2000} value={sense.meaning || ""} onChange={e => senseField(index, { meaning: e.target.value })} />
      <div className="grid gap-3 sm:grid-cols-2"><Pos value={sense.partOfSpeech} onChange={partOfSpeech => senseField(index, { partOfSpeech })} /><Input label="Ưu tiên từ aim (tùy chọn)" type="number" min={10} max={990} value={sense.minimumTargetScore ?? ""} onChange={e => senseField(index, { minimumTargetScore: e.target.value })} /></div>
      <Input label="Ví dụ đơn giản (tùy chọn)" maxLength={1000} value={sense.exampleSentence || ""} onChange={e => senseField(index, { exampleSentence: e.target.value })} />
      <Input label="Dịch ví dụ (tùy chọn)" maxLength={1000} value={sense.exampleTranslation || ""} onChange={e => senseField(index, { exampleTranslation: e.target.value })} />
      {sense.studyNote ? <Input label="Gợi ý học nghĩa này" maxLength={1000} value={sense.studyNote} onChange={e => senseField(index, { studyNote: e.target.value })} /> : null}
      <details><summary className="cursor-pointer text-sm font-medium text-blue-700">Đồng nghĩa, trái nghĩa và collocation{[sense.synonyms, sense.antonyms, sense.collocations].some(v => v?.length) ? " · Có nội dung" : " · Tùy chọn"}</summary>
        <div className="mt-3 grid gap-3 xl:grid-cols-3">{[["synonyms", "Đồng nghĩa"], ["antonyms", "Trái nghĩa"], ["collocations", "Collocation quan trọng"]].map(([name, label]) => <label key={name} className="block min-w-0 text-sm">{label}<textarea aria-label={`${label} nghĩa ${index + 1}`} value={(sense[name] || []).join("\n")} onChange={e => senseField(index, { [name]: e.target.value.split("\n") })} placeholder="Mỗi dòng một mục. Có thể để trống." className={fieldClass} /></label>)}</div>
      </details>
    </section>)}
    {value.senses.length < 5 ? <Button variant="secondary" onClick={() => onChange({ ...value, senses: [...value.senses, blankSense()] })}>+ Thêm nghĩa</Button> : null}
    <details open={value.family.length > 0 || undefined}><summary className="cursor-pointer font-medium">Họ từ (word family) · {value.family.length || "Tùy chọn"}</summary><div className="mt-3 space-y-3">
      {value.family.map((f, index) => <div key={index} className="grid gap-2 rounded-xl bg-slate-50 p-3 sm:grid-cols-2"><Input label="Từ cùng họ" maxLength={100} value={f.word || ""} onChange={e => onChange({ ...value, family: value.family.map((item, i) => i === index ? { ...item, word: e.target.value } : item) })} /><Input label="Từ loại (tùy chọn)" maxLength={40} value={f.partOfSpeech || ""} onChange={e => onChange({ ...value, family: value.family.map((item, i) => i === index ? { ...item, partOfSpeech: e.target.value } : item) })} /><Input label="Nghĩa (tùy chọn)" maxLength={500} value={f.meaning || ""} onChange={e => onChange({ ...value, family: value.family.map((item, i) => i === index ? { ...item, meaning: e.target.value } : item) })} /><Button variant="ghost" onClick={() => onChange({ ...value, family: value.family.filter((_, i) => i !== index) })}>Bỏ từ cùng họ</Button></div>)}
      {value.family.length < 12 ? <Button variant="secondary" onClick={() => onChange({ ...value, family: [...value.family, { word: "", partOfSpeech: "", meaning: "" }] })}>+ Thêm từ cùng họ</Button> : null}
    </div></details>
  </div>;
}
