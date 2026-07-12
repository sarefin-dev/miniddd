package com.sarefin.miniddd.adapter.in.web.dto;

import java.util.UUID;

// Adapter — HTTP response shape returned after an order is created.
public record OrderResponse(UUID orderId) {
}
