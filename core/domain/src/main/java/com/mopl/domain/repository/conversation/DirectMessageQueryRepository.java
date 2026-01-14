package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.support.cursor.CursorResponse;
import java.util.UUID;

public interface DirectMessageQueryRepository {

    CursorResponse<DirectMessageModel> findAllByConversationId(
            UUID conversationId,DirectMessageQueryRequest requst,UUID userId);

}
