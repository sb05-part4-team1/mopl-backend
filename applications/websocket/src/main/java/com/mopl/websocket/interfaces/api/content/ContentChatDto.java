package com.mopl.websocket.interfaces.api.content;

import com.mopl.dto.user.UserSummary;

public record ContentChatDto(
    UserSummary sender,
    String content
) {
}
