package com.sarefin.miniddd.application.order;

import com.sarefin.miniddd.application.order.command.CreateOrderCommand;
import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.port.in.CreateOrderPort;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Application — orchestrates the "place an order" flow; depends only on ports, never on a concrete adapter.
public class CreateOrderUseCase implements CreateOrderPort {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;

    public CreateOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderId createOrder(CreateOrderCommand command) {
        Money amount = Money.of(command.amount(), command.currency());
        Order order = Order.create(command.customerId(), amount);
        Order saved = orderRepository.save(order);
        log.info("Created order {} for customer {}", saved.id(), saved.customerId());
        return saved.id();
    }
}
