package com.mopl.websocket.interfaces.api.content;

import com.mopl.websocket.application.content.ContentChatFacade;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatRequest;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ContentChatController {

    private static final String CHAT_DESTINATION_PREFIX = "/sub/contents/";
    private static final String CHAT_DESTINATION_SUFFIX = "/chat";

    private final ContentChatFacade contentChatFacade;
    private final WebSocketBroadcaster webSocketBroadcaster;

    @MessageMapping("/contents/{contentId}/chat")
    public void sendChat(
        Principal principal,
        @DestinationVariable UUID contentId,
        ContentChatRequest request
    ) {
        UUID senderId = UUID.fromString(principal.getName());
        ContentChatResponse response = contentChatFacade.sendChatMessage(senderId, contentId, request.content());
        webSocketBroadcaster.broadcast(CHAT_DESTINATION_PREFIX + contentId + CHAT_DESTINATION_SUFFIX, response);
    }
}
