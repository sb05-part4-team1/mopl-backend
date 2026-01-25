package com.mopl.dto.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.dto.user.UserSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageResponseMapper {

    private final UserSummaryMapper userSummaryMapper;

    public DirectMessageResponse toResponse(DirectMessageModel directMessageModel, UserModel receiver) {
        if (directMessageModel == null) {
            return null;
        }

        return new DirectMessageResponse(
            directMessageModel.getId(),
            directMessageModel.getConversation().getId(),
            directMessageModel.getCreatedAt(),
            userSummaryMapper.toSummary(directMessageModel.getSender()),
            userSummaryMapper.toSummary(receiver),
            directMessageModel.getContent()
        );
    }
}
