// Accept the format commonly copied from question documents, without requiring JSON.
export function parseQuestions(text, part) {
  const labels = Number(part) === 2 ? ["A", "B", "C"] : ["A", "B", "C", "D"];
  const rows = [];
  let row = { questionText: "", choices: {}, correctAnswer: "" }, last = null;
  function finish() {
    if (!row.questionText && !Object.keys(row.choices).length) return;
    if (labels.some((label) => !row.choices[label]?.trim()) || Object.keys(row.choices).length !== labels.length || !labels.includes(row.correctAnswer)) {
      throw new Error(`Câu ${rows.length + 1}: cần đủ ${labels.join("/")} và dòng Đáp án: A/B/C${Number(part) === 2 ? "" : "/D"}.`);
    }
    rows.push(row); row = { questionText: "", choices: {}, correctAnswer: "" }; last = null;
  }
  for (const source of text.split(/\r?\n/)) {
    const line = source.trim(); if (!line) continue;
    const heading = line.match(/^(?:(?:Câu|Question)\s*\d+\s*[:.)-]?|\d+[.)])\s*(.*)$/i);
    const option = line.match(/^([A-D])[.):]\s*(.*)$/i);
    const answer = line.match(/^(?:Đáp án|Dap an|Answer|Key)\s*[:：]\s*([A-D])\s*$/i);
    if (heading) { finish(); row.questionText = heading[1]; last = null; }
    else if (answer) { row.correctAnswer = answer[1].toUpperCase(); finish(); }
    else if (option) {
      const label = option[1].toUpperCase();
      if (Object.hasOwn(row.choices, label)) throw new Error(`Câu ${rows.length + 1}: lựa chọn ${label} bị trùng.`);
      row.choices[label] = option[2]; last = label;
    } else if (last) row.choices[last] += `\n${line}`;
    else row.questionText += `${row.questionText ? "\n" : ""}${line}`;
  }
  finish();
  if (!rows.length || rows.length > 50) throw new Error("Mỗi lần dán từ 1 đến 50 câu.");
  return rows;
}
