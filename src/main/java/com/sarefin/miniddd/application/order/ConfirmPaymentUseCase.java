package com.sarefin.miniddd.application.order;

import com.sarefin.miniddd.application.order.command.ConfirmPaymentCommand;
import com.sarefin.miniddd.application.order.exception.OrderNotFoundException;
import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.port.in.ConfirmPaymentPort;
import com.sarefin.miniddd.port.out.EventPublisherPort;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;
import com.sarefin.miniddd.port.out.PaymentGatewayPort;
import com.sarefin.miniddd.port.out.PaymentGatewayResult;
import com.sarefin.miniddd.port.out.TransactionPort;

import java.util.List;

// Application — orchestrates the "confirm payment" flow across three interchangeable gateways, all converging on
// the same order.confirmed event regardless of which one was used.
public class ConfirmPaymentUseCase implements ConfirmPaymentPort {

    private final OrderRepositoryPort orderRepository;
    private final List<PaymentGatewayPort> gateways;
    private final EventPublisherPort eventPublisher;
    private final TransactionPort transactionPort;

    public ConfirmPaymentUseCase(
            OrderRepositoryPort orderRepository,
            List<PaymentGatewayPort> gateways,
            EventPublisherPort eventPublisher,
            TransactionPort transactionPort) {
        this.orderRepository = orderRepository;
        this.gateways = gateways;
        this.eventPublisher = eventPublisher;
        this.transactionPort = transactionPort;
    }

    @Override
    public void confirmPayment(ConfirmPaymentCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        PaymentGatewayPort gateway = resolveGateway(command);
        // Charge before opening a transaction: never hold a DB transaction open across a network call to an external gateway.
        PaymentGatewayResult result = gateway.charge(order.amount(), command.paymentToken());

        transactionPort.runInTransaction(() -> {
            if (result.success()) {
                order.confirmPayment(command.paymentMethod());
            } else {
                order.recordPaymentFailure(command.paymentMethod(), result.failureReason());
            }
            orderRepository.save(order);
            order.pullDomainEvents().forEach(eventPublisher::publish);
        });
    }

    private PaymentGatewayPort resolveGateway(ConfirmPaymentCommand command) {
        return gateways.stream()
                .filter(gateway -> gateway.supports(command.paymentMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No gateway adapter supports " + command.paymentMethod()));
    }
}
