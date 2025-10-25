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
    private static final String TABLE_EXISTS_SQL = "SELECT COUNT(*) FROM information_schema.TABLES "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureSchema() {
        ensureColumn("tsuki_application", "decision_note",
                "ALTER TABLE tsuki_application ADD COLUMN decision_note VARCHAR(500) NULL AFTER status");
        ensureResumeAttachmentTable();
    }

    private void ensureColumn(String table, String column, String ddl) {
        Integer count = jdbcTemplate.queryForObject(COLUMN_EXISTS_SQL, Integer.class, table, column);
        if (count == null || count == 0) {
            log.info("Adding missing column {}.{}", table, column);
            jdbcTemplate.execute(ddl);
        }
    }

    private void ensureResumeAttachmentTable() {
        Integer exists = jdbcTemplate.queryForObject(TABLE_EXISTS_SQL, Integer.class, "tsuki_resume_attachment");
        if (exists == null || exists == 0) {
            log.info("Creating table tsuki_resume_attachment for storing resume attachments");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tsuki_resume_attachment ("
                    + "attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',"
                    + "student_id BIGINT NOT NULL COMMENT '所属学生ID',"
                    + "resume_id BIGINT DEFAULT NULL COMMENT '关联简历ID',"
                    + "file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',"
                    + "file_path VARCHAR(255) NOT NULL COMMENT '存储路径',"
                    + "content_type VARCHAR(100) DEFAULT NULL COMMENT '文件类型',"
                    + "file_size BIGINT DEFAULT NULL COMMENT '文件大小(字节)',"
                    + "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',"
                    + "UNIQUE KEY uk_resume_attachment_path (file_path),"
                    + "FOREIGN KEY (student_id) REFERENCES tsuki_student(student_id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (resume_id) REFERENCES tsuki_resume(resume_id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历附件表'");
        }
        jdbcTemplate.execute("ALTER TABLE tsuki_resume_attachment COMMENT '学生简历附件表'");
    }
}

