package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.*;
import java.time.*;
import java.util.*;

/** One-time, explicit cutover. Legacy tables remain available for reconciliation/rollback. */
public class V1__Canonical_learning_model extends BaseJavaMigration {
    private Connection connection;
    private ZoneId legacyZone;
    private JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Long> ids = new HashMap<>();
    private static final List<String> LEGACY =
            List.of(
                    "users",
                    "invalidated_tokens",
                    "listening_parts",
                    "listening_levels",
                    "listening_questions",
                    "listening_user_progress",
                    "reading_parts",
                    "reading_levels",
                    "reading_passages",
                    "reading_questions",
                    "reading_user_progress",
                    "test_suites",
                    "test_parts",
                    "test_questions",
                    "user_test_results",
                    "user_test_answers",
                    "vocabulary_categories",
                    "vocabulary_sets",
                    "vocabulary_words",
                    "vocabulary_synonyms",
                    "user_vocabulary_collection",
                    "user_vocabulary_items",
                    "user_vocabulary_progress");

    @Override
    public Integer getChecksum() {
        try {
            var crc = new java.util.zip.CRC32();
            try (var schema = new ClassPathResource("db/core-schema.sql").getInputStream()) {
                crc.update(schema.readAllBytes());
            }
            try (var code = getClass().getResourceAsStream("V1__Canonical_learning_model.class")) {
                crc.update(code.readAllBytes());
            }
            return (int) crc.getValue();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void migrate(Context context) throws Exception {
        connection = context.getConnection();
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        boolean upgrade = exists("users");
        if (upgrade) {
            if (exists("toeic_parts"))
                throw new IllegalStateException(
                        "Canonical schema already exists without Flyway history; restore the"
                                + " correct history instead of baselining it.");
            preflight();
            for (String table : LEGACY)
                if (exists(table)) {
                    if (exists("legacy_" + table))
                        throw new IllegalStateException(
                                "Legacy backup table already exists: " + table);
                    jdbc.execute("ALTER TABLE " + table + " RENAME TO legacy_" + table);
                }
        }
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/core-schema.sql"));
        // NULL permits multiple non-default collections, while exactly one default is permitted per
        // user.
        boolean mysql =
                connection
                        .getMetaData()
                        .getDatabaseProductName()
                        .toLowerCase(Locale.ROOT)
                        .contains("mysql");
        jdbc.execute(
                "ALTER TABLE user_vocabulary_collection ADD COLUMN default_owner VARCHAR(255)"
                        + " GENERATED ALWAYS AS (CASE WHEN is_default THEN user_id ELSE NULL END)"
                        + (mysql ? " STORED" : ""));
        jdbc.execute(
                "ALTER TABLE user_vocabulary_collection ADD CONSTRAINT uk_default_collection"
                        + " UNIQUE(default_owner)");
        if (upgrade) {
            copyIdentityAndVocabulary();
            copySets();
            copyContent();
            copyProgress();
            copyTests();
            normalizeLegacyTimestamps();
        }
    }

    private void preflight() {
        String configuredZone =
                System.getProperty(
                        "toeic.migration.legacy-zone", System.getenv("LEGACY_TIME_ZONE"));
        if (configuredZone == null || configuredZone.isBlank())
            throw new IllegalStateException(
                    "Set LEGACY_TIME_ZONE to the time zone used by the old application before"
                            + " migrating existing data");
        legacyZone = ZoneId.of(configuredZone);
        for (String table : LEGACY) {
            if (exists("legacy_" + table)) {
                throw new IllegalStateException(
                        "Legacy backup already exists; reconcile before cutover: " + table);
            }
        }
        for (String section : List.of("listening", "reading")) {
            if (exists(section + "_parts")) {
                reject(
                        "SELECT COUNT(*) FROM "
                                + section
                                + "_parts WHERE part_number NOT BETWEEN "
                                + (section.equals("listening") ? "1 AND 4" : "5 AND 7"),
                        "Invalid legacy Part");
            }
            if (exists(section + "_levels")) {
                reject(
                        "SELECT COUNT(*) FROM "
                                + section
                                + "_levels WHERE level_number NOT BETWEEN 1 AND 5",
                        "Invalid legacy level");
            }
        }
        if (exists("test_parts")) {
            reject(
                    "SELECT COUNT(*) FROM test_parts WHERE time_limit<=0 OR part_number NOT BETWEEN"
                            + " 1 AND 7",
                    "Invalid legacy test time limit or Part");
        }
        reject(
                "SELECT COUNT(*) FROM (SELECT LOWER(TRIM(email)) e FROM users GROUP BY"
                        + " LOWER(TRIM(email)) HAVING COUNT(*)>1) d",
                "Duplicate normalized emails; reconcile before migration");
        if (exists("listening_levels"))
            reject(
                    "SELECT COUNT(*) FROM (SELECT part_id,level_number FROM listening_levels GROUP"
                            + " BY part_id,level_number HAVING COUNT(*)>1) d",
                    "Duplicate listening levels");
        if (exists("reading_levels"))
            reject(
                    "SELECT COUNT(*) FROM (SELECT part_id,level_number FROM reading_levels GROUP BY"
                            + " part_id,level_number HAVING COUNT(*)>1) d",
                    "Duplicate reading levels");
        if (exists("test_parts"))
            reject(
                    "SELECT COUNT(*) FROM (SELECT suite_id,part_number FROM test_parts GROUP BY"
                            + " suite_id,part_number HAVING COUNT(*)>1) d",
                    "Duplicate test parts");
        if (exists("user_vocabulary_collection"))
            reject(
                    "SELECT COUNT(*) FROM (SELECT user_id FROM user_vocabulary_collection WHERE"
                            + " is_default=TRUE GROUP BY user_id HAVING COUNT(*)>1) d",
                    "Multiple default collections");
        for (String table : List.of("listening_questions", "reading_questions"))
            if (exists(table))
                reject(
                        "SELECT COUNT(*) FROM (SELECT level_id,question_number FROM "
                                + table
                                + " GROUP BY level_id,question_number HAVING COUNT(*)>1) d",
                        "Duplicate question positions: " + table);
        if (exists("test_questions"))
            reject(
                    "SELECT COUNT(*) FROM (SELECT part_id,question_number FROM test_questions GROUP"
                            + " BY part_id,question_number HAVING COUNT(*)>1) d",
                    "Duplicate test question positions");
        if (exists("reading_questions"))
            reject(
                    "SELECT COUNT(*) FROM reading_questions q JOIN reading_passages p ON"
                            + " p.id=q.passage_id WHERE q.level_id<>p.level_id",
                    "Reading question and passage belong to different levels");
        if (exists("test_questions") && exists("reading_passages"))
            reject(
                    "SELECT COUNT(*) FROM test_questions q JOIN test_parts t ON t.id=q.part_id JOIN"
                            + " reading_passages p ON p.id=q.passage_id JOIN reading_levels l ON"
                            + " l.id=p.level_id JOIN reading_parts r ON r.id=l.part_id WHERE"
                            + " t.part_number<>r.part_number",
                    "Test passage belongs to another Part");
    }

    private void reject(String sql, String message) {
        if (jdbc.queryForObject(sql, Long.class) > 0) throw new IllegalStateException(message);
    }

    private void normalizeLegacyTimestamps() throws SQLException {
        if (legacyZone.equals(ZoneOffset.UTC) || legacyZone.getId().equals("UTC")) return;
        List<String> targets =
                List.of(
                        "users",
                        "invalidated_tokens",
                        "question_groups",
                        "questions",
                        "practice_sets",
                        "user_question_progress",
                        "test_suites",
                        "test_parts",
                        "test_attempts",
                        "test_attempt_items",
                        "vocabulary_categories",
                        "vocabulary_sets",
                        "vocabulary_words",
                        "vocabulary_synonyms",
                        "user_vocabulary_collection",
                        "user_vocabulary_items",
                        "user_vocabulary_progress");
        for (String table : targets) {
            List<String> dates = new ArrayList<>();
            try (ResultSet columns =
                    connection
                            .getMetaData()
                            .getColumns(
                                    connection.getCatalog(), connection.getSchema(), table, "%")) {
                while (columns.next())
                    if (columns.getInt("DATA_TYPE") == Types.TIMESTAMP)
                        dates.add(columns.getString("COLUMN_NAME"));
            }
            if (dates.isEmpty()) continue;
            String updates = String.join(",", dates.stream().map(c -> c + "=?").toList());
            jdbc.query(
                    "SELECT id," + String.join(",", dates) + " FROM " + table,
                    (org.springframework.jdbc.core.RowCallbackHandler)
                            rs -> {
                                List<Object> args = new ArrayList<>();
                                for (String column : dates) {
                                    LocalDateTime local = rs.getObject(column, LocalDateTime.class);
                                    args.add(
                                            local == null
                                                    ? null
                                                    : local.atZone(legacyZone)
                                                            .withZoneSameInstant(ZoneOffset.UTC)
                                                            .toLocalDateTime());
                                }
                                args.add(rs.getObject("id"));
                                jdbc.update(
                                        "UPDATE " + table + " SET " + updates + " WHERE id=?",
                                        args.toArray());
                            });
        }
        jdbc.update(
                "INSERT INTO migration_issues(source_type,detail) VALUES('time_zone',?)",
                "Legacy timestamps interpreted as " + legacyZone + " and converted to UTC");
    }

    private void copyIdentityAndVocabulary() {
        copy(
                "users",
                "id,username,password,email,full_name,role,enabled,auth_version,created_at,updated_at",
                "id,TRIM(username),password,LOWER(TRIM(email)),full_name,role,COALESCE(enabled,TRUE),0,created_at,updated_at");
        // Existing JWTs intentionally become invalid after the cutover (issuer/authVersion are
        // required).
        copy("invalidated_tokens", "id,expiry_time", "id,expiry_time");
        copy(
                "vocabulary_categories",
                "id,category_name,description,created_by,created_at,updated_at",
                "id,category_name,description,created_by,created_at,updated_at");
        copy(
                "vocabulary_sets",
                "id,category_id,set_name,description,created_at,updated_at",
                "id,category_id,set_name,description,created_at,updated_at");
        copy(
                "vocabulary_words",
                "id,word,meaning,part_of_speech,example_sentence,example_translation,pronunciation,created_at,updated_at",
                "id,word,meaning,part_of_speech,example_sentence,example_translation,pronunciation,created_at,updated_at");
        if (exists("legacy_vocabulary_words"))
            jdbc.update(
                    "INSERT INTO vocabulary_word_sets(word_id,set_id) SELECT id,set_id FROM"
                            + " legacy_vocabulary_words");
        copy(
                "vocabulary_synonyms",
                "id,word_id,synonym_phrase,created_at",
                "id,word_id,synonym_phrase,created_at");
        copy(
                "user_vocabulary_collection",
                "id,user_id,set_name,is_default,created_at",
                "id,user_id,set_name,is_default,created_at");
        if (exists("legacy_user_vocabulary_items"))
            jdbc.update(
                    "INSERT INTO user_vocabulary_items(id,collection_id,word_id,added_at) SELECT"
                        + " i.id,i.collection_id,i.word_id,i.added_at FROM"
                        + " legacy_user_vocabulary_items i WHERE i.id=(SELECT MAX(j.id) FROM"
                        + " legacy_user_vocabulary_items j WHERE j.collection_id=i.collection_id"
                        + " AND j.word_id=i.word_id)");
        if (exists("legacy_user_vocabulary_progress"))
            jdbc.update(
                    "INSERT INTO"
                        + " user_vocabulary_progress(id,user_id,word_id,status,last_reviewed,review_count)"
                        + " SELECT p.id,p.user_id,p.word_id,p.status,p.last_reviewed,p.review_count"
                        + " FROM legacy_user_vocabulary_progress p WHERE p.id=(SELECT MAX(j.id)"
                        + " FROM legacy_user_vocabulary_progress j WHERE j.user_id=p.user_id AND"
                        + " j.word_id=p.word_id)");
    }

    private void copySets() {
        for (String section : List.of("listening", "reading")) {
            if (!exists("legacy_" + section + "_levels")) continue;
            for (var p : jdbc.queryForList("SELECT * FROM legacy_" + section + "_parts")) {
                if (p.get("description") != null)
                    jdbc.update(
                            "UPDATE toeic_parts SET description=? WHERE id=?",
                            p.get("description"),
                            p.get("part_number"));
                remember(section + "_part", number(p, "id"), number(p, "part_number"));
            }
            for (var row :
                    jdbc.queryForList(
                            "SELECT l.*,p.part_number FROM legacy_"
                                    + section
                                    + "_levels l JOIN legacy_"
                                    + section
                                    + "_parts p ON p.id=l.part_id ORDER BY l.id")) {
                long id =
                        insert(
                                "INSERT INTO"
                                    + " practice_sets(part_id,level_number,level_name,audio_url,created_at,updated_at)"
                                    + " VALUES(?,?,?,?,?,?)",
                                row.get("part_number"),
                                row.get("level_number"),
                                row.get("level_name"),
                                row.get("audio_url"),
                                row.get("created_at"),
                                row.get("created_at"));
                remember(section + "_level", number(row, "id"), id);
            }
        }
        if (exists("legacy_reading_passages"))
            for (var row :
                    jdbc.queryForList(
                            "SELECT p.*,r.part_number FROM legacy_reading_passages p JOIN"
                                    + " legacy_reading_levels l ON l.id=p.level_id JOIN"
                                    + " legacy_reading_parts r ON r.id=l.part_id ORDER BY p.id")) {
                long group = group(row, ((Number) row.get("part_number")).intValue());
                remember("reading_passage", number(row, "id"), group);
                jdbc.update(
                        "INSERT INTO group_documents(group_id,position,title,content)"
                                + " VALUES(?,0,?,?)",
                        group,
                        row.get("passage_title"),
                        row.get("passage_content"));
            }
    }

    private void copyContent() throws Exception {
        for (String section : List.of("listening", "reading", "test")) {
            String table = section.equals("test") ? "test_questions" : section + "_questions";
            if (!exists("legacy_" + table)) continue;
            String join =
                    section.equals("test")
                            ? " JOIN legacy_test_parts p ON p.id=q.part_id"
                            : " JOIN legacy_"
                                    + section
                                    + "_levels l ON l.id=q.level_id JOIN legacy_"
                                    + section
                                    + "_parts p ON p.id=l.part_id";
            for (var row :
                    jdbc.queryForList(
                            "SELECT q.*,p.part_number FROM legacy_"
                                    + table
                                    + " q"
                                    + join
                                    + " ORDER BY q.id")) {
                int part = ((Number) row.get("part_number")).intValue();
                Long g =
                        row.get("passage_id") == null
                                ? null
                                : mapped("reading_passage", number(row, "passage_id"));
                if (g != null) {
                    Integer passagePart =
                            jdbc.queryForObject(
                                    "SELECT part_id FROM question_groups WHERE id=?",
                                    Integer.class,
                                    g);
                    if (passagePart != part)
                        throw new IllegalStateException(
                                "Passage crosses TOEIC parts at " + table + ":" + row.get("id"));
                }
                if (g == null) g = group(row, part);
                int position = ((Number) row.get("question_number")).intValue();
                String correct = String.valueOf(row.get("correct_answer")).toUpperCase(Locale.ROOT);
                boolean invalid = !correct.matches(part == 2 ? "[A-C]" : "[A-D]");
                long q =
                        insert(
                                "INSERT INTO"
                                    + " questions(group_id,position,question_text,prompt_text,explanation,hide_question,archived,created_at,updated_at)"
                                    + " VALUES(?,?,?,?,?,?,?,?,?)",
                                g,
                                position,
                                row.get("question_text"),
                                row.get("prompt_text"),
                                row.get("explanation"),
                                Boolean.TRUE.equals(row.get("hide_question")) || part == 2,
                                invalid,
                                row.get("created_at"),
                                row.get("created_at"));
                remember(section + "_question", number(row, "id"), q);
                List<Map<String, Object>> options = new ArrayList<>();
                for (int n = 0; n < (part == 2 && !invalid ? 3 : 4); n++) {
                    String label = String.valueOf((char) ('A' + n));
                    Object text = row.get("option_" + label.toLowerCase(Locale.ROOT));
                    jdbc.update(
                            "INSERT INTO"
                                + " question_options(question_id,position,label,content,correct)"
                                + " VALUES(?,?,?,?,?)",
                            q,
                            n,
                            label,
                            text,
                            label.equals(correct));
                    options.add(option(label, text));
                }
                if (invalid)
                    issue(
                            section + "_question",
                            number(row, "id"),
                            "Invalid legacy answer; canonical question archived for editorial"
                                    + " repair. Original retained.");
                if (part == 3 || part == 4)
                    issue(
                            section + "_question",
                            number(row, "id"),
                            "Legacy audio grouping is unknown. Curate group context before using"
                                    + " this content in a timed test.");
                if (!section.equals("test"))
                    jdbc.update(
                            "INSERT INTO practice_set_items(set_id,question_id,position)"
                                    + " VALUES(?,?,?)",
                            mapped(section + "_level", number(row, "level_id")),
                            q,
                            position);
                // Snapshot source data for migration of old results, including invalid/archived
                // questions.
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", q);
                snapshot.put("questionNumber", position);
                snapshot.put("partNumber", part);
                snapshot.put("questionText", row.get("question_text"));
                snapshot.put("correctAnswer", correct);
                snapshot.put("explanation", row.get("explanation"));
                snapshot.put("imageUrl", row.get("image_url"));
                snapshot.put("audioUrl", row.get("audio_url"));
                snapshot.put("options", options);
                for (int n = 0; n < 4; n++)
                    snapshot.put(
                            "option" + (char) ('A' + n), row.get("option_" + (char) ('a' + n)));
                var gr = jdbc.queryForMap("SELECT * FROM question_groups WHERE id=?", g);
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("id", g);
                group.put("title", gr.get("title"));
                group.put("audioUrl", gr.get("audio_url"));
                group.put("imageUrl", gr.get("image_url"));
                group.put(
                        "documents",
                        jdbc.query(
                                "SELECT title,content,image_url FROM group_documents WHERE"
                                        + " group_id=? ORDER BY position",
                                (rs, i) -> {
                                    Map<String, Object> d = new LinkedHashMap<>();
                                    d.put("title", rs.getString(1));
                                    d.put("content", rs.getString(2));
                                    d.put("imageUrl", rs.getString(3));
                                    return d;
                                },
                                g));
                snapshot.put("group", group);
                snapshots.put(q, json.writeValueAsString(snapshot));
            }
        }
    }

    private final Map<Long, String> snapshots = new HashMap<>();

    private Map<String, Object> option(String label, Object text) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("label", label);
        o.put("content", text);
        return o;
    }

    private void copyProgress() {
        for (String section : List.of("listening", "reading")) {
            String table = "legacy_" + section + "_user_progress";
            if (!exists(table)) continue;
            for (var p :
                    jdbc.queryForList(
                            "SELECT * FROM (SELECT p.*,ROW_NUMBER() OVER(PARTITION BY"
                                + " user_id,question_id ORDER BY last_attempted DESC,id DESC) rn"
                                + " FROM "
                                    + table
                                    + " p) ranked WHERE rn=1")) {
                long q = mapped(section + "_question", number(p, "question_id"));
                Object answer = p.get("selected_answer");
                String status = String.valueOf(p.get("status"));
                var correctLabels =
                        jdbc.queryForList(
                                "SELECT label FROM question_options WHERE question_id=? AND"
                                        + " correct=TRUE",
                                String.class,
                                q);
                String correct = correctLabels.isEmpty() ? "" : correctLabels.getFirst();
                Boolean ok = answer == null ? null : correct.equals(answer);
                Object last = answer == null ? null : p.get("last_attempted");
                jdbc.update(
                        "INSERT INTO"
                            + " user_question_progress(user_id,question_id,selected_answer,correct,bookmarked,attempt_count,last_answered_at)"
                            + " VALUES(?,?,?,?,?,?,?)",
                        p.get("user_id"),
                        q,
                        answer,
                        ok,
                        status.equals("MARKED"),
                        answer == null ? 0 : 1,
                        last);
            }
        }
    }

    private void copyTests() {
        copy(
                "test_suites",
                "id,suite_name,description,source,created_at,updated_at",
                "id,suite_name,description,source,created_at,created_at");
        copy(
                "test_parts",
                "id,suite_id,toeic_part_id,time_limit,audio_url,created_at,updated_at",
                "id,suite_id,part_number,time_limit,audio_url,created_at,created_at");
        if (exists("legacy_test_questions"))
            for (var q : jdbc.queryForList("SELECT * FROM legacy_test_questions"))
                jdbc.update(
                        "INSERT INTO test_part_items(part_id,question_id,position) VALUES(?,?,?)",
                        q.get("part_id"),
                        mapped("test_question", number(q, "id")),
                        q.get("question_number"));
        if (!exists("legacy_user_test_results")) return;
        for (var r :
                jdbc.queryForList(
                        "SELECT r.*,p.part_number,p.time_limit,p.audio_url,s.suite_name FROM"
                            + " legacy_user_test_results r JOIN legacy_test_parts p ON"
                            + " p.id=r.part_id JOIN legacy_test_suites s ON s.id=p.suite_id ORDER"
                            + " BY r.id")) {
            Timestamp started = (Timestamp) r.get("started_at");
            if (started == null)
                throw new IllegalStateException("Missing legacy attempt start: " + r.get("id"));
            jdbc.update(
                    "INSERT INTO"
                        + " test_attempts(id,user_id,part_id,status,started_at,expires_at,submitted_at,total_count,time_spent,correct_count,accuracy_percent,part_number,suite_name,audio_url)"
                        + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    r.get("id"),
                    r.get("user_id"),
                    r.get("part_id"),
                    r.get("status"),
                    started,
                    Timestamp.valueOf(
                            started.toLocalDateTime().plusMinutes(number(r, "time_limit"))),
                    r.get("submitted_at"),
                    r.get("total_count"),
                    r.get("time_spent"),
                    r.get("correct_count"),
                    r.get("score"),
                    r.get("part_number"),
                    r.get("suite_name"),
                    r.get("audio_url"));
            Map<Long, Map<String, Object>> answers = new HashMap<>();
            if (exists("legacy_user_test_answers"))
                for (var a :
                        jdbc.queryForList(
                                "SELECT * FROM legacy_user_test_answers WHERE result_id=? ORDER BY"
                                        + " id",
                                r.get("id"))) answers.put(number(a, "question_id"), a);
            for (var q :
                    jdbc.queryForList(
                            "SELECT * FROM legacy_test_questions WHERE part_id=? ORDER BY"
                                    + " question_number",
                            r.get("part_id"))) {
                var a = answers.get(number(q, "id"));
                long mapped = mapped("test_question", number(q, "id"));
                jdbc.update(
                        "INSERT INTO"
                            + " test_attempt_items(attempt_id,question_id,position,snapshot,selected_answer,correct,answered_at)"
                            + " VALUES(?,?,?,?,?,?,?)",
                        r.get("id"),
                        mapped,
                        q.get("question_number"),
                        snapshots.get(mapped),
                        a == null ? null : a.get("selected_answer"),
                        a == null ? null : a.get("is_correct"),
                        a == null ? null : a.get("answered_at"));
            }
            issue(
                    "test_result",
                    number(r, "id"),
                    "Snapshot reconstructed from content available at migration. Earlier edits"
                            + " cannot be recovered; original legacy result/answers retained.");
        }
    }

    private long group(Map<String, Object> r, int part) {
        return insert(
                "INSERT INTO"
                    + " question_groups(part_id,title,audio_url,image_url,created_at,updated_at)"
                    + " VALUES(?,?,?,?,?,?)",
                part,
                r.get("passage_title"),
                r.get("audio_url"),
                r.get("image_url"),
                r.get("created_at"),
                r.get("created_at"));
    }

    private void remember(String type, long old, long id) {
        ids.put(type + ":" + old, id);
        jdbc.update(
                "INSERT INTO legacy_content_map(source_type,legacy_id,new_id) VALUES(?,?,?)",
                type,
                old,
                id);
    }

    private long mapped(String type, long old) {
        Long id = ids.get(type + ":" + old);
        if (id == null)
            throw new IllegalStateException("Missing legacy mapping " + type + ":" + old);
        return id;
    }

    private void issue(String type, long id, String detail) {
        jdbc.update(
                "INSERT INTO migration_issues(source_type,legacy_id,detail) VALUES(?,?,?)",
                type,
                id,
                detail);
    }

    private long number(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private long insert(String sql, Object... args) {
        try (PreparedStatement s =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
            s.executeUpdate();
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key");
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void copy(String table, String columns, String expression) {
        if (exists("legacy_" + table))
            jdbc.update(
                    "INSERT INTO "
                            + table
                            + "("
                            + columns
                            + ") SELECT "
                            + expression
                            + " FROM legacy_"
                            + table);
    }

    private boolean exists(String table) {
        try (ResultSet rs =
                connection
                        .getMetaData()
                        .getTables(
                                connection.getCatalog(),
                                connection.getSchema(),
                                "%",
                                new String[] {"TABLE"})) {
            while (rs.next()) if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) return true;
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
