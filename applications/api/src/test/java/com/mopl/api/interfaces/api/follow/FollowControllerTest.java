package com.mopl.api.interfaces.api.follow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.follow.FollowFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.domain.exception.follow.FollowNotAllowedException;
import com.mopl.domain.exception.follow.FollowNotFoundException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowStatusResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowController.class)
@Import({
    ApiControllerAdvice.class,
    TestSecurityConfig.class
})
@DisplayName("FollowController 슬라이스 테스트")
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FollowFacade followFacade;

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
    @DisplayName("POST /api/follows - 팔로우")
    class FollowTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();
            UUID followId = UUID.randomUUID();
            FollowRequest request = new FollowRequest(followeeId);
            FollowResponse response = new FollowResponse(followId, followeeId, userId);

            given(followFacade.follow(userId, followeeId)).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/follows")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(followId.toString()))
                .andExpect(jsonPath("$.followeeId").value(followeeId.toString()))
                .andExpect(jsonPath("$.followerId").value(userId.toString()));

            then(followFacade).should().follow(userId, followeeId);
        }

        @Test
        @DisplayName("followeeId가 null인 경우 400 Bad Request 응답")
        void withNullFolloweeId_returns400BadRequest() throws Exception {
            // given
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("followeeId", null);

            // when & then
            mockMvc.perform(post("/api/follows")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(followFacade).should(never()).follow(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 팔로우 시 404 Not Found 응답")
        void withNonExistingFollowee_returns404NotFound() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();
            FollowRequest request = new FollowRequest(followeeId);

            given(followFacade.follow(userId, followeeId))
                .willThrow(UserNotFoundException.withId(followeeId));

            // when & then
            mockMvc.perform(post("/api/follows")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("DELETE /api/follows/{followId} - 언팔로우")
    class UnFollowTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID followId = UUID.randomUUID();

            willDoNothing().given(followFacade).unFollow(userId, followId);

            // when & then
            mockMvc.perform(delete("/api/follows/{followId}", followId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(followFacade).should().unFollow(userId, followId);
        }

        @Test
        @DisplayName("존재하지 않는 팔로우 ID로 요청 시 404 Not Found 응답")
        void withNonExistingFollowId_returns404NotFound() throws Exception {
            // given
            UUID followId = UUID.randomUUID();

            willThrow(FollowNotFoundException.withId(followId))
                .given(followFacade).unFollow(userId, followId);

            // when & then
            mockMvc.perform(delete("/api/follows/{followId}", followId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("다른 사용자의 팔로우 삭제 시도 시 403 Forbidden 응답")
        void withOtherUsersFollow_returns403Forbidden() throws Exception {
            // given
            UUID followId = UUID.randomUUID();

            willThrow(FollowNotAllowedException.withRequesterIdAndFollowId(userId, followId))
                .given(followFacade).unFollow(userId, followId);

            // when & then
            mockMvc.perform(delete("/api/follows/{followId}", followId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("GET /api/follows/count - 팔로워 수 조회")
    class GetFollowCountTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답과 팔로워 수 반환")
        void withValidRequest_returns200OKWithCount() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();
            long followerCount = 42L;

            given(followFacade.getFollowerCount(followeeId)).willReturn(followerCount);

            // when & then
            mockMvc.perform(get("/api/follows/count")
                .with(user(mockUserDetails))
                .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(followerCount));

            then(followFacade).should().getFollowerCount(followeeId);
        }

        @Test
        @DisplayName("followeeId 파라미터 누락 시 400 Bad Request 응답")
        void withoutFolloweeId_returns400BadRequest() throws Exception {
            // when & then
            mockMvc.perform(get("/api/follows/count")
                .with(user(mockUserDetails)))
                .andExpect(status().isBadRequest());

            then(followFacade).should(never()).getFollowerCount(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 팔로워 수 조회 시 404 Not Found 응답")
        void withNonExistingFollowee_returns404NotFound() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();

            given(followFacade.getFollowerCount(followeeId))
                .willThrow(UserNotFoundException.withId(followeeId));

            // when & then
            mockMvc.perform(get("/api/follows/count")
                .with(user(mockUserDetails))
                .param("followeeId", followeeId.toString()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/follows/followed-by-me - 팔로우 상태 조회")
    class GetFollowStatusTest {

        @Test
        @DisplayName("팔로우 중인 경우 200 OK 응답과 followed=true, followId 반환")
        void whenFollowing_returns200OKWithFollowedTrueAndFollowId() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();
            UUID followId = UUID.randomUUID();
            FollowStatusResponse response = new FollowStatusResponse(true, followId);

            given(followFacade.getFollowStatus(userId, followeeId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/follows/followed-by-me")
                .with(user(mockUserDetails))
                .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followed").value(true))
                .andExpect(jsonPath("$.followId").value(followId.toString()));

            then(followFacade).should().getFollowStatus(userId, followeeId);
        }

        @Test
        @DisplayName("팔로우하지 않은 경우 200 OK 응답과 followed=false, followId=null 반환")
        void whenNotFollowing_returns200OKWithFollowedFalseAndNullFollowId() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();
            FollowStatusResponse response = new FollowStatusResponse(false, null);

            given(followFacade.getFollowStatus(userId, followeeId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/follows/followed-by-me")
                .with(user(mockUserDetails))
                .param("followeeId", followeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followed").value(false))
                .andExpect(jsonPath("$.followId").isEmpty());

            then(followFacade).should().getFollowStatus(userId, followeeId);
        }

        @Test
        @DisplayName("followeeId 파라미터 누락 시 400 Bad Request 응답")
        void withoutFolloweeId_returns400BadRequest() throws Exception {
            // when & then
            mockMvc.perform(get("/api/follows/followed-by-me")
                .with(user(mockUserDetails)))
                .andExpect(status().isBadRequest());

            then(followFacade).should(never()).getFollowStatus(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 팔로우 상태 조회 시 404 Not Found 응답")
        void withNonExistingFollowee_returns404NotFound() throws Exception {
            // given
            UUID followeeId = UUID.randomUUID();

            given(followFacade.getFollowStatus(userId, followeeId))
                .willThrow(UserNotFoundException.withId(followeeId));

            // when & then
            mockMvc.perform(get("/api/follows/followed-by-me")
                .with(user(mockUserDetails))
                .param("followeeId", followeeId.toString()))
                .andExpect(status().isNotFound());
        }

    }
}
