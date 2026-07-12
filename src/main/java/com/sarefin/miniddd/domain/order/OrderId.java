package com.sarefin.miniddd.domain.order;

import java.util.UUID;

// Domain — identity value object; wraps a UUID so Order never exposes a bare primitive as its identity type.
public record OrderId(UUID value) {

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
