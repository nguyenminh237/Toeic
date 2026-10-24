import { ADMIN_ENDPOINTS } from "../../constants/api";
import { http } from "../../services/httpClient";
import { listeningService } from "../../services/listeningService";
import { readingService } from "../../services/readingService";
import { testService } from "../../services/testService";
import { vocabularyService } from "../../services/vocabularyService";

export const adminApi = {
  upload(file, type) {
    const body = new FormData();
    body.append("file", file);
    body.append("type", type);
    return http.post(ADMIN_ENDPOINTS.UPLOADS, body);
  },
  getDashboard() {
    return http.get(ADMIN_ENDPOINTS.DASHBOARD);
  },
  searchUsers(q) {
    return http.get(ADMIN_ENDPOINTS.USERS, { query: q ? { q } : undefined });
  },
  setUserStatus(id, enabled) {
    return http.patch(ADMIN_ENDPOINTS.USER_STATUS(id), { enabled });
  },
  setUserRole(id, role) {
    return http.patch(ADMIN_ENDPOINTS.USER_ROLE(id), { role });
  },

  listening: {
    listParts: () => listeningService.getParts(),
    listSections: (partNumber) => listeningService.getSections(partNumber),
    listQuestions: (sectionId) => http.get("/admin/listening/questions", { query: { sectionId } }),
    createPart: (body) => http.post(ADMIN_ENDPOINTS.LISTENING_PARTS, body),
    updatePart: (id, body) => http.put(ADMIN_ENDPOINTS.LISTENING_PART(id), body),
    deletePart: (id) => http.delete(ADMIN_ENDPOINTS.LISTENING_PART(id)),
    createSection: (body) => http.post(ADMIN_ENDPOINTS.LISTENING_SECTIONS, body),
    updateSection: (id, body) => http.put(ADMIN_ENDPOINTS.LISTENING_SECTION(id), body),
    deleteSection: (id) => http.delete(ADMIN_ENDPOINTS.LISTENING_SECTION(id)),
    createQuestion: (body) => http.post(ADMIN_ENDPOINTS.LISTENING_QUESTIONS, body),
    updateQuestion: (id, body) => http.put(ADMIN_ENDPOINTS.LISTENING_QUESTION(id), body),
    deleteQuestion: (id) => http.delete(ADMIN_ENDPOINTS.LISTENING_QUESTION(id)),
  },

  reading: {
    listParts: () => readingService.getParts(),
    listSections: (partNumber) => readingService.getSections(partNumber),
    listPassages: (sectionId) => readingService.getPassagesBySection(sectionId),
    listQuestions: (sectionId) => http.get("/admin/reading/questions", { query: { sectionId } }),
    createPart: (body) => http.post(ADMIN_ENDPOINTS.READING_PARTS, body),
    updatePart: (id, body) => http.put(ADMIN_ENDPOINTS.READING_PART(id), body),
    deletePart: (id) => http.delete(ADMIN_ENDPOINTS.READING_PART(id)),
    createSection: (body) => http.post(ADMIN_ENDPOINTS.READING_SECTIONS, body),
    updateSection: (id, body) => http.put(ADMIN_ENDPOINTS.READING_SECTION(id), body),
    deleteSection: (id) => http.delete(ADMIN_ENDPOINTS.READING_SECTION(id)),
    createPassage: (body) => http.post(ADMIN_ENDPOINTS.READING_PASSAGES, body),
    updatePassage: (id, body) => http.put(ADMIN_ENDPOINTS.READING_PASSAGE(id), body),
    deletePassage: (id) => http.delete(ADMIN_ENDPOINTS.READING_PASSAGE(id)),
    createQuestion: (body) => http.post(ADMIN_ENDPOINTS.READING_QUESTIONS, body),
    updateQuestion: (id, body) => http.put(ADMIN_ENDPOINTS.READING_QUESTION(id), body),
    deleteQuestion: (id) => http.delete(ADMIN_ENDPOINTS.READING_QUESTION(id)),
  },

  vocabulary: {
    listCategories: () => vocabularyService.getCategories(),
    listSets: (categoryId) => vocabularyService.getSets(categoryId),
    listWords: (setId) => vocabularyService.getWords(setId),
    createCategory: (body) => http.post(ADMIN_ENDPOINTS.VOCAB_CATEGORIES, body),
    updateCategory: (id, body) => http.put(ADMIN_ENDPOINTS.VOCAB_CATEGORY(id), body),
    deleteCategory: (id) => http.delete(ADMIN_ENDPOINTS.VOCAB_CATEGORY(id)),
    createSet: (body) => http.post(ADMIN_ENDPOINTS.VOCAB_SETS, body),
    updateSet: (id, body) => http.put(ADMIN_ENDPOINTS.VOCAB_SET(id), body),
    deleteSet: (id) => http.delete(ADMIN_ENDPOINTS.VOCAB_SET(id)),
    createWord: (body) => http.post(ADMIN_ENDPOINTS.VOCAB_WORDS, body),
    updateWord: (id, body) => http.put(ADMIN_ENDPOINTS.VOCAB_WORD(id), body),
    deleteWord: (id) => http.delete(ADMIN_ENDPOINTS.VOCAB_WORD(id)),
  },

  tests: {
    listSuites: () => testService.getSuites(),
    listParts: (suiteId) => testService.getParts(suiteId),
    listQuestions: (partId) => http.get("/admin/tests/questions", { query: { partId } }),
    listResults: () => http.get(ADMIN_ENDPOINTS.TEST_RESULTS).then((page) => page.items),
    createSuite: (body) => http.post(ADMIN_ENDPOINTS.TEST_SUITES, body),
    updateSuite: (id, body) => http.put(ADMIN_ENDPOINTS.TEST_SUITE(id), body),
    deleteSuite: (id) => http.delete(ADMIN_ENDPOINTS.TEST_SUITE(id)),
    createPart: (body) => http.post(ADMIN_ENDPOINTS.TEST_PARTS, body),
    updatePart: (id, body) => http.put(ADMIN_ENDPOINTS.TEST_PART(id), body),
    deletePart: (id) => http.delete(ADMIN_ENDPOINTS.TEST_PART(id)),
    createQuestion: (body) => http.post(ADMIN_ENDPOINTS.TEST_QUESTIONS, body),
    updateQuestion: (id, body) => http.put(ADMIN_ENDPOINTS.TEST_QUESTION(id), body),
    deleteQuestion: (id) => http.delete(ADMIN_ENDPOINTS.TEST_QUESTION(id)),
  },
};
