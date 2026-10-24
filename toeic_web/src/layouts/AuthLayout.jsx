import { Outlet } from "react-router-dom";
import { BrandMark } from "../components/brand/BrandMark";

export function AuthLayout() {
  return (
    <div className="grid min-h-screen bg-surface lg:grid-cols-2">
      <div className="hidden flex-col justify-between bg-primary px-12 py-16 text-white lg:flex">
        <BrandMark size="lg" light />
        <div>
          <h1 className="max-w-md text-4xl font-bold leading-tight">
            Luyện nghe, đọc và từ vựng theo từng Part.
          </h1>
          <p className="mt-4 max-w-md text-blue-100">
            Giao diện gọn, tập trung vào tiến độ học. Không thi full đề — chỉ luyện đúng phần bạn cần.
          </p>
        </div>
        <p className="text-sm text-blue-100">TOEICUP</p>
      </div>
      <div className="flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden">
            <BrandMark />
          </div>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
