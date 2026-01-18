package com.mopl.websocket.interfaces.api.content;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.ContentWebSocketFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContentWebSocketController {

    private final ContentWebSocketFacade contentWebSocketFacade;

    @MessageMapping("/contents/{contentId}/chat")
    @SendTo("/sub/contents/{contentId}/chat")
    public ContentChatDto chat(
        @DestinationVariable UUID contentId,
        ContentChatSendRequest request,
        Principal principal
    ) {
        UUID userId = extractUserId(principal);

        return contentWebSocketFacade.sendChatMessage(contentId, userId, request.content());
    }

    /**
     * Principal에서 MoplUserDetails를 추출하여 userId를 반환
     */
    private UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof MoplUserDetails userDetails) {
                return userDetails.userId();
            }
        }
        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }
}
