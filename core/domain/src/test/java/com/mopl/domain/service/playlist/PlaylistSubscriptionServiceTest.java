package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistSubscriptionAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistSubscriptionNotFoundException;
import com.mopl.domain.exception.playlist.SelfSubscriptionNotAllowedException;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistSubscriptionService 단위 테스트")
class PlaylistSubscriptionServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistSubscriberRepository playlistSubscriberRepository;

    @InjectMocks
    private PlaylistSubscriptionService playlistSubscriptionService;

    @Nested
    @DisplayName("isSubscribedByPlaylistIdAndSubscriberId()")
    class IsSubscribedByPlaylistIdAndSubscriberIdTest {

        @Test
        @DisplayName("구독 중이면 true 반환")
        void withExistingSubscription_returnsTrue() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(true);

            // when
            boolean result = playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            );

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("구독 중이 아니면 false 반환")
        void withNoSubscription_returnsFalse() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(false);

            // when
            boolean result = playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            );

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findSubscribedPlaylistIds()")
    class FindSubscribedPlaylistIdsTest {

        @Test
        @DisplayName("구독 중인 플레이리스트 ID 목록 반환")
        void withValidSubscriberIdAndPlaylistIds_returnsSubscribedIds() {
            // given
            UUID subscriberId = UUID.randomUUID();
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);
            Set<UUID> subscribedIds = Set.of(playlistId1);

            given(playlistSubscriberRepository.findSubscribedPlaylistIds(subscriberId, playlistIds))
                .willReturn(subscribedIds);

            // when
            Set<UUID> result = playlistSubscriptionService.findSubscribedPlaylistIds(
                subscriberId, playlistIds
            );

            // then
            assertThat(result).containsExactly(playlistId1);
        }
    }

    @Nested
    @DisplayName("getSubscriberIds()")
    class GetSubscriberIdsTest {

        @Test
        @DisplayName("플레이리스트 구독자 ID 목록 반환")
        void withValidPlaylistId_returnsSubscriberIds() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId1 = UUID.randomUUID();
            UUID subscriberId2 = UUID.randomUUID();
            List<UUID> subscriberIds = List.of(subscriberId1, subscriberId2);

            given(playlistSubscriberRepository.findSubscriberIdsByPlaylistId(playlistId))
                .willReturn(subscriberIds);

            // when
            List<UUID> result = playlistSubscriptionService.getSubscriberIds(playlistId);

            // then
            assertThat(result).containsExactly(subscriberId1, subscriberId2);
        }
    }

    @Nested
    @DisplayName("subscribe()")
    class SubscribeTest {

        @Test
        @DisplayName("구독 성공")
        void withValidPlaylistIdAndSubscriberId_subscribesSuccessfully() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();
            PlaylistModel playlist = createPlaylistWithOwner(playlistId, ownerId);

            given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(false);

            // when
            playlistSubscriptionService.subscribe(playlistId, subscriberId);

            // then
            then(playlistSubscriberRepository).should().save(playlistId, subscriberId);
        }

        @Test
        @DisplayName("플레이리스트가 존재하지 않으면 PlaylistNotFoundException 발생")
        void withNonExistentPlaylist_throwsPlaylistNotFoundException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> playlistSubscriptionService.subscribe(playlistId, subscriberId)
            ).isInstanceOf(PlaylistNotFoundException.class);

            then(playlistSubscriberRepository).should(never()).save(playlistId, subscriberId);
        }

        @Test
        @DisplayName("자기 자신의 플레이리스트 구독 시 SelfSubscriptionNotAllowedException 발생")
        void withSelfSubscription_throwsSelfSubscriptionNotAllowedException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            PlaylistModel playlist = createPlaylistWithOwner(playlistId, ownerId);

            given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

            // when & then
            assertThatThrownBy(
                () -> playlistSubscriptionService.subscribe(playlistId, ownerId)
            ).isInstanceOf(SelfSubscriptionNotAllowedException.class);

            then(playlistSubscriberRepository).should(never()).save(playlistId, ownerId);
        }

        @Test
        @DisplayName("이미 구독 중이면 PlaylistSubscriptionAlreadyExistsException 발생")
        void withExistingSubscription_throwsPlaylistSubscriptionAlreadyExistsException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();
            PlaylistModel playlist = createPlaylistWithOwner(playlistId, ownerId);

            given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> playlistSubscriptionService.subscribe(playlistId, subscriberId)
            ).isInstanceOf(PlaylistSubscriptionAlreadyExistsException.class);

            then(playlistSubscriberRepository).should(never()).save(playlistId, subscriberId);
        }

        private PlaylistModel createPlaylistWithOwner(UUID playlistId, UUID ownerId) {
            UserModel owner = UserModel.builder()
                .id(ownerId)
                .name("testuser")
                .build();

            return PlaylistModel.builder()
                .id(playlistId)
                .title("Test Playlist")
                .owner(owner)
                .build();
        }
    }

    @Nested
    @DisplayName("unsubscribe()")
    class UnsubscribeTest {

        @Test
        @DisplayName("구독 취소 성공")
        void withValidPlaylistIdAndSubscriberId_unsubscribesSuccessfully() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(true);

            // when
            playlistSubscriptionService.unsubscribe(playlistId, subscriberId);

            // then
            then(playlistSubscriberRepository).should().deleteByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            );
        }

        @Test
        @DisplayName("구독 중이 아니면 PlaylistSubscriptionNotFoundException 발생")
        void withNoSubscription_throwsPlaylistSubscriptionNotFoundException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(false);

            // when & then
            assertThatThrownBy(
                () -> playlistSubscriptionService.unsubscribe(playlistId, subscriberId)
            ).isInstanceOf(PlaylistSubscriptionNotFoundException.class);
        }
    }
}
