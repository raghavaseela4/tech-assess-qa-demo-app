package com.example.demo.adapter.in.web.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that manages correlation IDs for distributed tracing.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Checks for existing X-Correlation-ID header in incoming requests</li>
 *   <li>Generates new UUID if correlation ID is missing</li>
 *   <li>Stores correlation ID in MDC (Mapped Diagnostic Context) for automatic log enrichment</li>
 *   <li>Adds X-Correlation-ID header to response for client tracking</li>
 *   <li>Ensures MDC cleanup after request completes</li>
 * </ul>
 *
 * <p>MDC Key: "correlationId"
 * <p>HTTP Header: "X-Correlation-ID"
 *
 * @see org.slf4j.MDC
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Check for existing correlation ID in request header
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);

            // Generate new UUID if correlation ID is missing
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Store correlation ID in MDC for automatic inclusion in all log statements
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Add correlation ID to response header for client tracking
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // CRITICAL: Clear MDC to prevent thread-local memory leak
            // (Tomcat/Jetty reuse threads from pool)
            MDC.clear();
        }
    }
}
