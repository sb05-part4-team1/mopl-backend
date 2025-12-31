package com.mopl.domain.model.conversation;


import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationModel extends BaseModel {


    private UUID withId;
    private UUID messageId;
    private boolean hasUnread;


    public static ConversationModel create(
            UUID withId
    ) {
        if (withId == null) {
            throw new InvalidUserDataException("상대방의 Id는 비어있을 수 없습니다.");
        }

        return ConversationModel.builder()
                .withId(withId)
                .hasUnread(false)
                .build();
    }


}
