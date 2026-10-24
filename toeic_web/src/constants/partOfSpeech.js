export const PARTS_OF_SPEECH = [
  { value: "noun", label: "Noun", short: "n", className: "bg-blue-50 text-blue-700 ring-blue-200" },
  { value: "verb", label: "Verb", short: "v", className: "bg-emerald-50 text-emerald-700 ring-emerald-200" },
  { value: "adj", label: "Adjective", short: "adj", className: "bg-amber-50 text-amber-700 ring-amber-200" },
  { value: "adv", label: "Adverb", short: "adv", className: "bg-violet-50 text-violet-700 ring-violet-200" },
  { value: "phrase", label: "Phrase", short: "phr", className: "bg-rose-50 text-rose-700 ring-rose-200" },
  { value: "prep", label: "Preposition", short: "prep", className: "bg-teal-50 text-teal-700 ring-teal-200" },
  { value: "conj", label: "Conjunction", short: "conj", className: "bg-indigo-50 text-indigo-700 ring-indigo-200" },
];

const FALLBACK = "bg-slate-100 text-slate-600 ring-slate-200";

const ALIASES = {
  n: "noun",
  v: "verb",
  adjective: "adj",
  adverb: "adv",
  preposition: "prep",
  conjunction: "conj",
  phr: "phrase",
};

export function getPartOfSpeech(value) {
  if (!value) return null;
  const key = ALIASES[String(value).trim().toLowerCase()] || String(value).trim().toLowerCase();
  return (
    PARTS_OF_SPEECH.find((item) => item.value === key || item.short === key || item.label.toLowerCase() === key)
    || { value: key, label: value, short: key, className: FALLBACK }
  );
}
