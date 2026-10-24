export const API_BASE = "/api/v1";
export const SUCCESS_CODE = 1000;

export const AUTH_ENDPOINTS = {
  LOGIN: "/auth/login",
  INTROSPECT: "/auth/introspect",
  LOGOUT: "/auth/logout",
  REFRESH: "/auth/refresh",
  FORGOT_PASSWORD: "/auth/forgot-password",
  RESET_PASSWORD: () => "/auth/reset-password",
};

export const USER_ENDPOINTS = {
  CREATE: "/users",
  LIST: "/users",
  BY_ID: (userId) => `/users/${userId}`,
  BY_USERNAME: (username) => `/users/username/${username}`,
};

export const LISTENING_ENDPOINTS = {
  PARTS: "/listening/parts",
  PART: (partNumber) => `/listening/parts/${partNumber}`,
  SECTIONS: (partNumber) => `/listening/sections/${partNumber}`,
  QUESTIONS_BY_SECTION: (sectionId) => `/listening/questions/section/${sectionId}`,
  QUESTION: (questionId) => `/listening/questions/${questionId}`,
  ANSWER: "/listening/answer",
  PROGRESS: (userId) => `/listening/progress/${userId}`,
  PROGRESS_STATUS: "/listening/progress",
};

export const READING_ENDPOINTS = {
  PARTS: "/reading/parts",
  PART: (partNumber) => `/reading/parts/${partNumber}`,
  SECTIONS: (partNumber) => `/reading/sections/${partNumber}`,
  PASSAGES_BY_SECTION: (sectionId) => `/reading/passages/section/${sectionId}`,
  QUESTIONS_BY_SECTION: (sectionId) => `/reading/questions/section/${sectionId}`,
  QUESTION: (questionId) => `/reading/questions/${questionId}`,
  ANSWER: "/reading/answer",
  PROGRESS: (userId) => `/reading/progress/${userId}`,
  PROGRESS_STATUS: "/reading/progress",
};

export const VOCAB_ENDPOINTS = {
  CATEGORIES: "/vocabulary/categories",
  CATEGORY: (categoryId) => `/vocabulary/categories/${categoryId}`,
  SETS: (categoryId) => `/vocabulary/sets/${categoryId}`,
  WORDS: (setId) => `/vocabulary/sets/${setId}/words`,
  WORD: (wordId) => `/vocabulary/words/${wordId}`,
  CREATE_WORD: "/vocabulary/words",
};

export const TEST_ENDPOINTS = {
  SUITES: "/tests/suites",
  SUITE: (suiteId) => `/tests/suites/${suiteId}`,
  PARTS: (suiteId) => `/tests/suites/${suiteId}/parts`,
  PART: (partId) => `/tests/parts/${partId}`,
  QUESTIONS: (partId) => `/tests/parts/${partId}/questions`,
  QUESTION: (questionId) => `/tests/questions/${questionId}`,
  START: (partId) => `/tests/start/${partId}`,
  SUBMIT: "/tests/submit",
  RESULTS: (userId) => `/tests/results/${userId}`,
  RESULT_DETAIL: (resultId) => `/tests/results/detail/${resultId}`,
};

export const ADMIN_ENDPOINTS = {
  DASHBOARD: "/admin/dashboard",
  USERS: "/admin/users",
  USER_STATUS: (id) => `/admin/users/${id}/status`,
  USER_ROLE: (id) => `/admin/users/${id}/role`,
  LISTENING_PARTS: "/admin/listening/parts",
  LISTENING_PART: (id) => `/admin/listening/parts/${id}`,
  LISTENING_SECTIONS: "/admin/listening/sections",
  LISTENING_SECTION: (id) => `/admin/listening/sections/${id}`,
  LISTENING_QUESTIONS: "/admin/listening/questions",
  LISTENING_QUESTION: (id) => `/admin/listening/questions/${id}`,
  READING_PARTS: "/admin/reading/parts",
  READING_PART: (id) => `/admin/reading/parts/${id}`,
  READING_SECTIONS: "/admin/reading/sections",
  READING_SECTION: (id) => `/admin/reading/sections/${id}`,
  READING_PASSAGES: "/admin/reading/passages",
  READING_PASSAGE: (id) => `/admin/reading/passages/${id}`,
  READING_QUESTIONS: "/admin/reading/questions",
  READING_QUESTION: (id) => `/admin/reading/questions/${id}`,
  VOCAB_CATEGORIES: "/admin/vocabulary/categories",
  VOCAB_CATEGORY: (id) => `/admin/vocabulary/categories/${id}`,
  VOCAB_SETS: "/admin/vocabulary/sets",
  VOCAB_SET: (id) => `/admin/vocabulary/sets/${id}`,
  VOCAB_WORDS: "/admin/vocabulary/words",
  VOCAB_WORD: (id) => `/admin/vocabulary/words/${id}`,
  TEST_SUITES: "/admin/tests/suites",
  TEST_SUITE: (id) => `/admin/tests/suites/${id}`,
  TEST_PARTS: "/admin/tests/parts",
  TEST_PART: (id) => `/admin/tests/parts/${id}`,
  TEST_QUESTIONS: "/admin/tests/questions",
  TEST_QUESTION: (id) => `/admin/tests/questions/${id}`,
  TEST_RESULTS: "/admin/tests/results",
  UPLOADS: "/admin/uploads",
};
