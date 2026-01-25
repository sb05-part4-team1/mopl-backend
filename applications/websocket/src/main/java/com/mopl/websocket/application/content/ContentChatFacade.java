package com.mopl.websocket.application.content;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.user.UserSummaryMapper;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentChatFacade {

    private final UserService userService;
    private final UserSummaryMapper userSummaryMapper;

    public ContentChatResponse sendChatMessage(UUID userId, String message) {
        UserModel sender = userService.getById(userId);
        return new ContentChatResponse(userSummaryMapper.toSummary(sender), message);
    }
}
