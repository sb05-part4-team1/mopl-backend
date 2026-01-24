package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.util.UUID;

public interface ConversationQueryRepository {

    CursorResponse<ConversationModel> findAllConversation(UUID userId, ConversationQueryRequest request);
}
