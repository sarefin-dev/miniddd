package com.sarefin.miniddd.domain.order;

// Domain — the gateways a customer can pay through; lives here (not in an adapter) because "which method was used" is business-meaningful state on the order.
public enum PaymentMethod {
    STRIPE,
    BKASH,
    NAGAD
}
