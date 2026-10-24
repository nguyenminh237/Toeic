import { PrivateImage } from "../common/PrivateMedia";
export function QuestionDocuments({ group }) {
  if (!group?.documents?.length) return null;
  return <div className="space-y-5">{group.documents.map((document, index) => (
    <section key={index} className="rounded-xl border border-slate-200 bg-slate-50 p-4">
      {document.title ? <h4 className="mb-2 font-semibold">{document.title}</h4> : null}
      {document.imageUrl ? <PrivateImage src={document.imageUrl} alt={document.title || "Tài liệu đọc"} className="mb-3 max-h-96 w-full object-contain" /> : null}
      {document.content ? <p className="whitespace-pre-wrap leading-7 text-slate-700">{document.content}</p> : null}
    </section>
  ))}</div>;
}
