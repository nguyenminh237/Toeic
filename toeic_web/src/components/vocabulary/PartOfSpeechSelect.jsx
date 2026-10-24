import { PARTS_OF_SPEECH, getPartOfSpeech } from "../../constants/partOfSpeech";

export function PartOfSpeechSelect({ name = "partOfSpeech", value, onChange, required = true }) {
  const selected = getPartOfSpeech(value);

  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">Từ loại</span>
      <div className="flex flex-wrap gap-2">
        {PARTS_OF_SPEECH.map((item) => {
          const active = item.value === selected?.value;
          return (
            <button
              key={item.value}
              type="button"
              onClick={() => onChange({ target: { name, value: item.value } })}
              className={`rounded-full px-3 py-1.5 text-sm font-semibold ring-1 transition ${item.className} ${
                active ? "ring-2 ring-offset-1" : "opacity-70 hover:opacity-100"
              }`}
            >
              {item.label}
            </button>
          );
        })}
      </div>
      <input type="hidden" name={name} value={value || ""} required={required} />
    </label>
  );
}
