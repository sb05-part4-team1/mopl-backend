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

        ConversationModel conversationmodel = conversationFacade.createConversation(request);
        //senderId,messageId를 usersummary,messagedto로 변환해야 함.

        return conversationResponseMapper.toResponse(conversationmodel);
    }

}
