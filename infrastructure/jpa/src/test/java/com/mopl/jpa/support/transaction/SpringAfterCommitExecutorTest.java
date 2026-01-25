package com.mopl.jpa.support.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpringAfterCommitExecutor 단위 테스트")
class SpringAfterCommitExecutorTest {

    private SpringAfterCommitExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new SpringAfterCommitExecutor();
    }

    @Nested
    @DisplayName("트랜잭션 동기화가 비활성화된 경우")
    class WhenSynchronizationNotActive {

        @Test
        @DisplayName("액션이 즉시 실행된다")
        void executesActionImmediately() {
            // given
            AtomicBoolean executed = new AtomicBoolean(false);

            // when
            executor.execute(() -> executed.set(true));

            // then
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("여러 액션이 순서대로 즉시 실행된다")
        void executesMultipleActionsImmediately() {
            // given
            AtomicInteger counter = new AtomicInteger(0);

            // when
            executor.execute(counter::incrementAndGet);
            executor.execute(counter::incrementAndGet);
            executor.execute(counter::incrementAndGet);

            // then
            assertThat(counter.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("트랜잭션 동기화가 활성화된 경우")
    class WhenSynchronizationActive {

        @BeforeEach
        void initSynchronization() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @Test
        @DisplayName("액션이 즉시 실행되지 않고 등록된다")
        void defersActionExecution() {
            // given
            AtomicBoolean executed = new AtomicBoolean(false);

            // when
            executor.execute(() -> executed.set(true));

            // then
            assertThat(executed.get()).isFalse();
            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

            // cleanup
            TransactionSynchronizationManager.clearSynchronization();
        }

        @Test
        @DisplayName("afterCommit 호출 시 액션이 실행된다")
        void executesActionOnAfterCommit() {
            // given
            AtomicBoolean executed = new AtomicBoolean(false);
            executor.execute(() -> executed.set(true));

            // when
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            // then
            assertThat(executed.get()).isTrue();

            // cleanup
            TransactionSynchronizationManager.clearSynchronization();
        }

        @Test
        @DisplayName("여러 액션이 등록되고 afterCommit 시 모두 실행된다")
        void executesMultipleActionsOnAfterCommit() {
            // given
            AtomicInteger counter = new AtomicInteger(0);
            executor.execute(counter::incrementAndGet);
            executor.execute(counter::incrementAndGet);
            executor.execute(counter::incrementAndGet);

            assertThat(counter.get()).isZero();
            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(3);

            // when
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }

            // then
            assertThat(counter.get()).isEqualTo(3);

            // cleanup
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
