package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.application.watchingsession.WatchingSessionFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WatchingSessionController.class)
@Import({
    ApiControllerAdvice.class,
    TestSecurityConfig.class
})
@DisplayName("WatchingSessionController 슬라이스 테스트")
class WatchingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchingSessionFacade watchingSessionFacade;

    private MoplUserDetails mockUserDetails;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        UUID userId = UUID.randomUUID();

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(userId);
        given(mockUserDetails.getUsername()).willReturn(userId.toString());
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private static WatchingSessionResponse createWatchingSessionResponse(
        UUID sessionId,
        UUID watcherId,
        UUID contentId
    ) {
        return new WatchingSessionResponse(
            sessionId,
            Instant.now(),
            new UserSummary(watcherId, "TestUser", "https://cdn.example.com/profile.jpg"),
            new ContentSummary(
                contentId,
                ContentType.movie,
                "인셉션",
                "꿈속의 꿈을 다룬 SF 영화",
                "https://cdn.example.com/thumbnail.jpg",
                List.of("SF", "Action"),
                4.5,
                100
            )
        );
    }

    @Nested
    @DisplayName("GET /api/contents/{contentId}/watching-sessions")
    class GetWatchingSessionsTest {

        @Test
        @DisplayName("Returns 200 OK with watching session list")
        void withValidRequest_returns200OKWithSessionList() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            UUID sessionId1 = UUID.randomUUID();
            UUID sessionId2 = UUID.randomUUID();
            UUID watcherId1 = UUID.randomUUID();
            UUID watcherId2 = UUID.randomUUID();

            WatchingSessionResponse session1 = createWatchingSessionResponse(sessionId1, watcherId1, contentId);
            WatchingSessionResponse session2 = createWatchingSessionResponse(sessionId2, watcherId2, contentId);

            CursorResponse<WatchingSessionResponse> cursorResponse = CursorResponse.of(
                List.of(session1, session2),
                Instant.now().toString(),
                sessionId2,
                true,
                50,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(watchingSessionFacade.getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)
                .with(user(mockUserDetails))
                .param("limit", "20")
                .param("sortDirection", "DESCENDING")
                .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(sessionId1.toString()))
                .andExpect(jsonPath("$.data[0].watcher.userId").value(watcherId1.toString()))
                .andExpect(jsonPath("$.data[0].content.id").value(contentId.toString()))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalCount").value(50))
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            then(watchingSessionFacade).should().getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class));
        }

        @Test
        @DisplayName("Applies watcherNameLike filter correctly")
        void withWatcherNameLikeFilter_appliesFilter() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            CursorResponse<WatchingSessionResponse> emptyResponse = CursorResponse.empty(
                "createdAt", SortDirection.DESCENDING
            );

            given(watchingSessionFacade.getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class)))
                .willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)
                .with(user(mockUserDetails))
                .param("watcherNameLike", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));

            then(watchingSessionFacade).should().getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class));
        }

        @Test
        @DisplayName("Handles cursor-based pagination")
        void withCursorParams_handlesPagination() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            UUID idAfter = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            UUID watcherId = UUID.randomUUID();

            WatchingSessionResponse session = createWatchingSessionResponse(sessionId, watcherId, contentId);

            CursorResponse<WatchingSessionResponse> cursorResponse = CursorResponse.of(
                List.of(session),
                null,
                null,
                false,
                25,
                "createdAt",
                SortDirection.ASCENDING
            );

            given(watchingSessionFacade.getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)
                .with(user(mockUserDetails))
                .param("cursor", Instant.now().toString())
                .param("idAfter", idAfter.toString())
                .param("limit", "20")
                .param("sortDirection", "ASCENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

            then(watchingSessionFacade).should().getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class));
        }

        @Test
        @DisplayName("Returns empty list when no sessions")
        void withNoResults_returnsEmptyList() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            CursorResponse<WatchingSessionResponse> emptyResponse = CursorResponse.empty(
                "createdAt", SortDirection.DESCENDING
            );

            given(watchingSessionFacade.getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class)))
                .willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/watching-sessions", contentId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));

            then(watchingSessionFacade).should().getWatchingSessions(eq(contentId), any(WatchingSessionQueryRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{watcherId}/watching-sessions")
    class GetWatchingSessionTest {

        @Test
        @DisplayName("Returns 200 OK with watching session for valid watcher")
        void withValidWatcherId_returns200OK() throws Exception {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            WatchingSessionResponse response = createWatchingSessionResponse(sessionId, watcherId, contentId);

            given(watchingSessionFacade.getWatchingSession(watcherId)).willReturn(Optional.of(response));

            // when & then
            mockMvc.perform(get("/api/users/{watcherId}/watching-sessions", watcherId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.watcher.userId").value(watcherId.toString()))
                .andExpect(jsonPath("$.watcher.name").value("TestUser"))
                .andExpect(jsonPath("$.content.id").value(contentId.toString()))
                .andExpect(jsonPath("$.content.title").value("인셉션"));

            then(watchingSessionFacade).should().getWatchingSession(watcherId);
        }

        @Test
        @DisplayName("Returns 204 No Content when watcher has no active session")
        void withNoActiveSession_returns204NoContent() throws Exception {
            // given
            UUID watcherId = UUID.randomUUID();

            given(watchingSessionFacade.getWatchingSession(watcherId)).willReturn(Optional.empty());

            // when & then
            mockMvc.perform(get("/api/users/{watcherId}/watching-sessions", watcherId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

            then(watchingSessionFacade).should().getWatchingSession(watcherId);
        }

        @Test
        @DisplayName("Returns 404 Not Found for non-existing watcher")
        void withNonExistingWatcher_returns404NotFound() throws Exception {
            // given
            UUID nonExistingWatcherId = UUID.randomUUID();

            given(watchingSessionFacade.getWatchingSession(nonExistingWatcherId))
                .willThrow(UserNotFoundException.withId(nonExistingWatcherId));

            // when & then
            mockMvc.perform(get("/api/users/{watcherId}/watching-sessions", nonExistingWatcherId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

            then(watchingSessionFacade).should().getWatchingSession(nonExistingWatcherId);
        }
    }
}
