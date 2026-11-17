CREATE TABLE accounts
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_accounts_email UNIQUE (email)
);

CREATE TABLE account_roles
(
    account_id UUID        NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    role       VARCHAR(30) NOT NULL,
    PRIMARY KEY (account_id, role)
);

CREATE TABLE customer_profiles
(
    account_id UUID PRIMARY KEY REFERENCES accounts (id) ON DELETE CASCADE,
    full_name  VARCHAR(120) NOT NULL,
    cpf        VARCHAR(11)  NOT NULL,
    CONSTRAINT uk_customer_profiles_cpf UNIQUE (cpf)
);