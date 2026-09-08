package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filter that extracts JWT from access_token cookie and adds it as Authorization Bearer header
 * for Spring Security OAuth2 Resource Server to process.
 */
@Component
public class CookieAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip if Authorization header already present
        if (request.getHeader(AUTHORIZATION_HEADER) != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract access_token from cookies
        String accessToken = extractAccessTokenFromCookies(request);

        if (accessToken != null) {
            // Wrap request to add Authorization header
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if (AUTHORIZATION_HEADER.equalsIgnoreCase(name)) {
                        return BEARER_PREFIX + accessToken;
                    }
                    return super.getHeader(name);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
