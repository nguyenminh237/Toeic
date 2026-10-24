import { TEST_ENDPOINTS } from "../constants/api";
import { http } from "./httpClient";

export const testService = {
  getSummary: () => http.get("/tests/me/summary"),
  getResultPage: (userId, page = 0, size = 50) => http.get(TEST_ENDPOINTS.RESULTS(userId), { query: { page, size } }),
  saveDraft: (id, answers) => http.put(`/tests/attempts/${id}/answers`, answers),
  getSuites() {
    return http.get(TEST_ENDPOINTS.SUITES);
  },
  getSuite(suiteId) {
    return http.get(TEST_ENDPOINTS.SUITE(suiteId));
  },
  getParts(suiteId) {
    return http.get(TEST_ENDPOINTS.PARTS(suiteId));
  },
  getPart(partId) {
    return http.get(TEST_ENDPOINTS.PART(partId));
  },
  getQuestions(partId) {
    return http.get(TEST_ENDPOINTS.QUESTIONS(partId));
  },
  getQuestion(questionId) {
    return http.get(TEST_ENDPOINTS.QUESTION(questionId));
  },
  start(partId, userId) {
    return http.post(TEST_ENDPOINTS.START(partId), undefined, { query: { userId } });
  },
  submit(userId, body) {
    return http.post(TEST_ENDPOINTS.SUBMIT, body, { query: { userId } });
  },
  getResults(userId) {
    return http.get(TEST_ENDPOINTS.RESULTS(userId)).then((result) => result.items);
  },
  getResultDetail(resultId) {
    return http.get(TEST_ENDPOINTS.RESULT_DETAIL(resultId));
  },
};
