package com.sarefin.miniddd.application.order.exception;

import com.sarefin.miniddd.domain.order.OrderId;

// Application — an orchestration-level failure (the order doesn't exist), distinct from a domain invariant violation.
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(OrderId orderId) {
        super("Order not found: " + orderId);
    }
}
