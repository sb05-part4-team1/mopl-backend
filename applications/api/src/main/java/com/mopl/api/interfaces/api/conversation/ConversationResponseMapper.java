package com.mopl.api.interfaces.api.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    public ConversationResponse toResponse(ConversationModel conversationModel) {

        return new ConversationResponse(
            conversationModel.getId(),
            null, //상대 사용자의 usersummary가 들어갈 예정
            null, // 대화의 마지막 message가 들어갈 예정
            conversationModel.isHasUnread()
        );
    }
}
