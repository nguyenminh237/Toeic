import { QUESTION_STATUS } from "../constants/status";

export function summarizeSectionProgress(questions = [], progressList = []) {
  const map = Object.fromEntries((progressList || []).map((item) => [String(item.questionId), item]));
  let correct = 0;
  let wrong = 0;
  let unanswered = 0;

  for (const question of questions) {
    const status = map[String(question.id)]?.status;
    if (status === QUESTION_STATUS.CORRECT) correct += 1;
    else if (status === QUESTION_STATUS.WRONG) wrong += 1;
    else unanswered += 1;
  }

  return {
    total: questions.length,
    correct,
    wrong,
    unanswered,
  };
}
