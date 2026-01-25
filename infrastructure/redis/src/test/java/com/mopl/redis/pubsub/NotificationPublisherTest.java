package com.mopl.redis.pubsub;

import com.mopl.domain.model.notification.NotificationModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;


import java.util.List;
import java.util.UUID;

import static com.mopl.redis.pubsub.NotificationPublisher.CHANNEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPublisher 단위 테스트")
class NotificationPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private NotificationPublisher publisher;

    @Nested
    @DisplayName("publish()")
    class PublishTest {

        @Test
        @DisplayName("알림을 Redis 채널로 발행")
        void withNotification_publishesToChannel() {
            // given
            NotificationModel notification = createNotification();

            // when
            publisher.publish(notification);

            // then
            then(redisTemplate).should().convertAndSend(CHANNEL, notification);
        }
    }

    @Nested
    @DisplayName("publishAll()")
    class PublishAllTest {

        @Test
        @DisplayName("여러 알림을 순차적으로 발행")
        void withMultipleNotifications_publishesAll() {
            // given
            NotificationModel notification1 = createNotification();
            NotificationModel notification2 = createNotification();
            NotificationModel notification3 = createNotification();
            List<NotificationModel> notifications = List.of(notification1, notification2, notification3);

            // when
            publisher.publishAll(notifications);

            // then
            then(redisTemplate).should(times(3)).convertAndSend(eq(CHANNEL), any(NotificationModel.class));
        }

        @Test
        @DisplayName("빈 리스트인 경우 아무것도 발행하지 않음")
        void withEmptyList_publishesNothing() {
            // given
            List<NotificationModel> notifications = List.of();

            // when
            publisher.publishAll(notifications);

            // then
            then(redisTemplate).shouldHaveNoInteractions();
        }
    }

    private NotificationModel createNotification() {
        return NotificationModel.create(
            "테스트 알림",
            "테스트 내용",
            NotificationModel.NotificationLevel.INFO,
            UUID.randomUUID()
        );
    }
}
