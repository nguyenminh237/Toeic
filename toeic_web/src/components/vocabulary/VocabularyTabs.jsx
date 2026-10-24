import { useRef } from "react";

// Every tab is reachable with Tab as requested; focus activates its panel.
// Arrow keys/Home/End also work, and Tab can leave the list normally.
export function VocabularyTabs({ id, label, tabs, value, onChange }) {
  const buttons = useRef([]);
  function move(event, index) {
    let next;
    if (event.key === "ArrowRight") next = (index + 1) % tabs.length;
    else if (event.key === "ArrowLeft") next = (index - 1 + tabs.length) % tabs.length;
    else if (event.key === "Home") next = 0;
    else if (event.key === "End") next = tabs.length - 1;
    else return;
    event.preventDefault(); buttons.current[next]?.focus();
  }
  return <div role="tablist" aria-label={label} className="flex flex-wrap gap-1 rounded-2xl bg-slate-100 p-1.5">
    {tabs.map((tab, index) => <button key={tab.id} ref={el => { buttons.current[index] = el; }} type="button" role="tab" id={`${id}-${tab.id}-tab`} aria-controls={`${id}-${tab.id}-panel`} aria-selected={value === tab.id} onFocus={() => onChange(tab.id)} onClick={() => onChange(tab.id)} onKeyDown={event => move(event, index)} className={`inline-flex min-w-0 flex-1 items-center justify-center gap-1.5 rounded-xl px-2 py-3 text-xs font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 sm:gap-2 sm:px-3 sm:text-sm ${value === tab.id ? "bg-white text-primary shadow-sm" : "text-slate-500 hover:text-slate-900"}`}>
      {tab.icon ? <tab.icon className="h-3.5 w-3.5 shrink-0 sm:h-4 sm:w-4" /> : null}<span className="whitespace-nowrap">{tab.label}</span>
    </button>)}
  </div>;
}
