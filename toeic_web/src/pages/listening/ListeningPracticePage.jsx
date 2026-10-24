import { PrivateImage } from "../../components/common/PrivateMedia";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { AudioPlayer } from "../../components/practice/AudioPlayer";
import { OptionList } from "../../components/practice/OptionList";
import { PracticeActions } from "../../components/practice/PracticeActions";
import { QuestionPager } from "../../components/practice/QuestionPager";
import { QUESTION_STATUS } from "../../constants/status";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { listeningService } from "../../services/listeningService";

export function ListeningPracticePage() {
  const { partNumber, sectionId } = useParams();
  const { user } = useAuth();
  const [index, setIndex] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [localError, setLocalError] = useState("");

  const query = useAsync(async () => {
    const [questions, progress, part, sections] = await Promise.all([
      listeningService.getQuestionsBySection(sectionId),
      listeningService.getProgress(user.id),
      listeningService.getPart(partNumber).catch(() => null),
      listeningService.getSections(partNumber).catch(() => []),
    ]);
    const progressMap = Object.fromEntries((progress || []).map((item) => [String(item.questionId), item]));
    const section = (sections || []).find((item) => String(item.id) === String(sectionId)) || null;
    if ((questions || []).some((q) => q.partNumber !== Number(partNumber))) throw new Error("Bộ luyện không thuộc Part này");
    return { questions: questions || [], progressMap, part, section };
  }, [sectionId, partNumber, user.id]);

  const questions = query.data?.questions ?? [];
  const progressMap = query.data?.progressMap ?? {};
  const baseQuestion = questions[index];
  const progress = baseQuestion ? progressMap[String(baseQuestion.id)] : null;
  const question = baseQuestion ? { ...baseQuestion, correctAnswer: progress?.correctAnswer, explanation: progress?.explanation } : null;
  const hideQuestion = question?.hideQuestion ?? Number(partNumber) === 2;
  const hideOptions = Number(partNumber) === 1 || Boolean(hideQuestion);
  const revealed = progress?.status === QUESTION_STATUS.CORRECT || progress?.status === QUESTION_STATUS.WRONG;
  const showQuestionText = Boolean(question?.questionText) && (!hideQuestion || revealed);
  const audioSrc = question?.audioUrl || query.data?.section?.audioUrl;

  const statuses = questions.map((item) => progressMap[String(item.id)]?.status || QUESTION_STATUS.UNANSWERED);

  function applyProgress(result) {
    query.setData((current) => ({
      ...current,
      progressMap: { ...current.progressMap, [String(question.id)]: result },
    }));
  }

  async function handleSelect(selectedAnswer) {
    if (!question || submitting) return;
    setSubmitting(true);
    setLocalError("");
    try {
      applyProgress(await listeningService.answer(user.id, question.id, selectedAnswer));
    } catch (err) {
      setLocalError(err.message || "Không lưu được đáp án");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleReset() {
    if (!question || submitting) return;
    setSubmitting(true);
    setLocalError("");
    try {
      applyProgress(await listeningService.updateStatus(user.id, question.id, QUESTION_STATUS.UNANSWERED));
    } catch (err) {
      setLocalError(err.message || "Không xóa được vết làm");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleToggleMark() {
    if (!question || submitting) return;
    setSubmitting(true);
    setLocalError("");
    try {
      applyProgress(await listeningService.updateStatus(user.id, question.id, undefined, !progress?.bookmarked));
    } catch (err) {
      setLocalError(err.message || "Không đánh dấu được câu");
    } finally {
      setSubmitting(false);
    }
  }

  if (query.loading) return <Spinner label="Đang tải câu hỏi nghe..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;
  if (!questions.length) {
    return <EmptyState title="Mục luyện tập này chưa có câu hỏi" description="Nội dung của bộ luyện này đang được cập nhật." />;
  }

  return (
    <div>
      <PageHeader
        eyebrow={`Part ${partNumber}`}
        title={query.data.section?.name || query.data.part?.description || `Luyện nghe Part ${partNumber}`}
        description="Luyện từng câu, nghe lại thoải mái. Câu hỏi bị ẩn sẽ hiện sau khi bạn chọn đáp án."
      />
      {localError ? <div className="mb-4"><Alert tone="error">{localError}</Alert></div> : null}
      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
          {question.imageUrl ? (
            <PrivateImage
              src={question.imageUrl}
              alt="Listening"
              className="mb-4 max-h-80 w-full rounded-xl object-contain bg-slate-50"
            />
          ) : null}
          <AudioPlayer src={audioSrc} startMs={question.audioStartMs} endMs={question.audioEndMs} />
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
          <p className="text-sm font-semibold text-slate-500">Câu {question.questionNumber || index + 1}</p>
          {question.promptText ? (
            <p className="mt-2 text-sm italic text-slate-500">{question.promptText}</p>
          ) : null}
          {showQuestionText ? (
            <p className="mt-2 text-lg font-medium text-slate-900">{question.questionText}</p>
          ) : hideQuestion ? (
            <p className="mt-2 text-sm text-slate-400">Câu hỏi nằm trong audio. Chọn đáp án rồi sẽ hiện nội dung.</p>
          ) : null}
          <div className="mt-4">
            <OptionList
              question={question}
              selected={revealed || progress?.bookmarked ? progress?.selectedAnswer : undefined}
              revealed={revealed}
              hideTextUntilAnswered={hideOptions}
              disabled={submitting}
              onSelect={handleSelect}
            />
          </div>
          <PracticeActions
            status={progress?.status}
            bookmarked={progress?.bookmarked}
            disabled={submitting}
            onReset={handleReset}
            onToggleMark={handleToggleMark}
          />
          {revealed && question.explanation ? (
            <p className="mt-4 whitespace-pre-wrap text-sm text-slate-500">{question.explanation}</p>
          ) : null}
        </div>
      </div>
      <QuestionPager
        index={index}
        total={questions.length}
        statuses={statuses}
        bookmarks={questions.map((q) => Boolean(progressMap[String(q.id)]?.bookmarked))}
        onPrev={() => setIndex((value) => Math.max(0, value - 1))}
        onNext={() => setIndex((value) => Math.min(questions.length - 1, value + 1))}
        onJump={setIndex}
      />
    </div>
  );
}
