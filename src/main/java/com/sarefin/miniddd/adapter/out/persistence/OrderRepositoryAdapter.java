package com.sarefin.miniddd.adapter.out.persistence;

import com.sarefin.miniddd.domain.order.Money;
import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.domain.order.OrderStatus;
import com.sarefin.miniddd.domain.order.PaymentMethod;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Adapter — outbound persistence adapter; the only place that translates between the domain Order and its JPA row shape.
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        jpaRepository.save(toEntity(order));
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    private OrderJpaEntity toEntity(Order order) {
        return new OrderJpaEntity(
                order.id().value(),
                order.customerId(),
                order.amount().amount(),
                order.amount().currency().getCurrencyCode(),
                order.status().name(),
                order.paymentMethod() == null ? null : order.paymentMethod().name());
    }

    private Order toDomain(OrderJpaEntity entity) {
        return Order.reconstruct(
                OrderId.of(entity.getId()),
                entity.getCustomerId(),
                Money.of(entity.getAmount(), entity.getCurrency()),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getPaymentMethod() == null ? null : PaymentMethod.valueOf(entity.getPaymentMethod()));
    }
}
