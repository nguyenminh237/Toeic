export function Card({ children, className = "", onClick }) {
  const interactive = Boolean(onClick);
  const Tag = interactive ? "button" : "div";
  return (
    <Tag
      type={interactive ? "button" : undefined}
      onClick={onClick}
      className={`rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-[var(--shadow-card)] ${
        interactive ? "transition hover:-translate-y-0.5 hover:shadow-[var(--shadow-lift)]" : ""
      } ${className}`}
    >
      {children}
    </Tag>
  );
}
