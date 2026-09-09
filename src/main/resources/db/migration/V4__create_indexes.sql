CREATE INDEX idx_transactions_target_created ON transactions (target_wallet_id, created_at DESC);
CREATE INDEX idx_transactions_source_created ON transactions (source_wallet_id, created_at DESC);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
