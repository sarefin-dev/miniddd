package com.sarefin.miniddd.port.in;

import com.sarefin.miniddd.application.order.command.ConfirmPaymentCommand;

// Inbound port — driven side of the hexagon; ConfirmPaymentUseCase implements this, the web adapter calls it.
public interface ConfirmPaymentPort {
    void confirmPayment(ConfirmPaymentCommand command);
}
