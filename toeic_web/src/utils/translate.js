import { OPTIONS } from "../constants/status";
import { getPartOfSpeech } from "../constants/partOfSpeech";
import { optionText } from "./format";

const memory = new Map();

function cacheKey(text) {
  return String(text).trim().toLowerCase();
}

export async function translateText(text) {
  const source = String(text || "").trim();
  if (!source) return "";
  const key = `vi:${cacheKey(source)}`;
  if (memory.has(key)) return memory.get(key);

  let translated = "";
  try { translated = await translateWithMyMemory(source); } catch { /* Try the browser translator next. */ }
  translated ||= await translateWithChrome(source);
  if (!translated) throw new Error("Không dịch được. Thử lại sau.");
  memory.set(key, translated);
  return translated;
}

async function translateWithChrome(text) {
  if (typeof window === "undefined" || !("Translator" in window)) return "";
  try {
    const availability = await window.Translator.availability({
      sourceLanguage: "en",
      targetLanguage: "vi",
    });
    if (availability === "unavailable") return "";
    const translator = await window.Translator.create({
      sourceLanguage: "en",
      targetLanguage: "vi",
    });
    return String(await translator.translate(text) || "").trim();
  } catch {
    return "";
  }
}

async function translateWithMyMemory(text) {
  const url = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=en|vi`;
  const response = await fetch(url);
  if (!response.ok) throw new Error("Không kết nối được máy dịch");
  const data = await response.json();
  const translated = data?.responseData?.translatedText;
  if (!translated || /INVALID|QUERY LENGTH/i.test(translated)) return "";
  return String(translated).trim();
}

const POS_SHORT = {
  noun: "n",
  verb: "v",
  adjective: "adj",
  adverb: "adv",
  preposition: "prep",
  conjunction: "conj",
  pronoun: "pron",
  interjection: "int",
  determiner: "det",
  numeral: "num",
};

async function lookupPos(text) {
  const word = String(text || "").trim().replace(/[.,!?;:"()]/g, "");
  if (!word || word.split(/\s+/).length > 2) return "";
  const key = `pos:${cacheKey(word)}`;
  if (memory.has(key)) return memory.get(key);
  try {
    const response = await fetch(`https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(word)}`);
    if (!response.ok) {
      memory.set(key, "");
      return "";
    }
    const data = await response.json();
    const raw = data?.[0]?.meanings?.[0]?.partOfSpeech || "";
    const mapped = getPartOfSpeech(raw);
    const short = mapped?.short || POS_SHORT[raw.toLowerCase()] || "";
    memory.set(key, short);
    return short;
  } catch {
    memory.set(key, "");
    return "";
  }
}

export async function translateQuestion(question) {
  return translateText(question?.questionText || "");
}

export async function translateOptions(question) {
  const items = OPTIONS.map((key) => ({ key, english: optionText(question, key).trim() }))
    .filter((item) => item.english);
  if (!items.length) throw new Error("Câu này chưa có đáp án để dịch");

  const parts = await Promise.all(items.map(async ({ key, english }) => {
    const [pos, meaning] = await Promise.all([lookupPos(english), translateText(english)]);
    return pos ? `(${key}) ${english} (${pos}): ${meaning}` : `(${key}) ${english}: ${meaning}`;
  }));
  return parts.join(" | ");
}
