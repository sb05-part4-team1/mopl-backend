package com.mopl.websocket.interfaces.api.content;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.domain.model.user.UserModel;
import com.mopl.websocket.application.content.ContentWebSocketFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContentWebSocketController {

	private final ContentWebSocketFacade contentWebSocketFacade;

	@MessageMapping("/contents/{contentId}/watch")
	@SendTo("/sub/contents/{contentId}/watch")
	public WatchingSessionChange watchStatus(
		@DestinationVariable UUID contentId,
		WatchingSessionChange request,
		Principal principal
	) {

		UserModel user = (UserModel)((Authentication)principal).getPrincipal();
		UUID userId = user.getId();

		return contentWebSocketFacade.updateSession(contentId, userId, request.type());
	}

}
