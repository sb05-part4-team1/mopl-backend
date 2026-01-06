package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final UserFacade userFacade;
    private final UserSummaryMapper userSummaryMapper;
    private final ConversationFacade conversationFacade;
    private final ConversationResponseMapper conversationResponseMapper;

    @PostMapping
    public ConversationResponse createConversation(
        @AuthenticationPrincipal MoplUserDetails userDetails, //userId, role이 들어있음.
        @Valid @RequestBody ConversationCreateRequest request
    ) {

        UserModel withUser= userFacade.getUser(request.withUserId());
        UserSummary with = userSummaryMapper.toSummary(withUser);

        ConversationModel conversationmodel = conversationFacade.createConversation(request);

        //message를 가지고 오는게 남음.

        return conversationResponseMapper.toResponse(conversationmodel,with);
    }

}
