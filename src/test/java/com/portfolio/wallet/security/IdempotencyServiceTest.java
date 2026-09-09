package com.portfolio.wallet.security;

import com.portfolio.wallet.exception.IdempotencyConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
    }

    @Test
    @DisplayName("Should execute operation normally when idempotency key is null or blank")
    void shouldExecuteWithoutKey() {
        AtomicInteger counter = new AtomicInteger(0);

        String result1 = idempotencyService.execute(null, () -> "res-" + counter.incrementAndGet());
        String result2 = idempotencyService.execute("   ", () -> "res-" + counter.incrementAndGet());

        assertThat(result1).isEqualTo("res-1");
        assertThat(result2).isEqualTo("res-2");
        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return cached response on duplicate request with same idempotency key")
    void shouldReturnCachedResponseOnDuplicateKey() {
        AtomicInteger executions = new AtomicInteger(0);
        String key = "tx-key-12345";

        String firstResponse = idempotencyService.execute(key, () -> {
            executions.incrementAndGet();
            return "SUCCESS_TX_1";
        });

        String secondResponse = idempotencyService.execute(key, () -> {
            executions.incrementAndGet();
            return "SUCCESS_TX_2";
        });

        assertThat(firstResponse).isEqualTo("SUCCESS_TX_1");
        assertThat(secondResponse).isEqualTo("SUCCESS_TX_1"); // Returns original cached result
        assertThat(executions.get()).isEqualTo(1); // Underlying business logic only ran ONCE!
    }

    @Test
    @DisplayName("Should reject key exceeding maximum allowed length")
    void shouldRejectOversizedKey() {
        String oversizedKey = "A".repeat(129);

        assertThatThrownBy(() -> idempotencyService.execute(oversizedKey, () -> "OK"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no máximo 128 caracteres");
    }

    @Test
    @DisplayName("Should allow retry if initial execution failed with an exception")
    void shouldAllowRetryAfterFailure() {
        String key = "failing-key-999";
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> idempotencyService.execute(key, () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("Simulated database timeout");
        })).isInstanceOf(IllegalStateException.class);

        // Subsequent retry with the same key should be allowed to run
        String retryResult = idempotencyService.execute(key, () -> {
            attempts.incrementAndGet();
            return "RETRY_SUCCESS";
        });

        assertThat(retryResult).isEqualTo("RETRY_SUCCESS");
        assertThat(attempts.get()).isEqualTo(2);
    }
}
