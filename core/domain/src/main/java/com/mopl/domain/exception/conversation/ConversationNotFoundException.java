package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.follow.FollowErrorCode;
import com.mopl.domain.exception.follow.FollowException;
import com.mopl.domain.exception.user.UserNotFoundException;
import java.util.Map;
import java.util.UUID;

public class ConversationNotFoundException extends ConversationException {

  public ConversationNotFoundException(UUID id) {
        super( ConversationErrorCode.CONVERSATION_NOT_FOUND , Map.of("id",id) );
    }
}





