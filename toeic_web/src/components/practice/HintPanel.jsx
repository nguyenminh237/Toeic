const tones = {
  amber: {
    button: "bg-slate-100 text-amber-700 hover:bg-amber-50",
    buttonActive: "bg-amber-100 text-amber-800",
    panel: "border-amber-100 bg-amber-50/70",
  },
  blue: {
    button: "bg-slate-100 text-blue-700 hover:bg-blue-50",
    buttonActive: "bg-blue-100 text-blue-800",
    panel: "border-blue-100 bg-blue-50/70",
  },
  green: {
    button: "bg-slate-100 text-emerald-700 hover:bg-emerald-50",
    buttonActive: "bg-emerald-100 text-emerald-800",
    panel: "border-emerald-100 bg-emerald-50/70",
  },
};

export function HintPanel({ open, loading, error, tone = "amber", children }) {
  if (loading) {
    return <p className="mt-3 text-sm text-slate-500">Đang dịch...</p>;
  }
  if (error) {
    return <p className="mt-3 text-sm text-danger">{error}</p>;
  }
  if (!open || !children) return null;
  return (
    <div className={`mt-3 whitespace-pre-wrap rounded-xl border p-4 text-sm leading-6 text-slate-700 ${tones[tone].panel}`}>
      {children}
    </div>
  );
}

export function HintButton({ active, loading, tone = "amber", icon: Icon, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={loading}
      className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-semibold disabled:opacity-60 ${
        active ? tones[tone].buttonActive : tones[tone].button
      }`}
    >
      {Icon ? <Icon className="h-4 w-4" /> : null}
      {loading ? "Đang dịch..." : children}
    </button>
  );
}
