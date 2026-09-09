package com.portfolio.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that applies defense-in-depth HTTP security headers based on OWASP Top 10 recommendations.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Prevent MIME type sniffing attacks
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking by forbidding embedding in frames or iframes
        response.setHeader("X-Frame-Options", "DENY");

        // Content Security Policy (restricted resources, prevents XSS and framing)
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none';");

        // Disable legacy browser XSS auditor to prevent auditor vulnerabilities
        response.setHeader("X-XSS-Protection", "0");

        // Control referrer information leakage
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Restrict sensitive browser features/hardware APIs
        response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=(), payment=()");

        // Force HTTPS transport (HSTS)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Prevent intermediate proxy caching of sensitive financial API payloads
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        filterChain.doFilter(request, response);
    }
}
