package com.sarefin.miniddd.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

// Adapter — HTTP request shape; translated into a CreateOrderCommand before it ever reaches the application layer.
public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency) {
}
