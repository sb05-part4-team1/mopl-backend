package com.mopl.api.interfaces.api.notification;

import com.mopl.domain.fixture.NotificationModelFixture;
import com.mopl.domain.model.notification.NotificationModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationResponseMapper 단위 테스트")
class NotificationResponseMapperTest {

    private final NotificationResponseMapper mapper = new NotificationResponseMapper();

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("NotificationModel을 NotificationResponse로 변환")
        void withNotificationModel_returnsNotificationResponse() {
            // given
            NotificationModel notificationModel = NotificationModelFixture.create();

            // when
            NotificationResponse result = mapper.toResponse(notificationModel);

            // then
            assertThat(result.id()).isEqualTo(notificationModel.getId());
            assertThat(result.createdAt()).isEqualTo(notificationModel.getCreatedAt());
            assertThat(result.receiverId()).isEqualTo(notificationModel.getReceiver().getId());
            assertThat(result.title()).isEqualTo(notificationModel.getTitle());
            assertThat(result.content()).isEqualTo(notificationModel.getContent());
            assertThat(result.level()).isEqualTo(notificationModel.getLevel());
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            NotificationResponse result = mapper.toResponse(null);

            // then
            assertThat(result).isNull();
        }
    }
}
