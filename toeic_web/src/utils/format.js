export function formatDateTime(value) {
  if (!value) return "—";
  const normalized = typeof value === "string" && /^\d{4}-\d\d-\d\dT/.test(value) && !/(Z|[+-]\d\d:\d\d)$/.test(value) ? `${value}Z` : value;
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

export function formatDuration(totalSeconds) {
  const safe = Math.max(0, Number(totalSeconds) || 0);
  const minutes = Math.floor(safe / 60);
  const seconds = safe % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

export function optionText(question, key) {
  return question?.[`option${key}`] ?? "";
}

export function initials(name) {
  if (!name) return "U";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

export function statusLabel(status) {
  switch (status) {
    case "CORRECT":
      return "Đúng";
    case "WRONG":
      return "Sai";
    case "MARKED":
      return "Đánh dấu";
    case "IN_PROGRESS":
      return "Đang làm";
    case "SUBMITTED":
      return "Đã nộp";
    case "REVIEWED":
      return "Đã xem lại";
    default:
      return "Chưa làm";
  }
}
