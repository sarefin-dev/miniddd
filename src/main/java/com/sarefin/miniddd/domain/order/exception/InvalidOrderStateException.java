package com.sarefin.miniddd.domain.order.exception;

// Domain — thrown when an operation would violate one of Order's state-transition invariants.
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
