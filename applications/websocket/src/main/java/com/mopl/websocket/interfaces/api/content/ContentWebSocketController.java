package com.mopl.websocket.interfaces.api.content;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.ContentWebSocketFacade;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentWebSocketController {

    private final ContentWebSocketFacade contentWebSocketFacade;

    @MessageMapping("/contents/{contentId}/chat")
    @SendTo("/sub/contents/{contentId}/chat")
    public ContentChatDto chat(
        @DestinationVariable UUID contentId,
        ContentChatSendRequest request,
        @AuthenticationPrincipal MoplUserDetails user
    ) {
        return contentWebSocketFacade.sendChatMessage(contentId, user.userId(), request.content());
    }
}
