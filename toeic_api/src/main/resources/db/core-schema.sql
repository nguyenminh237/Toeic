CREATE TABLE users (
 id VARCHAR(255) PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL,
 email VARCHAR(255) NOT NULL UNIQUE, full_name VARCHAR(255), role VARCHAR(32) NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT TRUE, auth_version BIGINT NOT NULL DEFAULT 0,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6)
);
CREATE TABLE invalidated_tokens (id VARCHAR(255) PRIMARY KEY, expiry_time DATETIME(6), INDEX ix_token_expiry(expiry_time));
CREATE TABLE password_reset_tokens (
 token_hash VARCHAR(64) PRIMARY KEY, user_id VARCHAR(255) NOT NULL, expires_at DATETIME(6) NOT NULL,
 used_at DATETIME(6), auth_version BIGINT NOT NULL, FOREIGN KEY(user_id) REFERENCES users(id), INDEX ix_reset_expiry(expires_at)
);
CREATE TABLE toeic_parts (id INT PRIMARY KEY, description LONGTEXT NOT NULL, CHECK (id BETWEEN 1 AND 7));
INSERT INTO toeic_parts(id,description) VALUES (1,'Photographs'),(2,'Question-Response'),(3,'Conversations'),(4,'Talks'),(5,'Incomplete Sentences'),(6,'Text Completion'),(7,'Reading Comprehension');
CREATE TABLE question_groups (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, part_id INT NOT NULL, title VARCHAR(255), audio_url VARCHAR(255), image_url VARCHAR(255),
 audio_start_ms INT, audio_end_ms INT, transcript LONGTEXT,
 version BIGINT NOT NULL DEFAULT 0, archived BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 FOREIGN KEY(part_id) REFERENCES toeic_parts(id), INDEX ix_group_part(part_id,archived),
 CHECK ((audio_start_ms IS NULL AND audio_end_ms IS NULL) OR (audio_start_ms>=0 AND audio_end_ms>audio_start_ms))
);
CREATE TABLE group_documents (
 group_id BIGINT NOT NULL, position INT NOT NULL, title VARCHAR(255), content LONGTEXT, image_url VARCHAR(255),
 PRIMARY KEY(group_id,position), FOREIGN KEY(group_id) REFERENCES question_groups(id)
);
CREATE TABLE questions (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, group_id BIGINT NOT NULL, position INT NOT NULL, question_text LONGTEXT, prompt_text LONGTEXT,
 explanation LONGTEXT, hide_question BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
 archived BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 FOREIGN KEY(group_id) REFERENCES question_groups(id), CONSTRAINT uk_question_group_position UNIQUE(group_id,position), CHECK(position>0)
);
CREATE TABLE question_options (
 question_id BIGINT NOT NULL, position INT NOT NULL, label VARCHAR(1) NOT NULL, content LONGTEXT, correct BOOLEAN NOT NULL,
 PRIMARY KEY(question_id,position), CONSTRAINT uk_option_label UNIQUE(question_id,label), FOREIGN KEY(question_id) REFERENCES questions(id), CHECK(label IN ('A','B','C','D'))
);
CREATE TABLE practice_sets (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, part_id INT NOT NULL, level_number INT NOT NULL, level_name VARCHAR(255) NOT NULL, audio_url VARCHAR(255),
 version BIGINT NOT NULL DEFAULT 0, archived BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 FOREIGN KEY(part_id) REFERENCES toeic_parts(id), CONSTRAINT uk_set_part_level UNIQUE(part_id,level_number), CHECK(level_number BETWEEN 1 AND 5)
);
CREATE TABLE practice_set_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, set_id BIGINT NOT NULL, question_id BIGINT NOT NULL, position INT NOT NULL,
 FOREIGN KEY(set_id) REFERENCES practice_sets(id), FOREIGN KEY(question_id) REFERENCES questions(id),
 CONSTRAINT uk_practice_position UNIQUE(set_id,position), CONSTRAINT uk_practice_question UNIQUE(set_id,question_id), CHECK(position>0)
);
CREATE TABLE user_question_progress (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(255) NOT NULL, question_id BIGINT NOT NULL,
 selected_answer VARCHAR(255), question_version BIGINT NOT NULL DEFAULT 0, group_version BIGINT NOT NULL DEFAULT 0, correct BOOLEAN, bookmarked BOOLEAN NOT NULL DEFAULT FALSE, attempt_count BIGINT NOT NULL DEFAULT 0, last_answered_at DATETIME(6),
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(question_id) REFERENCES questions(id),
 CONSTRAINT uk_progress_user_question UNIQUE(user_id,question_id), INDEX ix_progress_user_time(user_id,last_answered_at)
);
CREATE TABLE practice_answer_events (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(255) NOT NULL, question_id BIGINT NOT NULL, question_version BIGINT NOT NULL,
 group_version BIGINT NOT NULL DEFAULT 0, selected_answer VARCHAR(255) NOT NULL, correct BOOLEAN NOT NULL, answered_at DATETIME(6) NOT NULL,
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(question_id) REFERENCES questions(id), INDEX ix_event_user_time(user_id,answered_at)
);
CREATE TABLE test_suites (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, suite_name VARCHAR(255) NOT NULL UNIQUE, description LONGTEXT, source VARCHAR(255),
 version BIGINT NOT NULL DEFAULT 0, archived BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL
);
CREATE TABLE test_parts (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, suite_id BIGINT NOT NULL, toeic_part_id INT NOT NULL, time_limit INT NOT NULL, audio_url VARCHAR(255),
 version BIGINT NOT NULL DEFAULT 0, archived BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 FOREIGN KEY(suite_id) REFERENCES test_suites(id), FOREIGN KEY(toeic_part_id) REFERENCES toeic_parts(id),
 CONSTRAINT uk_suite_part UNIQUE(suite_id,toeic_part_id), CHECK(time_limit>0)
);
CREATE TABLE test_part_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, part_id BIGINT NOT NULL, question_id BIGINT NOT NULL, position INT NOT NULL,
 FOREIGN KEY(part_id) REFERENCES test_parts(id), FOREIGN KEY(question_id) REFERENCES questions(id),
 CONSTRAINT uk_test_position UNIQUE(part_id,position), CONSTRAINT uk_test_question UNIQUE(part_id,question_id), CHECK(position>0)
);
CREATE TABLE test_attempts (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, version BIGINT NOT NULL DEFAULT 0, user_id VARCHAR(255) NOT NULL, part_id BIGINT NOT NULL,
 active_key VARCHAR(100) UNIQUE, status VARCHAR(255) NOT NULL, started_at DATETIME(6) NOT NULL, expires_at DATETIME(6) NOT NULL,
 submitted_at DATETIME(6), total_count INT NOT NULL, time_spent INT, correct_count INT, accuracy_percent INT, part_number INT NOT NULL, suite_name VARCHAR(255) NOT NULL, audio_url VARCHAR(255),
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(part_id) REFERENCES test_parts(id),
 INDEX ix_attempt_user_time(user_id,started_at), INDEX ix_attempt_expiry(status,expires_at),
 CHECK(status IN ('IN_PROGRESS','SUBMITTED','REVIEWED')), CHECK(accuracy_percent IS NULL OR accuracy_percent BETWEEN 0 AND 100)
);
CREATE TABLE test_attempt_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, attempt_id BIGINT NOT NULL, question_id BIGINT NOT NULL, position INT NOT NULL,
 snapshot LONGTEXT NOT NULL, selected_answer VARCHAR(255), correct BOOLEAN, answered_at DATETIME(6),
 FOREIGN KEY(attempt_id) REFERENCES test_attempts(id), FOREIGN KEY(question_id) REFERENCES questions(id),
 CONSTRAINT uk_attempt_position UNIQUE(attempt_id,position), CONSTRAINT uk_attempt_question UNIQUE(attempt_id,question_id)
);
CREATE TABLE vocabulary_categories (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, category_name VARCHAR(255) NOT NULL UNIQUE, description LONGTEXT, created_by VARCHAR(255),
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6), archived BOOLEAN NOT NULL DEFAULT FALSE, FOREIGN KEY(created_by) REFERENCES users(id)
);
CREATE TABLE vocabulary_sets (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, category_id BIGINT NOT NULL, set_name VARCHAR(255) NOT NULL, description LONGTEXT,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6), archived BOOLEAN NOT NULL DEFAULT FALSE, FOREIGN KEY(category_id) REFERENCES vocabulary_categories(id)
);
CREATE TABLE vocabulary_words (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, word VARCHAR(255) NOT NULL, meaning LONGTEXT NOT NULL, part_of_speech VARCHAR(255) NOT NULL,
 example_sentence LONGTEXT, example_translation LONGTEXT, pronunciation VARCHAR(255), audio_url VARCHAR(255),
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6), archived BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE vocabulary_word_sets (
 word_id BIGINT NOT NULL, set_id BIGINT NOT NULL, PRIMARY KEY(word_id,set_id),
 FOREIGN KEY(word_id) REFERENCES vocabulary_words(id), FOREIGN KEY(set_id) REFERENCES vocabulary_sets(id), INDEX ix_word_set(set_id,word_id)
);
CREATE TABLE vocabulary_synonyms (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, word_id BIGINT NOT NULL, synonym_phrase LONGTEXT NOT NULL, created_at DATETIME(6) NOT NULL,
 FOREIGN KEY(word_id) REFERENCES vocabulary_words(id)
);
CREATE TABLE user_vocabulary_collection (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(255) NOT NULL, set_name VARCHAR(255) NOT NULL, is_default BOOLEAN NOT NULL,
 created_at DATETIME(6) NOT NULL, FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE user_vocabulary_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, collection_id BIGINT NOT NULL, word_id BIGINT NOT NULL, added_at DATETIME(6) NOT NULL,
 FOREIGN KEY(collection_id) REFERENCES user_vocabulary_collection(id), FOREIGN KEY(word_id) REFERENCES vocabulary_words(id), CONSTRAINT uk_collection_word UNIQUE(collection_id,word_id)
);
CREATE TABLE user_vocabulary_progress (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(255) NOT NULL, word_id BIGINT NOT NULL, status VARCHAR(255) NOT NULL,
 last_reviewed DATETIME(6), review_count INT NOT NULL DEFAULT 0,
 FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(word_id) REFERENCES vocabulary_words(id), CONSTRAINT uk_vocab_progress UNIQUE(user_id,word_id)
);
CREATE TABLE legacy_content_map (source_type VARCHAR(50) NOT NULL, legacy_id BIGINT NOT NULL, new_id BIGINT NOT NULL, PRIMARY KEY(source_type,legacy_id));
CREATE TABLE migration_issues (id BIGINT AUTO_INCREMENT PRIMARY KEY, source_type VARCHAR(50) NOT NULL, legacy_id BIGINT, detail VARCHAR(1000) NOT NULL);
