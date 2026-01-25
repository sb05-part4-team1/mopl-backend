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
    private UserModel sender;
    private ConversationModel conversation;

    public static DirectMessageModel create(
        String content,
        UserModel sender,
        ConversationModel conversation
    ) {
        return DirectMessageModel.builder()
            .content(content)
            .sender(sender)
            .conversation(conversation)
            .build();
    }
}
