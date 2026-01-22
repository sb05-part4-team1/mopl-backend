package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString(callSuper = true)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationModel extends BaseUpdatableModel {

    private UserModel withUser;
    private DirectMessageModel lastMessage;
    private boolean hasUnread;

    public static ConversationModel create(
        UserModel with
    ) {

        return ConversationModel.builder()
            .withUser(with)
            .lastMessage(null)
            .hasUnread(false)
            .build();
    }

    public static ConversationModel create() {

        return ConversationModel.builder()
            .withUser(null)
            .lastMessage(null)
            .hasUnread(false)
            .build();
    }

    public ConversationModel withUser(UserModel withUser) {

        this.withUser = withUser;

        return this;
    }

    public ConversationModel lastMessage(DirectMessageModel lastMessage) {
        if (lastMessage == null) {
            return this;
        }
        this.lastMessage = lastMessage;

        return this;
    }

    public ConversationModel hasUnread(boolean hasUnread) {

        this.hasUnread = hasUnread;

        return this;
    }

}
