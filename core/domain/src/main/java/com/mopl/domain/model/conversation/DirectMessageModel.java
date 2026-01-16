package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageModel extends BaseModel {

    private ConversationModel conversation;
    private UserModel sender;
    private UserModel receiver;
    private String content;

    public DirectMessageModel setSender(UserModel sender) {
        this.sender = sender;
        return this;
    }

    public DirectMessageModel setReceiver(UserModel receiver) {
        this.receiver = receiver;
        return this;
    }

}
