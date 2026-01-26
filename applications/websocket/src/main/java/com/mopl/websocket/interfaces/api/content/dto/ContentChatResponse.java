package com.mopl.websocket.interfaces.api.content.dto;

import com.mopl.dto.user.UserSummary;

public record ContentChatResponse(
    UserSummary sender,
    String content
) {
}
