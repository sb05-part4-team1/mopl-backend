package com.mopl.domain.model.conversation;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatusModel extends BaseModel {

    private ConversationModel conversation;
    private UserModel user;
    private Instant lastRead;

    public static ReadStatusModel create(
        ConversationModel conversation,
        UserModel user
    ) {
        return ReadStatusModel.builder()
            .conversation(conversation)
            .user(user)
            .lastRead(Instant.now())
            .build();

    }

    public ReadStatusModel updateLastRead(Instant lastRead) {
        if (lastRead != null) {
            this.lastRead = lastRead;
        }
        return this;
    }

}
