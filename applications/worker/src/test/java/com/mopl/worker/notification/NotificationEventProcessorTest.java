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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
    }
}
