package com.mopl.test.support;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Utility for executing code within specific transaction boundaries in tests.
 *
 * <p>Useful when you need to persist entities and then query them in a separate transaction,
 * or when testing transactional behavior.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @Autowired
 * private TestTransaction testTransaction;
 *
 * @Test
 *       void testTransactionalBehavior() {
 *       // Save in one transaction
 *       var savedEntity = testTransaction.execute(() -> {
 *       return repository.save(entity);
 *       });
 *
 *       // Query in another transaction
 *       var foundEntity = testTransaction.execute(() -> {
 *       return repository.findById(savedEntity.getId());
 *       });
 *       }
 *       }</pre>
 */
@Component
public class TestTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Runnable action) {
        action.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public <T> T executeReadOnly(Supplier<T> action) {
        return action.get();
    }
}
