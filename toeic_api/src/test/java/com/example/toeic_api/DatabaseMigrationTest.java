package com.example.toeic_api;

import static org.junit.jupiter.api.Assertions.*;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.UUID;

class DatabaseMigrationTest {
    @org.junit.jupiter.api.BeforeEach
    void legacyZone() {
        System.setProperty("toeic.migration.legacy-zone", "UTC");
    }

    @org.junit.jupiter.api.AfterEach
    void clearLegacyZone() {
        System.clearProperty("toeic.migration.legacy-zone");
    }

    private DriverManagerDataSource database() {
        return new DriverManagerDataSource(
                "jdbc:h2:mem:migration-"
                        + UUID.randomUUID()
                        + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                "");
    }

    @Test
    void privateWorkspaceUpgradePreservesLegacyRowsAndScopesCategoryNamesByCreator() {
        var ds = database();
        Flyway.configure().dataSource(ds).target("3").load().migrate();
        var j = new JdbcTemplate(ds);
        for (String id : java.util.List.of("first", "second"))
            j.update(
                    "INSERT INTO users(id,username,password,email,role,enabled,created_at)"
                        + " VALUES(?,?, 'hash',?,'USER',TRUE,CURRENT_TIMESTAMP)",
                    id,
                    id,
                    id + "@example.com");
        j.update(
                "INSERT INTO vocabulary_categories(id,category_name,created_by,created_at)"
                    + " VALUES(41,'Work','first',CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO question_groups(id,part_id,title,created_at,updated_at)"
                    + " VALUES(42,5,'Legacy',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        var flyway = Flyway.configure().dataSource(ds).load();
        assertEquals(2, flyway.migrate().migrationsExecuted);
        assertEquals(
                "first",
                j.queryForObject(
                        "SELECT created_by FROM vocabulary_categories WHERE id=41", String.class));
        assertEquals(
                "Legacy",
                j.queryForObject("SELECT title FROM question_groups WHERE id=42", String.class));
        assertNull(
                j.queryForObject("SELECT owner_id FROM question_groups WHERE id=42", String.class));
        j.update(
                "INSERT INTO vocabulary_categories(category_name,created_by,created_at)"
                    + " VALUES('Work','second',CURRENT_TIMESTAMP)");
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        j.update(
                                "INSERT INTO"
                                    + " vocabulary_categories(category_name,created_by,created_at)"
                                    + " VALUES('Work','first',CURRENT_TIMESTAMP)"));
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> j.update("UPDATE question_groups SET owner_id='missing' WHERE id=42"));
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }

    @Test
    void upgradingV1PreservesPracticeMembershipAndProgressAndAllowsMoreThanFiveSections() {
        var ds = database();
        Flyway.configure().dataSource(ds).target("1").load().migrate();
        var j = new JdbcTemplate(ds);
        j.update(
                "INSERT INTO users(id,username,password,email,role,enabled,created_at)"
                    + " VALUES('section-user','sections','hash','sections@example.com','USER',TRUE,CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO"
                        + " practice_sets(id,part_id,level_number,level_name,created_at,updated_at)"
                        + " VALUES(42,5,1,'Existing name',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO question_groups(id,part_id,created_at,updated_at)"
                        + " VALUES(52,5,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO questions(id,group_id,position,created_at,updated_at)"
                        + " VALUES(62,52,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        j.update("INSERT INTO practice_set_items(set_id,question_id,position) VALUES(42,62,1)");
        j.update(
                "INSERT INTO"
                    + " user_question_progress(user_id,question_id,correct,bookmarked,attempt_count)"
                    + " VALUES('section-user',62,TRUE,TRUE,3)");
        var flyway = Flyway.configure().dataSource(ds).load();
        assertEquals(4, flyway.migrate().migrationsExecuted);
        assertEquals(
                "Existing name",
                j.queryForObject("SELECT name FROM practice_sets WHERE id=42", String.class));
        assertEquals(
                42,
                j.queryForObject(
                        "SELECT set_id FROM practice_set_items WHERE question_id=62",
                        Integer.class));
        assertEquals(
                3,
                j.queryForObject(
                        "SELECT attempt_count FROM user_question_progress WHERE question_id=62",
                        Integer.class));
        for (int n = 0; n < 7; n++)
            j.update(
                    "INSERT INTO practice_sets(part_id,name,description,created_at,updated_at)"
                            + " VALUES(5,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    "Section " + n,
                    "Description " + n);
        assertEquals(
                8,
                j.queryForObject(
                        "SELECT COUNT(*) FROM practice_sets WHERE part_id=5", Integer.class));
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }

    @Test
    void vocabularyV3BackfillsMeaningsWithoutLosingLegacySynonyms() {
        var ds = database();
        Flyway.configure().dataSource(ds).target("2").load().migrate();
        var j = new JdbcTemplate(ds);
        j.update(
                "INSERT INTO"
                    + " vocabulary_words(id,word,meaning,part_of_speech,example_sentence,created_at)"
                    + " VALUES(41,'invoice','hóa đơn','noun','Please check the"
                    + " invoice.',CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO vocabulary_synonyms(word_id,synonym_phrase,created_at)"
                        + " VALUES(41,'bill',CURRENT_TIMESTAMP)");
        var f = Flyway.configure().dataSource(ds).load();
        assertEquals(3, f.migrate().migrationsExecuted);
        assertEquals(
                "hóa đơn",
                j.queryForObject(
                        "SELECT meaning FROM vocabulary_senses WHERE word_id=41", String.class));
        assertNull(
                j.queryForObject(
                        "SELECT minimum_target_score FROM vocabulary_senses WHERE word_id=41",
                        Integer.class));
        assertEquals(
                "bill",
                j.queryForObject(
                        "SELECT synonym_phrase FROM vocabulary_synonyms WHERE word_id=41",
                        String.class));
        assertEquals(0, f.migrate().migrationsExecuted);
    }

    @Test
    void newDatabaseGetsCompleteConstrainedSchemaAndMigrationsAreRepeatable() {
        var ds = database();
        var f = Flyway.configure().dataSource(ds).load();
        f.migrate();
        assertEquals(0, f.migrate().migrationsExecuted);
        var j = new JdbcTemplate(ds);
        assertEquals(7, j.queryForObject("SELECT COUNT(*) FROM toeic_parts", Integer.class));
        assertEquals(5, f.info().applied().length);
        j.update(
                "INSERT INTO users(id,username,password,email,role,enabled,auth_version,created_at)"
                    + " VALUES('u','u','hash','u@example.com','USER',TRUE,0,CURRENT_TIMESTAMP)");
        j.update(
                "INSERT INTO user_vocabulary_collection(user_id,set_name,is_default,created_at)"
                    + " VALUES('u','One',FALSE,CURRENT_TIMESTAMP),('u','Two',FALSE,CURRENT_TIMESTAMP),('u','Default',TRUE,CURRENT_TIMESTAMP)");
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        j.update(
                                "INSERT INTO"
                                    + " user_vocabulary_collection(user_id,set_name,is_default,created_at)"
                                    + " VALUES('u','Other default',TRUE,CURRENT_TIMESTAMP)"));
    }

    @Test
    void legacyCutoverPreservesOriginalsMapsCollidingIdsAndReconstructsHistory() throws Exception {
        System.setProperty("toeic.migration.legacy-zone", "Asia/Ho_Chi_Minh");
        var ds = database();
        try (var c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("legacy-fixture.sql"));
        }
        assertThrows(
                org.flywaydb.core.api.FlywayException.class,
                () -> Flyway.configure().dataSource(ds).load().migrate());
        Flyway.configure()
                .dataSource(ds)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();
        var j = new JdbcTemplate(ds);
        assertEquals(2, j.queryForObject("SELECT COUNT(*) FROM questions", Integer.class));
        assertNotEquals(
                j.queryForObject(
                        "SELECT new_id FROM legacy_content_map WHERE"
                                + " source_type='listening_question' AND legacy_id=1",
                        Long.class),
                j.queryForObject(
                        "SELECT new_id FROM legacy_content_map WHERE source_type='test_question'"
                                + " AND legacy_id=1",
                        Long.class));
        assertEquals(
                2,
                j.queryForObject(
                        "SELECT COUNT(*) FROM legacy_listening_user_progress", Integer.class));
        assertEquals(
                1, j.queryForObject("SELECT COUNT(*) FROM user_question_progress", Integer.class));
        assertEquals(
                "A",
                j.queryForObject(
                        "SELECT selected_answer FROM user_question_progress", String.class));
        assertTrue(
                j.queryForObject("SELECT bookmarked FROM user_question_progress", Boolean.class));
        assertEquals(
                "learner@example.com", j.queryForObject("SELECT email FROM users", String.class));
        assertEquals(
                100,
                j.queryForObject(
                        "SELECT accuracy_percent FROM test_attempts WHERE id=7", Integer.class));
        assertTrue(
                j.queryForObject(
                                "SELECT snapshot FROM test_attempt_items WHERE attempt_id=7",
                                String.class)
                        .contains("Original reading"));
        assertEquals(
                "Original description",
                j.queryForObject("SELECT description FROM toeic_parts WHERE id=1", String.class));
        assertEquals(
                0, j.queryForObject("SELECT COUNT(*) FROM practice_answer_events", Integer.class));
        assertEquals(
                java.time.LocalDateTime.of(2026, 1, 1, 17, 0),
                j.queryForObject(
                        "SELECT started_at FROM test_attempts WHERE id=7",
                        java.time.LocalDateTime.class));
    }

    @Test
    void preflightStopsBeforeRenamingWhenEmailsConflict() throws Exception {
        var ds = database();
        try (var c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("legacy-fixture.sql"));
        }
        var j = new JdbcTemplate(ds);
        j.update(
                "INSERT INTO users(id,username,password,email,role,enabled,created_at)"
                    + " VALUES('second','second','hash','learner@example.com','USER',TRUE,CURRENT_TIMESTAMP)");
        assertThrows(
                org.flywaydb.core.api.FlywayException.class,
                () ->
                        Flyway.configure()
                                .dataSource(ds)
                                .baselineOnMigrate(true)
                                .baselineVersion("0")
                                .load()
                                .migrate());
        assertEquals(2, j.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
        assertThrows(
                org.springframework.dao.DataAccessException.class,
                () -> j.queryForObject("SELECT COUNT(*) FROM legacy_users", Integer.class));
    }
}
