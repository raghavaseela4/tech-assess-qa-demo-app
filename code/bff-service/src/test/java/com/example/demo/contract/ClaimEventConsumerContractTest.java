package com.example.demo.contract;

import com.example.demo.adapter.in.messaging.ClaimEventKafkaConsumer;
import com.example.demo.adapter.in.websocket.ClaimWebSocketHandler;
import com.example.demo.adapter.in.websocket.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Consumer-side contract test for bff-service.
 *
 * <p>Two things are checked here, deliberately kept separate because they answer
 * different questions:</p>
 *
 * <ol>
 *   <li><b>Producer/consumer compatibility in reality.</b> claims-service's actual
 *   published envelope (nested {@code {eventType, eventId, payload:{...}}}, per
 *   KafkaDomainEventPublisher — see the producer contract test) does NOT match either
 *   side's own committed JSON Schema. But does the BFF's consumer at least correctly
 *   parse what claims-service actually sends? These tests build the real envelope
 *   shape by hand and confirm it does — the two services currently agree with each
 *   other in practice, even though neither agrees with the documented schema.</li>
 *
 *   <li><b>CDC schema-vs-code drift.</b> The committed DebeziumClaimsMessage.json
 *   (identical copy in both services) documents the claim row's primary key field as
 *   {@code id}. But ClaimEventKafkaConsumer.handleCdcCreate/handleCdcUpdate actually
 *   read {@code claim_id} — matching the real Postgres column name (confirmed against
 *   ClaimEntity's @Column mapping), not the schema. A real Debezium payload works fine
 *   today because it always carries {@code claim_id}, but the schema itself is
 *   inaccurate and would mislead anyone building a new consumer off it.</li>
 * </ol>
 *
 * <p>Tagged {@code contract} — runs in the default fast {@code mvn test}, per this
 * project's own pom.xml convention.</p>
 */
@Tag("contract")
@ExtendWith(MockitoExtension.class)
class ClaimEventConsumerContractTest {

    @Mock
    private ClaimWebSocketHandler webSocketHandler;

    private ClaimEventKafkaConsumer consumer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        consumer = new ClaimEventKafkaConsumer(webSocketHandler, objectMapper);
    }

    @Test
    @DisplayName("Consumer correctly parses the REAL claim-submitted envelope shape claims-service "
            + "actually publishes (nested payload, no incidentDate/claimAmount/occurredAt) — "
            + "confirms the two services agree with each other today, independent of the schema")
    void parsesRealProducerEnvelope_claimSubmitted() {
        String claimId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String correlationId = "evt-" + UUID.randomUUID();

        // Exactly what KafkaDomainEventPublisher.buildEnvelope() produces for ClaimSubmitted.
        String realEnvelope = """
                {"eventType":"claim-submitted","eventId":"%s","payload":{"claimId":"%s","userId":"%s","correlationId":"%s"}}
                """.formatted(correlationId, claimId, userId, correlationId).strip();

        consumer.consumeDomainEvent(realEnvelope);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketHandler).broadcastToAdmins(captor.capture());
        WebSocketMessage message = captor.getValue();

        assertThat(message.type()).isEqualTo("CLAIM_SUBMITTED");
        assertThat(message.claimId()).isEqualTo(claimId);
        assertThat(message.newStatus()).isEqualTo("SUBMITTED");
        assertThat(message.correlationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Consumer correctly parses the REAL claim-status-changed envelope shape, "
            + "including routing to the affected user via userId (which, per the changedBy bug "
            + "documented in the domain layer, is actually the claim OWNER's id, not the admin's)")
    void parsesRealProducerEnvelope_claimStatusChanged() {
        String claimId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String correlationId = "evt-" + UUID.randomUUID();

        // Exactly what KafkaDomainEventPublisher.buildEnvelope() produces for ClaimStatusChanged.
        String realEnvelope = """
                {"eventType":"claim-status-changed","eventId":"%s","payload":{"claimId":"%s","newStatus":"APPROVED","oldStatus":"UNDER_REVIEW","userId":"%s","correlationId":"%s"}}
                """.formatted(correlationId, claimId, userId, correlationId).strip();

        consumer.consumeDomainEvent(realEnvelope);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketHandler).broadcastToUserAndAdmins(eq(userId), captor.capture());
        WebSocketMessage message = captor.getValue();

        assertThat(message.type()).isEqualTo("CLAIM_STATUS_CHANGED");
        assertThat(message.claimId()).isEqualTo(claimId);
        assertThat(message.newStatus()).isEqualTo("APPROVED");
        assertThat(message.oldStatus()).isEqualTo("UNDER_REVIEW");
    }

    @Test
    @DisplayName("CDC consumer correctly extracts claimId using the real Postgres column name "
            + "(claim_id) — this is what actually flows through Debezium in production")
    void cdcConsumerReadsRealColumnName() {
        String claimId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String cdcEnvelope = """
                {"op":"c","before":null,"after":{"claim_id":"%s","user_id":"%s","status":"SUBMITTED"},"ts_ms":1717000000000}
                """.formatted(claimId, userId).strip();

        consumer.consumeCdcEvent(cdcEnvelope);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketHandler).broadcastToAdmins(captor.capture());
        assertThat(captor.getValue().claimId()).isEqualTo(claimId);
    }

    @Test
    @DisplayName("BUG (schema-vs-code drift): the committed DebeziumClaimsMessage.json documents "
            + "the primary key field as \"id\", but the consumer only ever reads \"claim_id\" — a "
            + "payload built exactly per the documented schema produces an EMPTY claimId broadcast")
    void cdcConsumerDoesNotMatchItsOwnDocumentedSchemaFieldName() {
        String userId = UUID.randomUUID().toString();

        // Built using the field name the committed schema (DebeziumClaimsMessage.json,
        // identical in both services) actually documents: "id", not "claim_id".
        String schemaDocumentedShape = """
                {"op":"c","before":null,"after":{"id":"%s","user_id":"%s","status":"SUBMITTED"},"ts_ms":1717000000000}
                """.formatted(UUID.randomUUID(), userId).strip();

        consumer.consumeCdcEvent(schemaDocumentedShape);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketHandler).broadcastToAdmins(captor.capture());

        // This documents the CURRENT (broken w.r.t. the schema) behaviour: claimId comes back
        // empty because handleCdcCreate() reads "claim_id", which isn't present in a payload
        // built strictly from what the schema says the field should be called.
        assertThat(captor.getValue().claimId())
                .as("If the schema's documented field name (\"id\") were ever actually correct, "
                        + "this would equal the claim id — instead it's empty, proving the schema "
                        + "and the code disagree")
                .isEmpty();
    }
}
