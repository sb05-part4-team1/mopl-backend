package com.mopl.worker.config;

import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("DomainServiceConfig 단위 테스트")
class DomainServiceConfigTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private NotificationQueryRepository notificationQueryRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistSubscriberRepository playlistSubscriberRepository;

    @Test
    @DisplayName("FollowService 빈이 정상적으로 생성됨")
    void followService_beanCreated() {
        // given
        DomainServiceConfig config = new DomainServiceConfig();

        // when
        FollowService followService = config.followService(followRepository);

        // then
        assertThat(followService).isNotNull();
    }

    @Test
    @DisplayName("NotificationService 빈이 정상적으로 생성됨")
    void notificationService_beanCreated() {
        // given
        DomainServiceConfig config = new DomainServiceConfig();

        // when
        NotificationService notificationService = config.notificationService(
            notificationQueryRepository,
            notificationRepository
        );

        // then
        assertThat(notificationService).isNotNull();
    }

    @Test
    @DisplayName("PlaylistSubscriptionService 빈이 정상적으로 생성됨")
    void playlistSubscriptionService_beanCreated() {
        // given
        DomainServiceConfig config = new DomainServiceConfig();

        // when
        PlaylistSubscriptionService playlistSubscriptionService = config.playlistSubscriptionService(
            playlistRepository,
            playlistSubscriberRepository
        );

        // then
        assertThat(playlistSubscriptionService).isNotNull();
    }

    @Test
    @DisplayName("DomainServiceConfig 인스턴스가 정상적으로 생성됨")
    void domainServiceConfig_created() {
        // when
        DomainServiceConfig config = new DomainServiceConfig();

        // then
        assertThat(config).isNotNull();
    }
}
