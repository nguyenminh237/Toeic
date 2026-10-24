import { useId, useState } from "react";
import { RotateCw } from "lucide-react";
import { PartOfSpeechBadge } from "./PartOfSpeechBadge";
import { SpeakButton } from "./SpeakButton";
import { VocabularyTabs } from "./VocabularyTabs";
import { VocabularyLearningDetails } from "./VocabularyLearningDetails";

const tabs = [{ id: "meaning", label: "Nghĩa" }, { id: "usage", label: "Cách dùng" }, { id: "related", label: "Liên hệ từ" }];
export function VocabularyFlashcard({ word }) {
  const id = useId();
  const [flipped, setFlipped] = useState(false);
  const [section, setSection] = useState("meaning");
  function flip() { setFlipped(value => !value); }
  return <article className="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-[0_16px_60px_-32px_#64748b]">
    <div className="flex items-center justify-between gap-3 border-b border-slate-100 px-5 py-4 sm:px-8">
      <div className="flex min-w-0 items-center gap-2"><span className="text-xs font-semibold uppercase tracking-wider text-slate-400">{flipped ? "Mặt sau" : "Mặt trước"}</span>{flipped ? <span className="truncate text-sm font-semibold text-slate-700">{word.word}</span> : null}</div>
      <SpeakButton text={word.word} label={`Phát âm ${word.word}`} />
    </div>
    <div className="relative">
    <div className={`flex min-h-[340px] flex-col justify-center px-6 py-10 transition sm:min-h-[390px] sm:px-10 ${flipped ? "bg-white" : "bg-gradient-to-br from-blue-50/80 via-white to-indigo-50/70 text-center"}`}>
      {tabs.map(tab => <div key={tab.id} role="tabpanel" id={`${id}-${tab.id}-panel`} aria-labelledby={`${id}-${tab.id}-tab`} hidden={!flipped || section !== tab.id} className="min-w-0 break-words"><VocabularyLearningDetails word={word} section={tab.id} /></div>)}
      {!flipped ? <div>
        <p className="mb-6 text-xs font-semibold uppercase tracking-[0.2em] text-blue-400">Nhớ nghĩa của từ này?</p>
        <h2 className="break-words text-4xl font-bold tracking-tight text-slate-900 sm:text-6xl">{word.word}</h2>
        {word.pronunciation ? <p className="mt-4 text-lg text-slate-400">{word.pronunciation}</p> : null}
        <div className="mt-4"><PartOfSpeechBadge value={word.partOfSpeech} /></div>
        <p className="mt-10 flex items-center justify-center gap-2 text-sm text-slate-400"><RotateCw className="h-4 w-4" />Nhấp vào thẻ để xem nghĩa</p>
      </div> : null}
    </div>
    <button type="button" aria-label={`Lật thẻ ${word.word}: ${flipped ? "xem từ tiếng Anh" : "xem nghĩa"}`} aria-pressed={flipped} onClick={flip} className="absolute inset-0 h-full w-full cursor-pointer bg-transparent outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary" />
    </div>
    <div className="space-y-3 border-t border-slate-100 px-4 py-4 sm:px-8">
      <VocabularyTabs id={id} label="Nội dung thẻ" tabs={tabs} value={section} onChange={value => { setSection(value); setFlipped(true); }} />
      <p className="text-center text-xs text-slate-400">Nhấp thẻ để lật · Tab để chuyển mục · Enter hoặc Space cũng lật thẻ</p>
    </div>
  </article>;
}
