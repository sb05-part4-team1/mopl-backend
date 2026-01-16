package com.mopl.api.scheduler;

import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistSubscriberCountSyncScheduler 단위 테스트")
class PlaylistSubscriberCountSyncSchedulerTest {

    @Mock
    private PlaylistSubscriberRepository playlistSubscriberRepository;

    @Mock
    private PlaylistSubscriberCountRepository playlistSubscriberCountRepository;

    @InjectMocks
    private PlaylistSubscriberCountSyncScheduler scheduler;

    @Nested
    @DisplayName("syncSubscriberCounts()")
    class SyncSubscriberCountsTest {

        @Test
        @DisplayName("DB와 Redis 카운트가 다르면 Redis를 DB 값으로 동기화")
        void whenCountMismatch_syncsRedisToDbCount() {
            // given
            UUID playlistId = UUID.randomUUID();
            long dbCount = 10L;
            long redisCount = 5L;

            given(playlistSubscriberRepository.findAllPlaylistIds())
                .willReturn(Set.of(playlistId));
            given(playlistSubscriberRepository.countByPlaylistId(playlistId))
                .willReturn(dbCount);
            given(playlistSubscriberCountRepository.getCount(playlistId))
                .willReturn(redisCount);

            // when
            scheduler.syncSubscriberCounts();

            // then
            then(playlistSubscriberCountRepository).should().setCount(playlistId, dbCount);
        }

        @Test
        @DisplayName("DB와 Redis 카운트가 같으면 동기화하지 않음")
        void whenCountMatch_doesNotSync() {
            // given
            UUID playlistId = UUID.randomUUID();
            long count = 10L;

            given(playlistSubscriberRepository.findAllPlaylistIds())
                .willReturn(Set.of(playlistId));
            given(playlistSubscriberRepository.countByPlaylistId(playlistId))
                .willReturn(count);
            given(playlistSubscriberCountRepository.getCount(playlistId))
                .willReturn(count);

            // when
            scheduler.syncSubscriberCounts();

            // then
            then(playlistSubscriberCountRepository).should(never()).setCount(playlistId, count);
        }

        @Test
        @DisplayName("플레이리스트가 없으면 동기화 작업 없음")
        void whenNoPlaylists_doesNothing() {
            // given
            given(playlistSubscriberRepository.findAllPlaylistIds())
                .willReturn(Set.of());

            // when
            scheduler.syncSubscriberCounts();

            // then
            then(playlistSubscriberRepository).should(never()).countByPlaylistId(
                org.mockito.ArgumentMatchers.any());
            then(playlistSubscriberCountRepository).should(never()).getCount(
                org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("여러 플레이리스트 중 불일치하는 것만 동기화")
        void whenMultiplePlaylists_syncsOnlyMismatched() {
            // given
            UUID matchedId = UUID.randomUUID();
            UUID mismatchedId = UUID.randomUUID();

            given(playlistSubscriberRepository.findAllPlaylistIds())
                .willReturn(Set.of(matchedId, mismatchedId));

            given(playlistSubscriberRepository.countByPlaylistId(matchedId)).willReturn(5L);
            given(playlistSubscriberCountRepository.getCount(matchedId)).willReturn(5L);

            given(playlistSubscriberRepository.countByPlaylistId(mismatchedId)).willReturn(10L);
            given(playlistSubscriberCountRepository.getCount(mismatchedId)).willReturn(3L);

            // when
            scheduler.syncSubscriberCounts();

            // then
            then(playlistSubscriberCountRepository).should(never()).setCount(matchedId, 5L);
            then(playlistSubscriberCountRepository).should().setCount(mismatchedId, 10L);
        }
    }
}
