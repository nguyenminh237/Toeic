export function Spinner({ label = "Đang tải..." }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center gap-3 text-slate-500">
      <div className="h-9 w-9 animate-spin rounded-full border-2 border-slate-200 border-t-primary" />
      <p className="text-sm font-medium">{label}</p>
    </div>
  );
}
