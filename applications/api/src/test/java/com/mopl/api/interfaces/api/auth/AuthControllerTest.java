package com.mopl.api.interfaces.api.auth;

import com.mopl.api.interfaces.api.ApiControllerAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
