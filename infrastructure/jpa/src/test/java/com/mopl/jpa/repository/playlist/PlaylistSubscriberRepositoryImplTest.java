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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
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
                "owner@example.com",
                "소유자",
                "encodedPassword"
            )
        );

        subscriber = userRepository.save(
            UserModel.create(
                "subscriber@example.com",
                "구독자",
                "encodedPassword"
            )
        );

        playlist = playlistRepository.save(
            PlaylistModel.create(
                "테스트 플레이리스트",
                "설명",
                owner
            )
        );
    }

    @Nested
    @DisplayName("findSubscribedPlaylistIds()")
    class FindSubscribedPlaylistIdsTest {

        @Test
        @DisplayName("구독 중인 플레이리스트 ID 목록을 반환한다")
        void whenSubscribed_returnsPlaylistIds() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // when
            Set<UUID> subscribedIds = playlistSubscriberRepository.findSubscribedPlaylistIds(
                subscriber.getId(),
                List.of(playlist.getId())
            );

            // then
            assertThat(subscribedIds).containsExactly(playlist.getId());
        }

        @Test
        @DisplayName("구독하지 않은 플레이리스트는 결과에 포함되지 않는다")
        void whenNotSubscribed_returnsEmptySet() {
            // when
            Set<UUID> subscribedIds = playlistSubscriberRepository.findSubscribedPlaylistIds(
                subscriber.getId(),
                List.of(playlist.getId())
            );

            // then
            assertThat(subscribedIds).isEmpty();
        }

        @Test
        @DisplayName("빈 플레이리스트 ID 목록이 주어지면 빈 Set을 반환한다")
        void whenEmptyPlaylistIds_returnsEmptySet() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // when
            Set<UUID> subscribedIds = playlistSubscriberRepository.findSubscribedPlaylistIds(
                subscriber.getId(),
                List.of()
            );

            // then
            assertThat(subscribedIds).isEmpty();
        }

        @Test
        @DisplayName("여러 플레이리스트 중 구독한 것만 반환한다")
        void whenMultiplePlaylists_returnsOnlySubscribed() {
            // given
            UserModel owner = userRepository.save(
                UserModel.create("owner2@example.com", "소유자2", "encodedPassword")
            );
            PlaylistModel anotherPlaylist = playlistRepository.save(
                PlaylistModel.create("다른 플레이리스트", "설명", owner)
            );
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // when
            Set<UUID> subscribedIds = playlistSubscriberRepository.findSubscribedPlaylistIds(
                subscriber.getId(),
                List.of(playlist.getId(), anotherPlaylist.getId())
            );

            // then
            assertThat(subscribedIds).containsExactly(playlist.getId());
        }
    }

    @Nested
    @DisplayName("findSubscriberIdsByPlaylistId()")
    class FindSubscriberIdsByPlaylistIdTest {

        @Test
        @DisplayName("플레이리스트의 구독자 ID 목록을 반환한다")
        void returnsSubscriberIds() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());

            // when
            List<UUID> subscriberIds = playlistSubscriberRepository.findSubscriberIdsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(subscriberIds).containsExactly(subscriber.getId());
        }

        @Test
        @DisplayName("구독자가 없으면 빈 목록을 반환한다")
        void whenNoSubscribers_returnsEmptyList() {
            // when
            List<UUID> subscriberIds = playlistSubscriberRepository.findSubscriberIdsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(subscriberIds).isEmpty();
        }

        @Test
        @DisplayName("여러 구독자가 있으면 모두 반환한다")
        void whenMultipleSubscribers_returnsAll() {
            // given
            UserModel anotherSubscriber = userRepository.save(
                UserModel.create("another@example.com", "다른 구독자", "encodedPassword")
            );
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            playlistSubscriberRepository.save(playlist.getId(), anotherSubscriber.getId());

            // when
            List<UUID> subscriberIds = playlistSubscriberRepository.findSubscriberIdsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(subscriberIds).containsExactlyInAnyOrder(
                subscriber.getId(),
                anotherSubscriber.getId()
            );
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 빈 목록을 반환한다")
        void whenPlaylistNotExists_returnsEmptyList() {
            // given
            UUID nonExistentPlaylistId = UUID.randomUUID();

            // when
            List<UUID> subscriberIds = playlistSubscriberRepository.findSubscriberIdsByPlaylistId(
                nonExistentPlaylistId
            );

            // then
            assertThat(subscriberIds).isEmpty();
        }
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
            assertThat(subscriptions.getFirst().getPlaylist().getId()).isEqualTo(playlist.getId());
            assertThat(subscriptions.getFirst().getSubscriber().getId()).isEqualTo(subscriber.getId());
        }

        @Test
        @DisplayName("여러 사용자가 같은 플레이리스트를 구독할 수 있다")
        void save_multipleSubscribersForSamePlaylist() {
            // given
            UserModel anotherSubscriber = userRepository.save(
                UserModel.create(
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
        @DisplayName("특정 구독 관계를 삭제하면 true를 반환한다")
        void deleteByPlaylistIdAndSubscriberId_deletesSubscription_returnsTrue() {
            // given
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            assertThat(jpaPlaylistSubscriberRepository.findAll()).hasSize(1);

            // when
            boolean deleted = playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(deleted).isTrue();
            assertThat(jpaPlaylistSubscriberRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("다른 구독 관계는 유지된다")
        void deleteByPlaylistIdAndSubscriberId_keepsOtherSubscriptions() {
            // given
            UserModel anotherSubscriber = userRepository.save(
                UserModel.create(
                    "another@example.com",
                    "다른 구독자",
                    "encodedPassword"
                )
            );
            playlistSubscriberRepository.save(playlist.getId(), subscriber.getId());
            playlistSubscriberRepository.save(playlist.getId(), anotherSubscriber.getId());
            assertThat(jpaPlaylistSubscriberRepository.findAll()).hasSize(2);

            // when
            boolean deleted = playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(deleted).isTrue();
            List<PlaylistSubscriberEntity> remaining = jpaPlaylistSubscriberRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getSubscriber().getId()).isEqualTo(anotherSubscriber
                .getId());
        }

        @Test
        @DisplayName("존재하지 않는 구독 관계 삭제 시 false를 반환한다")
        void deleteByPlaylistIdAndSubscriberId_nonExistentSubscription_returnsFalse() {
            // when
            boolean deleted = playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlist.getId(),
                subscriber.getId()
            );

            // then
            assertThat(deleted).isFalse();
            assertThat(jpaPlaylistSubscriberRepository.findAll()).isEmpty();
        }
    }
}
