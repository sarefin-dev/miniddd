package com.sarefin.miniddd.domain.order;

// Domain — the finite set of states an Order can be in; transitions are enforced by Order itself, not by this enum.
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED
}
