import { ChevronLeft, ChevronRight, LayoutGrid } from "lucide-react";
import { useState } from "react";
import { QUESTION_STATUS } from "../../constants/status";
import { Button } from "../common/Button";
import { Modal } from "../common/Modal";

function tone(status) {
  if (status === QUESTION_STATUS.CORRECT) return "bg-emerald-500 text-white";
  if (status === QUESTION_STATUS.WRONG) return "bg-red-500 text-white";
  if (status === QUESTION_STATUS.MARKED) return "bg-amber-400 text-white";
  return "bg-slate-100 text-slate-600";
}

export function QuestionPager({ index, total, statuses = [], bookmarks = [], onPrev, onNext, onJump }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <div className="mt-6 flex items-center justify-between gap-3">
        <Button variant="secondary" onClick={onPrev} disabled={index <= 0}>
          <ChevronLeft className="h-4 w-4" />
          Câu trước
        </Button>
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="inline-flex items-center gap-2 rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
        >
          <LayoutGrid className="h-4 w-4" />
          Câu {index + 1}/{total}
        </button>
        <Button variant="secondary" onClick={onNext} disabled={index >= total - 1}>
          Câu sau
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
      <Modal open={open} title="Nhảy tới câu" onClose={() => setOpen(false)}>
        <div className="grid grid-cols-6 gap-2 sm:grid-cols-8">
          {Array.from({ length: total }, (_, i) => (
            <button
              key={i}
              type="button"
              onClick={() => {
                onJump(i);
                setOpen(false);
              }}
              title={bookmarks[i] ? "Đã đánh dấu ôn lại" : undefined}
              className={`h-10 rounded-lg text-sm font-semibold ${bookmarks[i] ? "border-2 border-amber-400" : ""} ${
                i === index ? "ring-2 ring-primary ring-offset-2" : ""
              } ${tone(statuses[i])}`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      </Modal>
    </>
  );
}
