import { GraduationCap } from "lucide-react";

export function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-6 text-sm text-slate-500">
        <p className="inline-flex items-center gap-2">
          <GraduationCap className="h-4 w-4 text-primary" />
          TOEICUP — luyện theo Part, không thi full đề.
        </p>
        <p>Học đều mỗi ngày</p>
      </div>
    </footer>
  );
}
