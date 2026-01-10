package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.conversation.DirectMessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

    private final UserSummaryMapper userSummaryMapper;

    public DirectMessageResponse toResponse(
        DirectMessageModel directMessageModel
    ) {
        return new DirectMessageResponse(
            directMessageModel.getId(),
            directMessageModel.getConversation().getId(),
            directMessageModel.getCreatedAt(),
            userSummaryMapper.toSummary(directMessageModel.getSender()),
            directMessageModel.getContent()
        );
    }
}
