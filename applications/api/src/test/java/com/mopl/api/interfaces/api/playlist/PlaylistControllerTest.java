package com.mopl.api.interfaces.api.playlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.playlist.PlaylistFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.content.ContentSummaryMapper;
import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistSubscriptionAlreadyExistsException;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaylistController.class)
@Import({
    ApiControllerAdvice.class,
    PlaylistResponseMapper.class,
    UserSummaryMapper.class,
    ContentSummaryMapper.class,
    TestSecurityConfig.class
})
@DisplayName("PlaylistController 슬라이스 테스트")
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaylistFacade playlistFacade;

    private MoplUserDetails mockUserDetails;
    private UUID userId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        userId = UUID.randomUUID();

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(userId);
        given(mockUserDetails.getUsername()).willReturn(userId.toString());
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("GET /api/playlists - 플레이리스트 목록 조회")
    class GetPlaylistsTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UserSummary ownerSummary = new UserSummary(userId, "테스트 사용자", null);
            PlaylistResponse playlistResponse = new PlaylistResponse(
                playlistId,
                ownerSummary,
                "테스트 플레이리스트",
                "테스트 설명",
                null,
                10L,
                true,
                Collections.emptyList()
            );
            CursorResponse<PlaylistResponse> response = CursorResponse.of(
                List.of(playlistResponse),
                null,
                null,
                false,
                1L,
                "updatedAt",
                SortDirection.ASCENDING
            );

            given(playlistFacade.getPlaylists(eq(userId), any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/playlists")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(playlistId.toString()))
                .andExpect(jsonPath("$.data[0].title").value("테스트 플레이리스트"))
                .andExpect(jsonPath("$.data[0].subscriberCount").value(10))
                .andExpect(jsonPath("$.data[0].subscribedByMe").value(true));

            then(playlistFacade).should().getPlaylists(eq(userId), any());
        }

        @Test
        @DisplayName("빈 목록 시 200 OK 응답")
        void withEmptyList_returns200OK() throws Exception {
            // given
            CursorResponse<PlaylistResponse> response = CursorResponse.empty(
                "updatedAt",
                SortDirection.ASCENDING
            );

            given(playlistFacade.getPlaylists(eq(userId), any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/playlists")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/playlists/{playlistId} - 플레이리스트 조회")
    class GetPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UserSummary ownerSummary = new UserSummary(userId, "테스트 사용자", null);
            PlaylistResponse playlistResponse = new PlaylistResponse(
                playlistId,
                ownerSummary,
                "테스트 플레이리스트",
                "테스트 설명",
                null,
                10L,
                true,
                Collections.emptyList()
            );

            given(playlistFacade.getPlaylist(userId, playlistId)).willReturn(playlistResponse);

            // when & then
            mockMvc.perform(get("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playlistId.toString()))
                .andExpect(jsonPath("$.title").value("테스트 플레이리스트"))
                .andExpect(jsonPath("$.owner.userId").value(userId.toString()))
                .andExpect(jsonPath("$.subscriberCount").value(10))
                .andExpect(jsonPath("$.subscribedByMe").value(true));

            then(playlistFacade).should().getPlaylist(userId, playlistId);
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회 시 404 Not Found 응답")
        void withNonExistingPlaylistId_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistFacade.getPlaylist(userId, playlistId))
                .willThrow(new PlaylistNotFoundException(playlistId));

            // when & then
            mockMvc.perform(get("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/playlists - 플레이리스트 생성")
    class CreatePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            String title = "내 플레이리스트";
            String description = "플레이리스트 설명";
            PlaylistCreateRequest request = new PlaylistCreateRequest(title, description);

            UUID playlistId = UUID.randomUUID();
            UserSummary ownerSummary = new UserSummary(userId, "테스트 사용자", null);
            PlaylistResponse playlistResponse = new PlaylistResponse(
                playlistId,
                ownerSummary,
                title,
                description,
                null,
                0L,
                false,
                Collections.emptyList()
            );

            given(playlistFacade.createPlaylist(eq(userId), any(PlaylistCreateRequest.class)))
                .willReturn(playlistResponse);

            // when & then
            mockMvc.perform(post("/api/playlists")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(playlistId.toString()))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.owner.userId").value(userId.toString()));

            then(playlistFacade).should().createPlaylist(eq(userId), any(
                PlaylistCreateRequest.class));
        }

        static Stream<Arguments> invalidRequestProvider() {
            return Stream.of(
                Arguments.of("제목이 비어있음", "", "설명"),
                Arguments.of("제목이 null", null, "설명"),
                Arguments.of("제목이 255자 초과", "a".repeat(256), "설명")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("유효하지 않은 요청 시 400 Bad Request 응답")
        void withInvalidRequest_returns400BadRequest(
            String description,
            String title,
            String desc
        ) throws Exception {
            // given
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("title", title);
            requestBody.put("description", desc);

            // when & then
            mockMvc.perform(post("/api/playlists")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(playlistFacade).should(never()).createPlaylist(any(), any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/playlists/{playlistId} - 플레이리스트 수정")
    class UpdatePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            String newTitle = "수정된 제목";
            String newDescription = "수정된 설명";
            PlaylistUpdateRequest request = new PlaylistUpdateRequest(newTitle, newDescription);

            UserSummary ownerSummary = new UserSummary(userId, "테스트 사용자", null);
            PlaylistResponse playlistResponse = new PlaylistResponse(
                playlistId,
                ownerSummary,
                newTitle,
                newDescription,
                null,
                0L,
                false,
                Collections.emptyList()
            );

            given(playlistFacade.updatePlaylist(eq(userId), eq(playlistId),
                any(PlaylistUpdateRequest.class)))
                .willReturn(playlistResponse);

            // when & then
            mockMvc.perform(patch("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playlistId.toString()))
                .andExpect(jsonPath("$.title").value(newTitle))
                .andExpect(jsonPath("$.description").value(newDescription));

            then(playlistFacade).should().updatePlaylist(eq(userId), eq(playlistId),
                any(PlaylistUpdateRequest.class));
        }

        @Test
        @DisplayName("권한이 없는 사용자가 수정 시 403 Forbidden 응답")
        void withNonOwner_returns403Forbidden() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest("새 제목", "새 설명");

            given(playlistFacade.updatePlaylist(eq(userId), eq(playlistId),
                any(PlaylistUpdateRequest.class)))
                .willThrow(new PlaylistForbiddenException(playlistId, userId, ownerId));

            // when & then
            mockMvc.perform(patch("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 수정 시 404 Not Found 응답")
        void withNonExistingPlaylist_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest("새 제목", "새 설명");

            given(playlistFacade.updatePlaylist(eq(userId), eq(playlistId),
                any(PlaylistUpdateRequest.class)))
                .willThrow(new PlaylistNotFoundException(playlistId));

            // when & then
            mockMvc.perform(patch("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/playlists/{playlistId} - 플레이리스트 삭제")
    class DeletePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willDoNothing().given(playlistFacade).deletePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(playlistFacade).should().deletePlaylist(userId, playlistId);
        }

        @Test
        @DisplayName("권한이 없는 사용자가 삭제 시 403 Forbidden 응답")
        void withNonOwner_returns403Forbidden() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            willThrow(new PlaylistForbiddenException(playlistId, userId, ownerId))
                .given(playlistFacade).deletePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 삭제 시 404 Not Found 응답")
        void withNonExistingPlaylist_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willThrow(new PlaylistNotFoundException(playlistId))
                .given(playlistFacade).deletePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/playlists/{playlistId}/contents/{contentId} - 콘텐츠 추가")
    class AddContentToPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            willDoNothing().given(playlistFacade).addContentToPlaylist(userId, playlistId,
                contentId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(playlistFacade).should().addContentToPlaylist(userId, playlistId, contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 추가 시 404 Not Found 응답")
        void withNonExistingContent_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            willThrow(ContentNotFoundException.withId(contentId))
                .given(playlistFacade).addContentToPlaylist(userId, playlistId, contentId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 콘텐츠 추가 시 403 Forbidden 응답")
        void withNonOwner_returns403Forbidden() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            willThrow(new PlaylistForbiddenException(playlistId, userId, ownerId))
                .given(playlistFacade).addContentToPlaylist(userId, playlistId, contentId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("이미 존재하는 콘텐츠 추가 시 409 Conflict 응답")
        void withAlreadyExistingContent_returns409Conflict() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            willThrow(new PlaylistContentAlreadyExistsException(playlistId, contentId))
                .given(playlistFacade).addContentToPlaylist(userId, playlistId, contentId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /api/playlists/{playlistId}/contents/{contentId} - 콘텐츠 삭제")
    class DeleteContentFromPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            willDoNothing().given(playlistFacade).deleteContentFromPlaylist(userId, playlistId,
                contentId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(playlistFacade).should().deleteContentFromPlaylist(userId, playlistId, contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 삭제 시 404 Not Found 응답")
        void withNonExistingContent_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            willThrow(new PlaylistContentNotFoundException(playlistId, contentId))
                .given(playlistFacade).deleteContentFromPlaylist(userId, playlistId, contentId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 콘텐츠 삭제 시 403 Forbidden 응답")
        void withNonOwner_returns403Forbidden() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            willThrow(new PlaylistForbiddenException(playlistId, userId, ownerId))
                .given(playlistFacade).deleteContentFromPlaylist(userId, playlistId, contentId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}/contents/{contentId}",
                playlistId, contentId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/playlists/{playlistId}/subscription - 구독")
    class SubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willDoNothing().given(playlistFacade).subscribePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/subscription", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(playlistFacade).should().subscribePlaylist(userId, playlistId);
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 구독 시 404 Not Found 응답")
        void withNonExistingPlaylist_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willThrow(new PlaylistNotFoundException(playlistId))
                .given(playlistFacade).subscribePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/subscription", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("이미 구독 중인 플레이리스트 구독 시 409 Conflict 응답")
        void withAlreadySubscribed_returns409Conflict() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willThrow(new PlaylistSubscriptionAlreadyExistsException(playlistId, userId))
                .given(playlistFacade).subscribePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(post("/api/playlists/{playlistId}/subscription", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /api/playlists/{playlistId}/subscription - 구독 취소")
    class UnsubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willDoNothing().given(playlistFacade).unsubscribePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}/subscription", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(playlistFacade).should().unsubscribePlaylist(userId, playlistId);
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 구독 취소 시 404 Not Found 응답")
        void withNonExistingPlaylist_returns404NotFound() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            willThrow(new PlaylistNotFoundException(playlistId))
                .given(playlistFacade).unsubscribePlaylist(userId, playlistId);

            // when & then
            mockMvc.perform(delete("/api/playlists/{playlistId}/subscription", playlistId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }
}
