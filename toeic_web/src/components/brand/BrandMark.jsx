import logo from "../../assets/logo.jpg";

export function BrandMark({
  className = "",
  size = "md",
  light = false,
  showName = true,
}) {
  const imageSize = size === "lg" ? "h-16 w-16" : size === "sm" ? "h-8 w-8" : "h-10 w-10";
  const textSize = size === "lg" ? "text-2xl" : size === "sm" ? "text-base" : "text-lg";

  return (
    <span className={`inline-flex items-center gap-2 ${className}`}>
      <img
        src={logo}
        alt="TOEICUP"
        className={`${imageSize} rounded-full object-cover ring-1 ring-black/5`}
      />
      {showName ? (
        <span className={`font-bold tracking-[0.12em] ${textSize} ${light ? "text-white" : "text-primary"}`}>
          TOEICUP
        </span>
      ) : null}
    </span>
  );
}
