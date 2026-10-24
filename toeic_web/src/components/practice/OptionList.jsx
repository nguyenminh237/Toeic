import { OPTIONS } from "../../constants/status";
import { optionText } from "../../utils/format";

export function OptionList({
  question,
  selected,
  revealed,
  hideTextUntilAnswered,
  hiddenLabel = "Đáp án ẩn — chọn rồi sẽ hiện nội dung",
  onSelect,
  disabled,
}) {
  return (
    <div className="space-y-3">
      {(question.options?.length ? question.options.map((o) => o.label) : question.partNumber === 2 ? ["A", "B", "C"] : OPTIONS).map((key) => {
        const text = question.options?.find((o) => o.label === key)?.content ?? optionText(question, key);
        const isSelected = selected === key;
        const isCorrect = revealed && question.correctAnswer === key;
        const isWrong = revealed && isSelected && selected !== question.correctAnswer;
        let style = "border-slate-200 bg-white hover:border-primary";
        if (isCorrect) style = "border-emerald-300 bg-emerald-50";
        else if (isWrong) style = "border-red-300 bg-red-50";
        else if (isSelected) style = "border-primary bg-blue-50";

        return (
          <button
            key={key}
            type="button"
            disabled={disabled}
            onClick={() => onSelect(key)}
            className={`flex w-full items-start gap-3 rounded-xl border px-4 py-3 text-left transition ${style}`}
          >
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-100 font-semibold">
              {key}
            </span>
            <span className="pt-1 text-sm text-slate-700">
              {hideTextUntilAnswered && !revealed ? hiddenLabel : text || "—"}
            </span>
          </button>
        );
      })}
    </div>
  );
}
