package com.mopl.websocket.config;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.ContentWebSocketFacade;
import com.mopl.websocket.interfaces.api.content.ChangeType;
import com.mopl.websocket.interfaces.api.content.WatchingSessionChange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {
	private final ContentWebSocketFacade contentWebSocketFacade;
	private final SimpMessagingTemplate messagingTemplate;

	// 1. 구독 시 (입장)
	@EventListener
	public void handleSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = accessor.getDestination();

		if (destination != null && destination.startsWith("/sub/contents/") && destination.endsWith("/watch")) {
			UUID contentId = UUID.fromString(destination.split("/")[3]);
			MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser()).getPrincipal();

			WatchingSessionChange change = contentWebSocketFacade.updateSession(contentId, user.userId(), ChangeType.JOIN);
			messagingTemplate.convertAndSend(destination, change);
			log.info("입장 완료 - 유저: {}, 인원: {}", user.userId(), change.watcherCount());
		}
	}

	// 2. 구독 해제 시 (페이지 이탈 등)
	@EventListener
	public void handleUnsubscribe(SessionUnsubscribeEvent event) {
		processLeave(StompHeaderAccessor.wrap(event.getMessage()));
	}

	// 3. 연결 끊김 시 (브라우저 종료 등)
	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		processLeave(StompHeaderAccessor.wrap(event.getMessage()));
	}

	// 공통 퇴장 로직
	private void processLeave(StompHeaderAccessor accessor) {
		UUID contentId = (UUID) accessor.getSessionAttributes().get("watchingContentId");

		if (contentId != null && accessor.getUser() != null) {
			MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser()).getPrincipal();

			// Redis에서 삭제 및 갱신된 인원수 조회
			WatchingSessionChange change = contentWebSocketFacade.updateSession(contentId, user.userId(), ChangeType.LEAVE);

			// 전송
			messagingTemplate.convertAndSend("/sub/contents/" + contentId + "/watch", change);

			// [중요] 중복 처리 방지를 위해 세션 속성 제거
			accessor.getSessionAttributes().remove("watchingContentId");
			log.info("퇴장 완료 - 유저: {}, 현재 인원: {}", user.userId(), change.watcherCount());
		}
	}
}
