import { Volume2 } from "lucide-react";
import { canSpeak, speakEnglish } from "../../utils/speech";

export function SpeakButton({ text, label = "Phát âm" }) {
  if (!text || !canSpeak()) return null;

  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      onClick={(event) => {
        event.stopPropagation();
        speakEnglish(text);
      }}
      className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-blue-50 text-primary hover:bg-blue-100"
    >
      <Volume2 className="h-4 w-4" />
    </button>
  );
}
