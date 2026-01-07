package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.UserSummary;
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
        ConversationModel conversationModel,
        UserSummary with

    ) {

        return new ConversationResponse(
            conversationModel.getId(),
            with,
            null, // 대화의 마지막 message가 들어갈 예정
            conversationModel.isHasUnread()
        );
    }

    public ConversationResponse toResponse(
        ConversationModel conversationModel
    ) {

        return new ConversationResponse(
            conversationModel.getId(),
            userSummaryMapper.toSummary(conversationModel.getWithUser()),
            directMessageMapper.toResponse(conversationModel.getDirectMessage()), // 대화의 마지막 message가 들어갈 예정
            conversationModel.isHasUnread()
        );
    }
}
