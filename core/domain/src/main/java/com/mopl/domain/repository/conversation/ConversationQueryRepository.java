package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.support.cursor.CursorResponse;

public interface ConversationQueryRepository {

    CursorResponse<ConversationModel> findAllConversation(ConversationQueryRequest request);
}
