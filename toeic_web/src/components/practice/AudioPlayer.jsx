import { PrivateAudio } from "../common/PrivateMedia";
export function AudioPlayer({ src, startMs, endMs }) {
  if (!src) return <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-500">Chưa có audio cho nội dung này.</div>;
  return <PrivateAudio key={`${src}:${startMs ?? 0}:${endMs ?? ""}`} controls className="w-full" src={src}
    onLoadedMetadata={(event) => { if (startMs != null) event.currentTarget.currentTime = startMs / 1000; }}
    onPlay={(event) => { const player = event.currentTarget; if (startMs != null && (player.currentTime < startMs / 1000 || (endMs != null && player.currentTime >= endMs / 1000))) player.currentTime = startMs / 1000; }}
    onTimeUpdate={(event) => { if (endMs != null && event.currentTarget.currentTime >= endMs / 1000) event.currentTarget.pause(); }}>
    Trình duyệt không hỗ trợ audio.
  </PrivateAudio>;
}
