package com.sarefin.miniddd.adapter.out.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Adapter — Spring Data repository backing the outbox table.
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findByPublishedFalseOrderByOccurredOnAsc();
}
