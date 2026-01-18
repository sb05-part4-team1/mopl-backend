package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController implements ConversationApiSpec {

    private final UserFacade userFacade;
    private final UserSummaryMapper userSummaryMapper;
    private final ConversationFacade conversationFacade;
    private final ConversationResponseMapper conversationResponseMapper;

    @GetMapping("{conversationId}/direct-messages")
    public CursorResponse<DirectMessageResponse> getDirectMessages(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable("conversationId") UUID conversationId,
        @ModelAttribute DirectMessageQueryRequest request
    ) {
        return conversationFacade.getAllDirectMessage(conversationId, request, userDetails
            .userId());
    }

    @GetMapping
    public CursorResponse<ConversationResponse> getConversations(
        @AuthenticationPrincipal MoplUserDetails userDetails, //userId, role이 들어있음.
        @ModelAttribute ConversationQueryRequest request
    ) {

        return conversationFacade.getAllConversation(request, userDetails.userId());
    }

    @GetMapping("/with")
    public ConversationResponse findByWith(
        @AuthenticationPrincipal MoplUserDetails userDetails, //userId, role이 들어있음.
        @RequestParam UUID userId
    ) {
        ConversationModel conversationModel = conversationFacade.getConversationByWith(userDetails
            .userId(), userId);

        return conversationResponseMapper.toResponse(conversationModel);

    }

    @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
    public void directMessageRead(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable("conversationId") UUID conversationId,
        @PathVariable("directMessageId") UUID directMessageId
    ) {

        conversationFacade.directMessageRead(conversationId, directMessageId, userDetails.userId());

    }

    @GetMapping("/{conversationId}")
    public ConversationResponse findConversationById(
        @AuthenticationPrincipal MoplUserDetails userDetails, //userId, role이 들어있음.
        @PathVariable("conversationId") UUID conversationId
    ) {
        ConversationModel conversationModel = conversationFacade.getConversation(conversationId,
            userDetails.userId());

        return conversationResponseMapper.toResponse(conversationModel);
    }

    @PostMapping
    public ConversationResponse createConversation(
        @AuthenticationPrincipal MoplUserDetails userDetails, //userId, role이 들어있음.
        @Valid @RequestBody ConversationCreateRequest request
    ) {

        ConversationModel conversationModel = conversationFacade.createConversation(
            request, userDetails.userId());

        return conversationResponseMapper.toResponse(conversationModel);
    }

}
