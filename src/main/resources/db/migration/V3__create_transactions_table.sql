CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    source_wallet_id BIGINT NULL,
    target_wallet_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES wallets (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_target_wallet FOREIGN KEY (target_wallet_id) REFERENCES wallets (id) ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0.00),
    CONSTRAINT chk_transaction_different_wallets CHECK (source_wallet_id IS NULL OR source_wallet_id <> target_wallet_id),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'TRANSFER')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('COMPLETED', 'FAILED'))
);
