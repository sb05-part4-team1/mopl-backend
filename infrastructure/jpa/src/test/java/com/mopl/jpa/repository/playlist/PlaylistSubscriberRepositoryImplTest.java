package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    PlaylistSubscriberRepositoryImpl.class,
    PlaylistRepositoryImpl.class,
    PlaylistEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("PlaylistSubscriberRepositoryImpl 슬라이스 테스트")
class PlaylistSubscriberRepositoryImplTest {

    @Autowired
    private PlaylistSubscriberRepository playlistSubscriberRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;

    private UserModel subscriber;
    private PlaylistModel playlist;

    @BeforeEach
    void setUp() {
        UserModel owner = userRepository.save(
            UserModel.create(
                UserModel.AuthProvider.EMAIL,
                "owner@example.com",
                "소유자",
                "encodedPassword"
            )
        );

        subscriber = userRepository.save(
            UserModel.create(
                UserModel.AuthProvider.EMAIL,
                "subscriber@example.com",
                "구독자",
                "encodedPassword"
            )
        );

        playlist = playlistRepository.save(
            PlaylistModel.create(
                owner,
                "테스트 플레이리스트",
                "설명"
            )
        );
    }

    @Nested
    @DisplayName("existsByPlaylistIdAndSubscriberId()")
    class ExistsByPlaylistIdAndSubscriberIdTest {

        @Test
        @DisplayName("구독 관계가 존재하면 true를 반환한다")
        void whenSubscriptionExists_returnsTrue() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // when
            boolean exists = playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("구독 관계가 존재하지 않으면 false를 반환한다")
        void whenSubscriptionNotExists_returnsFalse() {
            // when
            boolean exists = playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 플레이리스트 ID로 조회하면 false를 반환한다")
        void whenDifferentPlaylistId_returnsFalse() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            UUID differentPlaylistId = UUID.randomUUID();

            // when
            boolean exists = playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                differentPlaylistId,
                subscriber.getId()
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 구독자 ID로 조회하면 false를 반환한다")
        void whenDifferentSubscriberId_returnsFalse() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            UUID differentSubscriberId = UUID.randomUUID();

            // when
            boolean exists = playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlist.getId(),
                differentSubscriberId
            );

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("플레이리스트 구독 관계를 저장한다")
        void save_createsSubscriptionRelationship() {
            // when
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // then
            List<PlaylistSubscriberEntity> subscriptions = jpaPlaylistSubscriberRepository
                .findAll();
            assertThat(subscriptions).hasSize(1);
            assertThat(subscriptions.get(0).getPlaylist().getId()).isEqualTo(playlist.getId());
            assertThat(subscriptions.get(0).getSubscriber().getId()).isEqualTo(subscriber.getId());
        }

        @Test
        @DisplayName("여러 사용자가 같은 플레이리스트를 구독할 수 있다")
        void save_multipleSubscribersForSamePlaylist() {
            // given
            UserModel anotherSubscriber = userRepository.save(
                UserModel.create(
                    UserModel.AuthProvider.EMAIL,
                    "another@example.com",
                    "다른 구독자",
                    "encodedPassword"
                )
            );

            // when
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            playlistSubscriberRepository.save(playlist.getId(), anotherSubscriber.getId());

            // then
            List<PlaylistSubscriberEntity> subscriptions = jpaPlaylistSubscriberRepository
                .findAll();
            assertThat(subscriptions).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deleteByPlaylistIdAndSubscriberId()")
    class DeleteByPlaylistIdAndSubscriberIdTest {

        @Test
        @DisplayName("특정 구독 관계를 삭제한다")
        void deleteByPlaylistIdAndSubscriberId_deletesSubscription() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            assertThat(jpaPlaylistSubscriberRepository.findAll()).hasSize(1);

            // when
            playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(jpaPlaylistSubscriberRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("다른 구독 관계는 유지된다")
        void deleteByPlaylistIdAndSubscriberId_keepsOtherSubscriptions() {
            // given
            UserModel anotherSubscriber = userRepository.save(
                UserModel.create(
                    UserModel.AuthProvider.EMAIL,
                    "another@example.com",
                    "다른 구독자",
                    "encodedPassword"
                )
            );
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            playlistSubscriberRepository.save(playlist.getId(), anotherSubscriber.getId());
            assertThat(jpaPlaylistSubscriberRepository.findAll()).hasSize(2);

            // when
            playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            List<PlaylistSubscriberEntity> remaining = jpaPlaylistSubscriberRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getSubscriber().getId()).isEqualTo(anotherSubscriber
                .getId());
        }

        @Test
        @DisplayName("존재하지 않는 구독 관계 삭제 시 예외 없이 동작한다")
        void deleteByPlaylistIdAndSubscriberId_nonExistentSubscription_noException() {
            // when & then (no exception thrown)
            playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            assertThat(jpaPlaylistSubscriberRepository.findAll()).isEmpty();
        }
    }
}
