package com.sarefin.miniddd.application.order.command;

import com.sarefin.miniddd.domain.order.PaymentMethod;

import java.util.UUID;

// Application — input to ConfirmPaymentUseCase.
public record ConfirmPaymentCommand(UUID orderId, PaymentMethod paymentMethod, String paymentToken) {
}
