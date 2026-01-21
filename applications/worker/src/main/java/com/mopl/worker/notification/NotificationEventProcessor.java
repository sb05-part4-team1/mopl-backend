package com.mopl.worker.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.event.EventTopic;
import com.mopl.domain.event.playlist.PlaylistContentAddedEvent;
import com.mopl.domain.event.playlist.PlaylistCreatedEvent;
import com.mopl.domain.event.playlist.PlaylistSubscribedEvent;
import com.mopl.domain.event.user.UserFollowedEvent;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
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

            log.debug("Processed UserFollowedEvent for user: {}", event.getFolloweeId());
        } catch (Exception e) {
            log.error("Failed to process UserFollowedEvent: {}", payload, e);
        } finally {
            ack.acknowledge();
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
            for (UUID followerId : followerIds) {
                NotificationModel saved = createNotification(title, content, followerId);
                publishToSse(saved);
            }

            log.debug("Processed PlaylistCreatedEvent for playlist: {}", event.getPlaylistId());
        } catch (Exception e) {
            log.error("Failed to process PlaylistCreatedEvent: {}", payload, e);
        } finally {
            ack.acknowledge();
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

            log.debug("Processed PlaylistSubscribedEvent for user: {}", event.getOwnerId());
        } catch (Exception e) {
            log.error("Failed to process PlaylistSubscribedEvent: {}", payload, e);
        } finally {
            ack.acknowledge();
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
            String content = "\"" + event.getPlaylistTitle() + "\"에 새로운 콘텐츠가 추가되었습니다.";

            for (UUID subscriberId : event.getSubscriberIds()) {
                NotificationModel saved = createNotification(
                    title, content, subscriberId
                );
                publishToSse(saved);
            }

            log.debug("Processed PlaylistContentAddedEvent for playlist: {}", event.getPlaylistId());
        } catch (Exception e) {
            log.error("Failed to process PlaylistContentAddedEvent: {}", payload, e);
        } finally {
            ack.acknowledge();
        }
    }

    // TODO: 존재하지 않는 receiver인 알림 삭제 스케줄러 구현
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

    private void publishToSse(NotificationModel notification) {
        notificationPublisher.publish(notification);
    }
}
