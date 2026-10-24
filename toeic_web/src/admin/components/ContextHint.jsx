export function ContextHint({ label, value }) {
  if (!value) {
    return (
      <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
        Chưa chọn {label}. Hãy chọn ở bộ lọc phía trên rồi thêm mới.
      </p>
    );
  }
  return (
    <p className="rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-600">
      {label}: <span className="font-semibold text-slate-900">{value}</span>
    </p>
  );
}
