import { LISTENING_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const listeningService = {
  getSummary: (partNumber) => http.get(`/listening/parts/${partNumber}/summary`),
  getParts() {
    return http.get(LISTENING_ENDPOINTS.PARTS);
  },
  getPart(partNumber) {
    return http.get(LISTENING_ENDPOINTS.PART(partNumber));
  },
  getSections(partNumber) {
    return http.get(LISTENING_ENDPOINTS.SECTIONS(partNumber));
  },
  getQuestionsBySection(sectionId) {
    return http.get(LISTENING_ENDPOINTS.QUESTIONS_BY_SECTION(sectionId));
  },
  getQuestion(questionId) {
    return http.get(LISTENING_ENDPOINTS.QUESTION(questionId));
  },
  answer(userId, questionId, selectedAnswer) {
    return http.post(
      LISTENING_ENDPOINTS.ANSWER,
      { questionId, selectedAnswer },
      { query: { userId } },
    );
  },
  getProgress(userId) {
    return http.get(LISTENING_ENDPOINTS.PROGRESS(userId));
  },
  updateStatus(userId, questionId, status, bookmarked) {
    return http.patch(
      LISTENING_ENDPOINTS.PROGRESS_STATUS,
      { questionId, status, bookmarked },
      { query: { userId } },
    );
  },
};
