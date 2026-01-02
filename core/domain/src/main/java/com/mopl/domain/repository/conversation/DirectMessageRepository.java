package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;

public interface DirectMessageRepository {

    DirectMessageModel save(DirectMessageModel directMessageModel);

}
