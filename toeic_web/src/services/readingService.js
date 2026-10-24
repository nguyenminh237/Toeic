import { READING_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const readingService = {
  getSummary: (partNumber) => http.get(`/reading/parts/${partNumber}/summary`),
  getParts() {
    return http.get(READING_ENDPOINTS.PARTS);
  },
  getPart(partNumber) {
    return http.get(READING_ENDPOINTS.PART(partNumber));
  },
  getSections(partNumber) {
    return http.get(READING_ENDPOINTS.SECTIONS(partNumber));
  },
  getPassagesBySection(sectionId) {
    return http.get(READING_ENDPOINTS.PASSAGES_BY_SECTION(sectionId));
  },
  getQuestionsBySection(sectionId) {
    return http.get(READING_ENDPOINTS.QUESTIONS_BY_SECTION(sectionId));
  },
  getQuestion(questionId) {
    return http.get(READING_ENDPOINTS.QUESTION(questionId));
  },
  answer(userId, questionId, selectedAnswer) {
    return http.post(
      READING_ENDPOINTS.ANSWER,
      { questionId, selectedAnswer },
      { query: { userId } },
    );
  },
  getProgress(userId) {
    return http.get(READING_ENDPOINTS.PROGRESS(userId));
  },
  updateStatus(userId, questionId, status, bookmarked) {
    return http.patch(
      READING_ENDPOINTS.PROGRESS_STATUS,
      { questionId, status, bookmarked },
      { query: { userId } },
    );
  },
};
