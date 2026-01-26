package com.mopl.websocket.interfaces.api.conversation;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.websocket.application.conversation.DirectMessageFacade;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    private static final String DM_DESTINATION_PREFIX = "/sub/conversations/";
    private static final String DM_DESTINATION_SUFFIX = "/direct-messages";

    private final DirectMessageFacade directMessageFacade;
    private final WebSocketBroadcaster webSocketBroadcaster;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    public void sendDirectMessage(
        Principal principal,
        @DestinationVariable UUID conversationId,
        DirectMessageSendRequest request
    ) {
        UUID senderId = UUID.fromString(principal.getName());
        DirectMessageResponse response = directMessageFacade.sendDirectMessage(senderId, conversationId, request);
        webSocketBroadcaster.broadcast(DM_DESTINATION_PREFIX + conversationId + DM_DESTINATION_SUFFIX, response);
    }
}
