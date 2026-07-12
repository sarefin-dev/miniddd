package com.sarefin.miniddd.port.out;

// Outbound port — the shape a gateway adapter reports back in; this is the port's contract, not a domain concept.
public record PaymentGatewayResult(boolean success, String failureReason) {

    public PaymentGatewayResult {
        if (success && failureReason != null) {
            throw new IllegalArgumentException("A successful result must not carry a failure reason");
        }
        if (!success && (failureReason == null || failureReason.isBlank())) {
            throw new IllegalArgumentException("A failure result must carry a reason");
        }
    }

    public static PaymentGatewayResult succeeded() {
        return new PaymentGatewayResult(true, null);
    }

    public static PaymentGatewayResult failure(String reason) {
        return new PaymentGatewayResult(false, reason);
    }
}
