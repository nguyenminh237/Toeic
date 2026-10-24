export const AIM_OPTIONS = [350, 450, 550, 650, 750, 850, 950];
export const blankSense = () => ({ meaning: "", partOfSpeech: "noun", minimumTargetScore: null, exampleSentence: "", exampleTranslation: "", studyNote: "", synonyms: [], antonyms: [], collocations: [] });
export function editableWord(word = {}) {
  return { ...word, word: word.word || "", pronunciation: word.pronunciation || "", family: word.family || [], senses: word.senses?.length ? word.senses.map(s => ({ ...blankSense(), ...s })) : [{ ...blankSense(), meaning: word.meaning || "", partOfSpeech: word.partOfSpeech || "noun", exampleSentence: word.exampleSentence || "", exampleTranslation: word.exampleTranslation || "", synonyms: Array.isArray(word.synonyms) ? word.synonyms : [] }] };
}
export function wordPayload(word, setId) {
  const senses = word.senses.map(s => ({ ...s, meaning: s.meaning.trim(), minimumTargetScore: s.minimumTargetScore === "" || s.minimumTargetScore == null ? null : Number(s.minimumTargetScore), synonyms: (s.synonyms || []).map(x => x.trim()).filter(Boolean), antonyms: (s.antonyms || []).map(x => x.trim()).filter(Boolean), collocations: (s.collocations || []).map(x => x.trim()).filter(Boolean) }));
  const first = senses[0];
  return { word: word.word.trim(), setId: Number(setId), pronunciation: word.pronunciation || null, meaning: first.meaning, partOfSpeech: first.partOfSpeech, exampleSentence: first.exampleSentence || null, exampleTranslation: first.exampleTranslation || null, synonyms: first.synonyms, senses, family: (word.family || []).filter(f => f.word?.trim()).map(f => ({ ...f, word: f.word.trim() })) };
}
export function sensesForAim(word, aim) {
  const senses = editableWord(word).senses;
  return !aim ? senses : senses.filter(s => s.minimumTargetScore == null || s.minimumTargetScore <= Number(aim));
}
