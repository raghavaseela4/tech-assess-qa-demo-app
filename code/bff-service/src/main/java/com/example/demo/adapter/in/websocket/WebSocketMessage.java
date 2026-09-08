package com.example.demo.adapter.in.websocket;

import java.time.Instant;

public record WebSocketMessage(
        String type,
        String claimId,
        String newStatus,
        String oldStatus,
        Instant timestamp,
        String correlationId
) {
}
