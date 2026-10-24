export const ROUTES = {
  LOGIN: "/login",
  REGISTER: "/register",
  FORGOT_PASSWORD: "/forgot-password",
  RESET_PASSWORD: "/reset-password/:userId",
  DASHBOARD: "/",
  LISTENING: "/listening",
  LISTENING_PRACTICE: "/listening/:partNumber/sections/:sectionId",
  READING: "/reading",
  READING_PRACTICE: "/reading/:partNumber/sections/:sectionId",
  VOCABULARY: "/vocabulary",
  VOCABULARY_SETS: "/vocabulary/categories/:categoryId",
  VOCABULARY_WORDS: "/vocabulary/sets/:setId",
  TESTS: "/tests",
  TEST_PARTS: "/tests/suites/:suiteId",
  TEST_EXAM: "/tests/parts/:partId",
  TEST_RESULTS: "/tests/results",
  TEST_RESULT_DETAIL: "/tests/results/:resultId",
  PROFILE: "/profile",
  ADMIN: "/admin",
  ADMIN_USERS: "/admin/users",
  ADMIN_LISTENING: "/admin/listening",
  ADMIN_READING: "/admin/reading",
  ADMIN_VOCABULARY: "/admin/vocabulary",
  ADMIN_TESTS: "/admin/tests",
};

export const path = {
  resetPassword: (userId) => `/reset-password/${userId}`,
  listeningPractice: (partNumber, sectionId) =>
    `/listening/${partNumber}/sections/${sectionId}`,
  readingPractice: (partNumber, sectionId) =>
    `/reading/${partNumber}/sections/${sectionId}`,
  vocabularySets: (categoryId) => `/vocabulary/categories/${categoryId}`,
  vocabularyWords: (setId) => `/vocabulary/sets/${setId}`,
  testParts: (suiteId) => `/tests/suites/${suiteId}`,
  testExam: (partId) => `/tests/parts/${partId}`,
  testResultDetail: (resultId) => `/tests/results/${resultId}`,
};
