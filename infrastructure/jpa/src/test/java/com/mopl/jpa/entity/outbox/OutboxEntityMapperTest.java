package com.mopl.jpa.entity.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.model.outbox.OutboxModel.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxEntityMapper 단위 테스트")
class OutboxEntityMapperTest {

    private final OutboxEntityMapper mapper = new OutboxEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("OutboxEntity를 OutboxModel로 변환")
        void withOutboxEntity_returnsOutboxModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant publishedAt = Instant.now();
            OutboxEntity entity = OutboxEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .aggregateType("PLAYLIST")
                .aggregateId("12345678-1234-1234-1234-123456789abc")
                .eventType("PLAYLIST_CREATED")
                .topic("playlist-events")
                .payload("{\"id\":\"12345678-1234-1234-1234-123456789abc\"}")
                .status(OutboxStatus.PUBLISHED)
                .publishedAt(publishedAt)
                .retryCount(2)
                .build();

            // when
            OutboxModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getAggregateType()).isEqualTo("PLAYLIST");
            assertThat(result.getAggregateId()).isEqualTo("12345678-1234-1234-1234-123456789abc");
            assertThat(result.getEventType()).isEqualTo("PLAYLIST_CREATED");
            assertThat(result.getTopic()).isEqualTo("playlist-events");
            assertThat(result.getPayload()).isEqualTo("{\"id\":\"12345678-1234-1234-1234-123456789abc\"}");
            assertThat(result.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            assertThat(result.getPublishedAt()).isEqualTo(publishedAt);
            assertThat(result.getRetryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("PENDING 상태의 OutboxEntity를 OutboxModel로 변환")
        void withPendingOutboxEntity_returnsOutboxModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            OutboxEntity entity = OutboxEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .aggregateType("REVIEW")
                .aggregateId("87654321-4321-4321-4321-cba987654321")
                .eventType("REVIEW_CREATED")
                .topic("review-events")
                .payload("{\"rating\":5}")
                .status(OutboxStatus.PENDING)
                .publishedAt(null)
                .retryCount(0)
                .build();

            // when
            OutboxModel result = mapper.toModel(entity);

            // then
            assertThat(result.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(result.getPublishedAt()).isNull();
            assertThat(result.getRetryCount()).isZero();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            OutboxModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("OutboxModel을 OutboxEntity로 변환")
        void withOutboxModel_returnsOutboxEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant publishedAt = Instant.now();
            OutboxModel model = OutboxModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .aggregateType("CONTENT")
                .aggregateId("content-uuid")
                .eventType("CONTENT_UPDATED")
                .topic("content-events")
                .payload("{\"title\":\"새 제목\"}")
                .status(OutboxStatus.PUBLISHED)
                .publishedAt(publishedAt)
                .retryCount(1)
                .build();

            // when
            OutboxEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getAggregateType()).isEqualTo("CONTENT");
            assertThat(result.getAggregateId()).isEqualTo("content-uuid");
            assertThat(result.getEventType()).isEqualTo("CONTENT_UPDATED");
            assertThat(result.getTopic()).isEqualTo("content-events");
            assertThat(result.getPayload()).isEqualTo("{\"title\":\"새 제목\"}");
            assertThat(result.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            assertThat(result.getPublishedAt()).isEqualTo(publishedAt);
            assertThat(result.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            OutboxEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant publishedAt = Instant.now();
            OutboxModel originalModel = OutboxModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .aggregateType("NOTIFICATION")
                .aggregateId("notification-id")
                .eventType("NOTIFICATION_SENT")
                .topic("notification-events")
                .payload("{\"type\":\"FOLLOW\"}")
                .status(OutboxStatus.PUBLISHED)
                .publishedAt(publishedAt)
                .retryCount(0)
                .build();

            // when
            OutboxEntity entity = mapper.toEntity(originalModel);
            OutboxModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getDeletedAt()).isNull();
            assertThat(resultModel.getAggregateType()).isEqualTo(originalModel.getAggregateType());
            assertThat(resultModel.getAggregateId()).isEqualTo(originalModel.getAggregateId());
            assertThat(resultModel.getEventType()).isEqualTo(originalModel.getEventType());
            assertThat(resultModel.getTopic()).isEqualTo(originalModel.getTopic());
            assertThat(resultModel.getPayload()).isEqualTo(originalModel.getPayload());
            assertThat(resultModel.getStatus()).isEqualTo(originalModel.getStatus());
            assertThat(resultModel.getPublishedAt()).isEqualTo(originalModel.getPublishedAt());
            assertThat(resultModel.getRetryCount()).isEqualTo(originalModel.getRetryCount());
        }
    }
}
