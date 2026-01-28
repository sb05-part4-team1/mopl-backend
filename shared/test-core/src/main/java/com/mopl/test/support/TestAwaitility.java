package com.mopl.test.support;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Utility class for async testing with Awaitility.
 *
 * <p>Provides convenient methods for waiting on async operations in tests.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @Test
 * void testAsyncOperation() {
 * // Trigger async operation
 * service.processAsync(data);
 *
 * // Wait for result
 * TestAwaitility.awaitUntil(() -> repository.findById(id).isPresent());
 *
 * // Or with custom timeout
 * TestAwaitility.awaitUntil(
 * () -> repository.count() == expectedCount,
 * Duration.ofSeconds(10)
 * );
 * }
 * }</pre>
 */
public final class TestAwaitility {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);

    private TestAwaitility() {
    }

    /**
     * Returns a default configured ConditionFactory.
     */
    public static ConditionFactory await() {
        return Awaitility.await()
            .atMost(DEFAULT_TIMEOUT)
            .pollInterval(DEFAULT_POLL_INTERVAL);
    }

    /**
     * Returns a ConditionFactory with custom timeout.
     */
    public static ConditionFactory await(Duration timeout) {
        return Awaitility.await()
            .atMost(timeout)
            .pollInterval(DEFAULT_POLL_INTERVAL);
    }

    /**
     * Waits until the condition is true with default timeout.
     */
    public static void awaitUntil(Callable<Boolean> condition) {
        await().until(condition);
    }

    /**
     * Waits until the condition is true with custom timeout.
     */
    public static void awaitUntil(Callable<Boolean> condition, Duration timeout) {
        await(timeout).until(condition);
    }

    /**
     * Waits until the callable returns a non-null value.
     */
    public static <T> T awaitNonNull(Callable<T> callable) {
        return await().until(callable, result -> result != null);
    }

    /**
     * Waits until the callable returns a non-null value with custom timeout.
     */
    public static <T> T awaitNonNull(Callable<T> callable, Duration timeout) {
        return await(timeout).until(callable, Objects::nonNull);
    }
}
