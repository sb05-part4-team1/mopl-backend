package com.mopl.worker.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.event.conversation.DirectMessageSentEvent;
import com.mopl.domain.event.playlist.PlaylistContentAddedEvent;
import com.mopl.domain.event.playlist.PlaylistCreatedEvent;
import com.mopl.domain.event.playlist.PlaylistSubscribedEvent;
import com.mopl.domain.event.playlist.PlaylistUpdatedEvent;
import com.mopl.domain.event.user.UserFollowedEvent;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProcessor {

    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;
    private final FollowService followService;
    private final PlaylistSubscriptionService playlistSubscriptionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = EventTopic.USER_FOLLOWED,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserFollowed(String payload, Acknowledgment ack) {
        try {
            UserFollowedEvent event = objectMapper.readValue(payload, UserFollowedEvent.class);

            String title = "새로운 팔로워";
            String content = event.getFollowerName() + "님이 회원님을 팔로우했습니다.";

            NotificationModel savedNotificationModel = createNotification(
                title, content, event.getFolloweeId()
            );
            publishToSse(savedNotificationModel);

            ack.acknowledge();
            LogContext.with("event", "UserFollowed")
                .and("followeeId", event.getFolloweeId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "UserFollowed").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
        topics = EventTopic.PLAYLIST_CREATED,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlaylistCreated(String payload, Acknowledgment ack) {
        try {
            PlaylistCreatedEvent event = objectMapper.readValue(payload, PlaylistCreatedEvent.class);

            String title = "새로운 플레이리스트";
            String content = event.getOwnerName() + "님이 새로운 플레이리스트 \"" + event.getPlaylistTitle() + "\"을(를) 만들었습니다.";

            List<UUID> followerIds = followService.getFollowerIds(event.getOwnerId());
            createAndPublishNotifications(title, content, followerIds);

            ack.acknowledge();
            LogContext.with("event", "PlaylistCreated")
                .and("playlistId", event.getPlaylistId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "PlaylistCreated").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
        topics = EventTopic.PLAYLIST_UPDATED,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlaylistUpdated(String payload, Acknowledgment ack) {
        try {
            PlaylistUpdatedEvent event = objectMapper.readValue(payload, PlaylistUpdatedEvent.class);

            String title = "플레이리스트 업데이트";
            String content = "\"" + event.getPlaylistTitle() + "\" 플레이리스트가 업데이트되었습니다.";

            List<UUID> subscriberIds = playlistSubscriptionService.getSubscriberIds(event.getPlaylistId());
            createAndPublishNotifications(title, content, subscriberIds);

            ack.acknowledge();
            LogContext.with("event", "PlaylistUpdated")
                .and("playlistId", event.getPlaylistId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "PlaylistUpdated").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
        topics = EventTopic.PLAYLIST_SUBSCRIBED,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlaylistSubscribed(String payload, Acknowledgment ack) {
        try {
            PlaylistSubscribedEvent event = objectMapper.readValue(payload, PlaylistSubscribedEvent.class);

            String title = "새로운 구독자";
            String content = event.getSubscriberName() + "님이 \"" + event.getPlaylistTitle() + "\" 플레이리스트를 구독했습니다.";

            NotificationModel saved = createNotification(
                title, content, event.getOwnerId()
            );
            publishToSse(saved);

            ack.acknowledge();
            LogContext.with("event", "PlaylistSubscribed")
                .and("ownerId", event.getOwnerId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "PlaylistSubscribed").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
        topics = EventTopic.PLAYLIST_CONTENT_ADDED,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePlaylistContentAdded(String payload, Acknowledgment ack) {
        try {
            PlaylistContentAddedEvent event = objectMapper.readValue(payload, PlaylistContentAddedEvent.class);

            String title = "플레이리스트 업데이트";
            String content = "\"" + event.getPlaylistTitle() + "\"에 새로운 콘텐츠 \"" + event.getContentTitle() + "\"이(가) 추가되었습니다.";

            List<UUID> subscriberIds = playlistSubscriptionService.getSubscriberIds(event.getPlaylistId());
            createAndPublishNotifications(title, content, subscriberIds);

            ack.acknowledge();
            LogContext.with("event", "PlaylistContentAdded")
                .and("playlistId", event.getPlaylistId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "PlaylistContentAdded").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
        topics = EventTopic.DIRECT_MESSAGE_SENT,
        groupId = "worker-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDirectMessageSent(String payload, Acknowledgment ack) {
        try {
            DirectMessageSentEvent event = objectMapper.readValue(payload, DirectMessageSentEvent.class);

            String title = "[DM] " + event.getSenderName();
            String content = event.getMessageContent();

            NotificationModel savedNotification = createNotification(
                title, content, event.getReceiverId()
            );
            publishToSse(savedNotification);

            ack.acknowledge();
            LogContext.with("event", "DirectMessageSent")
                .and("receiverId", event.getReceiverId())
                .debug("Processed event");
        } catch (Exception e) {
            LogContext.with("event", "DirectMessageSent").error("Failed to process event", e);
            throw new RuntimeException(e);
        }
    }

    private NotificationModel createNotification(
        String title,
        String content,
        UUID receiverId
    ) {
        NotificationModel notification = NotificationModel.create(
            title, content, NotificationModel.NotificationLevel.INFO, receiverId
        );
        return notificationService.create(notification);
    }

    private void createAndPublishNotifications(String title, String content, List<UUID> receiverIds) {
        if (receiverIds.isEmpty()) {
            return;
        }

        List<NotificationModel> notifications = receiverIds.stream()
            .map(receiverId -> NotificationModel.create(
                title, content, NotificationModel.NotificationLevel.INFO, receiverId
            ))
            .toList();

        List<NotificationModel> savedNotifications = notificationService.createAll(notifications);
        notificationPublisher.publishAll(savedNotifications);
    }

    private void publishToSse(NotificationModel notification) {
        notificationPublisher.publish(notification);
    }
}
