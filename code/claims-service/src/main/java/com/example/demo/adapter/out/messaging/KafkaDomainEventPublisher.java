package com.example.demo.adapter.out.messaging;

import com.example.demo.application.events.DomainEventPublisher;
import com.example.demo.domain.DomainEvent;
import com.example.demo.domain.claim.events.ClaimStatusChanged;
import com.example.demo.domain.claim.events.ClaimSubmitted;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);
    private static final String TOPIC = "claim-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaMessageSchemaValidator schemaValidator;

    @Override
    public void publish(DomainEvent event) {
        String key = extractKey(event);
        String eventType = resolveEventType(event);
        String payload = buildEnvelope(eventType, event);
        String schemaName = resolveSchemaName(event);

        schemaValidator.validate(payload, schemaName);

        LOG.info("Publishing domain event to Kafka topic={}, key={}, eventType={}", TOPIC, key, eventType);
        kafkaTemplate.send(TOPIC, key, payload);
    }

    // Builds the envelope the BFF consumer expects:
    // { "eventType": "...", "eventId": "...", "payload": { ... } }
    private String buildEnvelope(String eventType, DomainEvent event) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("eventType", eventType);

            if (event instanceof ClaimSubmitted e) {
                envelope.put("eventId", e.eventId());
                ObjectNode payload = envelope.putObject("payload");
                payload.put("claimId", e.claimId().toString());
                payload.put("userId", e.userId().toString());
                payload.put("correlationId", e.eventId());
            } else if (event instanceof ClaimStatusChanged e) {
                envelope.put("eventId", e.eventId());
                ObjectNode payload = envelope.putObject("payload");
                payload.put("claimId", e.claimId().toString());
                payload.put("newStatus", e.newStatus().name());
                payload.put("oldStatus", e.oldStatus().name());
                if (e.changedBy() != null) {
                    payload.put("userId", e.changedBy().toString());
                }
                payload.put("correlationId", e.eventId());
            } else {
                throw new IllegalArgumentException("Unknown domain event type: " + event.getClass().getSimpleName());
            }

            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event envelope", ex);
        }
    }

    private String extractKey(DomainEvent event) {
        if (event instanceof ClaimSubmitted e) return e.claimId().toString();
        if (event instanceof ClaimStatusChanged e) return e.claimId().toString();
        throw new IllegalArgumentException("Unknown domain event type: " + event.getClass().getSimpleName());
    }

    private String resolveEventType(DomainEvent event) {
        if (event instanceof ClaimSubmitted) return "claim-submitted";
        if (event instanceof ClaimStatusChanged) return "claim-status-changed";
        throw new IllegalArgumentException("Unknown domain event type: " + event.getClass().getSimpleName());
    }

    private String resolveSchemaName(DomainEvent event) {
        if (event instanceof ClaimSubmitted) return "ClaimSubmittedMessage";
        if (event instanceof ClaimStatusChanged) return "ClaimStatusChangedMessage";
        throw new IllegalArgumentException("Unknown domain event type: " + event.getClass().getSimpleName());
    }
}
