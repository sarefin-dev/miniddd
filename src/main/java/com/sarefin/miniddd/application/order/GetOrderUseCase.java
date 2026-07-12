package com.sarefin.miniddd.application.order;

import com.sarefin.miniddd.application.order.exception.OrderNotFoundException;
import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.port.in.GetOrderPort;
import com.sarefin.miniddd.port.out.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Application — the read side of the order lifecycle; lets a caller check status after the async confirm-payment
// call instead of only finding out via the order.confirmed/order.payment-failed Kafka topics.
public class GetOrderUseCase implements GetOrderPort {

    private static final Logger log = LoggerFactory.getLogger(GetOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;

    public GetOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order getOrder(OrderId orderId) {
        log.debug("Fetching order {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
