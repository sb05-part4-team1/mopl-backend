package com.mopl.sse.interfaces.api;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.sse.application.SseFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseController 단위 테스트")
class SseControllerTest {

    @Mock
    private SseFacade sseFacade;

    @InjectMocks
    private SseController sseController;

    @Nested
    @DisplayName("subscribe()")
    class SubscribeTest {

        @Test
        @DisplayName("lastEventId 없이 구독 요청")
        void withoutLastEventId_subscribes() {
            // given
            UUID userId = UUID.randomUUID();
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(userId)
                .role(UserModel.Role.USER)
                .createdAt(Instant.now())
                .password("encoded")
                .email("test@example.com")
                .name("홍길동")
                .profileImagePath(null)
                .locked(false)
                .build();

            SseEmitter expectedEmitter = new SseEmitter();
            given(sseFacade.subscribe(userId, null)).willReturn(expectedEmitter);

            // when
            SseEmitter result = sseController.subscribe(userDetails, null);

            // then
            assertThat(result).isEqualTo(expectedEmitter);
            then(sseFacade).should().subscribe(userId, null);
        }

        @Test
        @DisplayName("lastEventId와 함께 구독 요청")
        void withLastEventId_subscribes() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.randomUUID();
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(userId)
                .role(UserModel.Role.USER)
                .createdAt(Instant.now())
                .password("encoded")
                .email("test@example.com")
                .name("홍길동")
                .profileImagePath(null)
                .locked(false)
                .build();

            SseEmitter expectedEmitter = new SseEmitter();
            given(sseFacade.subscribe(userId, lastEventId)).willReturn(expectedEmitter);

            // when
            SseEmitter result = sseController.subscribe(userDetails, lastEventId);

            // then
            assertThat(result).isEqualTo(expectedEmitter);
            then(sseFacade).should().subscribe(userId, lastEventId);
        }
    }
}
