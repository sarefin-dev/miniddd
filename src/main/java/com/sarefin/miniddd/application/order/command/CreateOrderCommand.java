package com.sarefin.miniddd.application.order.command;

import java.math.BigDecimal;
import java.util.UUID;

// Application — input to CreateOrderUseCase; kept separate from any domain type so the domain never depends on how a request arrived.
public record CreateOrderCommand(UUID customerId, BigDecimal amount, String currency) {
}
