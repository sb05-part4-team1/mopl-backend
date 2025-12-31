package com.mopl.domain.model.conversation;

import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.model.base.BaseModel;
import java.util.UUID;

public class DirectMessageModel extends BaseModel {

    private UUID conversationId;
    private UUID senderId;
    private UUID receiverId;
    private String content;


}
