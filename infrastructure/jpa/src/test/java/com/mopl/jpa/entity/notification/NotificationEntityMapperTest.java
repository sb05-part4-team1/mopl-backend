package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationLevel;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEntityMapper 단위 테스트")
class NotificationEntityMapperTest {

    private final NotificationEntityMapper mapper = new NotificationEntityMapper();

    private UserEntity createTestUserEntity(UUID receiverId) {
        return UserEntity.builder()
            .id(receiverId)
            .createdAt(Instant.now())
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("test@example.com")
            .name("테스트")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
    }

    private UserModel createTestUserModel(UUID receiverId) {
        return UserModel.builder()
            .id(receiverId)
            .createdAt(Instant.now())
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("test@example.com")
            .name("테스트")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
    }

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("NotificationEntity를 NotificationModel로 변환")
        void withNotificationEntity_returnsNotificationModel() {
            // given
            UUID id = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            Instant now = Instant.now();
            String title = "알림 제목";
            String content = "알림 내용";

            UserEntity receiver = createTestUserEntity(receiverId);

            NotificationEntity entity = NotificationEntity.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .title(title)
                .content(content)
                .level(NotificationLevel.INFO)
                .receiver(receiver)
                .build();

            // when
            NotificationModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(result.getReceiver().getId()).isEqualTo(receiverId);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            NotificationModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("NotificationModel을 NotificationEntity로 변환")
        void withNotificationModel_returnsNotificationEntity() {
            // given
            UUID id = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            Instant now = Instant.now();
            String title = "알림 제목";
            String content = "알림 내용";

            UserModel receiverModel = createTestUserModel(receiverId);
            UserEntity receiverEntity = createTestUserEntity(receiverId);

            NotificationModel model = NotificationModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .title(title)
                .content(content)
                .level(NotificationLevel.WARNING)
                .receiver(receiverModel)
                .build();

            // when
            NotificationEntity result = mapper.toEntity(model, receiverEntity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getLevel()).isEqualTo(NotificationLevel.WARNING);
            assertThat(result.getReceiver()).isEqualTo(receiverEntity);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // given
            UserEntity receiver = createTestUserEntity(UUID.randomUUID());

            // when
            NotificationEntity result = mapper.toEntity(null, receiver);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            Instant now = Instant.now();

            UserModel receiverModel = createTestUserModel(receiverId);
            UserEntity receiverEntity = createTestUserEntity(receiverId);

            NotificationModel originalModel = NotificationModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .title("알림 제목")
                .content("알림 내용")
                .level(NotificationLevel.ERROR)
                .receiver(receiverModel)
                .build();

            // when
            NotificationEntity entity = mapper.toEntity(originalModel, receiverEntity);
            NotificationModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getDeletedAt()).isEqualTo(originalModel.getDeletedAt());
            assertThat(resultModel.getTitle()).isEqualTo(originalModel.getTitle());
            assertThat(resultModel.getContent()).isEqualTo(originalModel.getContent());
            assertThat(resultModel.getLevel()).isEqualTo(originalModel.getLevel());
            assertThat(resultModel.getReceiver().getId()).isEqualTo(originalModel.getReceiver()
                .getId());
        }
    }
}
