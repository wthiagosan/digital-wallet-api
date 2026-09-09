package com.portfolio.wallet.security;

import com.portfolio.wallet.exception.IdempotencyConflictException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service providing idempotency guarantees for financial mutations (transfers and deposits),
 * preventing double-spending and duplicate executions due to network retries.
 */
@Service
public class IdempotencyService {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final long EXPIRATION_MILLIS = 24 * 60 * 60 * 1000L; // 24 hours

    private final Map<String, CachedOperation<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T execute(String key, Supplier<T> operation) {
        if (key == null || key.isBlank()) {
            return operation.get();
        }

        String sanitizedKey = key.trim();
        if (sanitizedKey.length() > 128) {
            throw new IllegalArgumentException("A chave de idempotência deve conter no máximo 128 caracteres.");
        }

        long now = System.currentTimeMillis();

        // Evict expired entries if cache grows
        if (cache.size() > 10000) {
            cache.entrySet().removeIf(entry -> now - entry.getValue().createdAt > EXPIRATION_MILLIS);
        }

        CachedOperation<?> existing = cache.get(sanitizedKey);
        if (existing != null && (now - existing.createdAt <= EXPIRATION_MILLIS)) {
            if (existing.inFlight) {
                throw new IdempotencyConflictException(sanitizedKey);
            }
            return (T) existing.result;
        }

        // Put in-flight placeholder atomically
        CachedOperation<T> inFlight = new CachedOperation<>(null, true, now);
        CachedOperation<?> previous = cache.putIfAbsent(sanitizedKey, inFlight);
        if (previous != null && (now - previous.createdAt <= EXPIRATION_MILLIS)) {
            if (previous.inFlight) {
                throw new IdempotencyConflictException(sanitizedKey);
            }
            return (T) previous.result;
        }

        try {
            T result = operation.get();
            cache.put(sanitizedKey, new CachedOperation<>(result, false, System.currentTimeMillis()));
            return result;
        } catch (Exception ex) {
            // Remove failed operation so client can safely retry
            cache.remove(sanitizedKey);
            throw ex;
        }
    }

    private static class CachedOperation<T> {
        final T result;
        final boolean inFlight;
        final long createdAt;

        CachedOperation(T result, boolean inFlight, long createdAt) {
            this.result = result;
            this.inFlight = inFlight;
            this.createdAt = createdAt;
        }
    }
}
