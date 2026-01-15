package com.mopl.domain.service.notification;

import com.mopl.domain.exception.notification.NotificationNotFoundException;
import com.mopl.domain.fixture.NotificationModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.notification.NotificationSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationQueryRepository notificationQueryRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 알림 ID로 조회하면 NotificationModel 반환")
        void withExistingNotificationId_returnsNotificationModel() {
            // given
            UUID notificationId = UUID.randomUUID();
            NotificationModel notification = NotificationModelFixture.builder()
                .set("id", notificationId)
                .sample();

            given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

            // when
            NotificationModel result = notificationService.getById(notificationId);

            // then
            assertThat(result).isEqualTo(notification);
            then(notificationRepository).should().findById(notificationId);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 조회하면 NotificationNotFoundException 발생")
        void withNonExistingNotificationId_throwsNotificationNotFoundException() {
            // given
            UUID notificationId = UUID.randomUUID();

            given(notificationRepository.findById(notificationId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.getById(notificationId))
                .isInstanceOf(NotificationNotFoundException.class)
                .satisfies(e -> {
                    NotificationNotFoundException ex = (NotificationNotFoundException) e;
                    assertThat(ex.getDetails().get("notificationId")).isEqualTo(notificationId);
                });

            then(notificationRepository).should().findById(notificationId);
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteByIdTest {

        @Test
        @DisplayName("존재하는 알림 삭제 성공")
        void withExistingNotification_deletesNotification() {
            // given
            UUID notificationId = UUID.randomUUID();
            NotificationModel notification = NotificationModelFixture.builder()
                .set("id", notificationId)
                .sample();

            given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));
            given(notificationRepository.save(any(NotificationModel.class)))
                .willReturn(notification);

            // when
            notificationService.deleteById(notificationId);

            // then
            then(notificationRepository).should().findById(notificationId);
            then(notificationRepository).should().save(any(NotificationModel.class));
        }

        @Test
        @DisplayName("존재하지 않는 알림 삭제 시 NotificationNotFoundException 발생")
        void withNonExistingNotification_throwsNotificationNotFoundException() {
            // given
            UUID notificationId = UUID.randomUUID();

            given(notificationRepository.findById(notificationId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.deleteById(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);

            then(notificationRepository).should().findById(notificationId);
            then(notificationRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @Test
        @DisplayName("유효한 요청 시 알림 목록 반환")
        void withValidRequest_returnsNotificationList() {
            // given
            UserModel receiver = UserModelFixture.create();
            UUID receiverId = receiver.getId();
            NotificationModel notification1 = NotificationModelFixture.builder()
                .set("receiver", receiver)
                .sample();
            NotificationModel notification2 = NotificationModelFixture.builder()
                .set("receiver", receiver)
                .sample();

            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 10, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            CursorResponse<NotificationModel> expectedResponse = CursorResponse.of(
                List.of(notification1, notification2),
                notification2.getCreatedAt().toString(),
                notification2.getId(),
                true,
                10,
                "createdAt",
                SortDirection.ASCENDING
            );

            given(notificationQueryRepository.findAll(receiverId, request))
                .willReturn(expectedResponse);

            // when
            CursorResponse<NotificationModel> result = notificationService.getAll(receiverId,
                request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.totalCount()).isEqualTo(10);

            then(notificationQueryRepository).should().findAll(receiverId, request);
        }

        @Test
        @DisplayName("결과가 없으면 빈 목록 반환")
        void withNoResults_returnsEmptyList() {
            // given
            UUID receiverId = UUID.randomUUID();
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 10, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            CursorResponse<NotificationModel> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.ASCENDING
            );

            given(notificationQueryRepository.findAll(receiverId, request))
                .willReturn(emptyResponse);

            // when
            CursorResponse<NotificationModel> result = notificationService.getAll(receiverId,
                request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();

            then(notificationQueryRepository).should().findAll(receiverId, request);
        }
    }
}
