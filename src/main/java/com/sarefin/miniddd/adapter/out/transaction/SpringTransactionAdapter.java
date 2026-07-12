package com.sarefin.miniddd.adapter.out.transaction;

import com.sarefin.miniddd.port.out.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// Adapter — the only place that knows Spring manages transactions; lets the application layer demarcate a
// transaction through a plain port instead of putting @Transactional on a use case.
@Component
public class SpringTransactionAdapter implements TransactionPort {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void runInTransaction(Runnable work) {
        transactionTemplate.executeWithoutResult(status -> work.run());
    }
}
