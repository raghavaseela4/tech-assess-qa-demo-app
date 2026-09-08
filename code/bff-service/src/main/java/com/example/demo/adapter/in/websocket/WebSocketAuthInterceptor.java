package com.example.demo.adapter.in.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ROLES_ATTRIBUTE = "userRoles";

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null) {
            LOG.warn("WebSocket handshake rejected: no token provided");
            return false;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userId = jwt.getSubject();
            List<String> roles = extractRoles(jwt);

            attributes.put(USER_ID_ATTRIBUTE, userId);
            attributes.put(USER_ROLES_ATTRIBUTE, roles);

            LOG.debug("WebSocket handshake accepted for user: {}", userId);
            return true;
        } catch (JwtException exception) {
            LOG.warn("WebSocket handshake rejected: invalid token - {}", exception.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }

    private String extractToken(ServerHttpRequest request) {
        // First try to extract from cookies (preferred for security)
        String tokenFromCookie = extractTokenFromCookies(request);
        if (tokenFromCookie != null && !tokenFromCookie.isBlank()) {
            return tokenFromCookie;
        }

        // Fall back to query parameter (for compatibility)
        var queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String token = queryParams.getFirst("token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        return null;
    }

    private String extractTokenFromCookies(ServerHttpRequest request) {
        List<String> cookieHeaders = request.getHeaders().get("Cookie");
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            return null;
        }

        for (String cookieHeader : cookieHeaders) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String trimmed = cookie.trim();
                if (trimmed.startsWith("access_token=")) {
                    return trimmed.substring("access_token=".length());
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles != null ? roles : Collections.emptyList();
    }
}
