package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController implements ConversationApiSpec {

    private final ConversationFacade conversationFacade;

    @GetMapping
    public CursorResponse<ConversationResponse> getConversations(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @ModelAttribute ConversationQueryRequest request
    ) {
        return conversationFacade.getConversations(userDetails.userId(), request);
    }

    @GetMapping("/{conversationId}")
    public ConversationResponse getConversation(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID conversationId
    ) {
        return conversationFacade.getConversation(
            userDetails.userId(),
            conversationId
        );
    }

    @GetMapping("/with")
    public ConversationResponse getConversationWith(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestParam UUID userId
    ) {
        return conversationFacade.getConversationByWith(
            userDetails.userId(),
            userId
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid ConversationCreateRequest request
    ) {
        return conversationFacade.createConversation(userDetails.userId(), request);
    }

    @GetMapping("/{conversationId}/direct-messages")
    public CursorResponse<DirectMessageResponse> getDirectMessages(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID conversationId,
        @ModelAttribute DirectMessageQueryRequest request
    ) {
        return conversationFacade.getDirectMessages(
            userDetails.userId(),
            conversationId,
            request
        );
    }

    @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID conversationId,
        @PathVariable UUID directMessageId
    ) {
        conversationFacade.markAsRead(userDetails.userId(), conversationId);
    }
}
