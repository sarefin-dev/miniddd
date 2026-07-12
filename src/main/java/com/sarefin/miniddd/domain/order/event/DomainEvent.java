package com.sarefin.miniddd.domain.order.event;

import java.time.Instant;

// Domain — marker for anything the aggregate raises as a fact about something that already happened.
public interface DomainEvent {
    Instant occurredOn();
}
