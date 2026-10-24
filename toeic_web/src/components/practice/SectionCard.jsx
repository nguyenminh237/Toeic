import { CircleHelp, CircleX, Play, SquareCheck } from "lucide-react";
import { Badge } from "../common/Badge";
import { Button } from "../common/Button";
import { Card } from "../common/Card";


function StatBox({ icon: Icon, label, value, className }) {
  return (
    <div className={`rounded-xl px-2 py-2 text-center ${className}`}>
      <Icon className="mx-auto mb-1 h-3.5 w-3.5" />
      <p className="text-lg font-bold leading-none">{value ?? 0}</p>
      <p className="mt-1 text-[11px] font-medium">{label}</p>
    </div>
  );
}

export function SectionCard({ section, onStart, stats }) {
  const total = stats?.total ?? 0;

  return (
    <Card className="flex h-full flex-col">
      <div className="mb-4 h-1.5 w-10 rounded-full bg-primary" />
      <Badge tone="blue">Mục luyện tập</Badge>
      <h3 className="mt-3 text-xl font-semibold">{section.name}</h3>
      <p className="mt-2 whitespace-pre-line text-sm text-slate-600">{section.description}</p>
      <p className="mt-2 text-sm text-slate-500">{total} câu hỏi</p>
      <div className="mt-4 grid grid-cols-3 gap-2">
        <StatBox icon={SquareCheck} label="Đúng" value={stats?.correct} className="bg-emerald-50 text-emerald-700" />
        <StatBox icon={CircleX} label="Sai" value={stats?.wrong} className="bg-red-50 text-red-700" />
        <StatBox icon={CircleHelp} label="Chưa làm" value={stats?.unanswered ?? total} className="bg-slate-100 text-slate-600" />
      </div>
      <Button className="mt-6" onClick={() => onStart(section)}>
        <Play className="h-4 w-4" />
        Luyện ngay
      </Button>
    </Card>
  );
}
