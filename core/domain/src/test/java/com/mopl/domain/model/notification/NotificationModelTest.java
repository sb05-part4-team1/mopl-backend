package com.mopl.domain.model.notification;

import com.mopl.domain.exception.notification.InvalidNotificationDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static com.mopl.domain.model.notification.NotificationModel.CONTENT_MAX_LENGTH;
import static com.mopl.domain.model.notification.NotificationModel.NotificationLevel;
import static com.mopl.domain.model.notification.NotificationModel.TITLE_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            UUID receiverId = UUID.randomUUID();

            // when
            NotificationModel notification = NotificationModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .title("알림 제목")
                .content("알림 내용")
                .level(NotificationLevel.INFO)
                .receiverId(receiverId)
                .build();

            // then
            assertThat(notification.getId()).isEqualTo(id);
            assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
            assertThat(notification.getDeletedAt()).isNull();
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getReceiverId()).isEqualTo(receiverId);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 NotificationModel 생성")
        void withValidData_createsNotificationModel() {
            // given
            UUID receiverId = UUID.randomUUID();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                "알림 내용",
                NotificationLevel.INFO,
                receiverId
            );

            // then
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isEqualTo("알림 내용");
            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getReceiverId()).isEqualTo(receiverId);
        }

        @Test
        @DisplayName("content가 null이어도 생성 가능")
        void withNullContent_createsNotificationModel() {
            // given
            UUID receiverId = UUID.randomUUID();

            // when
            NotificationModel notification = NotificationModel.create(
                "알림 제목",
                null,
                NotificationLevel.INFO,
                receiverId
            );

            // then
            assertThat(notification.getTitle()).isEqualTo("알림 제목");
            assertThat(notification.getContent()).isNull();
        }

        static Stream<Arguments> invalidTitleProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTitleProvider")
        @DisplayName("title이 비어있으면 예외 발생")
        void withEmptyTitle_throwsException(String description, String title) {
            UUID receiverId = UUID.randomUUID();

            assertThatThrownBy(() -> NotificationModel.create(
                title, "내용", NotificationLevel.INFO, receiverId
            ))
                .isInstanceOf(InvalidNotificationDataException.class)
                .satisfies(e -> assertThat(((InvalidNotificationDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("제목은 비어있을 수 없습니다."));
        }

        @Test
        @DisplayName("title이 500자 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            UUID receiverId = UUID.randomUUID();
            String longTitle = "가".repeat(TITLE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> NotificationModel.create(
                longTitle, "내용", NotificationLevel.INFO, receiverId
            ))
                .isInstanceOf(InvalidNotificationDataException.class)
                .satisfies(e -> assertThat(((InvalidNotificationDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        @Test
        @DisplayName("content가 9999자 초과하면 예외 발생")
        void withContentExceedingMaxLength_throwsException() {
            UUID receiverId = UUID.randomUUID();
            String longContent = "가".repeat(CONTENT_MAX_LENGTH + 1);

            assertThatThrownBy(() -> NotificationModel.create(
                "제목", longContent, NotificationLevel.INFO, receiverId
            ))
                .isInstanceOf(InvalidNotificationDataException.class)
                .satisfies(e -> assertThat(((InvalidNotificationDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("내용은 " + CONTENT_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        @Test
        @DisplayName("level이 null이면 예외 발생")
        void withNullLevel_throwsException() {
            UUID receiverId = UUID.randomUUID();

            assertThatThrownBy(() -> NotificationModel.create(
                "제목", "내용", null, receiverId
            ))
                .isInstanceOf(InvalidNotificationDataException.class)
                .satisfies(e -> assertThat(((InvalidNotificationDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("알림 레벨은 null일 수 없습니다."));
        }

        @Test
        @DisplayName("receiverId가 null이면 예외 발생")
        void withNullReceiverId_throwsException() {
            assertThatThrownBy(() -> NotificationModel.create(
                "제목", "내용", NotificationLevel.INFO, null
            ))
                .isInstanceOf(InvalidNotificationDataException.class)
                .satisfies(e -> assertThat(((InvalidNotificationDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("수신자 ID는 null일 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("NotificationLevel")
    class NotificationLevelTest {

        @Test
        @DisplayName("모든 레벨 값이 존재함")
        void allLevelsExist() {
            assertThat(NotificationLevel.values())
                .containsExactlyInAnyOrder(
                    NotificationLevel.INFO,
                    NotificationLevel.WARNING,
                    NotificationLevel.ERROR
                );
        }
    }
}
