package com.sarefin.miniddd.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarefin.miniddd.domain.order.event.DomainEvent;
import com.sarefin.miniddd.domain.order.event.OrderConfirmed;
import com.sarefin.miniddd.domain.order.event.PaymentFailed;
import com.sarefin.miniddd.port.out.EventPublisherPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Adapter — outbound event adapter; writes to the outbox table instead of publishing directly, so the write rides
// the same database transaction as the order save. OutboxRelay does the actual Kafka publish afterward.
@Component
public class OutboxEventPublisherAdapter implements EventPublisherPort {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisherAdapter(OutboxEventJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            OutboxEventEntity entity = new OutboxEventEntity(
                    aggregateIdOf(event),
                    event.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(event),
                    event.occurredOn());
            outboxRepository.save(entity);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize domain event " + event, e);
        }
    }

    private UUID aggregateIdOf(DomainEvent event) {
        return switch (event) {
            case OrderConfirmed e -> e.orderId().value();
            case PaymentFailed e -> e.orderId().value();
            default -> throw new IllegalArgumentException("Unhandled domain event type: " + event.getClass());
        };
    }
}
