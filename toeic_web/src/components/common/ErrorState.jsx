import { RefreshCw, TriangleAlert } from "lucide-react";
import { Button } from "./Button";

export function ErrorState({ error, onRetry }) {
  return (
    <div className="rounded-2xl border border-red-100 bg-red-50 px-6 py-12 text-center">
      <TriangleAlert className="mx-auto mb-3 h-10 w-10 text-red-400" />
      <h3 className="text-lg font-semibold text-red-700">Không tải được dữ liệu</h3>
      <p className="mt-2 text-sm text-red-600">
        {error?.message || "Đã xảy ra lỗi. Hãy thử lại sau."}
      </p>
      {onRetry ? (
        <Button className="mt-5" variant="secondary" onClick={onRetry}>
          <RefreshCw className="h-4 w-4" />
          Thử lại
        </Button>
      ) : null}
    </div>
  );
}
