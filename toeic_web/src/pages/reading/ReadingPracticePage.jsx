import { useState } from "react";
import { useParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { EmptyState } from "../../components/common/EmptyState";
import { ErrorState } from "../../components/common/ErrorState";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { Languages, Lightbulb, ListChecks } from "lucide-react";
import { HintButton, HintPanel } from "../../components/practice/HintPanel";
import { OptionList } from "../../components/practice/OptionList";
import { PracticeActions } from "../../components/practice/PracticeActions";
import { QuestionPager } from "../../components/practice/QuestionPager";
import { QuestionDocuments } from "../../components/practice/QuestionDocuments";
import { QUESTION_STATUS } from "../../constants/status";
import { useAsync } from "../../hooks/useAsync";
import { useAuth } from "../../hooks/useAuth";
import { readingService } from "../../services/readingService";
import { optionText } from "../../utils/format";
import { translateOptions, translateQuestion } from "../../utils/translate";

export function ReadingPracticePage() {
  const { partNumber, sectionId } = useParams();
  const { user } = useAuth();
  const [index, setIndex] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [localError, setLocalError] = useState("");
  const [showExplanation, setShowExplanation] = useState(false);
  const [showQuestionMeaning, setShowQuestionMeaning] = useState(false);
  const [showOptionMeaning, setShowOptionMeaning] = useState(false);
  const [questionMeaning, setQuestionMeaning] = useState("");
  const [optionMeaning, setOptionMeaning] = useState("");
  const [questionHintError, setQuestionHintError] = useState("");
  const [optionHintError, setOptionHintError] = useState("");
  const [translatingQuestion, setTranslatingQuestion] = useState(false);
  const [translatingOptions, setTranslatingOptions] = useState(false);

  const query = useAsync(async () => {
    const [questions, progress, part, sections] = await Promise.all([
      readingService.getQuestionsBySection(sectionId),
      readingService.getProgress(user.id),
      readingService.getPart(partNumber).catch(() => null),
      readingService.getSections(partNumber),
    ]);
    const progressMap = Object.fromEntries((progress || []).map((item) => [String(item.questionId), item]));
    const passageMap = {};
    const section = sections.find((item) => String(item.id) === String(sectionId));
    if ((questions || []).some((q) => q.partNumber !== Number(partNumber))) throw new Error("Bộ luyện không thuộc Part này");
    return { questions: questions || [], progressMap, part, passageMap, section };
  }, [sectionId, partNumber, user.id]);

  const questions = query.data?.questions ?? [];
  const progressMap = query.data?.progressMap ?? {};
  const baseQuestion = questions[index];
  const progress = baseQuestion ? progressMap[String(baseQuestion.id)] : null;
  const question = baseQuestion ? { ...baseQuestion, correctAnswer: progress?.correctAnswer, explanation: progress?.explanation } : null;
  const showPassagePane = Number(partNumber) >= 6;
  const revealed = progress?.status === QUESTION_STATUS.CORRECT || progress?.status === QUESTION_STATUS.WRONG;
  const hasQuestionText = Boolean(question?.questionText?.trim());
  const hasOptions = Boolean(question && ["A", "B", "C", "D"].some((key) => optionText(question, key).trim()));

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
      applyProgress(await readingService.answer(user.id, question.id, selectedAnswer));
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
      applyProgress(await readingService.updateStatus(user.id, question.id, QUESTION_STATUS.UNANSWERED));
      setShowExplanation(false);
      setShowQuestionMeaning(false);
      setShowOptionMeaning(false);
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
      applyProgress(await readingService.updateStatus(user.id, question.id, undefined, !progress?.bookmarked));
    } catch (err) {
      setLocalError(err.message || "Không đánh dấu được câu");
    } finally {
      setSubmitting(false);
    }
  }

  function resetHints() {
    setShowExplanation(false);
    setShowQuestionMeaning(false);
    setShowOptionMeaning(false);
    setQuestionMeaning("");
    setOptionMeaning("");
    setQuestionHintError("");
    setOptionHintError("");
  }

  async function toggleQuestionMeaning() {
    if (showQuestionMeaning) {
      setShowQuestionMeaning(false);
      return;
    }
    if (questionMeaning) {
      setShowQuestionMeaning(true);
      return;
    }
    setTranslatingQuestion(true);
    setQuestionHintError("");
    try {
      setQuestionMeaning(await translateQuestion(question));
      setShowQuestionMeaning(true);
    } catch (err) {
      setQuestionHintError(err.message || "Không dịch được câu hỏi");
      setShowQuestionMeaning(true);
    } finally {
      setTranslatingQuestion(false);
    }
  }

  async function toggleOptionMeaning() {
    if (showOptionMeaning) {
      setShowOptionMeaning(false);
      return;
    }
    if (optionMeaning) {
      setShowOptionMeaning(true);
      return;
    }
    setTranslatingOptions(true);
    setOptionHintError("");
    try {
      setOptionMeaning(await translateOptions(question));
      setShowOptionMeaning(true);
    } catch (err) {
      setOptionHintError(err.message || "Không dịch được đáp án");
      setShowOptionMeaning(true);
    } finally {
      setTranslatingOptions(false);
    }
  }

  if (query.loading) return <Spinner label="Đang tải câu hỏi đọc..." />;
  if (query.error) return <ErrorState error={query.error} onRetry={query.reload} />;
  if (!questions.length) {
    return <EmptyState title="Mục luyện tập này chưa có câu hỏi" description="Nội dung của bộ luyện này đang được cập nhật." />;
  }

  return (
    <div>
      <PageHeader
        eyebrow={`Part ${partNumber}`}
        title={query.data.section?.name || query.data.part?.description || `Luyện đọc Part ${partNumber}`}
        description="Chọn đáp án để lưu tiến độ. Đúng/sai hiện ngay sau khi chọn."
      />
      {localError ? <div className="mb-4"><Alert tone="error">{localError}</Alert></div> : null}
      <div className={`grid gap-6 ${showPassagePane ? "lg:grid-cols-2" : ""}`}>
        {showPassagePane ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
            <h3 className="mb-3 font-semibold">Đoạn đọc</h3>
            <QuestionDocuments group={question.group} />
            {question.questionText ? <p className="mt-4 leading-7 text-slate-700">{question.questionText}</p> : null}
          </div>
        ) : null}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-[var(--shadow-card)]">
          {!showPassagePane && question.questionText ? (
            <p className="mb-4 text-lg font-medium">{question.questionText}</p>
          ) : (
            <p className="mb-4 text-sm font-semibold text-slate-500">
              Câu {question.questionNumber || index + 1}
            </p>
          )}
          <OptionList
            question={question}
            selected={revealed || progress?.bookmarked ? progress?.selectedAnswer : undefined}
            revealed={revealed}
            disabled={submitting}
            onSelect={handleSelect}
          />
          <PracticeActions
            status={progress?.status}
            bookmarked={progress?.bookmarked}
            disabled={submitting}
            onReset={handleReset}
            onToggleMark={handleToggleMark}
          />
          <div className="mt-4 flex flex-wrap gap-2">
            {question.explanation ? (
              <HintButton icon={Lightbulb} tone="amber" active={showExplanation} onClick={() => setShowExplanation((current) => !current)}>
                {showExplanation ? "Ẩn giải thích" : "Xem giải thích"}
              </HintButton>
            ) : null}
            {hasQuestionText ? (
              <HintButton icon={Languages} tone="blue" active={showQuestionMeaning} loading={translatingQuestion} onClick={toggleQuestionMeaning}>
                {showQuestionMeaning ? "Ẩn dịch câu hỏi" : "Dịch nghĩa câu hỏi"}
              </HintButton>
            ) : null}
            {hasOptions ? (
              <HintButton icon={ListChecks} tone="green" active={showOptionMeaning} loading={translatingOptions} onClick={toggleOptionMeaning}>
                {showOptionMeaning ? "Ẩn dịch đáp án" : "Dịch nghĩa đáp án"}
              </HintButton>
            ) : null}
          </div>
          {showExplanation && question.explanation ? (
            <HintPanel open tone="amber"><div className="whitespace-pre-wrap">{question.explanation}</div></HintPanel>
          ) : null}
          <HintPanel open={showQuestionMeaning} loading={translatingQuestion} error={questionHintError} tone="blue">
            {questionMeaning}
          </HintPanel>
          <HintPanel open={showOptionMeaning} loading={translatingOptions} error={optionHintError} tone="green">
            {optionMeaning}
          </HintPanel>
        </div>
      </div>
      <QuestionPager
        index={index}
        total={questions.length}
        statuses={statuses}
        bookmarks={questions.map((q) => Boolean(progressMap[String(q.id)]?.bookmarked))}
        onPrev={() => {
          resetHints();
          setIndex((value) => Math.max(0, value - 1));
        }}
        onNext={() => {
          resetHints();
          setIndex((value) => Math.min(questions.length - 1, value + 1));
        }}
        onJump={(value) => {
          resetHints();
          setIndex(value);
        }}
      />
    </div>
  );
}
