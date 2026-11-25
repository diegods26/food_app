ALTER TABLE accounts
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE accounts
    ADD COLUMN locked_until TIMESTAMPTZ;

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_failed_login_attempts
        CHECK (failed_login_attempts >= 0);