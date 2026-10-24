import { Bookmark, BookmarkCheck, RotateCcw } from "lucide-react";
import { QUESTION_STATUS } from "../../constants/status";
import { Button } from "../common/Button";

export function PracticeActions({ status, bookmarked, disabled, onReset, onToggleMark }) {
  const marked = Boolean(bookmarked);
  const canReset = status && status !== QUESTION_STATUS.UNANSWERED;

  return (
    <div className="mt-4 flex flex-wrap gap-2">
      <Button variant="secondary" disabled={disabled || !canReset} onClick={onReset}>
        <RotateCcw className="h-4 w-4" />
        Làm lại
      </Button>
      <Button
        variant={marked ? "accent" : "ghost"}
        disabled={disabled}
        onClick={onToggleMark}
        className={marked ? "" : "border border-amber-200 text-amber-700 hover:bg-amber-50"}
      >
        {marked ? <BookmarkCheck className="h-4 w-4" /> : <Bookmark className="h-4 w-4" />}
        {marked ? "Bỏ đánh dấu" : "Đánh dấu"}
      </Button>
    </div>
  );
}
