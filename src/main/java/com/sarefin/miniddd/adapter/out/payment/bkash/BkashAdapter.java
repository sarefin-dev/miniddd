package com.sarefin.miniddd.adapter.out.payment.bkash;

import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.PaymentMethod;
import com.sarefin.miniddd.port.out.PaymentGatewayPort;
import com.sarefin.miniddd.port.out.PaymentGatewayResult;
import org.springframework.stereotype.Component;

// Adapter — outbound gateway adapter; translates the generic PaymentGatewayPort contract into a (simulated) call
// to bKash's grant-token + create/execute-payment REST endpoints.
@Component
public class BkashAdapter implements PaymentGatewayPort {

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.BKASH;
    }

    @Override
    public PaymentGatewayResult charge(Money amount, String paymentToken) {
        if (paymentToken == null || paymentToken.isBlank()) {
            return PaymentGatewayResult.failure("Missing bKash payment token");
        }
        return PaymentGatewayResult.succeeded();
    }
}
