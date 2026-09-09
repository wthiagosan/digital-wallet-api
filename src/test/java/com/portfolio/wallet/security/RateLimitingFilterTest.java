package com.portfolio.wallet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new RateLimitingFilter(objectMapper);
    }

    @Test
    @DisplayName("Should permit requests within the rate limit and return tracking headers")
    void shouldPermitRequestsWithinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/wallets/1");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("100");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("99");
    }

    @Test
    @DisplayName("Should block and return 429 Too Many Requests when limit is exceeded")
    void shouldBlockWhenLimitExceeded() throws ServletException, IOException {
        String testIp = "10.0.0.55";

        // Exhaust the 100 requests allowance
        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/transfers");
            req.setRemoteAddr(testIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 101st request should be rejected with 429
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/v1/transfers");
        blockedReq.setRemoteAddr(testIp);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilter(blockedReq, blockedRes, new MockFilterChain());

        assertThat(blockedRes.getStatus()).isEqualTo(429);
        assertThat(blockedRes.getHeader("Retry-After")).isNotNull();
        assertThat(blockedRes.getContentType()).contains("application/problem+json");
        assertThat(blockedRes.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("Should bypass rate limiting for documentation endpoints")
    void shouldBypassSwaggerAndDocs() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        request.setRemoteAddr("10.0.0.1");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }
}
