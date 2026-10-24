import { PrivateImage } from "../../components/common/PrivateMedia";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Badge } from "../../components/common/Badge";
import { Button } from "../../components/common/Button";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { AudioPlayer } from "../../components/practice/AudioPlayer";
import { OptionList } from "../../components/practice/OptionList";
import { QuestionDocuments } from "../../components/practice/QuestionDocuments";
import { path } from "../../constants/routes";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { testService } from "../../services/testService";
import { formatDuration } from "../../utils/format";

export function TestExamPage() {
  const { partId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const query = useAsync(() => testService.start(partId, user.id), [partId, user.id]);
  const [secondsLeft, setSecondsLeft] = useState(null);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const submitLock = useRef(false);
  const autoSubmitted = useRef(null);
  const saveQueue = useRef(Promise.resolve());
  const attempt = query.data;
  const answers = attempt?.selectedAnswers || {};
  const questions = attempt?.questions || [];

  useEffect(() => {
    if (!attempt?.expiresAt) return;
    const tick = () => setSecondsLeft(Math.max(0, Math.ceil((Date.parse(attempt.expiresAt) - Date.now()) / 1000)));
    tick();
    const timer = setInterval(tick, 500);
    return () => clearInterval(timer);
  }, [attempt?.expiresAt]);

  const submitExam = useCallback(async (automatic = false) => {
    if (!attempt || submitLock.current) return;
    const missing = attempt.questions.filter((q) => !attempt.selectedAnswers?.[q.id]).length;
    if (!automatic && missing && !window.confirm(`Còn ${missing} câu chưa chọn. Nộp bài luôn?`)) return;
    submitLock.current = true;
    setSubmitting(true);
    try {
      await saveQueue.current.catch(() => {});
      const result = await testService.submit(user.id, {
        resultId: attempt.id,
        answers: attempt.questions.map((q) => ({ questionId: q.id, selectedAnswer: attempt.selectedAnswers?.[q.id] || null })),
      });
      navigate(path.testResultDetail(result.id), { replace: true });
    } catch (err) {
      setError(err.message);
      submitLock.current = false;
      setSubmitting(false);
    }
  }, [attempt, navigate, user.id]);

  useEffect(() => {
    if (secondsLeft === 0 && attempt && !submitting && autoSubmitted.current !== attempt.id) {
      autoSubmitted.current = attempt.id;
      submitExam(true);
    }
  }, [secondsLeft, attempt, submitExam, submitting]);

  function select(questionId, selectedAnswer) {
    query.setData((current) => ({ ...current, selectedAnswers: { ...current.selectedAnswers, [questionId]: selectedAnswer } }));
    setError("");
    saveQueue.current = saveQueue.current.catch(() => {}).then(async () => {
      const result = await testService.saveDraft(attempt.id, [{ questionId, selectedAnswer }]);
      if (result.status !== "IN_PROGRESS") navigate(path.testResultDetail(result.id), { replace: true });
    });
    saveQueue.current.catch((err) => setError(`Chưa lưu được lựa chọn: ${err.message}. Lựa chọn sẽ được gửi lại khi nộp bài.`));
  }

  if (query.loading) return <Spinner label="Đang khôi phục bài thi..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;
  const answeredCount = questions.filter((q) => answers[q.id]).length;
  return <div>
    <PageHeader eyebrow={`Part ${attempt.partNumber}`} title={attempt.suiteName}
      description={`${questions.length} câu · Tiến độ được lưu theo lượt làm bài.`}
      actions={<><Badge tone="amber">{formatDuration(secondsLeft)}</Badge><Button loading={submitting} onClick={() => submitExam()}>Nộp bài</Button></>} />
    {error ? <div className="mb-4"><Alert tone="error">{error}</Alert></div> : null}
    <div className="sticky top-16 z-10 mb-6 space-y-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      {attempt.audioUrl ? <AudioPlayer src={attempt.audioUrl} /> : null}
      <p className="text-sm text-slate-500">Đã chọn {answeredCount}/{questions.length} câu</p>
      <div className="flex flex-wrap gap-2">{questions.map((q) => <button key={q.id} type="button"
        className={`rounded-lg px-3 py-2 text-sm ${answers[q.id] ? "bg-primary text-white" : "bg-slate-100"}`}
        onClick={() => document.getElementById(`question-${q.id}`)?.scrollIntoView({ behavior: "smooth", block: "center" })}>{q.questionNumber}</button>)}</div>
    </div>
    <div className="space-y-6">{questions.map((q, index) => {
      const newGroup = index === 0 || questions[index - 1].group?.id !== q.group?.id;
      return <article id={`question-${q.id}`} key={q.id} className="rounded-2xl border border-slate-200 bg-white p-6">
        {newGroup ? <><QuestionDocuments group={q.group} />
          {!attempt.audioUrl && q.audioUrl ? <AudioPlayer src={q.audioUrl} startMs={q.audioStartMs} endMs={q.audioEndMs} /> : null}
          {q.imageUrl ? <PrivateImage src={q.imageUrl} alt="Ngữ liệu câu hỏi" className="mb-4 max-h-80 w-full object-contain" /> : null}</> : null}
        <p className="mb-3 font-semibold">Câu {q.questionNumber}</p>
        {q.questionText && attempt.partNumber > 2 ? <p className="mb-4 whitespace-pre-wrap">{q.questionText}</p> : null}
        <OptionList question={q} selected={answers[q.id]} revealed={false} hideTextUntilAnswered={attempt.partNumber <= 2}
          hiddenLabel="" disabled={secondsLeft === 0 || submitting} onSelect={(key) => select(q.id, key)} />
      </article>;
    })}</div>
    <Button className="mt-6" loading={submitting} onClick={() => submitExam()}>Kết thúc · Nộp bài</Button>
  </div>;
}
