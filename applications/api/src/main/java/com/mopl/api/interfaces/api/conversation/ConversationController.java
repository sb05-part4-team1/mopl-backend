package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.domain.model.conversation.ConversationModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

//    private final UserFacade userFacade;
//    private final UserResponseMapper userResponseMapper;
    private final ConversationFacade conversationFacade;
    private final ConversationResponseMapper conversationResponseMapper;

    @PostMapping
    public ConversationResponse createConversation(
        //security에서 유저 정보 가지고 오기
        @Valid @RequestBody ConversationCreateRequest request
    ) {
//        private UUID id;
//        private UUID withId;
//        private UUID messageId;
//        private boolean hasUnread;
        ConversationModel conversationmodel = conversationFacade.createConversation(request);
        //senderId,messageId 있음.

//        return userResponseMapper.toResponse(userModel);
        return conversationResponseMapper.toResponse(conversationmodel);
    }

}
