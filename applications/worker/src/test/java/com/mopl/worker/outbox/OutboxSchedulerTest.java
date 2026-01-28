package com.mopl.worker.outbox;

import com.mopl.domain.fixture.OutboxModelFixture;
import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxScheduler 단위 테스트")
class OutboxSchedulerTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxScheduler(outboxRepository, kafkaTemplate);
    }

    @Nested
    @DisplayName("publishPendingEvents()")
    class PublishPendingEventsTest {

        @Test
        @DisplayName("pending 이벤트가 없으면 아무 작업도 하지 않음")
        void withNoPendingEvents_doesNothing() {
            // given
            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of());

            // when
            scheduler.publishPendingEvents();

            // then
            then(kafkaTemplate).should(never()).send(any(), any(), any());
            then(outboxRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("pending 이벤트 발행 성공 시 PUBLISHED로 마킹")
        void withPendingEvent_publishesAndMarksAsPublished() {
            // given
            OutboxModel event = OutboxModelFixture.builder()
                .set("topic", "test-topic")
                .set("aggregateId", "agg-123")
                .set("payload", "{\"data\":\"test\"}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 0)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event));

            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(
                createSendResult("test-topic")
            );
            given(kafkaTemplate.send(eq("test-topic"), eq("agg-123"), eq("{\"data\":\"test\"}"))).willReturn(future);

            // when
            scheduler.publishPendingEvents();

            // then
            then(kafkaTemplate).should().send("test-topic", "agg-123", "{\"data\":\"test\"}");

            ArgumentCaptor<OutboxModel> captor = ArgumentCaptor.forClass(OutboxModel.class);
            then(outboxRepository).should().save(captor.capture());

            OutboxModel saved = captor.getValue();
            assertThat(saved.isPublished()).isTrue();
            assertThat(saved.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("여러 pending 이벤트 모두 발행")
        void withMultiplePendingEvents_publishesAll() {
            // given
            OutboxModel event1 = OutboxModelFixture.builder()
                .set("topic", "topic-1")
                .set("aggregateId", "agg-1")
                .set("payload", "{\"id\":1}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .sample();

            OutboxModel event2 = OutboxModelFixture.builder()
                .set("topic", "topic-2")
                .set("aggregateId", "agg-2")
                .set("payload", "{\"id\":2}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event1, event2));

            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(
                createSendResult("topic")
            );
            given(kafkaTemplate.send(any(), any(), any())).willReturn(future);

            // when
            scheduler.publishPendingEvents();

            // then
            then(kafkaTemplate).should(times(2)).send(any(), any(), any());
            then(outboxRepository).should(times(2)).save(any());
        }

        @Test
        @DisplayName("발행 실패 시 retryCount 증가")
        void withPublishFailure_incrementsRetryCount() {
            // given
            OutboxModel event = OutboxModelFixture.builder()
                .set("topic", "test-topic")
                .set("aggregateId", "agg-123")
                .set("payload", "{\"data\":\"test\"}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 0)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event));

            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new TimeoutException("Kafka timeout"));
            given(kafkaTemplate.send(any(), any(), any())).willReturn(failedFuture);

            // when
            scheduler.publishPendingEvents();

            // then
            ArgumentCaptor<OutboxModel> captor = ArgumentCaptor.forClass(OutboxModel.class);
            then(outboxRepository).should().save(captor.capture());

            OutboxModel saved = captor.getValue();
            assertThat(saved.getRetryCount()).isEqualTo(1);
            assertThat(saved.isPending()).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 횟수 도달 시 FAILED로 마킹")
        void withMaxRetryReached_marksAsFailed() {
            // given
            OutboxModel event = OutboxModelFixture.builder()
                .set("topic", "test-topic")
                .set("aggregateId", "agg-123")
                .set("payload", "{\"data\":\"test\"}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 2)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event));

            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka error"));
            given(kafkaTemplate.send(any(), any(), any())).willReturn(failedFuture);

            // when
            scheduler.publishPendingEvents();

            // then
            ArgumentCaptor<OutboxModel> captor = ArgumentCaptor.forClass(OutboxModel.class);
            then(outboxRepository).should().save(captor.capture());

            OutboxModel saved = captor.getValue();
            assertThat(saved.getRetryCount()).isEqualTo(3);
            assertThat(saved.isFailed()).isTrue();
        }

        @Test
        @DisplayName("일부 이벤트 실패해도 나머지 이벤트는 계속 처리")
        void withPartialFailure_continuesProcessingOtherEvents() {
            // given
            OutboxModel event1 = OutboxModelFixture.builder()
                .set("topic", "topic-1")
                .set("aggregateId", "agg-1")
                .set("payload", "{\"id\":1}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 0)
                .sample();

            OutboxModel event2 = OutboxModelFixture.builder()
                .set("topic", "topic-2")
                .set("aggregateId", "agg-2")
                .set("payload", "{\"id\":2}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 0)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event1, event2));

            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka error"));

            CompletableFuture<SendResult<String, Object>> successFuture = CompletableFuture.completedFuture(
                createSendResult("topic-2")
            );

            given(kafkaTemplate.send(eq("topic-1"), any(), any())).willReturn(failedFuture);
            given(kafkaTemplate.send(eq("topic-2"), any(), any())).willReturn(successFuture);

            // when
            scheduler.publishPendingEvents();

            // then
            then(kafkaTemplate).should(times(2)).send(any(), any(), any());
            then(outboxRepository).should(times(2)).save(any());
        }

        @Test
        @DisplayName("이벤트 발행 실패 후 저장 실패해도 예외 처리")
        void withPublishFailureAndSaveFailure_handlesException() {
            // given
            OutboxModel event = OutboxModelFixture.builder()
                .set("topic", "test-topic")
                .set("aggregateId", "agg-123")
                .set("payload", "{\"data\":\"test\"}")
                .set("status", OutboxModel.OutboxStatus.PENDING)
                .set("retryCount", 0)
                .sample();

            given(outboxRepository.findPendingEvents(anyInt(), anyInt())).willReturn(List.of(event));

            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka error"));
            given(kafkaTemplate.send(any(), any(), any())).willReturn(failedFuture);
            given(outboxRepository.save(any())).willThrow(new RuntimeException("Save failed"));

            // when
            scheduler.publishPendingEvents();

            // then
            then(outboxRepository).should().save(any());
        }
    }

    @Nested
    @DisplayName("cleanupOldEvents()")
    class CleanupOldEventsTest {

        @Test
        @DisplayName("오래된 이벤트 정리 호출")
        void cleansUpOldPublishedEvents() {
            // given
            given(outboxRepository.deletePublishedEventsBefore(any(Instant.class))).willReturn(10);

            // when
            scheduler.cleanupOldEvents();

            // then
            ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
            then(outboxRepository).should().deletePublishedEventsBefore(captor.capture());

            Instant cutoff = captor.getValue();
            assertThat(cutoff).isBefore(Instant.now().minusSeconds(6 * 24 * 60 * 60));
        }

        @Test
        @DisplayName("삭제할 이벤트가 없어도 정상 동작")
        void withNoEventsToDelete_completesNormally() {
            // given
            given(outboxRepository.deletePublishedEventsBefore(any(Instant.class))).willReturn(0);

            // when
            scheduler.cleanupOldEvents();

            // then
            then(outboxRepository).should().deletePublishedEventsBefore(any(Instant.class));
        }
    }

    private SendResult<String, Object> createSendResult(String topic) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "key", "value");
        RecordMetadata metadata = new RecordMetadata(
            new org.apache.kafka.common.TopicPartition(topic, 0),
            0, 0, 0, 0, 0
        );
        return new SendResult<>(record, metadata);
    }
}
