export function VocabularyProgress({ value, total, label, tone = "blue" }) {
  const percent = Math.min(100, Math.max(0, value / Math.max(total, 1) * 100));
  return <div role="progressbar" aria-label={label} aria-valuemin={0} aria-valuemax={total} aria-valuenow={value} className="h-1.5 w-full overflow-hidden rounded-full bg-slate-200"><div className={`h-full rounded-full transition-[width] ${tone === "green" ? "bg-emerald-500" : "bg-blue-500"}`} style={{ width: `${percent}%` }} /></div>;
}
