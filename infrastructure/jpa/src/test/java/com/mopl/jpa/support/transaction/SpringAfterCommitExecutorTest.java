package com.mopl.jpa.support.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
    @DisplayName("execute()")
    class ExecuteTest {

        @Nested
        @DisplayName("동기화 비활성화 시")
        class WhenSynchronizationNotActive {

            @Test
            @DisplayName("액션 즉시 실행")
            void executesActionImmediately() {
                // given
                AtomicBoolean executed = new AtomicBoolean(false);

                // when
                executor.execute(() -> executed.set(true));

                // then
                assertThat(executed.get()).isTrue();
            }

            @Test
            @DisplayName("여러 액션 순서대로 즉시 실행")
            void executesMultipleActionsInOrder() {
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
        @DisplayName("동기화 활성화 시")
        class WhenSynchronizationActive {

            @BeforeEach
            void initSynchronization() {
                TransactionSynchronizationManager.initSynchronization();
            }

            @AfterEach
            void clearSynchronization() {
                TransactionSynchronizationManager.clearSynchronization();
            }

            @Test
            @DisplayName("액션 즉시 실행되지 않고 등록")
            void defersActionExecution() {
                // given
                AtomicBoolean executed = new AtomicBoolean(false);

                // when
                executor.execute(() -> executed.set(true));

                // then
                assertThat(executed.get()).isFalse();
                assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
            }

            @Test
            @DisplayName("afterCommit 호출 시 가상 스레드에서 실행")
            void executesOnVirtualThreadAfterCommit() throws InterruptedException {
                // given
                AtomicBoolean executed = new AtomicBoolean(false);
                CountDownLatch latch = new CountDownLatch(1);

                executor.execute(() -> {
                    executed.set(true);
                    latch.countDown();
                });

                List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();

                // when
                syncs.forEach(TransactionSynchronization::afterCommit);

                // then
                assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
                assertThat(executed.get()).isTrue();
            }

            @Test
            @DisplayName("여러 액션 등록 후 afterCommit 시 모두 실행")
            void executesAllActionsAfterCommit() throws InterruptedException {
                // given
                AtomicInteger counter = new AtomicInteger(0);
                CountDownLatch latch = new CountDownLatch(3);

                for (int i = 0; i < 3; i++) {
                    executor.execute(() -> {
                        counter.incrementAndGet();
                        latch.countDown();
                    });
                }

                assertThat(counter.get()).isZero();
                assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(3);

                List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();

                // when
                syncs.forEach(TransactionSynchronization::afterCommit);

                // then
                assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
                assertThat(counter.get()).isEqualTo(3);
            }
        }
    }
}
