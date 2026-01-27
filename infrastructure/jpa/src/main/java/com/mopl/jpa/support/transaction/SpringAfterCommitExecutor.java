package com.mopl.jpa.support.transaction;

import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.logging.context.LogContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class SpringAfterCommitExecutor implements AfterCommitExecutor {

    @Override
    public void execute(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    Thread.startVirtualThread(() -> {
                        try {
                            action.run();
                        } catch (Exception e) {
                            LogContext.with("executor", "afterCommit").error("Error executing action", e);
                        }
                    });
                }
            }
        );
    }
}
