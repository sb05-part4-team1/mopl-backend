package com.mopl.api.interfaces.api.conversation;


import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.UserResponseMapper;
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
    private final ConversationFacade  conversationFacade;


    @PostMapping
    public ConversationResponse createConversation(
        @Valid @RequestBody ConversationCreateRequest request
    ){

//        return conversationFacade.createConversation(request);

        return null;
    }


}
