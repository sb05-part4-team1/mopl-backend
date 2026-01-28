package com.mopl.sse.interfaces.api;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.sse.application.SseFacade;
import com.mopl.sse.config.TestSecurityConfig;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SseController.class)
@Import(TestSecurityConfig.class)
@DisplayName("SseController 슬라이스 테스트")
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SseFacade sseFacade;

    private MoplUserDetails mockUserDetails;
    private UUID mockUserId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        mockUserId = UUID.randomUUID();

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(mockUserId);
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("GET /sse/api/sse")
    class SubscribeTest {

        @Test
        @DisplayName("lastEventId 없이 구독 요청 시 200 OK 반환")
        void withoutLastEventId_returns200() throws Exception {
            // given
            SseEmitter expectedEmitter = new SseEmitter();
            given(sseFacade.subscribe(mockUserId, null)).willReturn(expectedEmitter);

            // when & then
            mockMvc.perform(get("/sse/api/sse")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("lastEventId와 함께 구독 요청 시 200 OK 반환")
        void withLastEventId_returns200() throws Exception {
            // given
            UUID lastEventId = UUID.randomUUID();
            SseEmitter expectedEmitter = new SseEmitter();
            given(sseFacade.subscribe(mockUserId, lastEventId)).willReturn(expectedEmitter);

            // when & then
            mockMvc.perform(get("/sse/api/sse")
                .param("lastEventId", lastEventId.toString())
                .with(user(mockUserDetails)))
                .andExpect(status().isOk());
        }
    }
}
