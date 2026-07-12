package com.sarefin.miniddd.adapter.in.web.dto;

import com.sarefin.miniddd.domain.order.PaymentMethod;

// Adapter — HTTP request shape for confirming payment. Referencing the domain enum here is fine: adapters depend
// inward on the domain, the dependency rule only forbids the reverse.
public record ConfirmPaymentRequest(PaymentMethod paymentMethod, String paymentToken) {
}
