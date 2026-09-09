package com.portfolio.wallet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe sliding window rate limiter filter to protect financial endpoints
 * against Denial of Service (DoS), brute-force, and automated credential abuse.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_DURATION_MILLIS = 60_000L;
    private static final String RATE_LIMIT_ERROR_URI = "https://api.wallet.com/errors/rate-limit-exceeded";

    private final Map<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }
        // Exclude Swagger UI, OpenAPI docs and healthchecks from rate limiting
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/favicon.ico") ||
               !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        long now = System.currentTimeMillis();

        // Prune old entries if map grows excessively (memory exhaustion defense)
        if (clientWindows.size() > 5000) {
            clientWindows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_DURATION_MILLIS);
        }

        ClientWindow window = clientWindows.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_DURATION_MILLIS) {
                return new ClientWindow(now, new AtomicInteger(1));
            }
            existing.requestCount.incrementAndGet();
            return existing;
        });

        int currentCount = window.requestCount.get();
        int remaining = Math.max(0, MAX_REQUESTS_PER_MINUTE - currentCount);

        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            long retryAfterSeconds = Math.max(1, (WINDOW_DURATION_MILLIS - (now - window.windowStart)) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Limite de requisições excedido. Limite configurado: " + MAX_REQUESTS_PER_MINUTE + " requisições por minuto."
            );
            problemDetail.setTitle("Limite de Requisições Excedido");
            problemDetail.setType(URI.create(RATE_LIMIT_ERROR_URI));
            problemDetail.setInstance(URI.create(request.getRequestURI()));
            problemDetail.setProperty("code", "RATE_LIMIT_EXCEEDED");
            problemDetail.setProperty("retryAfterSeconds", retryAfterSeconds);
            problemDetail.setProperty("timestamp", Instant.now().toString());

            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            if (correlationId != null) {
                problemDetail.setProperty("correlationId", correlationId);
            }

            objectMapper.writeValue(response.getWriter(), problemDetail);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static class ClientWindow {
        final long windowStart;
        final AtomicInteger requestCount;

        ClientWindow(long windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
