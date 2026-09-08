package com.example.demo.user.domain;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
