package com.mopl.api.interfaces.api.conversation;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConversationCreateRequest(
        @NotNull(message = "상대 유저의 id는 필수입니다.") UUID withUserId
) {

}
