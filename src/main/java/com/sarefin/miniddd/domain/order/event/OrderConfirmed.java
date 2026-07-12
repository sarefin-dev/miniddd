package com.sarefin.miniddd.domain.order.event;

import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.domain.order.PaymentMethod;

import java.time.Instant;

// Domain — raised by Order.confirmPayment(); the single fact every gateway path converges on, published via the outbox.
public record OrderConfirmed(OrderId orderId, PaymentMethod paymentMethod, Money amount, Instant occurredOn) implements DomainEvent {
}
