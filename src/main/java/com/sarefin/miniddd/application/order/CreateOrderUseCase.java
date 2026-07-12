package com.sarefin.miniddd.application.order;

import com.sarefin.miniddd.application.order.command.CreateOrderCommand;
import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.port.in.CreateOrderPort;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;

// Application — orchestrates the "place an order" flow; depends only on ports, never on a concrete adapter.
public class CreateOrderUseCase implements CreateOrderPort {

    private final OrderRepositoryPort orderRepository;

    public CreateOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderId createOrder(CreateOrderCommand command) {
        Money amount = Money.of(command.amount(), command.currency());
        Order order = Order.create(command.customerId(), amount);
        return orderRepository.save(order).id();
    }
}
