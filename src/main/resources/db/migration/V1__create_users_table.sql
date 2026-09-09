CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    document_number VARCHAR(14) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_document_number UNIQUE (document_number),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_document_number ON users (document_number);
CREATE INDEX idx_users_email ON users (email);
