package com.mopl.domain.service.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchingSessionService 단위 테스트")
class WatchingSessionServiceTest {

    @Mock
    private WatchingSessionQueryRepository watchingSessionQueryRepository;

    @Mock
    private WatchingSessionRepository watchingSessionRepository;

    @InjectMocks
    private WatchingSessionService watchingSessionService;

    @Nested
    @DisplayName("getWatchingSessions()")
    class GetWatchingSessionsTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            UUID contentId = UUID.randomUUID();
            WatchingSessionQueryRequest request = new WatchingSessionQueryRequest(
                null,
                null,
                null,
                null,
                null,
                null
            );
            CursorResponse<WatchingSessionModel> expectedResponse = CursorResponse.empty(
                WatchingSessionSortField.createdAt.name(), SortDirection.ASCENDING
            );

            given(watchingSessionQueryRepository.findAllByContentId(contentId, request))
                .willReturn(expectedResponse);

            // when
            CursorResponse<WatchingSessionModel> result = watchingSessionService.getWatchingSessions(contentId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(watchingSessionQueryRepository).should().findAllByContentId(contentId, request);
        }
    }

    @Nested
    @DisplayName("getWatchingSessionByWatcherId()")
    class GetWatchingSessionByWatcherIdTest {

        @Test
        @DisplayName("존재하는 시청자 ID로 조회하면 WatchingSessionModel 반환")
        void withExistingWatcherId_returnsWatchingSessionModel() {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            WatchingSessionModel watchingSessionModel = WatchingSessionModel.create(
                watcherId,
                "홍길동",
                "/profile/image.png",
                contentId,
                "테스트 콘텐츠"
            );

            given(watchingSessionRepository.findByWatcherId(watcherId))
                .willReturn(Optional.of(watchingSessionModel));

            // when
            Optional<WatchingSessionModel> result = watchingSessionService.getWatchingSessionByWatcherId(watcherId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(watchingSessionModel);
            then(watchingSessionRepository).should().findByWatcherId(watcherId);
        }

        @Test
        @DisplayName("존재하지 않는 시청자 ID로 조회하면 빈 Optional 반환")
        void withNonExistingWatcherId_returnsEmpty() {
            // given
            UUID watcherId = UUID.randomUUID();

            given(watchingSessionRepository.findByWatcherId(watcherId))
                .willReturn(Optional.empty());

            // when
            Optional<WatchingSessionModel> result = watchingSessionService.getWatchingSessionByWatcherId(watcherId);

            // then
            assertThat(result).isEmpty();
            then(watchingSessionRepository).should().findByWatcherId(watcherId);
        }
    }

    @Nested
    @DisplayName("countByContentId()")
    class CountByContentIdTest {

        @Test
        @DisplayName("콘텐츠 ID로 시청 세션 수 조회")
        void returnsCount() {
            // given
            UUID contentId = UUID.randomUUID();
            long expectedCount = 42L;

            given(watchingSessionRepository.countByContentId(contentId))
                .willReturn(expectedCount);

            // when
            long result = watchingSessionService.countByContentId(contentId);

            // then
            assertThat(result).isEqualTo(expectedCount);
            then(watchingSessionRepository).should().countByContentId(contentId);
        }
    }

    @Nested
    @DisplayName("countByContentIdIn()")
    class CountByContentIdInTest {

        @Test
        @DisplayName("여러 콘텐츠 ID로 시청 세션 수 조회")
        void returnsCountMap() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(contentId1, contentId2);
            Map<UUID, Long> expectedCounts = Map.of(
                contentId1, 10L,
                contentId2, 20L
            );

            given(watchingSessionRepository.countByContentIdIn(contentIds))
                .willReturn(expectedCounts);

            // when
            Map<UUID, Long> result = watchingSessionService.countByContentIdIn(contentIds);

            // then
            assertThat(result).isEqualTo(expectedCounts);
            then(watchingSessionRepository).should().countByContentIdIn(contentIds);
        }
    }
}
