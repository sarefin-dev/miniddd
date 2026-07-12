package com.sarefin.miniddd.adapter.in.web;

import com.sarefin.miniddd.adapter.in.web.dto.ConfirmPaymentRequest;
import com.sarefin.miniddd.adapter.in.web.dto.CreateOrderRequest;
import com.sarefin.miniddd.adapter.in.web.dto.OrderResponse;
import com.sarefin.miniddd.application.order.command.ConfirmPaymentCommand;
import com.sarefin.miniddd.application.order.command.CreateOrderCommand;
import com.sarefin.miniddd.domain.order.OrderId;
import com.sarefin.miniddd.port.in.ConfirmPaymentPort;
import com.sarefin.miniddd.port.in.CreateOrderPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Adapter — inbound driving adapter; depends only on port.in interfaces, never on the use case classes directly.
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderPort createOrderPort;
    private final ConfirmPaymentPort confirmPaymentPort;

    public OrderController(CreateOrderPort createOrderPort, ConfirmPaymentPort confirmPaymentPort) {
        this.createOrderPort = createOrderPort;
        this.confirmPaymentPort = confirmPaymentPort;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderId orderId = createOrderPort.createOrder(
                new CreateOrderCommand(request.customerId(), request.amount(), request.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(orderId.value()));
    }

    // Confirmation is async by nature (a gateway call in the middle), so this returns 202 and the caller checks
    // the order's status afterward rather than getting a synchronous success/failure verdict here.
    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<Void> confirmPayment(@PathVariable UUID orderId, @RequestBody ConfirmPaymentRequest request) {
        confirmPaymentPort.confirmPayment(
                new ConfirmPaymentCommand(orderId, request.paymentMethod(), request.paymentToken()));
        return ResponseEntity.accepted().build();
    }
}
