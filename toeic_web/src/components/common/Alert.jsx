import { AlertTriangle, CheckCircle2, Info, XCircle } from "lucide-react";

const tones = {
  info: {
    box: "border-blue-100 bg-blue-50 text-blue-800",
    icon: Info,
  },
  warning: {
    box: "border-amber-100 bg-amber-50 text-amber-800",
    icon: AlertTriangle,
  },
  success: {
    box: "border-emerald-100 bg-emerald-50 text-emerald-800",
    icon: CheckCircle2,
  },
  error: {
    box: "border-red-100 bg-red-50 text-red-700",
    icon: XCircle,
  },
};

export function Alert({ tone = "info", children }) {
  const current = tones[tone] || tones.info;
  const Icon = current.icon;
  return (
    <div className={`flex items-start gap-2 rounded-xl border px-4 py-3 text-sm ${current.box}`}>
      <Icon className="mt-0.5 h-4 w-4 shrink-0" />
      <div>{children}</div>
    </div>
  );
}
