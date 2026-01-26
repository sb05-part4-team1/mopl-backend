package com.mopl.websocket.interfaces.api.conversation;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.websocket.application.conversation.DirectMessageFacade;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageFacade directMessageFacade;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    @SendTo("/sub/conversations/{conversationId}/direct-messages")
    public DirectMessageResponse sendDirectMessage(
        Principal principal,
        @DestinationVariable UUID conversationId,
        DirectMessageSendRequest request
    ) {
        UUID senderId = UUID.fromString(principal.getName());
        return directMessageFacade.sendDirectMessage(senderId, conversationId, request);
    }
}
