package com.medcore.hms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Drops legacy columns left behind when Patient was refactored off User linkage.
 * Runs before DataSeeder so seed inserts match the current entity model.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class LegacySchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        dropColumnIfExists("patient", "user_id");
        dropColumnIfExists("patient", "deleted_at");
        dropColumnIfExists("doctor", "experience_years");
    }

    private void dropColumnIfExists(String table, String column) {
        try {
            Boolean exists = jdbcTemplate.queryForObject(
                    """
                    SELECT EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = ?
                          AND column_name = ?
                    )
                    """,
                    Boolean.class,
                    table,
                    column);
            if (Boolean.TRUE.equals(exists)) {
                jdbcTemplate.execute("ALTER TABLE " + table + " DROP COLUMN " + column);
                log.info("Dropped legacy column {}.{}", table, column);
            }
        } catch (Exception e) {
            log.warn("Could not drop {}.{}: {}", table, column, e.getMessage());
        }
    }
}
