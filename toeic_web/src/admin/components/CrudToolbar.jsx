import { Plus } from "lucide-react";
import { Button } from "../../components/common/Button";

export function CrudToolbar({ tabs, active, onTab, onCreate, createLabel = "Thêm mới", extra }) {
  return (
    <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
      <div className="flex flex-wrap gap-2">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => onTab(tab.id)}
            className={`rounded-full px-4 py-2 text-sm font-semibold ${
              active === tab.id ? "bg-slate-900 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <div className="flex min-w-0 flex-wrap items-center gap-3">
        {extra}
        <Button onClick={onCreate}>
          <Plus className="h-4 w-4" />
          {createLabel}
        </Button>
      </div>
    </div>
  );
}
