import { useCallback, useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { Modal } from "../../components/common/Modal";
import { Spinner } from "../../components/common/Spinner";
import { ContextHint } from "../components/ContextHint";
import { CrudToolbar } from "../components/CrudToolbar";
import { DataTable } from "../components/DataTable";
import { PartOfSpeechBadge } from "../../components/vocabulary/PartOfSpeechBadge";
import { WordLearningEditor } from "../components/WordLearningEditor";
import { editableWord, wordPayload } from "../../utils/vocabulary";
import { vocabularyService } from "../../services/vocabularyService";
import { useAuthoringScope } from "../../hooks/useAuthoringScope";

const TABS = [
  { id: "categories", label: "Category" },
  { id: "sets", label: "Set" },
  { id: "words", label: "Từ" },
];

export function VocabularyManagementPage() {
  const { api: authoringApi, basePath } = useAuthoringScope();
  const api = authoringApi.vocabulary;
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const initialSetId = search.get("setId");
  const [tab, setTab] = useState(initialSetId ? "words" : "categories");
  const [categories, setCategories] = useState([]);
  const [sets, setSets] = useState([]);
  const [words, setWords] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [setId, setSetId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);

  const selectedCategory = categories.find((item) => String(item.id) === String(categoryId));
  const selectedSet = sets.find((item) => String(item.id) === String(setId));

  const loadCategories = useCallback(async () => {
    const data = (await api.listCategories()) || [];
    setCategories(data);
    const preferred = initialSetId ? await vocabularyService.getSet(initialSetId) : null;
    setCategoryId((current) => current || String(preferred?.categoryId || data[0]?.id || ""));
  }, [api, initialSetId]);

  useEffect(() => {
    loadCategories().catch((err) => setError(err.message)).finally(() => setLoading(false));
  }, [loadCategories]);

  useEffect(() => {
    let active = true;
    setSets([]); setSetId("");
    if (categoryId) {
      api.listSets(categoryId).then((data) => { if (!active) return; setSets(data || []); setSetId(String(data?.find(s => String(s.id) === initialSetId)?.id || data?.[0]?.id || "")); }).catch((err) => { if (active) setError(err.message); });
    }
    return () => { active = false; };
  }, [api, categoryId, initialSetId]);

  useEffect(() => {
    let active = true;
    setWords([]);
    if (setId) {
      api.listWords(setId).then((data) => { if (active) setWords(data || []); }).catch((err) => { if (active) setError(err.message); });
    }
    return () => { active = false; };
  }, [api, setId]);

  function change(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function openCreate() {
    setError("");
    if (tab === "words") {
      if (!setId) { setError("Chọn hoặc tạo bộ từ vựng trước khi thêm từ."); return; }
      navigate(`${basePath}/vocabulary/import?setId=${setId}`); return;
    }
    setEditing(null);
    if (tab === "categories") setForm({ categoryName: "", description: "" });
    if (tab === "sets") setForm({ setName: "", description: "" });
    setOpen(true);
  }

  async function onSubmit(event) {
    event.preventDefault();
    if (saving) return;
    setError("");
    setSaving(true);
    try {
      if (tab === "categories") {
        const saved = editing
          ? await api.updateCategory(editing.id, form)
          : await api.createCategory(form);
        await loadCategories();
        if (!editing && saved?.id) setCategoryId(String(saved.id));
      }
      if (tab === "sets") {
        if (!categoryId) throw new Error("Hãy chọn category ở bộ lọc trước khi thêm set.");
        const payload = { setName: form.setName, description: form.description, categoryId: Number(categoryId) };
        const saved = editing ? await api.updateSet(editing.id, payload) : await api.createSet(payload);
        const next = (await api.listSets(categoryId)) || [];
        setSets(next);
        if (!editing && saved?.id) setSetId(String(saved.id));
      }
      if (tab === "words") {
        if (!setId) throw new Error("Hãy chọn set ở bộ lọc trước khi thêm từ.");
        const payload = wordPayload(form, setId);
        const saved = editing ? await api.updateWord(editing.id, payload) : await api.createWord(payload);
        setOpen(false);
        if (saved) {
          setWords((current) => {
            const others = current.filter((item) => item.id !== saved.id);
            return [...others, saved];
          });
        }
        try {
          setWords((await api.listWords(setId)) || []);
        } catch {
          // Từ đã lưu; nếu reload lỗi vẫn giữ bản vừa tạo trên bảng.
        }
        setSaving(false);
        return;
      }
      setOpen(false);
    } catch (err) {
      setError(err.message || "Không lưu được");
    } finally {
      setSaving(false);
    }
  }

  async function onDelete(row) {
    if (!window.confirm("Lưu trữ mục này? Lịch sử học viên được giữ lại.")) return;
    try {
      if (tab === "categories") { await api.deleteCategory(row.id); await loadCategories(); }
      if (tab === "sets") { await api.deleteSet(row.id); setSets((await api.listSets(categoryId)) || []); }
      if (tab === "words") { await api.deleteWord(row.id); setWords((await api.listWords(setId)) || []); }
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <Spinner />;
  const rows = { categories, sets, words }[tab];
  const columns = {
    categories: [{ key: "categoryName", label: "Tên" }, { key: "description", label: "Mô tả" }],
    sets: [{ key: "setName", label: "Set" }, { key: "description", label: "Mô tả" }],
    words: [
      { key: "word", label: "Từ" },
      { key: "meaning", label: "Nghĩa" },
      { key: "partOfSpeech", label: "Loại", render: (row) => <PartOfSpeechBadge value={row.partOfSpeech} /> },
    ],
  }[tab];

  return (
    <div>
      {error ? <div className="mb-4"><Alert tone="error">{error}</Alert></div> : null}
      <CrudToolbar
        tabs={TABS}
        active={tab}
        onTab={setTab}
        onCreate={openCreate}
        createLabel={tab === "words" ? "Thêm từ" : "Thêm mới"}
        extra={
          tab !== "categories" ? (
            <select
              className="h-10 rounded-lg border border-slate-200 px-3"
              value={tab === "sets" ? categoryId : setId}
              onChange={(event) => (tab === "sets" ? setCategoryId(event.target.value) : setSetId(event.target.value))}
            >
              {(tab === "sets" ? categories : sets).map((item) => (
                <option key={item.id} value={item.id}>
                  {item.categoryName || item.setName}
                </option>
              ))}
            </select>
          ) : null
        }
      />
      <DataTable
        rows={rows}
        columns={[
          ...columns,
          {
            key: "actions",
            label: "",
            render: (row) => (
              <div className="flex gap-2">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setEditing(row);
                    setForm(tab === "words" ? editableWord(row) : row);
                    setOpen(true);
                  }}
                >
                  Sửa
                </Button>
                <Button variant="ghost" onClick={() => onDelete(row)}>Lưu trữ</Button>
              </div>
            ),
          },
        ]}
      />
      <Modal open={open} title={editing ? "Cập nhật" : "Tạo mới"} onClose={() => !saving && setOpen(false)}>
        <form className="space-y-3" onSubmit={onSubmit}>
          {tab === "categories" ? (
            <>
              <Input label="Tên category" name="categoryName" value={form.categoryName || ""} onChange={change} required />
              <Input label="Mô tả" name="description" value={form.description || ""} onChange={change} />
            </>
          ) : null}
          {tab === "sets" ? (
            <>
              <ContextHint label="Category" value={selectedCategory?.categoryName} />
              <Input label="Tên set" name="setName" value={form.setName || ""} onChange={change} required />
              <Input label="Mô tả" name="description" value={form.description || ""} onChange={change} />
            </>
          ) : null}
          {tab === "words" ? (
            <>
              <ContextHint label="Set" value={selectedSet?.setName} />
              <WordLearningEditor value={form} onChange={setForm} />
            </>
          ) : null}
          <Button type="submit" className="w-full" loading={saving} disabled={tab === "words" && !setId}>
            Lưu
          </Button>
        </form>
      </Modal>
    </div>
  );
}
