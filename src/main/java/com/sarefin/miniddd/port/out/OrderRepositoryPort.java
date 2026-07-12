package com.sarefin.miniddd.port.out;

import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;

import java.util.Optional;

// Outbound port — driving side of the hexagon; use cases depend on this interface, the Postgres adapter implements it.
public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(OrderId id);
}
