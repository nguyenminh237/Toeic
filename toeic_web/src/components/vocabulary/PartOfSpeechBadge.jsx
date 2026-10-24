import { getPartOfSpeech } from "../../constants/partOfSpeech";

export function PartOfSpeechBadge({ value }) {
  const item = getPartOfSpeech(value);
  if (!item) return null;
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${item.className}`}>
      {item.label}
    </span>
  );
}
