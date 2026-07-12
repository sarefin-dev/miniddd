package com.sarefin.miniddd.config;

import com.sarefin.miniddd.application.order.ConfirmPaymentUseCase;
import com.sarefin.miniddd.application.order.CreateOrderUseCase;
import com.sarefin.miniddd.application.order.GetOrderUseCase;
import com.sarefin.miniddd.port.out.EventPublisherPort;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;
import com.sarefin.miniddd.port.out.PaymentGatewayPort;
import com.sarefin.miniddd.port.out.TransactionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Composition root — the only place that constructs use cases as Spring beans; keeps @Service/@Component
// annotations off the application layer entirely.
@Configuration
public class UseCaseConfiguration {

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepositoryPort orderRepository) {
        return new CreateOrderUseCase(orderRepository);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepositoryPort orderRepository) {
        return new GetOrderUseCase(orderRepository);
    }

    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase(
            OrderRepositoryPort orderRepository,
            List<PaymentGatewayPort> gateways,
            EventPublisherPort eventPublisher,
            TransactionPort transactionPort) {
        return new ConfirmPaymentUseCase(orderRepository, gateways, eventPublisher, transactionPort);
    }
}
