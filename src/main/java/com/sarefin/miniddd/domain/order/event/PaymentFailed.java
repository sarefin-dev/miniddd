package com.sarefin.miniddd.domain.order.event;

import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.domain.order.PaymentMethod;

import java.time.Instant;

// Domain — raised by Order.recordPaymentFailure(); lets downstream consumers react to a declined/erroring charge without polling.
public record PaymentFailed(OrderId orderId, PaymentMethod paymentMethod, String reason, Instant occurredOn) implements DomainEvent {
}
