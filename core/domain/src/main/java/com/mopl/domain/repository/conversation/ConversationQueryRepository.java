package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserSortField;
import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import java.util.Optional;
import java.util.UUID;

public interface ConversationQueryRepository {

    CursorResponse<ConversationModel> getAllConversation(ConversationQueryRequest request);

}



