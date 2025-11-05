-- Schema alignment for updated JPA entities (executed manually on tsuki_campus database)

-- Ensure additional decision notes for applications
ALTER TABLE tsuki_application
    ADD COLUMN IF NOT EXISTS decision_note VARCHAR(500) NULL AFTER status;

-- Ensure wallet tracking for companies
ALTER TABLE tsuki_company
    ADD COLUMN IF NOT EXISTS wallet_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER audit_reason;

-- Support threaded discussions
ALTER TABLE tsuki_discussion_post
    ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL AFTER reviewer_id;

ALTER TABLE tsuki_discussion_post
    ADD CONSTRAINT IF NOT EXISTS fk_discussion_parent
        FOREIGN KEY (parent_id) REFERENCES tsuki_discussion_post (discussion_id)
        ON DELETE SET NULL ON UPDATE CASCADE;

CREATE INDEX IF NOT EXISTS idx_discussion_parent
    ON tsuki_discussion_post (parent_id);

-- Company invite management
CREATE TABLE IF NOT EXISTS tsuki_company_invite (
    invite_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    admin_id BIGINT NOT NULL,
    note VARCHAR(255) NULL,
    company_name_hint VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_time DATETIME NULL,
    CONSTRAINT uk_company_invite_code UNIQUE (code),
    CONSTRAINT fk_company_invite_admin FOREIGN KEY (admin_id)
        REFERENCES tsuki_admin (admin_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Resume attachment library
CREATE TABLE IF NOT EXISTS tsuki_resume_attachment (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    resume_id BIGINT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NULL,
    file_size BIGINT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_attachment_student FOREIGN KEY (student_id)
        REFERENCES tsuki_student (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_resume_attachment_resume FOREIGN KEY (resume_id)
        REFERENCES tsuki_resume (resume_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resume_attachment_student
    ON tsuki_resume_attachment (student_id);

CREATE INDEX IF NOT EXISTS idx_resume_attachment_resume
    ON tsuki_resume_attachment (resume_id);

-- Student resume uploads for quick apply
CREATE TABLE IF NOT EXISTS tsuki_resume_upload (
    upload_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'company_only',
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_upload_student FOREIGN KEY (student_id)
        REFERENCES tsuki_student (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resume_upload_student
    ON tsuki_resume_upload (student_id);

-- Wallet summary per owner
CREATE TABLE IF NOT EXISTS tsuki_wallet (
    wallet_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallet_owner UNIQUE (owner_id, owner_type)
);

-- Detailed wallet transactions
CREATE TABLE IF NOT EXISTS tsuki_wallet_transaction (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255) NULL,
    reference_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_transaction_wallet FOREIGN KEY (wallet_id)
        REFERENCES tsuki_wallet (wallet_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wallet_transaction_wallet
    ON tsuki_wallet_transaction (wallet_id);

-- Keep wallet balances in sync with company records
UPDATE tsuki_company c
LEFT JOIN tsuki_wallet w ON w.owner_id = c.company_id AND w.owner_type = 'company'
SET c.wallet_balance = COALESCE(w.balance, 0.00)
WHERE c.wallet_balance IS NULL;
