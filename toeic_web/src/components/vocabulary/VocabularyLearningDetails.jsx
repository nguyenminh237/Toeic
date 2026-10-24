import { PartOfSpeechBadge } from "./PartOfSpeechBadge";
import { editableWord } from "../../utils/vocabulary";

export function VocabularyLearningDetails({ word, section = "meaning" }) {
  const senses = editableWord(word).senses;
  if (section === "usage") {
    const available = senses.filter(s => s.exampleSentence || s.studyNote || s.collocations?.length);
    return <div className="space-y-5">{!available.length ? <p className="text-slate-500">Từ này chưa có ví dụ hoặc collocation.</p> : available.map((sense, index) => <section key={index} className="space-y-3">
      <p className="text-sm font-semibold text-slate-500">{sense.meaning}</p>
      {sense.exampleSentence ? <div className="rounded-2xl bg-blue-50 p-4"><p className="text-base font-medium leading-7 text-slate-900">{sense.exampleSentence}</p>{sense.exampleTranslation ? <p className="mt-2 text-sm leading-6 text-slate-500">{sense.exampleTranslation}</p> : null}</div> : null}
      {sense.collocations?.length ? <div><p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-400">Collocation</p><ul className="space-y-2 text-sm text-slate-700">{sense.collocations.map((phrase, i) => <li key={i} className="rounded-lg border border-slate-200 px-3 py-2">{phrase}</li>)}</ul></div> : null}
      {sense.studyNote ? <p className="text-sm leading-6 text-blue-700">{sense.studyNote}</p> : null}
    </section>)}</div>;
  }
  if (section === "related") {
    const related = senses.filter(s => s.synonyms?.length || s.antonyms?.length);
    return <div className="space-y-5">{word.family?.length ? <section><p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">Họ từ · Word family</p><div className="space-y-2">{word.family.map((family, i) => <div key={i} className="flex flex-wrap items-center gap-2 rounded-xl bg-violet-50 p-3"><span className="font-semibold text-violet-800">{family.word}</span><PartOfSpeechBadge value={family.partOfSpeech} />{family.meaning ? <span className="text-sm text-slate-600">{family.meaning}</span> : null}</div>)}</div></section> : null}
      {related.map((sense, index) => <section key={index} className="space-y-3"><p className="text-sm font-semibold text-slate-500">{sense.meaning}</p>{[["synonyms", "Đồng nghĩa", "bg-emerald-50 text-emerald-800"], ["antonyms", "Trái nghĩa", "bg-amber-50 text-amber-800"]].map(([field, label, tone]) => sense[field]?.length ? <div key={field}><p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-400">{label}</p><div className="flex flex-wrap gap-2">{sense[field].map((phrase, i) => <span key={i} className={`rounded-lg px-3 py-2 text-sm ${tone}`}>{phrase}</span>)}</div></div> : null)}</section>)}
      {!related.length && !word.family?.length ? <p className="text-slate-500">Từ này chưa có họ từ, đồng nghĩa hoặc trái nghĩa.</p> : null}
    </div>;
  }
  return <div className="space-y-5">{senses.map((sense, index) => <section key={index} className="space-y-3">
    <div className="flex flex-wrap items-center gap-2"><span className="flex h-6 w-6 items-center justify-center rounded-full bg-slate-100 text-xs font-semibold text-slate-400">{index + 1}</span><PartOfSpeechBadge value={sense.partOfSpeech} /></div>
    <p className="text-2xl font-semibold leading-snug text-slate-900 sm:text-3xl">{sense.meaning}</p>
  </section>)}<p className="pt-2 text-xs text-slate-400">Chuyển sang Cách dùng để xem ví dụ và collocation.</p></div>;
}
