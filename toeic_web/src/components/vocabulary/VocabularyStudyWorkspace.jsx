import { useId, useState } from "react";
import { ArrowLeft, ArrowRight, BookOpen, Layers, List, Search, Shuffle } from "lucide-react";
import { Button } from "../common/Button";
import { PartOfSpeechBadge } from "./PartOfSpeechBadge";
import { VocabularyTabs } from "./VocabularyTabs";
import { VocabularyFlashcard } from "./VocabularyFlashcard";
import { VocabularyLearningMode } from "./VocabularyLearningMode";
import { VocabularyProgress } from "./VocabularyProgress";
import { shuffleWords, primarySense } from "../../utils/vocabularyStudy";
import { editableWord } from "../../utils/vocabulary";

const tabs = [{ id: "cards", label: "Flashcard", icon: Layers }, { id: "learn", label: "Luyện nhớ", icon: BookOpen }, { id: "list", label: "Danh sách", icon: List }];
export function VocabularyStudyWorkspace({ words }) {
  const id = useId();
  const [mode, setMode] = useState("cards");
  const [deck, setDeck] = useState(words);
  const [index, setIndex] = useState(0);
  const [shuffled, setShuffled] = useState(false);
  const [search, setSearch] = useState("");
  const word = deck[index];
  const filtered = words.filter(w => `${w.word} ${editableWord(w).senses.map(s => s.meaning).join(" ")}`.toLowerCase().includes(search.trim().toLowerCase()));
  function toggleShuffle() { setDeck(shuffled ? words : shuffleWords(words)); setShuffled(value => !value); setIndex(0); }
  function openWord(selected) { setIndex(deck.findIndex(w => w.id === selected.id)); setMode("cards"); }
  return <div className="mx-auto max-w-4xl space-y-6">
    <VocabularyTabs id={id} label="Chế độ học từ vựng" tabs={tabs} value={mode} onChange={setMode} />
    <section role="tabpanel" id={`${id}-cards-panel`} aria-labelledby={`${id}-cards-tab`} hidden={mode !== "cards"}>
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3"><p className="text-sm font-medium text-slate-500" aria-live="polite">Thẻ <span className="font-bold text-slate-900">{index + 1}</span> / {deck.length}</p><Button variant="ghost" aria-pressed={shuffled} onClick={toggleShuffle}><Shuffle className="h-4 w-4" />{shuffled ? "Thứ tự gốc" : "Trộn thẻ"}</Button></div>
      <VocabularyFlashcard key={word.id} word={word} />
      <div className="mt-5 flex items-center justify-between gap-3"><Button variant="secondary" disabled={index === 0} onClick={() => setIndex(value => value - 1)} aria-label="Thẻ trước"><ArrowLeft className="h-4 w-4" /><span className="hidden sm:inline">Thẻ trước</span></Button><div className="min-w-0 flex-1 px-3"><VocabularyProgress value={index + 1} total={deck.length} label="Vị trí trong bộ thẻ" /></div><Button variant="secondary" disabled={index === deck.length - 1} onClick={() => setIndex(value => value + 1)} aria-label="Thẻ tiếp theo"><span className="hidden sm:inline">Thẻ tiếp</span><ArrowRight className="h-4 w-4" /></Button></div>
    </section>
    <section role="tabpanel" id={`${id}-learn-panel`} aria-labelledby={`${id}-learn-tab`} hidden={mode !== "learn"}><VocabularyLearningMode words={words} /></section>
    <section role="tabpanel" id={`${id}-list-panel`} aria-labelledby={`${id}-list-tab`} hidden={mode !== "list"} className="space-y-4">
      <label className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3"><Search className="h-5 w-5 shrink-0 text-slate-400" /><input type="search" aria-label="Tìm từ hoặc nghĩa" placeholder="Tìm từ hoặc nghĩa…" value={search} onChange={event => setSearch(event.target.value)} className="min-w-0 w-full bg-transparent py-1 text-sm outline-none" /></label>
      <p className="text-xs text-slate-400">{filtered.length} từ · Chọn một từ để mở flashcard</p>
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">{filtered.length ? filtered.map((item, i) => <button type="button" key={item.id} onClick={() => openWord(item)} className="flex w-full items-center gap-4 border-b border-slate-100 p-4 text-left last:border-0 hover:bg-blue-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary sm:p-5"><span className="w-6 shrink-0 text-xs text-slate-300">{i + 1}</span><div className="min-w-0 flex-1"><div className="flex flex-wrap items-center gap-2"><span className="break-words font-semibold text-slate-900">{item.word}</span><PartOfSpeechBadge value={item.partOfSpeech} /></div><p className="mt-1 break-words text-sm text-slate-500">{primarySense(item).meaning}</p></div><ArrowRight className="h-4 w-4 shrink-0 text-slate-300" /></button>) : <p className="p-8 text-center text-sm text-slate-500">Không tìm thấy từ phù hợp.</p>}</div>
    </section>
  </div>;
}
