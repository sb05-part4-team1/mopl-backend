package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.notification.NotificationModel.NotificationLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEntityMapper 단위 테스트")
class NotificationEntityMapperTest {

    private final NotificationEntityMapper notificationEntityMapper = new NotificationEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("NotificationEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            NotificationModel result = notificationEntityMapper.toModel(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Entity를 Model로 변환한다")
        void withValidEntity_mapsToModel() {
            // given
            UUID notificationId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            String title = "알림 제목";
            String content = "알림 내용";
            NotificationLevel level = NotificationLevel.INFO;
            Instant now = Instant.now();

            NotificationEntity entity = NotificationEntity.builder()
                .id(notificationId)
                .title(title)
                .content(content)
                .level(level)
                .receiverId(receiverId)
                .createdAt(now)
                .deletedAt(null)
                .build();

            // when
            NotificationModel result = notificationEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(notificationId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getLevel()).isEqualTo(level);
            assertThat(result.getReceiverId()).isEqualTo(receiverId);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("content가 null인 Entity도 정상적으로 변환한다")
        void withNullContent_mapsToModel() {
            // given
            NotificationEntity entity = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .title("제목")
                .content(null)
                .level(NotificationLevel.WARNING)
                .receiverId(UUID.randomUUID())
                .build();

            // when
            NotificationModel result = notificationEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("NotificationModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            NotificationEntity result = notificationEntityMapper.toEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Model을 Entity로 변환한다")
        void withValidModel_mapsToEntity() {
            // given
            UUID notificationId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            String title = "알림 제목";
            String content = "알림 내용";
            NotificationLevel level = NotificationLevel.ERROR;
            Instant now = Instant.now();

            NotificationModel model = NotificationModel.builder()
                .id(notificationId)
                .title(title)
                .content(content)
                .level(level)
                .receiverId(receiverId)
                .createdAt(now)
                .deletedAt(null)
                .build();

            // when
            NotificationEntity result = notificationEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(notificationId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getLevel()).isEqualTo(level);
            assertThat(result.getReceiverId()).isEqualTo(receiverId);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("content가 null인 Model도 정상적으로 변환한다")
        void withNullContent_mapsToEntity() {
            // given
            NotificationModel model = NotificationModel.builder()
                .id(UUID.randomUUID())
                .title("제목")
                .content(null)
                .level(NotificationLevel.INFO)
                .receiverId(UUID.randomUUID())
                .build();

            // when
            NotificationEntity result = notificationEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNull();
        }
    }
}
