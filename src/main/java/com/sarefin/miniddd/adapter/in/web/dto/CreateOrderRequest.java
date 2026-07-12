package com.sarefin.miniddd.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Adapter — HTTP request shape; translated into a CreateOrderCommand before it ever reaches the application layer.
public record CreateOrderRequest(UUID customerId, BigDecimal amount, String currency) {
}
