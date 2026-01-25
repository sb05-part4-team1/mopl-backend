package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface DirectMessageQueryRepository {

    CursorResponse<DirectMessageModel> findAll(
        UUID userId,
        UUID conversationId,
        DirectMessageQueryRequest request
    );

    Map<UUID, DirectMessageModel> findLastDirectMessagesWithSenderByConversationIdIn(Collection<UUID> conversationIds);
}
