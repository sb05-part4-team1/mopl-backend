package com.mopl.websocket.interfaces.api.content;

import com.mopl.websocket.application.content.ContentWebSocketFacade;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatRequest;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ContentWebSocketController {

    private final ContentWebSocketFacade contentWebSocketFacade;

    @MessageMapping("/contents/{contentId}/chat")
    @SendTo("/sub/contents/{contentId}/chat")
    public ContentChatResponse sendChat(
        Principal principal,
        ContentChatRequest request
    ) {
        UUID senderId = UUID.fromString(principal.getName());
        return contentWebSocketFacade.sendChatMessage(senderId, request.content());
    }
}
