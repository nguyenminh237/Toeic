import { test } from "node:test";
import assert from "node:assert/strict";
import { parseQuestions } from "./parseQuestions.js";

const sample = "Câu 1: The team responded _____.\nA. quick\nB. quickly\nC. quickness\nD. quicker\nĐáp án: B";
test("imports multiple questions with correct answers and CRLF", () => {
  const rows = parseQuestions(`${sample}\n\n${sample.replace("Câu 1", "Câu 2")}`.replaceAll("\n", "\r\n"), 6);
  assert.equal(rows.length, 2); assert.equal(rows[0].correctAnswer, "B");
  assert.equal(rows[1].choices.D, "quicker");
});
test("preserves multiline stems and options", () => {
  const rows = parseQuestions(sample.replace("A. quick", "Second line\nA. quick\ncontinued"), 5);
  assert.match(rows[0].questionText, /Second line/);
  assert.equal(rows[0].choices.A, "quick\ncontinued");
});
test("supports Part 2 three options and English answer labels", () => {
  const text = "Question 1: Why?\nA) Because\nB) Tomorrow\nC) In office\nAnswer: A";
  assert.equal(parseQuestions(text, 2)[0].correctAnswer, "A");
  assert.throws(() => parseQuestions(text, 5));
});
test("rejects missing keys, duplicate labels and invalid Part 2 choices", () => {
  assert.throws(() => parseQuestions(sample.replace("Đáp án: B", ""), 5));
  assert.throws(() => parseQuestions(sample.replace("B. quickly", "A. quickly"), 5));
  assert.throws(() => parseQuestions(sample, 2));
  assert.throws(() => parseQuestions("", 5));
});
