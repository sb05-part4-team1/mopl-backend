package com.mopl.domain.model.conversation;

import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.user.UserModel;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationModel extends BaseUpdatableModel {

//    ConversationDto에 UserSummary, DirectMessageDto가 필요하므로,
//    id 대신 UserModel withUser, DirectMessage lastMessage를 사용하는 것이 좋겠네요.
    private UserModel withUser;
    private DirectMessageModel directMessage;
    private boolean hasUnread;

    public static ConversationModel create(
        UserModel with
    ) {
        if (with == null) {
            throw new InvalidUserDataException("상대방은 비어있을 수 없습니다.");
        }

        return ConversationModel.builder()
            .withUser(with)
            .hasUnread(false)
            .build();
    }


}
