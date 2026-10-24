export function Input({
  label,
  name,
  type = "text",
  error,
  className = "",
  ...props
}) {
  return (
    <label className={`block ${className}`}>
      {label ? (
        <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      ) : null}
      <input
        name={name}
        type={type}
        className={`h-12 w-full rounded-lg border bg-white px-3 text-slate-900 outline-none transition placeholder:text-slate-400 ${
          error
            ? "border-danger focus:border-danger focus:ring-2 focus:ring-red-100"
            : "border-slate-200 focus:border-primary focus:ring-2 focus:ring-blue-100"
        }`}
        {...props}
      />
      {error ? <p className="mt-1 text-sm text-danger">{error}</p> : null}
    </label>
  );
}
