package com.example.demo.adapter.in.messaging;

import com.example.demo.adapter.in.websocket.ClaimWebSocketHandler;
import com.example.demo.adapter.in.websocket.WebSocketMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ClaimEventKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimEventKafkaConsumer.class);

    private final ClaimWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public ClaimEventKafkaConsumer(ClaimWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "claim-events",
            groupId = "bff-websocket-consumer-group",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    public void consumeDomainEvent(String message) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventType = envelope.path("eventType").asText();
            JsonNode payload = envelope.path("payload");
            String correlationId = payload.path("correlationId").asText();
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = envelope.path("eventId").asText();
            }

            switch (eventType) {
                case "claim-submitted" -> handleClaimSubmitted(payload, correlationId);
                case "claim-status-changed" -> handleClaimStatusChanged(payload, correlationId);
                default -> LOG.debug("Ignoring unknown domain event type: {}", eventType);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            LOG.error("Failed to process domain event from claim-events topic: {}", exception.getMessage(), exception);
        }
    }

    @KafkaListener(
            topics = "cdc.demo-app.public.claims",
            groupId = "bff-websocket-consumer-group",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    public void consumeCdcEvent(String message) {
        try {
            JsonNode cdcEnvelope = objectMapper.readTree(message);
            String operation = cdcEnvelope.path("op").asText();

            if ("u".equals(operation)) {
                handleCdcUpdate(cdcEnvelope);
            } else if ("c".equals(operation)) {
                handleCdcCreate(cdcEnvelope);
            } else {
                LOG.debug("Ignoring CDC operation: {}", operation);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            LOG.error("Failed to process CDC event from cdc.demo-app.public.claims topic: {}", exception.getMessage(), exception);
        }
    }

    private void handleClaimSubmitted(JsonNode payload, String correlationId) {
        String claimId = payload.path("claimId").asText();

        WebSocketMessage webSocketMessage = new WebSocketMessage(
                "CLAIM_SUBMITTED",
                claimId,
                "SUBMITTED",
                null,
                Instant.now(),
                correlationId
        );

        webSocketHandler.broadcastToAdmins(webSocketMessage);
        LOG.debug("Broadcast CLAIM_SUBMITTED for claimId={}", claimId);
    }

    private void handleClaimStatusChanged(JsonNode payload, String correlationId) {
        String claimId = payload.path("claimId").asText();
        String newStatus = payload.path("newStatus").asText();
        String oldStatus = payload.path("oldStatus").asText();
        String userId = payload.path("userId").asText(null);

        WebSocketMessage webSocketMessage = new WebSocketMessage(
                "CLAIM_STATUS_CHANGED",
                claimId,
                newStatus,
                oldStatus,
                Instant.now(),
                correlationId
        );

        if (userId != null && !userId.isEmpty()) {
            webSocketHandler.broadcastToUserAndAdmins(userId, webSocketMessage);
        } else {
            webSocketHandler.broadcastToAdmins(webSocketMessage);
        }
        LOG.debug("Broadcast CLAIM_STATUS_CHANGED for claimId={}, {} -> {}", claimId, oldStatus, newStatus);
    }

    private void handleCdcUpdate(JsonNode cdcEnvelope) {
        JsonNode before = cdcEnvelope.path("before");
        JsonNode after = cdcEnvelope.path("after");

        String claimId = after.path("claim_id").asText();
        String newStatus = after.path("status").asText();
        String oldStatus = before.path("status").asText();
        String userId = after.path("user_id").asText(null);
        long timestampMs = cdcEnvelope.path("ts_ms").asLong();
        // CDC events have no prior correlation ID (database-level change), generate new for tracing
        String correlationId = UUID.randomUUID().toString();

        WebSocketMessage webSocketMessage = new WebSocketMessage(
                "CLAIM_STATUS_CHANGED",
                claimId,
                newStatus,
                oldStatus,
                Instant.ofEpochMilli(timestampMs),
                correlationId
        );

        if (userId != null && !userId.isEmpty()) {
            webSocketHandler.broadcastToUserAndAdmins(userId, webSocketMessage);
        } else {
            webSocketHandler.broadcastToAdmins(webSocketMessage);
        }
        LOG.debug("Broadcast CDC CLAIM_STATUS_CHANGED for claimId={}, {} -> {}", claimId, oldStatus, newStatus);
    }

    private void handleCdcCreate(JsonNode cdcEnvelope) {
        JsonNode after = cdcEnvelope.path("after");

        String claimId = after.path("claim_id").asText();
        long timestampMs = cdcEnvelope.path("ts_ms").asLong();
        // CDC events have no prior correlation ID (database-level change), generate new for tracing
        String correlationId = UUID.randomUUID().toString();

        WebSocketMessage webSocketMessage = new WebSocketMessage(
                "CLAIM_SUBMITTED",
                claimId,
                "SUBMITTED",
                null,
                Instant.ofEpochMilli(timestampMs),
                correlationId
        );

        webSocketHandler.broadcastToAdmins(webSocketMessage);
        LOG.debug("Broadcast CDC CLAIM_SUBMITTED for claimId={}", claimId);
    }
}
