import test from "node:test";
import assert from "node:assert/strict";
import { editableWord, wordPayload, sensesForAim } from "../../utils/vocabulary.js";

test("legacy words retain their meaning and optional fields stay empty", () => {
  const draft = editableWord({ word: "invoice", meaning: "hóa đơn", partOfSpeech: "noun", synonyms: ["bill"] });
  const payload = wordPayload(draft, "2");
  assert.equal(payload.setId, 2); assert.equal(payload.senses[0].meaning, "hóa đơn");
  assert.deepEqual(payload.family, []); assert.deepEqual(payload.senses[0].antonyms, []);
  assert.deepEqual(payload.synonyms, ["bill"]);
});
test("aim filters individual senses and retains legacy unclassified meanings", () => {
  const word = { senses: [{ meaning: "base", minimumTargetScore: null }, { meaning: "common", minimumTargetScore: 450 }, { meaning: "advanced", minimumTargetScore: 850 }] };
  assert.deepEqual(sensesForAim(word, 650).map(s => s.meaning), ["base", "common"]);
  assert.equal(sensesForAim(word, "").length, 3);
});
test("save uses the reviewed first meaning and removes blank optional phrases", () => {
  const draft = editableWord({ word: " charge ", senses: [{ meaning: " tính phí ", partOfSpeech: "verb", minimumTargetScore: "450", collocations: ["charge a fee", ""] }] });
  const payload = wordPayload(draft, 4);
  assert.equal(payload.word, "charge"); assert.equal(payload.meaning, "tính phí");
  assert.equal(payload.senses[0].minimumTargetScore, 450); assert.deepEqual(payload.senses[0].collocations, ["charge a fee"]);
});
