import { editableWord } from "./vocabulary.js";

export function shuffleWords(items, random = Math.random) {
  const result = [...items];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}

const normalize = value => String(value || "").trim().toLowerCase().replace(/\s+/g, " ").replace(/[.!?]+$/, "");
export function primarySense(word) {
  return editableWord(word).senses[0];
}

export function learningQuestion(word, words, random = Math.random) {
  const sense = primarySense(word);
  const validMeanings = new Set(editableWord(word).senses.map(s => normalize(s.meaning)));
  const seen = new Set(validMeanings);
  const distractors = shuffleWords(words.filter(w => w.id !== word.id), random).flatMap(w => {
    const meaning = primarySense(w).meaning;
    const key = normalize(meaning);
    if (!key || seen.has(key)) return [];
    seen.add(key);
    return [meaning];
  }).slice(0, 3);
  return { sense, choices: distractors.length ? shuffleWords([sense.meaning, ...distractors], random) : [] };
}

export function createStudySession(words, random = Math.random) {
  const queue = shuffleWords(words.filter(w => primarySense(w).meaning?.trim()).map(w => w.id), random);
  return { queue, total: queue.length, mastered: [], missed: [], answered: 0, correct: 0, feedback: null };
}

export function recordStudyAnswer(session, correct, selected = null) {
  if (!session.queue.length || session.feedback) return session;
  return {
    ...session,
    answered: session.answered + 1,
    correct: session.correct + Number(correct),
    missed: correct ? session.missed : [...new Set([...session.missed, session.queue[0]])],
    feedback: { correct, selected },
  };
}

export function advanceStudySession(session) {
  if (!session.feedback || !session.queue.length) return session;
  const [current, ...rest] = session.queue;
  return {
    ...session,
    queue: session.feedback.correct ? rest : [...rest, current],
    mastered: session.feedback.correct ? [...new Set([...session.mastered, current])] : session.mastered,
    feedback: null,
  };
}
