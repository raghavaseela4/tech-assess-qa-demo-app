package com.example.demo.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller for injecting mock CDC events into Kafka.
 * Enables deterministic E2E testing without waiting for Debezium CDC polling.
 * Only active when the "test" Spring profile is enabled.
 */
@RestController
@RequestMapping("/api/test")
@Profile("test")
@Tag(name = "Test Utilities", description = "Test-only endpoints (active in test profile)")
public class TestEventController {

    private static final String CDC_TOPIC = "cdc.demo-app.public.claims";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TestEventController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Operation(
        summary = "Trigger mock CDC event",
        description = "Injects a mock Change Data Capture event into Kafka topic for deterministic E2E testing. " +
                      "Bypasses Debezium polling delay. Only available in test profile.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "CDC event published successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = @Content(schema = @Schema(hidden = true))
            )
        }
    )
    @PostMapping("/trigger-event")
    public ResponseEntity<Void> triggerCDCEvent(
            @Valid @RequestBody MockCDCEventRequest request) {
        String payload = buildMockCDCPayload(request);
        kafkaTemplate.send(CDC_TOPIC, request.claimId(), payload);
        return ResponseEntity.ok().build();
    }

    private String buildMockCDCPayload(MockCDCEventRequest request) {
        long timestamp = System.currentTimeMillis();
        return """
            {
              "op": "u",
              "after": {
                "id": "%s",
                "status": "%s",
                "updated_at": %d
              },
              "ts_ms": %d
            }
            """.formatted(request.claimId(), request.newStatus(), timestamp, timestamp);
    }
}
