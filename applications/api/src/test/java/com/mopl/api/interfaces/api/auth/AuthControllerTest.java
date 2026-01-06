package com.mopl.api.interfaces.api.auth;

import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.jwt.dto.JwtResponse;
import com.mopl.security.jwt.service.TokenRefreshService;
import com.mopl.security.jwt.service.TokenRefreshService.TokenRefreshResult;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenRefreshService tokenRefreshService;

    @Nested
    @DisplayName("GET /api/auth/csrf-token")
    class GetCsrfToken {

        @Test
        @DisplayName("CSRF 토큰 요청 시 204 No Content 반환")
        void getCsrfToken_returns204() throws Exception {
            mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 갱신 시 200 OK 반환")
        void withValidRefreshToken_returns200OK() throws Exception {
            // given
            String oldRefreshToken = "old-refresh-token";
            String newRefreshToken = "new-refresh-token";
            String newAccessToken = "new-access-token";

            UserModel user = UserModelFixture.create();
            MoplUserDetails userDetails = MoplUserDetails.from(user);
            JwtResponse jwtResponse = JwtResponse.from(userDetails, newAccessToken);
            Cookie newCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, newRefreshToken);

            given(tokenRefreshService.refresh(oldRefreshToken))
                .willReturn(new TokenRefreshResult(jwtResponse, newCookie));

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, newRefreshToken))
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.userDto.id").value(user.getId().toString()));
        }

        @Test
        @DisplayName("리프레시 토큰 쿠키가 없으면 400 Bad Request 반환")
        void withoutRefreshTokenCookie_returns400BadRequest() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 401 Unauthorized 반환")
        void withInvalidRefreshToken_returns401Unauthorized() throws Exception {
            // given
            String invalidToken = "invalid-refresh-token";

            given(tokenRefreshService.refresh(invalidToken))
                .willThrow(new InvalidTokenException());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, invalidToken)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("차단된 사용자의 토큰 갱신 시 403 Forbidden 반환")
        void withLockedUser_returns403Forbidden() throws Exception {
            // given
            String refreshToken = "valid-refresh-token";

            given(tokenRefreshService.refresh(refreshToken))
                .willThrow(new AccountLockedException());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
                .andExpect(status().isForbidden());
        }
    }
}
