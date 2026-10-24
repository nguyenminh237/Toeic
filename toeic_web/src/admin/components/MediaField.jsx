import { PrivateImage, PrivateAudio } from "../../components/common/PrivateMedia";
import { Upload } from "lucide-react";
import { useState } from "react";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";

const ACCEPT = {
  image: "image/jpeg,image/png,image/webp,image/gif",
  audio: "audio/mpeg,audio/mp3,audio/wav,audio/mp4,audio/x-m4a,audio/aac,audio/ogg",
};

export function MediaField({
  label,
  name,
  value,
  onChange,
  kind = "image",
  required = false,
}) {
  const { api } = useAuthoringScope();
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");

  function setUrl(next) {
    onChange({ target: { name, value: next } });
  }

  async function onFile(event) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    setError("");
    setUploading(true);
    try {
      const uploaded = await api.upload(file, kind);
      setUrl(uploaded.url);
    } catch (err) {
      setError(err.message || "Không tải được file");
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <span className="mb-1.5 block text-sm font-medium text-slate-700">
        {label}
        {required ? <span className="text-danger"> *</span> : null}
      </span>
      <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
        <label className="flex cursor-pointer items-center justify-center gap-2 rounded-lg border border-dashed border-slate-300 bg-white px-3 py-3 text-sm font-medium text-slate-600 hover:border-primary hover:text-primary">
          <Upload className="h-4 w-4" />
          <input
            type="file"
            accept={ACCEPT[kind]}
            className="hidden"
            disabled={uploading}
            onChange={onFile}
          />
          {uploading ? "Đang tải lên kho riêng..." : "Chọn file từ máy"}
        </label>
        <input
          name={name}
          value={value || ""}
          required={required}
          placeholder="hoặc dán URL"
          onChange={(event) => setUrl(event.target.value)}
          className="mt-2 h-11 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-blue-100"
        />
        {value ? (
          <div className="mt-3">
            {kind === "image" ? (
              <PrivateImage src={value} alt="" className="max-h-40 rounded-lg object-contain" />
            ) : (
              <PrivateAudio src={value} controls className="w-full" />
            )}
          </div>
        ) : null}
      </div>
      {error ? <p className="mt-1 text-sm text-danger">{error}</p> : null}
    </div>
  );
}
