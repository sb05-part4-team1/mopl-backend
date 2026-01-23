package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
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
        if (directMessageModel == null) {
            return null;
        }
        return new DirectMessageResponse(
            directMessageModel.getId(),
            directMessageModel.getConversation().getId(),
            directMessageModel.getCreatedAt(),
            userSummaryMapper.toSummary(directMessageModel.getSender()),
            userSummaryMapper.toSummary(directMessageModel.getReceiver()),
            directMessageModel.getContent()
        );
    }
}
