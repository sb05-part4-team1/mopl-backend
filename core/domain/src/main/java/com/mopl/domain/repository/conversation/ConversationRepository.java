package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;

public interface ConversationRepository {

    ConversationModel save(ConversationModel conversationModel);
}
