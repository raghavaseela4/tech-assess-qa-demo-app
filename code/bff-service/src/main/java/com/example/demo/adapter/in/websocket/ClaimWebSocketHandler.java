package com.example.demo.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ClaimWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimWebSocketHandler.class);
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ROLES_ATTRIBUTE = "userRoles";

    private final Map<String, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ClaimWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId == null) {
            LOG.warn("WebSocket connection without userId, closing session: {}", session.getId());
            closeSession(session);
            return;
        }

        sessionsByUserId.computeIfAbsent(userId, key -> new CopyOnWriteArraySet<>()).add(session);
        LOG.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        if (userId != null) {
            removeSessionForUser(userId, session);
            LOG.info("WebSocket disconnected: userId={}, sessionId={}, status={}", userId, session.getId(), status);
        }
    }

    private void removeSessionForUser(String userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByUserId.remove(userId);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOG.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        removeSession(session);
    }

    public void broadcastToUser(String userId, WebSocketMessage message) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        sendToSessions(sessions, message);
    }

    public void broadcastToAdmins(WebSocketMessage message) {
        sessionsByUserId.forEach((userId, sessions) -> {
            for (WebSocketSession session : sessions) {
                if (isAdmin(session)) {
                    sendMessage(session, message);
                }
            }
        });
    }

    public void broadcastToUserAndAdmins(String targetUserId, WebSocketMessage message) {
        LOG.debug("Broadcasting to targetUserId={} and admins. Message type={}, claimId={}",
                targetUserId, message.type(), message.claimId());

        // Clean up closed sessions first to avoid sending to stale connections
        cleanupClosedSessions();

        sessionsByUserId.forEach((userId, sessions) -> {
            for (WebSocketSession session : sessions) {
                boolean matchesTarget = userId.equals(targetUserId);
                boolean isAdminUser = isAdmin(session);

                if (matchesTarget || isAdminUser) {
                    LOG.debug("  → Sending to userId={}, sessionId={}, isOpen={}, matchesTarget={}, isAdmin={}",
                            userId, session.getId(), session.isOpen(), matchesTarget, isAdminUser);
                    sendMessage(session, message);
                }
            }
        });
    }

    private boolean isClosedSession(WebSocketSession session, String userId) {
        boolean closed = !session.isOpen();
        if (closed) {
            LOG.debug("Removing closed session during cleanup: userId={}, sessionId={}", userId, session.getId());
        }
        return closed;
    }

    private void cleanupClosedSessions() {
        sessionsByUserId.forEach((userId, sessions) ->
            sessions.removeIf(session -> isClosedSession(session, userId))
        );
        // Remove empty user entries
        sessionsByUserId.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Scheduled(fixedRate = 30_000)
    public void sendHeartbeat() {
        PingMessage pingMessage = new PingMessage(ByteBuffer.wrap("ping".getBytes()));
        sessionsByUserId.values().forEach(sessions ->
                sessions.forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(pingMessage);
                        }
                    } catch (IOException exception) {
                        LOG.warn("Failed to send heartbeat to session {}, removing", session.getId());
                        removeSession(session);
                    }
                })
        );
    }

    int getActiveSessionCount() {
        return sessionsByUserId.values().stream().mapToInt(Set::size).sum();
    }

    int getActiveUserCount() {
        return sessionsByUserId.size();
    }

    private void sendToSessions(Set<WebSocketSession> sessions, WebSocketMessage message) {
        for (WebSocketSession session : sessions) {
            sendMessage(session, message);
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            // Double-check session state before sending
            if (!session.isOpen()) {
                LOG.debug("Skipping closed session: sessionId={}", session.getId());
                removeSession(session);
                return;
            }

            String json = objectMapper.writeValueAsString(message);
            LOG.debug("Sending WebSocket message to sessionId={}, json={}", session.getId(), json);

            session.sendMessage(new TextMessage(json));
            LOG.debug("✅ Successfully sent message to sessionId={}", session.getId());
        } catch (IOException exception) {
            LOG.warn("IOException sending WebSocket message to session {}: {} - removing stale connection",
                    session.getId(), exception.getMessage());
            removeSession(session);
        } catch (IllegalStateException exception) {
            LOG.warn("IllegalStateException sending WebSocket message to session {}: {} - session already closed",
                    session.getId(), exception.getMessage());
            removeSession(session);
        }
    }

    private void removeSession(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId != null) {
            removeSessionForUser(userId, session);
        }
        closeSession(session);
    }

    private void closeSession(WebSocketSession session) {
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException exception) {
            LOG.debug("Error closing WebSocket session: {}", exception.getMessage());
        }
    }

    private String getUserId(WebSocketSession session) {
        return (String) session.getAttributes().get(USER_ID_ATTRIBUTE);
    }

    @SuppressWarnings("unchecked")
    private boolean isAdmin(WebSocketSession session) {
        List<String> roles = (List<String>) session.getAttributes().get(USER_ROLES_ATTRIBUTE);
        return roles != null && roles.stream().anyMatch("ADMIN"::equalsIgnoreCase);
    }
}
