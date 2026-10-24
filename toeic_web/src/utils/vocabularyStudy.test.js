import test from "node:test";
import assert from "node:assert/strict";
import { createStudySession, recordStudyAnswer, advanceStudySession, learningQuestion } from "./vocabularyStudy.js";

const words = [{ id: 1, word: "invoice", meaning: "hóa đơn" }, { id: 2, word: "deadline", meaning: "hạn chót" }];
const stable = () => 0.999;
test("wrong answers return later and the round ends only after every word is recalled", () => {
  let session = createStudySession(words, stable);
  session = advanceStudySession(recordStudyAnswer(session, false));
  assert.deepEqual(session.queue, [2, 1]); assert.deepEqual(session.mastered, []);
  session = advanceStudySession(recordStudyAnswer(session, true));
  assert.deepEqual(session.queue, [1]); assert.deepEqual(session.mastered, [2]);
  session = advanceStudySession(recordStudyAnswer(session, true));
  assert.deepEqual(session.queue, []); assert.equal(session.answered, 3); assert.equal(session.correct, 2);
  assert.deepEqual(session.missed, [1]); assert.equal(session.mastered.length, 2);
});
test("double clicks and next-before-answer cannot inflate progress", () => {
  const initial = createStudySession(words, stable);
  assert.equal(advanceStudySession(initial), initial);
  const answered = recordStudyAnswer(initial, true);
  assert.equal(recordStudyAnswer(answered, false), answered);
  assert.equal(answered.answered, 1);
});
test("choices exclude alternate correct meanings and duplicate distractors", () => {
  const word = { id: 1, word: "charge", senses: [{ meaning: "tính phí" }, { meaning: "phụ trách" }] };
  const pool = [word, { id: 2, meaning: "phụ trách" }, { id: 3, meaning: "Hóa đơn" }, { id: 4, meaning: "hóa đơn." }, { id: 5, meaning: "hạn chót" }];
  const question = learningQuestion(word, pool, stable);
  assert.equal(question.choices.length, 3); assert.ok(question.choices.includes("tính phí"));
  assert.ok(!question.choices.includes("phụ trách")); assert.ok(!question.choices.includes("hóa đơn."));
});
test("single words or identical meanings use self recall rather than invented choices", () => {
  assert.deepEqual(learningQuestion(words[0], [words[0]], stable).choices, []);
  assert.deepEqual(learningQuestion(words[0], [words[0], { id: 2, meaning: "HÓA ĐƠN" }], stable).choices, []);
  assert.equal(createStudySession([], stable).total, 0);
});
