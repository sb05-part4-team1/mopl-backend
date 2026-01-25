package com.mopl.websocket.interfaces.api.content;

import com.mopl.websocket.application.content.ContentChatFacade;
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
public class ContentChatController {

    private final ContentChatFacade contentChatFacade;

    @MessageMapping("/contents/{contentId}/chat")
    @SendTo("/sub/contents/{contentId}/chat")
    public ContentChatResponse sendChat(
        Principal principal,
        ContentChatRequest request
    ) {
        UUID senderId = UUID.fromString(principal.getName());
        return contentChatFacade.sendChatMessage(senderId, request.content());
    }
}
