package com.sarefin.miniddd.adapter.in.web.dto;

import com.sarefin.miniddd.domain.order.Order;

import java.math.BigDecimal;
import java.util.UUID;

// Adapter — HTTP response shape for GET /api/orders/{orderId}; maps the domain Order to a wire-friendly view.
public record OrderDetailResponse(
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String status,
        String paymentMethod) {

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.id().value(),
                order.customerId(),
                order.amount().amount(),
                order.amount().currency().getCurrencyCode(),
                order.status().name(),
                order.paymentMethod() == null ? null : order.paymentMethod().name());
    }
}
