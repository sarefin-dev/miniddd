package com.sarefin.miniddd.port.out;

// Outbound port — the shape a gateway adapter reports back in; this is the port's contract, not a domain concept.
public record PaymentGatewayResult(boolean success, String failureReason) {

    public static PaymentGatewayResult succeeded() {
        return new PaymentGatewayResult(true, null);
    }

    public static PaymentGatewayResult failure(String reason) {
        return new PaymentGatewayResult(false, reason);
    }
}
