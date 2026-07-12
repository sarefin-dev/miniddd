package com.sarefin.miniddd.port.out;

import com.sarefin.miniddd.domain.order.event.DomainEvent;

// Outbound port — driving side of the hexagon; use cases publish through this, the outbox adapter implements it.
public interface EventPublisherPort {
    void publish(DomainEvent event);
}
