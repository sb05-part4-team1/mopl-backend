package com.mopl.domain.model.notification;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationModel 단위 테스트")
class NotificationModelTest {

    private UserModel createTestUser() {
        return UserModel.builder()
            .id(UUID.randomUUID())
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
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void withBuilder_initializesAllFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserModel receiver = createTestUser();

            // when
            NotificationModel notification = NotificationModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .title("알림 제목")
                .content("알림 내용")
                .level(NotificationLevel.INFO)
                .receiver(receiver)
                .build();

            // then
            assertThat(notification.getId()).isEqualTo(id);
            assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getReceiver()).isEqualTo(receiver);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 NotificationModel 생성")
        void withValidData_createsNotificationModel() {
            // given
            UserModel receiver = createTestUser();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                "알림 내용",
                NotificationLevel.INFO,
                receiver
            );

            // then
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getReceiver()).isEqualTo(receiver);
        }

        @Test
        @DisplayName("WARNING 레벨로 NotificationModel 생성")
        void withWarningLevel_createsNotificationModel() {
            // given
            UserModel receiver = createTestUser();

            // when
            NotificationModel notification = NotificationModel.create(
                "경고 알림",
                "경고 내용",
                NotificationLevel.WARNING,
                receiver
            );

            // then
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.WARNING);
        }

        @Test
        @DisplayName("ERROR 레벨로 NotificationModel 생성")
        void withErrorLevel_createsNotificationModel() {
            // given
            UserModel receiver = createTestUser();

            // when
            NotificationModel notification = NotificationModel.create(
                "에러 알림",
                "에러 내용",
                NotificationLevel.ERROR,
                receiver
            );

            // then
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.ERROR);
        }

        @Test
        @DisplayName("content가 null이어도 생성 가능")
        void withNullContent_createsNotificationModel() {
            // given
            UserModel receiver = createTestUser();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                null,
                NotificationLevel.INFO,
                receiver
            );

            // then
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isNull();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("삭제 시 deletedAt이 설정됨")
        void withValidNotification_setsDeletedAt() {
            // given
            UserModel receiver = createTestUser();
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                "알림 내용",
                NotificationLevel.INFO,
                receiver
            );

            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.isDeleted()).isFalse();

            // when
            notification.delete();

            // then
            assertThat(notification.getDeletedAt()).isNotNull();
            assertThat(notification.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("restore()")
    class RestoreTest {

        @Test
        @DisplayName("복원 시 deletedAt이 null로 설정됨")
        void withDeletedNotification_clearsDeletedAt() {
            // given
            UserModel receiver = createTestUser();
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                "알림 내용",
                NotificationLevel.INFO,
                receiver
            );
            notification.delete();

            assertThat(notification.isDeleted()).isTrue();

            // when
            notification.restore();

            // then
            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.isDeleted()).isFalse();
        }
    }
}
