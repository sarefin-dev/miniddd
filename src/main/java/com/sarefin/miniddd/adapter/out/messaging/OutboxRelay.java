package com.sarefin.miniddd.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Adapter — polls the outbox table and relays each unpublished row to Kafka. This decoupling from
// OutboxEventPublisherAdapter is what makes the outbox write transactional and the Kafka publish independently
// retryable (at-least-once delivery to Kafka; consumers must be idempotent).
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelay(OutboxEventJpaRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Each row is sent and, only on confirmed delivery, saved as published — as its own repository call, which
    // Spring Data wraps in its own transaction. Marking a row published before the Kafka send is confirmed (the
    // original bug: send() returns a future immediately, so a transaction spanning the whole batch could commit
    // markPublished() before the send actually reached the broker) permanently loses the event on a Kafka outage,
    // since the next poll only looks at published = false rows.
    @Scheduled(fixedDelayString = "${miniddd.outbox.polling-interval-ms:2000}")
    public void relayPendingEvents() {
        outboxRepository.findByPublishedFalseOrderByOccurredOnAsc().forEach(this::relay);
    }

    private void relay(OutboxEventEntity event) {
        String topic = topicFor(event.getEventType());
        try {
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload()).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Failed to relay outbox event {} (aggregate {}); will retry next poll",
                    event.getId(), event.getAggregateId(), e);
            return;
        }
        event.markPublished();
        outboxRepository.save(event);
        log.info("Relayed outbox event {} (aggregate {}) to topic {}", event.getId(), event.getAggregateId(), topic);
    }

    private String topicFor(String eventType) {
        return switch (eventType) {
            case "OrderConfirmed" -> "order.confirmed";
            case "PaymentFailed" -> "order.payment-failed";
            default -> throw new IllegalStateException("No topic mapped for event type " + eventType);
        };
    }
}
