export function StatCard({ label, value, hint, icon: Icon }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)]">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">{label}</p>
        {Icon ? <Icon className="h-4 w-4 text-slate-400" /> : null}
      </div>
      <p className="mt-2 text-3xl font-bold text-slate-900">{value ?? 0}</p>
      {hint ? <p className="mt-1 text-xs text-slate-400">{hint}</p> : null}
    </div>
  );
}
