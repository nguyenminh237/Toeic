import { http } from "./httpClient";
import { listeningService } from "./listeningService";
import { readingService } from "./readingService";
import { vocabularyService } from "./vocabularyService";

const root = "/workspace";
const resource = (path, name) => ({
  [`create${name}`]: body => http.post(`${root}/${path}`, body),
  [`update${name}`]: (id, body) => http.put(`${root}/${path}/${id}`, body),
  [`delete${name}`]: id => http.delete(`${root}/${path}/${id}`),
});
function practice(kind, reader) {
  return {
    listParts: () => reader.getParts(),
    listSections: part => reader.getSections(part),
    listQuestions: sectionId => http.get(`${root}/${kind}/questions`, { query: { sectionId } }),
    ...resource(`${kind}/sections`, "Section"),
    ...resource(`${kind}/questions`, "Question"),
  };
}
export const workspaceApi = {
  upload(file, type) {
    const body = new FormData(); body.append("file", file); body.append("type", type);
    return http.post(`${root}/uploads`, body);
  },
  listening: practice("listening", listeningService),
  reading: practice("reading", readingService),
  vocabulary: {
    listCategories: () => vocabularyService.getCategories(),
    listSets: id => vocabularyService.getSets(id),
    listWords: id => vocabularyService.getWords(id),
    ...resource("vocabulary/categories", "Category"),
    ...resource("vocabulary/sets", "Set"),
    ...resource("vocabulary/words", "Word"),
  },
};
