package com.sarefin.miniddd.port.in;

import com.sarefin.miniddd.domain.order.Order;
import com.sarefin.miniddd.domain.order.OrderId;

// Inbound port — driven side of the hexagon; GetOrderUseCase implements this, the web adapter calls it.
public interface GetOrderPort {
    Order getOrder(OrderId orderId);
}
