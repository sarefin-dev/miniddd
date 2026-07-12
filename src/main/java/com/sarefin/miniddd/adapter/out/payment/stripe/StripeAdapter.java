package com.sarefin.miniddd.adapter.out.payment.stripe;

import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.PaymentMethod;
import com.sarefin.miniddd.port.out.PaymentGatewayPort;
import com.sarefin.miniddd.port.out.PaymentGatewayResult;
import org.springframework.stereotype.Component;

// Adapter — outbound gateway adapter; translates the generic PaymentGatewayPort contract into a (simulated) call
// to the Stripe API. A real implementation would call com.stripe:stripe-java here and map its response/exceptions
// into a PaymentGatewayResult.
@Component
public class StripeAdapter implements PaymentGatewayPort {

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.STRIPE;
    }

    @Override
    public PaymentGatewayResult charge(Money amount, String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank()) {
            return PaymentGatewayResult.failure("Missing Stripe payment token");
        }
        return PaymentGatewayResult.succeeded();
    }
}
