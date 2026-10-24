const variants = {
  primary: "bg-primary text-white hover:bg-primary-dark shadow-sm",
  accent: "bg-accent text-white hover:bg-amber-500 shadow-sm",
  secondary: "bg-blue-50 text-primary hover:bg-blue-100",
  ghost: "bg-transparent text-slate-600 hover:bg-slate-100",
  danger: "bg-danger text-white hover:bg-red-600",
};

export function Button({
  children,
  variant = "primary",
  className = "",
  type = "button",
  disabled,
  loading,
  ...props
}) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${variants[variant]} ${className}`}
      {...props}
    >
      {loading ? "Đang xử lý..." : children}
    </button>
  );
}
