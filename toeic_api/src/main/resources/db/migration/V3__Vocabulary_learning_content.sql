CREATE TABLE vocabulary_senses (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    word_id BIGINT NOT NULL,
    position INT NOT NULL,
    meaning LONGTEXT NOT NULL,
    part_of_speech VARCHAR(255) NOT NULL,
    minimum_target_score INT NULL,
    example_sentence LONGTEXT NULL,
    example_translation LONGTEXT NULL,
    study_note LONGTEXT NULL,
    CONSTRAINT fk_vocab_sense_word FOREIGN KEY (word_id) REFERENCES vocabulary_words(id),
    CONSTRAINT uq_vocab_sense_position UNIQUE(word_id, position),
    CONSTRAINT ck_vocab_sense_score CHECK (minimum_target_score IS NULL OR minimum_target_score BETWEEN 10 AND 990)
);
CREATE TABLE vocabulary_sense_phrases (
    sense_id BIGINT NOT NULL,
    position INT NOT NULL,
    kind VARCHAR(20) NOT NULL,
    phrase VARCHAR(300) NOT NULL,
    PRIMARY KEY(sense_id, position),
    CONSTRAINT fk_vocab_phrase_sense FOREIGN KEY (sense_id) REFERENCES vocabulary_senses(id),
    CONSTRAINT ck_vocab_phrase_kind CHECK (kind IN ('SYNONYM','ANTONYM','COLLOCATION'))
);
CREATE TABLE vocabulary_word_family (
    word_id BIGINT NOT NULL,
    position INT NOT NULL,
    family_word VARCHAR(100) NOT NULL,
    part_of_speech VARCHAR(40) NULL,
    meaning VARCHAR(500) NULL,
    PRIMARY KEY(word_id, position),
    CONSTRAINT fk_vocab_family_word FOREIGN KEY (word_id) REFERENCES vocabulary_words(id)
);
INSERT INTO vocabulary_senses(word_id,position,meaning,part_of_speech,example_sentence,example_translation)
SELECT id,1,meaning,part_of_speech,example_sentence,example_translation FROM vocabulary_words;
