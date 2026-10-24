import { useMemo, useState } from "react";
import { ArrowRight, Check, RotateCcw, Trophy, X } from "lucide-react";
import { Button } from "../common/Button";
import { EmptyState } from "../common/EmptyState";
import { SpeakButton } from "./SpeakButton";
import { PartOfSpeechBadge } from "./PartOfSpeechBadge";
import { VocabularyProgress } from "./VocabularyProgress";
import { createStudySession, learningQuestion, recordStudyAnswer, advanceStudySession } from "../../utils/vocabularyStudy";

export function VocabularyLearningMode({ words }) {
  const [session, setSession] = useState(() => createStudySession(words));
  const [revealed, setRevealed] = useState(false);
  const word = words.find(w => w.id === session.queue[0]);
  const question = useMemo(() => word ? learningQuestion(word, words) : null, [word, words]);
  function restart(pool = words) {
    if (session.queue.length && session.answered && !window.confirm("Bắt đầu lại lượt học này?")) return;
    setSession(createStudySession(pool)); setRevealed(false);
  }
  function next() { setSession(advanceStudySession); setRevealed(false); }
  if (!session.total) return <EmptyState title="Chưa có nghĩa để luyện nhớ" />;
  if (!word) return <div className="rounded-3xl border border-slate-200 bg-white px-5 py-12 text-center">
    <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600"><Trophy className="h-8 w-8" /></div>
    <h2 className="text-2xl font-bold text-slate-900">Hoàn thành lượt học!</h2><p className="mt-3 text-slate-500">Bạn đã nhớ đúng {session.total} từ trong lượt này.</p>
    <div className="mx-auto my-7 grid max-w-sm grid-cols-2 gap-3"><div className="rounded-2xl bg-slate-50 p-4"><p className="text-2xl font-bold text-slate-800">{session.answered}</p><p className="mt-1 text-xs text-slate-500">Lượt trả lời</p></div><div className="rounded-2xl bg-slate-50 p-4"><p className="text-2xl font-bold text-slate-800">{Math.round(session.correct / session.answered * 100)}%</p><p className="mt-1 text-xs text-slate-500">Trả lời đúng</p></div></div>
    <div className="flex flex-wrap justify-center gap-3"><Button onClick={() => restart()}>Học lại cả bộ</Button>{session.missed.length ? <Button variant="secondary" onClick={() => restart(words.filter(w => session.missed.includes(w.id)))}>Ôn {session.missed.length} từ từng nhầm</Button> : null}</div>
  </div>;
  return <div className="space-y-5">
    <div className="flex flex-wrap items-center justify-between gap-3"><div><h2 className="font-semibold text-slate-800">Nhìn từ, chọn nghĩa</h2><p className="mt-1 text-sm text-slate-500">Từ trả lời sai sẽ xuất hiện lại. Ưu tiên nhớ nghĩa chính trước.</p></div><Button variant="ghost" onClick={() => restart()}><RotateCcw className="h-4 w-4" />Bắt đầu lại</Button></div>
    <div><div className="mb-2 flex justify-between gap-3 text-xs font-medium text-slate-500"><span>Đã nhớ {session.mastered.length}/{session.total} từ</span><span>Còn {session.queue.length} từ trong lượt này</span></div><VocabularyProgress value={session.mastered.length} total={session.total} label="Tiến độ luyện nhớ" tone="green" /></div>
    <section className="space-y-7 rounded-3xl border border-slate-200 bg-white p-5 sm:p-8">
      <div className="space-y-3 text-center"><div className="flex flex-wrap items-center justify-center gap-3"><h3 className="break-words text-4xl font-bold text-slate-900">{word.word}</h3><SpeakButton text={word.word} label={`Phát âm ${word.word}`} /></div>{word.pronunciation ? <p className="text-slate-400">{word.pronunciation}</p> : null}<PartOfSpeechBadge value={question.sense.partOfSpeech} />{question.sense.exampleSentence ? <p className="mx-auto max-w-xl pt-3 text-base leading-7 text-slate-500">“{question.sense.exampleSentence}”</p> : null}</div>
      {question.choices.length ? <div className="grid gap-3 sm:grid-cols-2">{question.choices.map((choice, index) => {
        const correct = session.feedback && choice === question.sense.meaning;
        const wrong = session.feedback && choice === session.feedback.selected && !correct;
        return <button type="button" key={choice} disabled={Boolean(session.feedback)} onClick={() => setSession(value => recordStudyAnswer(value, choice === question.sense.meaning, choice))} className={`flex items-start gap-3 rounded-2xl border p-4 text-left text-sm transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 ${correct ? "border-emerald-300 bg-emerald-50 text-emerald-900" : wrong ? "border-rose-300 bg-rose-50 text-rose-900" : "border-slate-200 text-slate-700 enabled:hover:border-blue-300 enabled:hover:bg-blue-50"}`}><span className="font-semibold text-slate-400">{String.fromCharCode(65 + index)}</span><span className="min-w-0 flex-1 break-words">{choice}</span>{correct ? <Check className="h-4 w-4 shrink-0" /> : wrong ? <X className="h-4 w-4 shrink-0" /> : null}</button>;
      })}</div> : <div className="space-y-4 text-center"><p className="text-sm text-slate-500">Bộ từ chưa đủ nghĩa khác nhau để tạo lựa chọn. Hãy tự nhớ rồi kiểm tra đáp án.</p>{!revealed ? <Button variant="secondary" onClick={() => setRevealed(true)}>Xem nghĩa để tự kiểm tra</Button> : <><p className="text-2xl font-semibold text-slate-900">{question.sense.meaning}</p><div className="flex flex-wrap justify-center gap-3"><Button variant="secondary" disabled={Boolean(session.feedback)} onClick={() => setSession(value => recordStudyAnswer(value, false))}>Cần ôn lại</Button><Button disabled={Boolean(session.feedback)} onClick={() => setSession(value => recordStudyAnswer(value, true))}>Đã nhớ</Button></div></>}</div>}
      {session.feedback ? <div role="status" className={`space-y-3 rounded-2xl p-4 ${session.feedback.correct ? "bg-emerald-50 text-emerald-900" : "bg-amber-50 text-amber-900"}`}><p className="font-semibold">{session.feedback.correct ? "Đúng rồi!" : "Chưa đúng, mình sẽ ôn lại từ này."}</p><p>{question.sense.meaning}</p>{question.sense.exampleTranslation ? <p className="text-sm opacity-80">{question.sense.exampleTranslation}</p> : null}<Button onClick={next}>{session.queue.length === 1 && session.feedback.correct ? "Xem kết quả" : "Từ tiếp theo"}<ArrowRight className="h-4 w-4" /></Button></div> : null}
    </section>
  </div>;
}
