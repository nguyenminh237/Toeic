export function PartTabs({ parts, active, onChange, getLabel }) {
  return (
    <div className="mb-6 flex flex-wrap gap-2">
      {parts.map((part) => {
        const number = part.partNumber ?? part;
        const selected = Number(active) === Number(number);
        return (
          <button
            key={number}
            type="button"
            onClick={() => onChange(number)}
            className={`rounded-full px-4 py-2 text-sm font-semibold ${
              selected ? "bg-primary text-white" : "bg-white text-slate-600 ring-1 ring-slate-200"
            }`}
          >
            {getLabel ? getLabel(part) : `Part ${number}`}
          </button>
        );
      })}
    </div>
  );
}
