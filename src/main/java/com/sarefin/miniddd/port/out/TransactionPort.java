package com.sarefin.miniddd.port.out;

// Outbound port — lets ConfirmPaymentUseCase demarcate a transaction (order save + outbox write must be atomic)
// without importing Spring's @Transactional into the application layer.
public interface TransactionPort {
    void runInTransaction(Runnable work);
}
