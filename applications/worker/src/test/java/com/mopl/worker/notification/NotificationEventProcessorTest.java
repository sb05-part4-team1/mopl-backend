package com.mopl.worker.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.redis.pubsub.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventProcessor 단위 테스트")
class NotificationEventProcessorTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private FollowService followService;

    @Mock
    private PlaylistSubscriptionService playlistSubscriptionService;

    @Mock
    private Acknowledgment acknowledgment;

    private NotificationEventProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        processor = new NotificationEventProcessor(
            notificationService,
            notificationPublisher,
            followService,
            playlistSubscriptionService,
            objectMapper
        );
    }

    @Nested
    @DisplayName("handleDirectMessageSent()")
    class HandleDirectMessageSentTest {

        @Test
        @DisplayName("DM 전송 이벤트 처리 시 알림 생성 및 SSE 발행")
        void withValidEvent_createsNotificationAndPublishesToSse() {
            // given
            UUID messageId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            String senderName = "테스트유저";
            String messageContent = "안녕하세요!";

            String payload = """
                {
                    "messageId": "%s",
                    "conversationId": "%s",
                    "senderId": "%s",
                    "senderName": "%s",
                    "receiverId": "%s",
                    "messageContent": "%s"
                }
                """.formatted(messageId, conversationId, senderId, senderName, receiverId, messageContent);

            NotificationModel savedNotification = NotificationModel.create(
                "[DM] " + senderName,
                messageContent,
                NotificationModel.NotificationLevel.INFO,
                receiverId
            );

            given(notificationService.create(any(NotificationModel.class))).willReturn(savedNotification);

            // when
            processor.handleDirectMessageSent(payload, acknowledgment);

            // then
            ArgumentCaptor<NotificationModel> notificationCaptor = ArgumentCaptor.forClass(NotificationModel.class);
            then(notificationService).should().create(notificationCaptor.capture());

            NotificationModel capturedNotification = notificationCaptor.getValue();
            assertThat(capturedNotification.getTitle()).isEqualTo("[DM] " + senderName);
            assertThat(capturedNotification.getContent()).isEqualTo(messageContent);
            assertThat(capturedNotification.getReceiverId()).isEqualTo(receiverId);
            assertThat(capturedNotification.getLevel()).isEqualTo(NotificationModel.NotificationLevel.INFO);

            then(notificationPublisher).should().publish(savedNotification);
            then(acknowledgment).should().acknowledge();
        }
    }

    @Nested
    @DisplayName("handleUserFollowed()")
    class HandleUserFollowedTest {

        @Test
        @DisplayName("팔로우 이벤트 처리 시 알림 생성 및 SSE 발행")
        void withValidEvent_createsNotificationAndPublishesToSse() {
            // given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            String followerName = "팔로워";

            String payload = """
                {
                    "followerId": "%s",
                    "followerName": "%s",
                    "followeeId": "%s"
                }
                """.formatted(followerId, followerName, followeeId);

            NotificationModel savedNotification = NotificationModel.create(
                "새로운 팔로워",
                followerName + "님이 회원님을 팔로우했습니다.",
                NotificationModel.NotificationLevel.INFO,
                followeeId
            );

            given(notificationService.create(any(NotificationModel.class))).willReturn(savedNotification);

            // when
            processor.handleUserFollowed(payload, acknowledgment);

            // then
            ArgumentCaptor<NotificationModel> notificationCaptor = ArgumentCaptor.forClass(NotificationModel.class);
            then(notificationService).should().create(notificationCaptor.capture());

            NotificationModel capturedNotification = notificationCaptor.getValue();
            assertThat(capturedNotification.getTitle()).isEqualTo("새로운 팔로워");
            assertThat(capturedNotification.getContent()).contains(followerName);
            assertThat(capturedNotification.getReceiverId()).isEqualTo(followeeId);

            then(notificationPublisher).should().publish(savedNotification);
            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handleUserFollowed(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistCreated()")
    class HandlePlaylistCreatedTest {

        @Test
        @DisplayName("플레이리스트 생성 이벤트 처리 시 팔로워들에게 알림 생성 및 발행")
        void withValidEvent_createsNotificationsForFollowers() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            String ownerName = "플레이리스트소유자";
            String playlistTitle = "내 플레이리스트";

            String payload = """
                {
                    "playlistId": "%s",
                    "ownerId": "%s",
                    "ownerName": "%s",
                    "playlistTitle": "%s"
                }
                """.formatted(playlistId, ownerId, ownerName, playlistTitle);

            UUID follower1 = UUID.randomUUID();
            UUID follower2 = UUID.randomUUID();
            List<UUID> followerIds = List.of(follower1, follower2);

            given(followService.getFollowerIds(ownerId)).willReturn(followerIds);

            List<NotificationModel> savedNotifications = followerIds.stream()
                .map(id -> NotificationModel.create(
                    "새로운 플레이리스트",
                    ownerName + "님이 새로운 플레이리스트 \"" + playlistTitle + "\"을(를) 만들었습니다.",
                    NotificationModel.NotificationLevel.INFO,
                    id
                ))
                .toList();
            given(notificationService.createAll(anyList())).willReturn(savedNotifications);

            // when
            processor.handlePlaylistCreated(payload, acknowledgment);

            // then
            then(followService).should().getFollowerIds(ownerId);
            then(notificationService).should().createAll(anyList());
            then(notificationPublisher).should().publishAll(savedNotifications);
            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("팔로워가 없는 경우 알림 생성하지 않음")
        void withNoFollowers_doesNotCreateNotifications() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            String payload = """
                {
                    "playlistId": "%s",
                    "ownerId": "%s",
                    "ownerName": "소유자",
                    "playlistTitle": "플레이리스트"
                }
                """.formatted(playlistId, ownerId);

            given(followService.getFollowerIds(ownerId)).willReturn(List.of());

            // when
            processor.handlePlaylistCreated(payload, acknowledgment);

            // then
            then(notificationService).should(never()).createAll(anyList());
            then(notificationPublisher).should(never()).publishAll(anyList());
            then(acknowledgment).should().acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistUpdated()")
    class HandlePlaylistUpdatedTest {

        @Test
        @DisplayName("플레이리스트 업데이트 이벤트 처리 시 구독자들에게 알림 발행")
        void withValidEvent_createsNotificationsForSubscribers() {
            // given
            UUID playlistId = UUID.randomUUID();
            String playlistTitle = "업데이트된 플레이리스트";

            String payload = """
                {
                    "playlistId": "%s",
                    "playlistTitle": "%s"
                }
                """.formatted(playlistId, playlistTitle);

            UUID subscriber1 = UUID.randomUUID();
            UUID subscriber2 = UUID.randomUUID();
            List<UUID> subscriberIds = List.of(subscriber1, subscriber2);

            given(playlistSubscriptionService.getSubscriberIds(playlistId)).willReturn(subscriberIds);

            List<NotificationModel> savedNotifications = subscriberIds.stream()
                .map(id -> NotificationModel.create(
                    "플레이리스트 업데이트",
                    "\"" + playlistTitle + "\" 플레이리스트가 업데이트되었습니다.",
                    NotificationModel.NotificationLevel.INFO,
                    id
                ))
                .toList();
            given(notificationService.createAll(anyList())).willReturn(savedNotifications);

            // when
            processor.handlePlaylistUpdated(payload, acknowledgment);

            // then
            then(playlistSubscriptionService).should().getSubscriberIds(playlistId);
            then(notificationService).should().createAll(anyList());
            then(notificationPublisher).should().publishAll(savedNotifications);
            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("구독자가 없는 경우 알림 생성하지 않음")
        void withNoSubscribers_doesNotCreateNotifications() {
            // given
            UUID playlistId = UUID.randomUUID();

            String payload = """
                {
                    "playlistId": "%s",
                    "playlistTitle": "플레이리스트"
                }
                """.formatted(playlistId);

            given(playlistSubscriptionService.getSubscriberIds(playlistId)).willReturn(List.of());

            // when
            processor.handlePlaylistUpdated(payload, acknowledgment);

            // then
            then(notificationService).should(never()).createAll(anyList());
            then(notificationPublisher).should(never()).publishAll(anyList());
            then(acknowledgment).should().acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistSubscribed()")
    class HandlePlaylistSubscribedTest {

        @Test
        @DisplayName("플레이리스트 구독 이벤트 처리 시 소유자에게 알림 발행")
        void withValidEvent_createsNotificationForOwner() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();
            String subscriberName = "구독자";
            String playlistTitle = "인기 플레이리스트";

            String payload = """
                {
                    "playlistId": "%s",
                    "ownerId": "%s",
                    "subscriberId": "%s",
                    "subscriberName": "%s",
                    "playlistTitle": "%s"
                }
                """.formatted(playlistId, ownerId, subscriberId, subscriberName, playlistTitle);

            NotificationModel savedNotification = NotificationModel.create(
                "새로운 구독자",
                subscriberName + "님이 \"" + playlistTitle + "\" 플레이리스트를 구독했습니다.",
                NotificationModel.NotificationLevel.INFO,
                ownerId
            );

            given(notificationService.create(any(NotificationModel.class))).willReturn(savedNotification);

            // when
            processor.handlePlaylistSubscribed(payload, acknowledgment);

            // then
            ArgumentCaptor<NotificationModel> notificationCaptor = ArgumentCaptor.forClass(NotificationModel.class);
            then(notificationService).should().create(notificationCaptor.capture());

            NotificationModel capturedNotification = notificationCaptor.getValue();
            assertThat(capturedNotification.getTitle()).isEqualTo("새로운 구독자");
            assertThat(capturedNotification.getContent()).contains(subscriberName);
            assertThat(capturedNotification.getContent()).contains(playlistTitle);
            assertThat(capturedNotification.getReceiverId()).isEqualTo(ownerId);

            then(notificationPublisher).should().publish(savedNotification);
            then(acknowledgment).should().acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistContentAdded()")
    class HandlePlaylistContentAddedTest {

        @Test
        @DisplayName("플레이리스트 콘텐츠 추가 이벤트 처리 시 구독자들에게 알림 발행")
        void withValidEvent_createsNotificationsForSubscribers() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            String playlistTitle = "영화 모음";
            String contentTitle = "인터스텔라";

            String payload = """
                {
                    "playlistId": "%s",
                    "contentId": "%s",
                    "playlistTitle": "%s",
                    "contentTitle": "%s"
                }
                """.formatted(playlistId, contentId, playlistTitle, contentTitle);

            UUID subscriber1 = UUID.randomUUID();
            UUID subscriber2 = UUID.randomUUID();
            List<UUID> subscriberIds = List.of(subscriber1, subscriber2);

            given(playlistSubscriptionService.getSubscriberIds(playlistId)).willReturn(subscriberIds);

            List<NotificationModel> savedNotifications = subscriberIds.stream()
                .map(id -> NotificationModel.create(
                    "플레이리스트 업데이트",
                    "\"" + playlistTitle + "\"에 새로운 콘텐츠 \"" + contentTitle + "\"이(가) 추가되었습니다.",
                    NotificationModel.NotificationLevel.INFO,
                    id
                ))
                .toList();
            given(notificationService.createAll(anyList())).willReturn(savedNotifications);

            // when
            processor.handlePlaylistContentAdded(payload, acknowledgment);

            // then
            then(playlistSubscriptionService).should().getSubscriberIds(playlistId);
            then(notificationService).should().createAll(anyList());
            then(notificationPublisher).should().publishAll(savedNotifications);
            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("구독자가 없는 경우 알림 생성하지 않음")
        void withNoSubscribers_doesNotCreateNotifications() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            String payload = """
                {
                    "playlistId": "%s",
                    "contentId": "%s",
                    "playlistTitle": "플레이리스트",
                    "contentTitle": "콘텐츠"
                }
                """.formatted(playlistId, contentId);

            given(playlistSubscriptionService.getSubscriberIds(playlistId)).willReturn(List.of());

            // when
            processor.handlePlaylistContentAdded(payload, acknowledgment);

            // then
            then(notificationService).should(never()).createAll(anyList());
            then(notificationPublisher).should(never()).publishAll(anyList());
            then(acknowledgment).should().acknowledge();
        }
    }

    @Nested
    @DisplayName("handleDirectMessageSent()")
    class HandleDirectMessageSentExceptionTest {

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handleDirectMessageSent(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistCreated() - Exception Tests")
    class HandlePlaylistCreatedExceptionTest {

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handlePlaylistCreated(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistUpdated() - Exception Tests")
    class HandlePlaylistUpdatedExceptionTest {

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handlePlaylistUpdated(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistSubscribed() - Exception Tests")
    class HandlePlaylistSubscribedExceptionTest {

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handlePlaylistSubscribed(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("handlePlaylistContentAdded() - Exception Tests")
    class HandlePlaylistContentAddedExceptionTest {

        @Test
        @DisplayName("잘못된 JSON 페이로드 처리 시 예외 발생")
        void withInvalidPayload_throwsException() {
            // given
            String invalidPayload = "{ invalid json }";

            // when & then
            assertThatThrownBy(() -> processor.handlePlaylistContentAdded(invalidPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class);
            then(acknowledgment).should(never()).acknowledge();
        }
    }
}
