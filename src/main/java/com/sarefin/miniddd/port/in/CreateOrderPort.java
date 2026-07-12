package com.sarefin.miniddd.port.in;

import com.sarefin.miniddd.application.order.command.CreateOrderCommand;
import com.sarefin.miniddd.domain.order.OrderId;

// Inbound port — driven side of the hexagon; CreateOrderUseCase implements this, the web adapter calls it.
public interface CreateOrderPort {
    OrderId createOrder(CreateOrderCommand command);
}
