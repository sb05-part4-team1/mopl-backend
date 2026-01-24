package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final DirectMessageResponseMapper directMessageResponseMapper;

    public ConversationResponse toResponse(
        ConversationModel conversationModel
    ) {
        return new ConversationResponse(
            conversationModel.getId(),
            userSummaryMapper.toSummary(conversationModel.getWithUser()),
            directMessageResponseMapper.toResponse(conversationModel.getLastMessage()),
            conversationModel.isHasUnread()
        );
    }
}
