import { useEffect, useState } from "react";
import { http } from "../../services/httpClient";

function useMediaSource(src) {
  const privateSource = src?.startsWith("/api/v1/workspace/media/");
  const [result, setResult] = useState(null);
  useEffect(() => {
    if (!privateSource) return;
    let active = true, objectUrl;
    http.blob(src.slice("/api/v1".length)).then(blob => {
      if (!active) return;
      objectUrl = URL.createObjectURL(blob);
      setResult({ src, url: objectUrl });
    }).catch(() => { if (active) setResult({ src, error: true }); });
    return () => { active = false; if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [src, privateSource]);
  return privateSource ? (result?.src === src ? result : {}) : { url: src };
}

function Media({ kind, src, ...props }) {
  const source = useMediaSource(src);
  if (source.error) return <p role="alert" className="text-sm text-red-600">Không tải được tài liệu. Hãy kiểm tra quyền truy cập hoặc thử lại.</p>;
  if (!source.url) return <p role="status" className="text-sm text-slate-500">Đang tải tài liệu…</p>;
  return kind === "image" ? <img {...props} src={source.url} /> : <audio {...props} src={source.url} />;
}
export function PrivateImage(props) { return <Media kind="image" {...props} />; }
export function PrivateAudio(props) { return <Media kind="audio" {...props} />; }
