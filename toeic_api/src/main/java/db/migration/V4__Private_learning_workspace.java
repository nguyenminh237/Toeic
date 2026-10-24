package db.migration;

import org.flywaydb.core.api.migration.*;

import java.sql.*;
import java.util.*;

/** Legacy records without a verifiable author remain unassigned and private. */
public class V4__Private_learning_workspace extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        boolean mysql =
                connection
                        .getMetaData()
                        .getDatabaseProductName()
                        .toLowerCase(Locale.ROOT)
                        .contains("mysql");
        String schema = mysql ? connection.getCatalog() : connection.getSchema();
        try (var statement = connection.createStatement()) {
            for (String table :
                    List.of(
                            "question_groups",
                            "practice_sets",
                            "vocabulary_words",
                            "test_suites")) {
                statement.execute(
                        "ALTER TABLE " + table + " ADD COLUMN owner_id VARCHAR(255) NULL");
                statement.execute(
                        "ALTER TABLE "
                                + table
                                + " ADD CONSTRAINT fk_"
                                + table
                                + "_owner FOREIGN KEY (owner_id) REFERENCES users(id)");
                statement.execute(
                        "CREATE INDEX ix_"
                                + table
                                + "_owner ON "
                                + table
                                + "(owner_id,archived,id)");
            }
        }
        // Category ownership already exists in created_by. Names are unique per author.
        var names = new ArrayList<String>();
        try (var statement =
                connection.prepareStatement(
                        "SELECT DISTINCT t.CONSTRAINT_NAME FROM"
                            + " information_schema.TABLE_CONSTRAINTS t JOIN"
                            + " information_schema.KEY_COLUMN_USAGE k ON"
                            + " k.CONSTRAINT_NAME=t.CONSTRAINT_NAME AND"
                            + " k.TABLE_SCHEMA=t.TABLE_SCHEMA AND k.TABLE_NAME=t.TABLE_NAME WHERE"
                            + " LOWER(t.TABLE_NAME)='vocabulary_categories' AND"
                            + " t.CONSTRAINT_TYPE='UNIQUE' AND LOWER(k.COLUMN_NAME)='category_name'"
                            + " AND t.TABLE_SCHEMA=?")) {
            statement.setString(1, schema);
            try (var rows = statement.executeQuery()) {
                while (rows.next()) names.add(rows.getString(1));
            }
        }
        try (var statement = connection.createStatement()) {
            for (var name : names) {
                String quoted =
                        mysql
                                ? "`" + name.replace("`", "``") + "`"
                                : "\"" + name.replace("\"", "\"\"") + "\"";
                statement.execute(
                        "ALTER TABLE vocabulary_categories DROP "
                                + (mysql ? "INDEX " : "CONSTRAINT ")
                                + quoted);
            }
            statement.execute(
                    "ALTER TABLE vocabulary_categories ADD CONSTRAINT uq_vocab_category_owner_name"
                        + " UNIQUE(created_by,category_name)");
        }
    }
}
