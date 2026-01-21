package com.mopl.domain.model.notification;

import com.mopl.domain.fixture.NotificationModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationModel 단위 테스트")
class NotificationModelTest {

    @Nested
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void withBuilder_initializesAllFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserModel receiver = UserModelFixture.create();

            // when
            NotificationModel notification = NotificationModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .title("알림 제목")
                .content("알림 내용")
                .level(NotificationModel.NotificationLevel.INFO)
                .receiver(receiver)
                .build();

            // then
            assertThat(notification.getId()).isEqualTo(id);
            assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationModel.NotificationLevel.INFO);
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
            UserModel receiver = UserModelFixture.create();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                "알림 내용",
                NotificationModel.NotificationLevel.INFO,
                receiver
            );

            // then
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationModel.NotificationLevel.INFO);
            assertThat(notification.getReceiver()).isEqualTo(receiver);
        }

        @Test
        @DisplayName("WARNING 레벨로 NotificationModel 생성")
        void withWarningLevel_createsNotificationModel() {
            // given
            UserModel receiver = UserModelFixture.create();

            // when
            NotificationModel notification = NotificationModel.create(
                "경고 알림",
                "경고 내용",
                NotificationModel.NotificationLevel.WARNING,
                receiver
            );

            // then
            assertThat(notification.getLevel()).isEqualTo(NotificationModel.NotificationLevel.WARNING);
        }

        @Test
        @DisplayName("ERROR 레벨로 NotificationModel 생성")
        void withErrorLevel_createsNotificationModel() {
            // given
            UserModel receiver = UserModelFixture.create();

            // when
            NotificationModel notification = NotificationModel.create(
                "에러 알림",
                "에러 내용",
                NotificationModel.NotificationLevel.ERROR,
                receiver
            );

            // then
            assertThat(notification.getLevel()).isEqualTo(NotificationModel.NotificationLevel.ERROR);
        }

        @Test
        @DisplayName("content가 null이어도 생성 가능")
        void withNullContent_createsNotificationModel() {
            // given
            UserModel receiver = UserModelFixture.create();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                null,
                NotificationModel.NotificationLevel.INFO,
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
            NotificationModel notification = NotificationModelFixture.create();

            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.isDeleted()).isFalse();

            // when
            notification.delete();

            // then
            assertThat(notification.getDeletedAt()).isNotNull();
            assertThat(notification.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("이미 삭제된 알림을 다시 삭제해도 에러 없이 멱등성이 보장된다")
        void deleteAlreadyDeletedNotification_isIdempotent() {
            // given
            NotificationModel notification = NotificationModelFixture.create();
            notification.delete();

            // when
            notification.delete();

            // then
            assertThat(notification).isNotNull();
            assertThat(notification.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("restore()")
    class RestoreTest {

        @Test
        @DisplayName("복원 시 deletedAt이 null로 설정됨")
        void withDeletedNotification_clearsDeletedAt() {
            // given
            NotificationModel notification = NotificationModelFixture.create();
            notification.delete();

            assertThat(notification.isDeleted()).isTrue();

            // when
            notification.restore();

            // then
            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("NotificationLevel")
    class NotificationLevelTest {

        @Test
        @DisplayName("모든 레벨 값이 존재함")
        void allLevelsExist() {
            assertThat(NotificationModel.NotificationLevel.values())
                .containsExactlyInAnyOrder(
                    NotificationModel.NotificationLevel.INFO,
                    NotificationModel.NotificationLevel.WARNING,
                    NotificationModel.NotificationLevel.ERROR
                );
        }
    }
}
