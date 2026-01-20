package com.mopl.websocket.interfaces.api.conversation;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.api.interfaces.api.conversation.DirectMessageResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.conversation.DirectMessageFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageFacade directMessageFacade;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    @SendTo("/sub/conversations/{conversationId}/direct-messages")
    public DirectMessageResponse directMessage(
        @DestinationVariable UUID conversationId,
        DirectMessageSendRequest request,
        Principal principal
    ) {
        UUID userId = extractUserId(principal);

        return directMessageFacade.sendDirectMessage(conversationId, userId, request.content());
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
