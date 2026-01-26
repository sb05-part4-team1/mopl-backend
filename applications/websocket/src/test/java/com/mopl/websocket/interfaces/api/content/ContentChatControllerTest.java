package com.mopl.websocket.interfaces.api.content;

import com.mopl.dto.user.UserSummary;
import com.mopl.websocket.application.content.ContentChatFacade;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatRequest;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentChatController 슬라이스 테스트")
class ContentChatControllerTest {

    @Mock
    private ContentChatFacade contentChatFacade;

    @Mock
    private Principal principal;

    private ContentChatController contentChatController;

    private UUID senderId;
    private UUID contentId;

    @BeforeEach
    void setUp() {
        contentChatController = new ContentChatController(contentChatFacade);

        senderId = UUID.randomUUID();
        contentId = UUID.randomUUID();

        given(principal.getName()).willReturn(senderId.toString());
    }

    @Nested
    @DisplayName("sendChat() - 콘텐츠 채팅 메시지 전송")
    class SendChatTest {

        @Test
        @DisplayName("유효한 요청 시 ContentChatResponse 반환")
        void withValidRequest_returnsContentChatResponse() {
            // given
            String content = "안녕하세요";
            ContentChatRequest request = new ContentChatRequest(content);

            UserSummary senderSummary = new UserSummary(senderId, "테스트 사용자", null);
            ContentChatResponse expectedResponse = new ContentChatResponse(senderSummary, content);

            given(contentChatFacade.sendChatMessage(senderId, contentId, content))
                .willReturn(expectedResponse);

            // when
            ContentChatResponse result = contentChatController.sendChat(
                principal,
                contentId,
                request
            );

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.sender()).isEqualTo(senderSummary);
            assertThat(result.content()).isEqualTo(content);

            then(contentChatFacade).should().sendChatMessage(senderId, contentId, content);
        }

        @Test
        @DisplayName("빈 내용의 메시지 전송 시에도 facade 호출")
        void withEmptyContent_callsFacade() {
            // given
            String content = "";
            ContentChatRequest request = new ContentChatRequest(content);

            UserSummary senderSummary = new UserSummary(senderId, "테스트 사용자", null);
            ContentChatResponse expectedResponse = new ContentChatResponse(senderSummary, content);

            given(contentChatFacade.sendChatMessage(senderId, contentId, content))
                .willReturn(expectedResponse);

            // when
            ContentChatResponse result = contentChatController.sendChat(
                principal,
                contentId,
                request
            );

            // then
            assertThat(result.content()).isEmpty();
            then(contentChatFacade).should().sendChatMessage(senderId, contentId, content);
        }

        @Test
        @DisplayName("긴 메시지 내용도 정상 처리")
        void withLongContent_returnsResponse() {
            // given
            String content = "가".repeat(1000);
            ContentChatRequest request = new ContentChatRequest(content);

            UserSummary senderSummary = new UserSummary(senderId, "테스트 사용자", null);
            ContentChatResponse expectedResponse = new ContentChatResponse(senderSummary, content);

            given(contentChatFacade.sendChatMessage(senderId, contentId, content))
                .willReturn(expectedResponse);

            // when
            ContentChatResponse result = contentChatController.sendChat(
                principal,
                contentId,
                request
            );

            // then
            assertThat(result.content()).hasSize(1000);
            then(contentChatFacade).should().sendChatMessage(senderId, contentId, content);
        }

        @Test
        @DisplayName("sender 정보가 null인 경우에도 응답 반환")
        void withNullSender_returnsResponse() {
            // given
            String content = "메시지";
            ContentChatRequest request = new ContentChatRequest(content);

            ContentChatResponse expectedResponse = new ContentChatResponse(null, content);

            given(contentChatFacade.sendChatMessage(senderId, contentId, content))
                .willReturn(expectedResponse);

            // when
            ContentChatResponse result = contentChatController.sendChat(
                principal,
                contentId,
                request
            );

            // then
            assertThat(result.sender()).isNull();
            assertThat(result.content()).isEqualTo(content);
            then(contentChatFacade).should().sendChatMessage(senderId, contentId, content);
        }
    }
}
