package com.sarefin.miniddd.domain.order;

import com.sarefin.miniddd.domain.order.event.PaymentFailed;
import com.sarefin.miniddd.domain.order.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test — no Spring context, no mocked adapters: if this test ever needed either, the domain layer would
// have leaked a dependency it shouldn't have.
class OrderTest {

    private final Money amount = Money.of(new BigDecimal("100.00"), "USD");

    @Test
    void confirmingPaymentTransitionsToConfirmedAndRaisesOrderConfirmed() {
        Order order = Order.create(UUID.randomUUID(), amount);

        order.confirmPayment(PaymentMethod.STRIPE);

        assertEquals(OrderStatus.CONFIRMED, order.status());
        assertEquals(1, order.pullDomainEvents().size());
    }

    @Test
    void cannotConfirmPaymentTwice() {
        Order order = Order.create(UUID.randomUUID(), amount);
        order.confirmPayment(PaymentMethod.STRIPE);

        assertThrows(InvalidOrderStateException.class, () -> order.confirmPayment(PaymentMethod.BKASH));
    }

    @Test
    void recordingPaymentFailureTransitionsToPaymentFailedAndRaisesPaymentFailed() {
        Order order = Order.create(UUID.randomUUID(), amount);

        order.recordPaymentFailure(PaymentMethod.BKASH, "insufficient funds");

        assertEquals(OrderStatus.PAYMENT_FAILED, order.status());
        assertInstanceOf(PaymentFailed.class, order.pullDomainEvents().get(0));
    }

    @Test
    void cancellingAConfirmedOrderIsRejected() {
        Order order = Order.create(UUID.randomUUID(), amount);
        order.confirmPayment(PaymentMethod.STRIPE);

        assertThrows(InvalidOrderStateException.class, order::cancel);
    }
}
