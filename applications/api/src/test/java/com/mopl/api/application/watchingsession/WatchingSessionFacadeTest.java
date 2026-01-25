package com.mopl.api.application.watchingsession;

import com.mopl.dto.watchingsession.WatchingSessionResponse;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.domain.fixture.WatchingSessionModelFixture;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchingSessionFacade 단위 테스트")
class WatchingSessionFacadeTest {

    @Mock
    private WatchingSessionService watchingSessionService;

    @Mock
    private WatchingSessionResponseMapper watchingSessionResponseMapper;

    @InjectMocks
    private WatchingSessionFacade watchingSessionFacade;

    @Nested
    @DisplayName("getWatchingSession()")
    class GetWatchingSessionTest {

        @Test
        @DisplayName("유효한 요청 시 시청 세션 조회 성공")
        void withValidRequest_getWatchingSessionSuccess() {
            // given
            WatchingSessionModel watchingSessionModel = WatchingSessionModelFixture.create();
            UUID watcherId = watchingSessionModel.getWatcherId();

            WatchingSessionResponse expectedResponse = new WatchingSessionResponse(
                watcherId,
                watchingSessionModel.getCreatedAt(),
                null,
                null
            );

            given(watchingSessionService.getWatchingSessionByWatcherId(watcherId))
                .willReturn(Optional.of(watchingSessionModel));
            given(watchingSessionResponseMapper.toDto(watchingSessionModel))
                .willReturn(expectedResponse);

            // when
            Optional<WatchingSessionResponse> result = watchingSessionFacade.getWatchingSession(watcherId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(watcherId);

            then(watchingSessionService).should().getWatchingSessionByWatcherId(watcherId);
            then(watchingSessionResponseMapper).should().toDto(watchingSessionModel);
        }

        @Test
        @DisplayName("시청 세션이 없는 경우 빈 Optional 반환")
        void withNoSession_returnsEmptyOptional() {
            // given
            UUID watcherId = UUID.randomUUID();

            given(watchingSessionService.getWatchingSessionByWatcherId(watcherId))
                .willReturn(Optional.empty());

            // when
            Optional<WatchingSessionResponse> result = watchingSessionFacade.getWatchingSession(watcherId);

            // then
            assertThat(result).isEmpty();

            then(watchingSessionService).should().getWatchingSessionByWatcherId(watcherId);
            then(watchingSessionResponseMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getWatchingSessions()")
    class GetWatchingSessionsTest {

        @Test
        @DisplayName("유효한 요청 시 시청 세션 목록 조회 성공")
        void withValidRequest_getWatchingSessionsSuccess() {
            // given
            UUID contentId = UUID.randomUUID();
            WatchingSessionModel session1 = WatchingSessionModelFixture.builder()
                .set("contentId", contentId)
                .sample();
            WatchingSessionModel session2 = WatchingSessionModelFixture.builder()
                .set("contentId", contentId)
                .sample();

            WatchingSessionResponse response1 = new WatchingSessionResponse(
                session1.getWatcherId(),
                session1.getCreatedAt(),
                null,
                null
            );
            WatchingSessionResponse response2 = new WatchingSessionResponse(
                session2.getWatcherId(),
                session2.getCreatedAt(),
                null,
                null
            );

            CursorResponse<WatchingSessionModel> serviceResponse = CursorResponse.of(
                List.of(session1, session2),
                session2.getCreatedAt().toString(),
                session2.getWatcherId(),
                true,
                10,
                "createdAt",
                SortDirection.ASCENDING
            );

            WatchingSessionQueryRequest request = new WatchingSessionQueryRequest(
                null, null, null, 10, SortDirection.ASCENDING, WatchingSessionSortField.createdAt
            );

            given(watchingSessionService.getWatchingSessions(contentId, request))
                .willReturn(serviceResponse);
            given(watchingSessionResponseMapper.toDto(session1)).willReturn(response1);
            given(watchingSessionResponseMapper.toDto(session2)).willReturn(response2);

            // when
            CursorResponse<WatchingSessionResponse> result = watchingSessionFacade.getWatchingSessions(contentId, request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data().get(0).id()).isEqualTo(session1.getWatcherId());
            assertThat(result.data().get(1).id()).isEqualTo(session2.getWatcherId());
            assertThat(result.hasNext()).isTrue();
            assertThat(result.totalCount()).isEqualTo(10);

            then(watchingSessionService).should().getWatchingSessions(contentId, request);
        }

        @Test
        @DisplayName("빈 결과 시 빈 목록 반환")
        void withNoSessions_returnsEmptyList() {
            // given
            UUID contentId = UUID.randomUUID();

            CursorResponse<WatchingSessionModel> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.ASCENDING
            );

            WatchingSessionQueryRequest request = new WatchingSessionQueryRequest(
                null, null, null, 10, SortDirection.ASCENDING, WatchingSessionSortField.createdAt
            );

            given(watchingSessionService.getWatchingSessions(contentId, request))
                .willReturn(emptyResponse);

            // when
            CursorResponse<WatchingSessionResponse> result = watchingSessionFacade.getWatchingSessions(contentId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();

            then(watchingSessionService).should().getWatchingSessions(contentId, request);
            then(watchingSessionResponseMapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("필터 조건이 적용된 요청 처리")
        void withFilters_appliesFiltersCorrectly() {
            // given
            UUID contentId = UUID.randomUUID();
            String watcherNameLike = "User";

            WatchingSessionModel session = WatchingSessionModelFixture.builder()
                .set("contentId", contentId)
                .set("watcherName", "User_Test")
                .sample();

            WatchingSessionResponse response = new WatchingSessionResponse(
                session.getWatcherId(),
                session.getCreatedAt(),
                null,
                null
            );

            CursorResponse<WatchingSessionModel> serviceResponse = CursorResponse.of(
                List.of(session),
                null,
                null,
                false,
                1,
                "createdAt",
                SortDirection.ASCENDING
            );

            WatchingSessionQueryRequest request = new WatchingSessionQueryRequest(
                watcherNameLike, null, null, 10, SortDirection.ASCENDING, WatchingSessionSortField.createdAt
            );

            given(watchingSessionService.getWatchingSessions(contentId, request))
                .willReturn(serviceResponse);
            given(watchingSessionResponseMapper.toDto(session)).willReturn(response);

            // when
            CursorResponse<WatchingSessionResponse> result = watchingSessionFacade.getWatchingSessions(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.hasNext()).isFalse();

            then(watchingSessionService).should().getWatchingSessions(contentId, request);
        }
    }
}
