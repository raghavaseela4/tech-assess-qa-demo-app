package com.example.demo.application.events;

import com.example.demo.domain.DomainEvent;

/**
 * Port for publishing domain events.
 * Implementations are in the infrastructure/adapter layer.
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
