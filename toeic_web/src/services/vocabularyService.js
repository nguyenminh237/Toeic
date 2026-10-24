import { VOCAB_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const vocabularyService = {
  getSet: (id) => http.get(`/vocabulary/sets/${id}/detail`),
  getCategories() {
    return http.get(VOCAB_ENDPOINTS.CATEGORIES);
  },
  getCategory(categoryId) {
    return http.get(VOCAB_ENDPOINTS.CATEGORY(categoryId));
  },
  getSets(categoryId) {
    return http.get(VOCAB_ENDPOINTS.SETS(categoryId));
  },
  getWords(setId) {
    return http.get(VOCAB_ENDPOINTS.WORDS(setId));
  },
  getWord(wordId) {
    return http.get(VOCAB_ENDPOINTS.WORD(wordId));
  },
  createWord(payload) {
    return http.post(VOCAB_ENDPOINTS.CREATE_WORD, payload);
  },
};
