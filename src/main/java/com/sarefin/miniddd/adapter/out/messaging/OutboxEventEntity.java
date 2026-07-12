package com.sarefin.miniddd.adapter.out.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

// Adapter — outbox table row; written in the same transaction as the order, relayed to Kafka separately
// (transactional outbox pattern).
@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant occurredOn;

    @Column(nullable = false)
    private boolean published;

    private Instant publishedAt;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(UUID aggregateId, String eventType, String payload, Instant occurredOn) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredOn = occurredOn;
        this.published = false;
    }

    public void markPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public boolean isPublished() {
        return published;
    }
}
