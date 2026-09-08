package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String authHeader = httpRequest.getHeader("Authorization");

        LOG.debug("========================================");
        LOG.debug("CLAIMS SERVICE - Incoming Request:");
        LOG.debug("  Method: {}", method);
        LOG.debug("  URI: {}", uri);
        LOG.debug("  Authorization header present: {}", authHeader != null);
        if (authHeader != null) {
            LOG.debug("  Authorization header (first 50 chars): {}",
                authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader);
        }
        LOG.debug("  Remote Address: {}", httpRequest.getRemoteAddr());

        // Log all headers
        LOG.debug("  Headers:");
        Collections.list(httpRequest.getHeaderNames()).forEach(headerName ->
            LOG.debug("    {}: {}", headerName, httpRequest.getHeader(headerName))
        );

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            LOG.debug("CLAIMS SERVICE - Response:");
            LOG.debug("  Status: {}", httpResponse.getStatus());
            LOG.debug("  Duration: {}ms", duration);
            LOG.debug("========================================");
        }
    }
}
