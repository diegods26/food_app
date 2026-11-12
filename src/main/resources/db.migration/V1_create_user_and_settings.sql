CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified_at TIMESTAMPZ,
    created_at TIMESTAMPZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_cpf UNIQUE (cpf),
    CONSTRAINT ck_users_full_name_length CHECK (char_length(btrim(full_name)) BETWEEN 2 AND 120),
    CONSTRAINT ck_users_cpf_length CHECK (char_length(btrim(cpf)) = 11),
    CONSTRAINT ck_users_email_not_blank CHECK (char_length(btrim(email)) > 0),
    CONSTRAINT ck_users_password_hash_not_blank CHECK (char_length(btrim(password_hash)) > 0)
);
CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    time_zone VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo',
    currency CHAR(3) NOT NULL DEFAULT 'BRL',
    locale VARCHAR(16) NOT NULL DEFAULT 'pt-BR',
    created_at TIMESTAMPZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_settings_currency CHECK (currency = 'BRL'),
    CONSTRAINT ck_user_settings_locale CHECK (locale = 'pt-BR')
);