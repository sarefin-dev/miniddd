package com.sarefin.miniddd.adapter.out.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Adapter — polls the outbox table and relays each unpublished row to Kafka. This decoupling from
// OutboxEventPublisherAdapter is what makes the outbox write transactional and the Kafka publish independently
// retryable (at-least-once delivery to Kafka; consumers must be idempotent).
@Component
public class OutboxRelay {

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelay(OutboxEventJpaRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayPendingEvents() {
        outboxRepository.findByPublishedFalseOrderByOccurredOnAsc().forEach(event -> {
            kafkaTemplate.send(topicFor(event.getEventType()), event.getAggregateId().toString(), event.getPayload());
            event.markPublished();
        });
    }

    private String topicFor(String eventType) {
        return switch (eventType) {
            case "OrderConfirmed" -> "order.confirmed";
            case "PaymentFailed" -> "order.payment-failed";
            default -> throw new IllegalStateException("No topic mapped for event type " + eventType);
        };
    }
}
