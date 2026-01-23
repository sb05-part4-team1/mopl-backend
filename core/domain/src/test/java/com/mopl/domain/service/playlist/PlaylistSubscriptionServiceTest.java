package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistSubscriptionAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistSubscriptionNotFoundException;
import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistSubscriptionService 단위 테스트")
class PlaylistSubscriptionServiceTest {

    @Mock
    private PlaylistSubscriberRepository playlistSubscriberRepository;

    @Mock
    private PlaylistSubscriberCountRepository playlistSubscriberCountRepository;

    @InjectMocks
    private PlaylistSubscriptionService playlistSubscriptionService;

    @Nested
    @DisplayName("getSubscriberCount()")
    class GetSubscriberCountTest {

        @Test
        @DisplayName("구독자 수 조회 성공")
        void withValidPlaylistId_returnsCount() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistSubscriberCountRepository.getCount(playlistId)).willReturn(100L);

            // when
            long result = playlistSubscriptionService.getSubscriberCount(playlistId);

            // then
            assertThat(result).isEqualTo(100L);
            then(playlistSubscriberCountRepository).should().getCount(playlistId);
            then(playlistSubscriberRepository).should(never()).countByPlaylistId(playlistId);
        }

        @Test
        @DisplayName("Redis 장애 시 DB fallback")
        void withRedisFailure_fallsBackToDb() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistSubscriberCountRepository.getCount(playlistId))
                .willThrow(new RuntimeException("Redis connection failed"));
            given(playlistSubscriberRepository.countByPlaylistId(playlistId)).willReturn(50L);

            // when
            long result = playlistSubscriptionService.getSubscriberCount(playlistId);

            // then
            assertThat(result).isEqualTo(50L);
            then(playlistSubscriberCountRepository).should().getCount(playlistId);
            then(playlistSubscriberRepository).should().countByPlaylistId(playlistId);
        }
    }

    @Nested
    @DisplayName("getSubscriberCounts()")
    class GetSubscriberCountsTest {

        @Test
        @DisplayName("여러 플레이리스트 구독자 수 조회 성공")
        void withValidPlaylistIds_returnsCountsMap() {
            // given
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);
            Map<UUID, Long> expectedCounts = Map.of(playlistId1, 10L, playlistId2, 20L);

            given(playlistSubscriberCountRepository.getCounts(playlistIds))
                .willReturn(expectedCounts);

            // when
            Map<UUID, Long> result = playlistSubscriptionService.getSubscriberCounts(playlistIds);

            // then
            assertThat(result).isEqualTo(expectedCounts);
            then(playlistSubscriberCountRepository).should().getCounts(playlistIds);
            then(playlistSubscriberRepository).should(never()).countByPlaylistIdIn(playlistIds);
        }

        @Test
        @DisplayName("Redis 장애 시 DB fallback")
        void withRedisFailure_fallsBackToDb() {
            // given
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);
            Map<UUID, Long> dbCounts = Map.of(playlistId1, 5L, playlistId2, 15L);

            given(playlistSubscriberCountRepository.getCounts(playlistIds))
                .willThrow(new RuntimeException("Redis connection failed"));
            given(playlistSubscriberRepository.countByPlaylistIdIn(playlistIds)).willReturn(dbCounts);

            // when
            Map<UUID, Long> result = playlistSubscriptionService.getSubscriberCounts(playlistIds);

            // then
            assertThat(result).isEqualTo(dbCounts);
            then(playlistSubscriberCountRepository).should().getCounts(playlistIds);
            then(playlistSubscriberRepository).should().countByPlaylistIdIn(playlistIds);
        }
    }

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
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(false);

            // when
            playlistSubscriptionService.subscribe(playlistId, subscriberId);

            // then
            then(playlistSubscriberRepository).should().save(playlistId, subscriberId);
            then(playlistSubscriberCountRepository).should().increment(playlistId);
        }

        @Test
        @DisplayName("이미 구독 중이면 PlaylistSubscriptionAlreadyExistsException 발생")
        void withExistingSubscription_throwsPlaylistSubscriptionAlreadyExistsException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> playlistSubscriptionService.subscribe(playlistId, subscriberId)
            ).isInstanceOf(PlaylistSubscriptionAlreadyExistsException.class);

            then(playlistSubscriberRepository).should(never()).save(playlistId, subscriberId);
            then(playlistSubscriberCountRepository).should(never()).increment(playlistId);
        }

        @Test
        @DisplayName("Redis increment 실패해도 구독 성공")
        void withRedisIncrementFailure_subscribesSuccessfully() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(false);
            doThrow(new RuntimeException("Redis connection failed"))
                .when(playlistSubscriberCountRepository).increment(playlistId);

            // when
            playlistSubscriptionService.subscribe(playlistId, subscriberId);

            // then
            then(playlistSubscriberRepository).should().save(playlistId, subscriberId);
            then(playlistSubscriberCountRepository).should().increment(playlistId);
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
            then(playlistSubscriberCountRepository).should().decrement(playlistId);
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

            then(playlistSubscriberCountRepository).should(never()).decrement(playlistId);
        }

        @Test
        @DisplayName("Redis decrement 실패해도 구독 취소 성공")
        void withRedisDecrementFailure_unsubscribesSuccessfully() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID subscriberId = UUID.randomUUID();

            given(playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            )).willReturn(true);
            doThrow(new RuntimeException("Redis connection failed"))
                .when(playlistSubscriberCountRepository).decrement(playlistId);

            // when
            playlistSubscriptionService.unsubscribe(playlistId, subscriberId);

            // then
            then(playlistSubscriberRepository).should().deleteByPlaylistIdAndSubscriberId(
                playlistId, subscriberId
            );
            then(playlistSubscriberCountRepository).should().decrement(playlistId);
        }
    }
}
