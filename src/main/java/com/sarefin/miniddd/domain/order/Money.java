package com.sarefin.miniddd.domain.order;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

// Domain — value object; makes "an amount with a currency" a first-class type instead of a bare BigDecimal.
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
}
