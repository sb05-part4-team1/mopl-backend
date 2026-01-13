package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public class ConversationQueryRepositoryImpl implements ConversationQueryRepository {

    @Override
    public CursorResponse<ConversationModel> getAllConversation(ConversationQueryRequest request) {
        return null;
    }



}
