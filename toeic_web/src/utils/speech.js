const VOICE_KEY = "toeic.speechVoice";

const ACCENT_LABELS = {
  "en-US": "Mỹ",
  "en-GB": "Anh",
  "en-AU": "Úc",
  "en-IN": "Ấn Độ",
  "en-CA": "Canada",
  "en-IE": "Ireland",
  "en-ZA": "Nam Phi",
  "en-NZ": "New Zealand",
  "en-NG": "Nigeria",
  "en-PH": "Philippines",
  "en-SG": "Singapore",
};

function loadVoices() {
  if (typeof window === "undefined" || !window.speechSynthesis) return [];
  return window.speechSynthesis.getVoices();
}

export function canSpeak() {
  return typeof window !== "undefined" && Boolean(window.speechSynthesis);
}

export function accentLabel(lang) {
  if (!lang) return "English";
  const key = lang.replace("_", "-");
  return ACCENT_LABELS[key] || ACCENT_LABELS[key.slice(0, 5)] || key;
}

export function voiceLabel(voice) {
  return `${accentLabel(voice.lang)} — ${voice.name}`;
}

export function getEnglishVoices() {
  return loadVoices()
    .filter((voice) => voice.lang?.toLowerCase().startsWith("en"))
    .sort((a, b) => a.lang.localeCompare(b.lang) || a.name.localeCompare(b.name));
}

export function getSelectedVoiceUri() {
  return localStorage.getItem(VOICE_KEY) || "";
}

export function setSelectedVoiceUri(uri) {
  if (uri) localStorage.setItem(VOICE_KEY, uri);
  else localStorage.removeItem(VOICE_KEY);
}

function defaultVoice() {
  const voices = getEnglishVoices();
  return (
    voices.find((voice) => voice.lang === "en-US" && /google|microsoft|natural/i.test(voice.name))
    || voices.find((voice) => voice.lang.startsWith("en-US"))
    || voices.find((voice) => voice.lang.toLowerCase().startsWith("en"))
    || null
  );
}

export function getSelectedVoice() {
  const voices = getEnglishVoices();
  const uri = getSelectedVoiceUri();
  return voices.find((voice) => voice.voiceURI === uri) || defaultVoice();
}

export function speakEnglish(text) {
  if (!text || !canSpeak()) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  const voice = getSelectedVoice();
  utterance.lang = voice?.lang || "en-US";
  utterance.rate = 0.9;
  if (voice) utterance.voice = voice;
  window.speechSynthesis.speak(utterance);
}
