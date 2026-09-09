package com.portfolio.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @Test
    @DisplayName("Should generate new correlation ID when request header is absent")
    void shouldGenerateCorrelationIdWhenAbsent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();

        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));

        filter.doFilter(request, response, chain);

        String headerValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(headerValue).isNotBlank();
        assertThat(capturedMdc.get()).isEqualTo(headerValue);
        // MDC must be cleared after request finishes
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should preserve valid client provided correlation ID")
    void shouldPreserveValidCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String customId = "REQ-TRACE-987654";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, customId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();

        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(customId);
        assertThat(capturedMdc.get()).isEqualTo(customId);
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Should sanitize/regenerate correlation ID if contains malicious injection characters")
    void shouldSanitizeMaliciousCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String maliciousId = "header\r\nInjected-Header: evil<script>";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, maliciousId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String generatedId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotEqualTo(maliciousId);
        assertThat(generatedId).matches("^[a-zA-Z0-9-]{36}$"); // Valid UUID
    }
}
