package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class DirectMessageModel extends BaseModel {

    private String content;
    private ConversationModel conversation;
    private UserModel sender;

    public static DirectMessageModel create(
        String content,
        ConversationModel conversation,
        UserModel sender
    ) {
        return DirectMessageModel.builder()
            .content(content)
            .conversation(conversation)
            .sender(sender)
            .build();
    }
}
