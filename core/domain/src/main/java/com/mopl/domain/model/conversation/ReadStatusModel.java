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
    private UserModel participant;

    public static ReadStatusModel create(
        ConversationModel conversation,
        UserModel participant
    ) {
        return ReadStatusModel.builder()
            .lastReadAt(Instant.now())
            .conversation(conversation)
            .participant(participant)
            .build();
    }

    public ReadStatusModel updateLastReadAt(Instant readAt) {
        if (lastReadAt != null && !readAt.isAfter(lastReadAt)) {
            return this;
        }

        return this.toBuilder()
            .lastReadAt(readAt)
            .build();
    }
}
