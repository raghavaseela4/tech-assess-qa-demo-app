package com.example.demo.contract;

import com.example.demo.adapter.out.messaging.KafkaDomainEventPublisher;
import com.example.demo.adapter.out.messaging.KafkaMessageSchemaValidator;
import com.example.demo.adapter.out.messaging.KafkaMessageValidationException;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.domain.claim.events.ClaimStatusChanged;
import com.example.demo.domain.claim.events.ClaimSubmitted;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Producer-side contract test for claims-service.
 *
 * <p>Verifies the actual JSON published to the {@code claim-events} Kafka topic by
 * {@link KafkaDomainEventPublisher} against this project's own committed, machine-readable
 * contract: the JSON Schema files under
 * {@code src/main/resources/asyncapi/schemas/*.json}, which back the human-readable
 * AsyncAPI spec at {@code src/main/resources/asyncapi/claim-events-api.yml}.</p>
 *
 * <p><b>Why this matters:</b> the validation wiring already exists in production code
 * ({@link KafkaMessageSchemaValidator} is called from every {@code publish()} call) but is
 * DISABLED by default ({@code kafka.producer.schema-validation.enabled: false} in
 * application.yml). This test forces it on via the real constructor (not a mock) to check
 * the contract independently of that runtime toggle — because a toggle that's off by
 * default is exactly the kind of thing that lets a contract silently drift for months
 * without anyone noticing, until someone flips it on in a stricter environment (or a
 * schema-registry integration is added later) and claim submission breaks in production.</p>
 *
 * <p>Tagged {@code contract}, per this project's own pom.xml comment ("Contract tests run
 * during test phase via surefire") — runs as part of the default fast {@code mvn test},
 * unlike {@code integration}-tagged tests which are excluded by default.</p>
 */
@Tag("contract")
@ExtendWith(MockitoExtension.class)
class ClaimEventProducerContractTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaDomainEventPublisher publisher;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Force schema validation ON regardless of the application.yml default (false) —
        // that default is exactly what let this contract violation go undetected.
        KafkaMessageSchemaValidator validator = new KafkaMessageSchemaValidator(objectMapper, true);
        publisher = new KafkaDomainEventPublisher(kafkaTemplate, objectMapper, validator);
    }

    @Test
    @DisplayName("BUG: the ClaimSubmitted envelope violates its own committed schema "
            + "(ClaimSubmittedMessage.json) — required fields incidentDate, claimAmount, "
            + "occurredAt are entirely absent from the published JSON, and claimId/userId "
            + "are nested under \"payload\" instead of sitting at the top level the schema expects")
    void claimSubmittedEnvelopeViolatesItsOwnSchema() {
        ClaimSubmitted event = new ClaimSubmitted(
                "evt-" + UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now().minusDays(1),
                new BigDecimal("500.00"),
                Instant.now());

        // Per claim-events-api.yml this publish should succeed. It currently throws —
        // KafkaDomainEventPublisher.buildEnvelope() does not match the schema it is
        // registered to validate against (ClaimSubmittedMessage.json).
        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(KafkaMessageValidationException.class)
                .hasMessageContaining("ClaimSubmittedMessage");

        // Never reaches kafkaTemplate.send() when validation is enabled — meaning claim
        // submission would hard-fail in any environment that turns this flag on, not just
        // log a warning and carry on.
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("BUG: the ClaimStatusChanged envelope also violates its own committed schema "
            + "(ClaimStatusChangedMessage.json) — same top-level-vs-nested mismatch as ClaimSubmitted")
    void claimStatusChangedEnvelopeViolatesItsOwnSchema() {
        ClaimStatusChanged event = new ClaimStatusChanged(
                "evt-" + UUID.randomUUID(),
                UUID.randomUUID(),
                ClaimStatus.SUBMITTED,
                ClaimStatus.UNDER_REVIEW,
                UUID.randomUUID(),
                Instant.now());

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(KafkaMessageValidationException.class)
                .hasMessageContaining("ClaimStatusChangedMessage");

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}
