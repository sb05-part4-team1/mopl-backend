package com.mopl.jpa.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.outbox.OutboxEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    OutboxRepositoryImpl.class,
    OutboxEntityMapper.class
})
@DisplayName("OutboxRepositoryImpl 슬라이스 테스트")
class OutboxRepositoryImplTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 OutboxModel을 저장하고 반환한다")
        void withNewOutbox_savesAndReturnsOutbox() {
            // given
            OutboxModel outbox = OutboxModel.create(
                "User",
                "550e8400-e29b-41d4-a716-446655440000",
                "UserCreated",
                "mopl.user.created",
                "{\"userId\":\"123\"}"
            );

            // when
            OutboxModel saved = outboxRepository.save(outbox);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAggregateType()).isEqualTo("User");
            assertThat(saved.getAggregateId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
            assertThat(saved.getEventType()).isEqualTo("UserCreated");
            assertThat(saved.getTopic()).isEqualTo("mopl.user.created");
            assertThat(saved.getPayload()).isEqualTo("{\"userId\":\"123\"}");
            assertThat(saved.getStatus()).isEqualTo(OutboxModel.OutboxStatus.PENDING);
            assertThat(saved.getRetryCount()).isZero();
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findPendingEvents()")
    class FindPendingEventsTest {

        @Test
        @DisplayName("PENDING 상태의 이벤트를 조회한다")
        void withPendingEvents_returnsPendingEvents() {
            // given
            outboxRepository.save(createOutbox("Event1"));
            outboxRepository.save(createOutbox("Event2"));

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 10);

            // then
            assertThat(pendingEvents).hasSize(2);
            assertThat(pendingEvents).extracting(OutboxModel::getEventType)
                .containsExactly("Event1", "Event2");
        }

        @Test
        @DisplayName("createdAt 오름차순으로 정렬된다")
        void withMultipleEvents_orderedByCreatedAtAsc() throws InterruptedException {
            // given
            outboxRepository.save(createOutbox("First"));
            Thread.sleep(10);
            outboxRepository.save(createOutbox("Second"));
            Thread.sleep(10);
            outboxRepository.save(createOutbox("Third"));

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 10);

            // then
            assertThat(pendingEvents).hasSize(3);
            assertThat(pendingEvents.get(0).getEventType()).isEqualTo("First");
            assertThat(pendingEvents.get(1).getEventType()).isEqualTo("Second");
            assertThat(pendingEvents.get(2).getEventType()).isEqualTo("Third");
        }

        @Test
        @DisplayName("limit만큼만 조회한다")
        void withMoreEventsThanLimit_returnsLimitedEvents() {
            // given
            for (int i = 0; i < 5; i++) {
                outboxRepository.save(createOutbox("Event" + i));
            }

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 3);

            // then
            assertThat(pendingEvents).hasSize(3);
        }

        @Test
        @DisplayName("retryCount가 maxRetry 이상인 이벤트는 조회되지 않는다")
        void withHighRetryCount_excludesEvents() {
            // given
            outboxRepository.save(createOutbox("Normal"));

            OutboxModel retriedOutbox = createOutbox("Retried");
            retriedOutbox.incrementRetryCount();
            retriedOutbox.incrementRetryCount();
            retriedOutbox.incrementRetryCount();
            outboxRepository.save(retriedOutbox);

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 10);

            // then
            assertThat(pendingEvents).hasSize(1);
            assertThat(pendingEvents.get(0).getEventType()).isEqualTo("Normal");
        }

        @Test
        @DisplayName("PUBLISHED 상태의 이벤트는 조회되지 않는다")
        void withPublishedEvents_excludesPublishedEvents() {
            // given
            outboxRepository.save(createOutbox("Pending"));

            OutboxModel publishedOutbox = createOutbox("Published");
            publishedOutbox.markAsPublished();
            outboxRepository.save(publishedOutbox);

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 10);

            // then
            assertThat(pendingEvents).hasSize(1);
            assertThat(pendingEvents.get(0).getEventType()).isEqualTo("Pending");
        }

        @Test
        @DisplayName("FAILED 상태의 이벤트는 조회되지 않는다")
        void withFailedEvents_excludesFailedEvents() {
            // given
            outboxRepository.save(createOutbox("Pending"));

            OutboxModel failedOutbox = createOutbox("Failed");
            failedOutbox.markAsFailed();
            outboxRepository.save(failedOutbox);

            // when
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(3, 10);

            // then
            assertThat(pendingEvents).hasSize(1);
            assertThat(pendingEvents.get(0).getEventType()).isEqualTo("Pending");
        }
    }

    @Nested
    @DisplayName("deletePublishedEventsBefore()")
    class DeletePublishedEventsBeforeTest {

        @Test
        @DisplayName("지정된 시간 이전에 발행된 이벤트를 삭제한다")
        void withOldPublishedEvents_deletesOldEvents() {
            // given
            OutboxModel oldPublished = createOutbox("OldPublished");
            oldPublished.markAsPublished();
            outboxRepository.save(oldPublished);

            Instant cutoff = Instant.now().plus(1, ChronoUnit.HOURS);

            // when
            int deletedCount = outboxRepository.deletePublishedEventsBefore(cutoff);

            // then
            assertThat(deletedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("PENDING 상태의 이벤트는 삭제되지 않는다")
        void withPendingEvents_doesNotDelete() {
            // given
            outboxRepository.save(createOutbox("Pending"));

            Instant cutoff = Instant.now().plus(1, ChronoUnit.HOURS);

            // when
            int deletedCount = outboxRepository.deletePublishedEventsBefore(cutoff);

            // then
            assertThat(deletedCount).isZero();
        }
    }

    private OutboxModel createOutbox(String eventType) {
        return OutboxModel.create(
            "TestAggregate",
            "550e8400-e29b-41d4-a716-446655440000",
            eventType,
            "test.topic",
            "{\"test\":\"data\"}"
        );
    }
}
