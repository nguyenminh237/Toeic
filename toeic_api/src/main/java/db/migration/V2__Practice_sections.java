package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.*;

/** Preserve practice-set IDs and memberships while replacing bounded levels with named sections. */
public class V2__Practice_sections extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection c = context.getConnection();
        boolean mysql =
                c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("mysql");
        String schema = mysql ? c.getCatalog() : c.getSchema();
        List<String> checks = new ArrayList<>();
        try (PreparedStatement s =
                c.prepareStatement(
                        "SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE"
                            + " LOWER(TABLE_NAME)='practice_sets' AND CONSTRAINT_TYPE='CHECK' AND"
                            + " TABLE_SCHEMA=?")) {
            s.setString(1, schema);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) checks.add(r.getString(1));
            }
        }
        try (Statement s = c.createStatement()) {
            // The old composite unique key may also be the supporting index for the Part FK.
            s.execute("CREATE INDEX ix_practice_part ON practice_sets(part_id,archived,id)");
            for (String check : checks) {
                String quoted =
                        mysql
                                ? "`" + check.replace("`", "``") + "`"
                                : "\"" + check.replace("\"", "\"\"") + "\"";
                s.execute(
                        "ALTER TABLE practice_sets DROP "
                                + (mysql ? "CHECK " : "CONSTRAINT ")
                                + quoted);
            }
            s.execute(
                    mysql
                            ? "ALTER TABLE practice_sets DROP INDEX uk_set_part_level"
                            : "ALTER TABLE practice_sets DROP CONSTRAINT uk_set_part_level");
            s.execute("ALTER TABLE practice_sets RENAME COLUMN level_name TO name");
            s.execute("ALTER TABLE practice_sets ADD COLUMN description LONGTEXT");
            s.execute("ALTER TABLE practice_sets DROP COLUMN level_number");
        }
    }
}
