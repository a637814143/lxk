package com.example.campus.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    private static final String COLUMN_EXISTS_SQL = "SELECT COUNT(*) FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureSchema() {
        ensureColumn("tsuki_application", "decision_note",
                "ALTER TABLE tsuki_application ADD COLUMN decision_note VARCHAR(500) NULL AFTER status");
    }

    private void ensureColumn(String table, String column, String ddl) {
        Integer count = jdbcTemplate.queryForObject(COLUMN_EXISTS_SQL, Integer.class, table, column);
        if (count == null || count == 0) {
            log.info("Adding missing column {}.{}", table, column);
            jdbcTemplate.execute(ddl);
        }
    }
}

