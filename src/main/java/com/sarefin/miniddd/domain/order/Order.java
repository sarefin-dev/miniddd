package com.sarefin.miniddd.domain.order;

import com.sarefin.miniddd.domain.order.event.DomainEvent;
import com.sarefin.miniddd.domain.order.event.OrderConfirmed;
import com.sarefin.miniddd.domain.order.event.PaymentFailed;
import com.sarefin.miniddd.domain.order.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Domain — aggregate root; the only place order lifecycle rules and invariants are allowed to live.
public class Order {

    private final OrderId id;
    private final UUID customerId;
    private final Money amount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    private Order(OrderId id, UUID customerId, Money amount, OrderStatus status, PaymentMethod paymentMethod) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public static Order create(UUID customerId, Money amount) {
        return new Order(OrderId.generate(), customerId, amount, OrderStatus.PENDING, null);
    }

    // Rehydrates state a persistence adapter already validated on a previous save; application code should never call this directly.
    public static Order reconstruct(OrderId id, UUID customerId, Money amount, OrderStatus status, PaymentMethod paymentMethod) {
        return new Order(id, customerId, amount, status, paymentMethod);
    }

    public void confirmPayment(PaymentMethod method) {
        requireStatus(OrderStatus.PENDING, "confirm payment for");
        this.status = OrderStatus.CONFIRMED;
        this.paymentMethod = method;
        pendingEvents.add(new OrderConfirmed(id, method, amount, Instant.now()));
    }

    // Terminal outcome by design: retrying with a different gateway means creating a new order, not resurrecting this one.
    public void recordPaymentFailure(PaymentMethod method, String reason) {
        requireStatus(OrderStatus.PENDING, "record a payment failure for");
        this.status = OrderStatus.PAYMENT_FAILED;
        this.paymentMethod = method;
        pendingEvents.add(new PaymentFailed(id, method, reason, Instant.now()));
    }

    public void cancel() {
        requireStatus(OrderStatus.PENDING, "cancel");
        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus required, String action) {
        if (this.status != required) {
            throw new InvalidOrderStateException(
                    "Cannot %s order %s while it is %s".formatted(action, id, status));
        }
    }

    // Drains events accumulated during this call; callers must publish them only after the resulting state is durably saved.
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public OrderId id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public Money amount() {
        return amount;
    }

    public OrderStatus status() {
        return status;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }
}
