package com.sarefin.miniddd.port.out;

import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.PaymentMethod;

// Outbound port — driving side of the hexagon; one interface, three possible adapters (Stripe/bKash/Nagad), so the
// application layer never knows which gateway it's talking to.
public interface PaymentGatewayPort {
    boolean supports(PaymentMethod method);

    PaymentGatewayResult charge(Money amount, String paymentToken);
}
