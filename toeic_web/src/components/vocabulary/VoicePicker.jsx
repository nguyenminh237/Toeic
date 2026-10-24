import { Volume2 } from "lucide-react";
import { useEffect, useState } from "react";
import {
  canSpeak,
  getEnglishVoices,
  getSelectedVoice,
  getSelectedVoiceUri,
  setSelectedVoiceUri,
  speakEnglish,
  voiceLabel,
} from "../../utils/speech";

export function VoicePicker() {
  const [voices, setVoices] = useState([]);
  const [selected, setSelected] = useState("");

  useEffect(() => {
    function refresh() {
      const next = getEnglishVoices();
      setVoices(next);
      const current = getSelectedVoice();
      if (current && !getSelectedVoiceUri()) {
        setSelectedVoiceUri(current.voiceURI);
      }
      setSelected(getSelectedVoiceUri() || current?.voiceURI || "");
    }
    refresh();
    if (!canSpeak()) return undefined;
    window.speechSynthesis.addEventListener("voiceschanged", refresh);
    return () => window.speechSynthesis.removeEventListener("voiceschanged", refresh);
  }, []);

  if (!canSpeak() || !voices.length) return null;

  return (
    <label className="flex items-center gap-2 text-sm text-slate-600">
      <Volume2 className="h-4 w-4 text-primary" />
      <span className="whitespace-nowrap font-medium">Giọng</span>
      <select
        value={selected}
        onChange={(event) => {
          const uri = event.target.value;
          setSelected(uri);
          setSelectedVoiceUri(uri);
          speakEnglish("example");
        }}
        className="h-10 max-w-64 rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-800 outline-none focus:border-primary focus:ring-2 focus:ring-blue-100"
      >
        {voices.map((voice) => (
          <option key={voice.voiceURI} value={voice.voiceURI}>
            {voiceLabel(voice)}
          </option>
        ))}
      </select>
    </label>
  );
}
