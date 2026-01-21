package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final DirectMessageMapper directMessageMapper;

    public ConversationResponse toResponse(
        ConversationModel conversationModel
    ) {

        return new ConversationResponse(
            conversationModel.getId(),
            userSummaryMapper.toSummary(conversationModel.getWithUser()),
            directMessageMapper.toResponse(conversationModel.getLastMessage()),
            conversationModel.isHasUnread()
        );
    }
}
