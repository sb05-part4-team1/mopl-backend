package com.mopl.jpa.support.transaction;

import com.mopl.domain.support.transaction.AfterCommitExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
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
                            log.error("Error executing after-commit action", e);
                        }
                    });
                }
            }
        );
    }
}
