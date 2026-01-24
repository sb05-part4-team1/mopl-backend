package com.mopl.api.application.notification;

import com.mopl.api.interfaces.api.notification.dto.NotificationResponse;
import com.mopl.api.interfaces.api.notification.mapper.NotificationResponseMapper;
import com.mopl.domain.exception.notification.NotificationForbiddenException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.fixture.NotificationModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.repository.notification.NotificationSortField;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationFacade 단위 테스트")
class NotificationFacadeTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Spy
    private NotificationResponseMapper notificationResponseMapper = new NotificationResponseMapper();

    @InjectMocks
    private NotificationFacade notificationFacade;

    @Nested
    @DisplayName("getNotifications()")
    class GetNotificationsTest {

        @Test
        @DisplayName("유효한 요청 시 알림 목록 조회 성공")
        void withValidRequest_getNotificationsSuccess() {
            // given
            UUID userId = UUID.randomUUID();

            NotificationModel notification1 = NotificationModelFixture.builder()
                .set("receiverId", userId)
                .set("title", "알림1")
                .sample();
            NotificationModel notification2 = NotificationModelFixture.builder()
                .set("receiverId", userId)
                .set("title", "알림2")
                .sample();

            CursorResponse<NotificationModel> serviceResponse = CursorResponse.of(
                List.of(notification1, notification2),
                notification2.getCreatedAt().toString(),
                notification2.getId(),
                true,
                10,
                "createdAt",
                SortDirection.ASCENDING
            );

            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 10, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            given(userService.getById(userId)).willReturn(UserModelFixture.builder().set("id", userId).sample());
            given(notificationService.getAll(userId, request)).willReturn(serviceResponse);

            // when
            CursorResponse<NotificationResponse> result = notificationFacade.getNotifications(
                userId, request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data().getFirst().title()).isEqualTo("알림1");
            assertThat(result.data().get(1).title()).isEqualTo("알림2");
            assertThat(result.hasNext()).isTrue();
            assertThat(result.totalCount()).isEqualTo(10);

            then(userService).should().getById(userId);
            then(notificationService).should().getAll(userId, request);
        }

        @Test
        @DisplayName("빈 결과 시 빈 목록 반환")
        void withNoNotifications_returnsEmptyList() {
            // given
            UUID userId = UUID.randomUUID();

            CursorResponse<NotificationModel> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.ASCENDING
            );

            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 10, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            given(userService.getById(userId)).willReturn(UserModelFixture.builder().set("id", userId).sample());
            given(notificationService.getAll(userId, request)).willReturn(emptyResponse);

            // when
            CursorResponse<NotificationResponse> result = notificationFacade.getNotifications(
                userId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();

            then(userService).should().getById(userId);
            then(notificationService).should().getAll(userId, request);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 요청 시 UserNotFoundException 발생")
        void withNonExistingUser_throwsUserNotFoundException() {
            // given
            UUID userId = UUID.randomUUID();
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 10, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            given(userService.getById(userId)).willThrow(UserNotFoundException.withId(userId));

            // when & then
            assertThatThrownBy(() -> notificationFacade.getNotifications(userId, request))
                .isInstanceOf(UserNotFoundException.class);

            then(notificationService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("readNotification()")
    class ReadNotificationTest {

        @Test
        @DisplayName("유효한 요청 시 알림 읽음 처리 성공")
        void withValidRequest_readNotificationSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            NotificationModel notification = NotificationModelFixture.builder()
                .set("id", notificationId)
                .set("receiverId", userId)
                .sample();

            given(userService.getById(userId)).willReturn(UserModelFixture.builder().set("id", userId).sample());
            given(notificationService.getById(notificationId)).willReturn(notification);
            willDoNothing().given(notificationService).deleteById(notificationId);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> notificationFacade.readNotification(userId, notificationId));

            then(userService).should().getById(userId);
            then(notificationService).should().getById(notificationId);
            then(notificationService).should().deleteById(notificationId);
        }

        @Test
        @DisplayName("다른 사용자의 알림에 접근 시 NotificationOwnershipException 발생")
        void withOtherUserNotification_throwsNotificationOwnershipException() {
            // given
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            NotificationModel notification = NotificationModelFixture.builder()
                .set("id", notificationId)
                .set("receiverId", otherUserId)
                .sample();

            given(userService.getById(userId)).willReturn(UserModelFixture.builder().set("id", userId).sample());
            given(notificationService.getById(notificationId)).willReturn(notification);

            // when & then
            assertThatThrownBy(() -> notificationFacade.readNotification(userId, notificationId))
                .isInstanceOf(NotificationForbiddenException.class);

            then(notificationService).should().getById(notificationId);
            then(notificationService).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 요청 시 UserNotFoundException 발생")
        void withNonExistingUser_throwsUserNotFoundException() {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            given(userService.getById(userId)).willThrow(UserNotFoundException.withId(userId));

            // when & then
            assertThatThrownBy(() -> notificationFacade.readNotification(userId, notificationId))
                .isInstanceOf(UserNotFoundException.class);

            then(notificationService).shouldHaveNoInteractions();
        }
    }
}
