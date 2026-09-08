package com.example.demo.adapter.out.messaging;

import com.example.demo.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(DomainEventListener.class);

    private final KafkaDomainEventPublisher kafkaDomainEventPublisher;

    @TransactionalEventListener
    public void handleDomainEvent(DomainEvent event) {
        LOG.info("DomainEventListener received event: {}", event.getClass().getSimpleName());
        kafkaDomainEventPublisher.publish(event);
    }
}
