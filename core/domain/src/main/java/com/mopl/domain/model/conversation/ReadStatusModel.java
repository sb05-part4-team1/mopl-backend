package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class ReadStatusModel extends BaseModel {

    private Instant lastReadAt;
    private ConversationModel conversation;
    private UserModel user;

    public static ReadStatusModel create(
        ConversationModel conversation,
        UserModel user
    ) {
        return ReadStatusModel.builder()
            .lastReadAt(Instant.now())
            .conversation(conversation)
            .user(user)
            .build();
    }

    public ReadStatusModel markAsRead() {
        return this.toBuilder()
            .lastReadAt(Instant.now())
            .build();
    }
}
