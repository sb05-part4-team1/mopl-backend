package com.mopl.websocket.interfaces.api.content;

import com.mopl.api.interfaces.api.user.UserSummary;

public record ContentChatDto(
    UserSummary sender,
    String content
) {
}
