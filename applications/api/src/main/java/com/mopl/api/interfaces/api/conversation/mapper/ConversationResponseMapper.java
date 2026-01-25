package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    private final UserSummaryMapper userSummaryMapper;
    private final DirectMessageResponseMapper directMessageResponseMapper;

    public ConversationResponse toResponse(
        ConversationModel conversationModel,
        UserModel withUser,
        DirectMessageModel lastMessage,
        boolean hasUnread
    ) {
        return new ConversationResponse(
            conversationModel.getId(),
            userSummaryMapper.toSummary(withUser),
            directMessageResponseMapper.toResponse(lastMessage, withUser),
            hasUnread
        );
    }
}
