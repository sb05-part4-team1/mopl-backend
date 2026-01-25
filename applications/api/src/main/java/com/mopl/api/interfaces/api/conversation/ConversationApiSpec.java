package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.common.CommonApiResponse;
import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationSortField;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Conversation API")
public interface ConversationApiSpec {

    @Operation(
        summary = "대화 목록 조회 (커서 페이지네이션)",
        description = "본인의 대화 목록만 조회할 수 있습니다."
    )
    @Parameters({
        @Parameter(
            name = "keywordLike",
            description = "이름 검색어",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "cursor",
            description = "커서 (다음 페이지 시작점)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서 (현재 페이지 마지막 요소 ID)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class, defaultValue = "20")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = SortDirection.class, defaultValue = "DESCENDING")
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ConversationSortField.class, defaultValue = "createdAt")
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @CommonApiResponse.Default
    CursorResponse<ConversationResponse> getConversations(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(hidden = true) ConversationQueryRequest request
    );

    @Operation(summary = "대화 상세 조회")
    @Parameter(name = "conversationId", description = "대화 ID", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ConversationResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    @CommonApiResponse.NotFound
    ConversationResponse getConversation(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID conversationId
    );

    @Operation(summary = "특정 사용자와의 대화 조회")
    @Parameter(name = "userId", description = "상대 사용자 ID", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ConversationResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.NotFound
    ConversationResponse getConversationWith(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID userId
    );

    @Operation(
        summary = "대화 생성",
        description = "새로운 1:1 대화를 생성합니다. 자기 자신과의 대화는 생성할 수 없으며, 이미 존재하는 대화가 있으면 409 에러를 반환합니다."
    )
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = ConversationResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.NotFound
    @CommonApiResponse.Conflict
    ConversationResponse createConversation(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        ConversationCreateRequest request
    );

    @Operation(
        summary = "DM 목록 조회 (커서 페이지네이션)",
        description = "특정 대화의 DM 목록을 조회합니다. 해당 대화의 참여자만 조회할 수 있습니다."
    )
    @Parameter(name = "conversationId", description = "대화 ID", required = true)
    @Parameters({
        @Parameter(
            name = "cursor",
            description = "커서 (다음 페이지 시작점)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서 (현재 페이지 마지막 요소 ID)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class, defaultValue = "20")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = SortDirection.class, defaultValue = "DESCENDING")
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = DirectMessageSortField.class, defaultValue = "createdAt")
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    @CommonApiResponse.NotFound
    CursorResponse<DirectMessageResponse> getDirectMessages(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID conversationId,
        @Parameter(hidden = true) DirectMessageQueryRequest request
    );

    @Operation(summary = "대화 읽음 처리")
    @Parameter(name = "conversationId", description = "대화 ID", required = true)
    @Parameter(name = "directMessageId", description = "메시지 ID", required = true)
    @ApiResponse(responseCode = "204", description = "성공")
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    @CommonApiResponse.NotFound
    void markAsRead(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID conversationId,
        UUID directMessageId
    );
}
