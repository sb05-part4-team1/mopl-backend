package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationCursorResponse;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageCursorResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.domain.exception.ErrorResponse;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Conversation API", description = "대화 API")
public interface ConversationApiSpec {

    /* =========================
     * 대화 목록 조회Re
     * ========================= */
    @Operation(
        summary = "대화 목록 조회(커서 페이지네이션)",
        description = "API 요청자 본인의 대화 목록만 조회할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                schema = @Schema(
                    implementation = ConversationCursorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @Parameters({
        @Parameter(
            name = "keywordLike",
            description = "검색 키워드",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class, defaultValue = "100")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = SortDirection.class, defaultValue = "ASCENDING")
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ConversationSortField.class, defaultValue = "createdAt")
        )
    })
    CursorResponse<ConversationResponse> getConversations(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @ParameterObject ConversationQueryRequest request
    );

    /* =========================
     * 대화 메시지 목록 조회
     * ========================= */
    @Operation(
        summary = "DM 목록 조회(커서 페이지네이션)",
        description = "특정 대화의 DM 목록을 조회합니다. API 요청자가 해당 대화의 참여자여야 합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                schema = @Schema(
                    implementation = DirectMessageCursorResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @Parameters({
        @Parameter(
            name = "conversationId",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class, defaultValue = "100")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = SortDirection.class, defaultValue = "ASCENDING")
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = DirectMessageSortField.class, defaultValue = "createdAt")
        )
    })
    CursorResponse<DirectMessageResponse> getDirectMessages(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(
            description = "대화 ID",
            required = true
        ) @PathVariable UUID conversationId,

        @ParameterObject DirectMessageQueryRequest request
    );

    /* =========================
     * 상대방으로 대화 조회
     * ========================= */
    @Operation(
        summary = "특정 사용자와의 대화 조회"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                schema = @Schema(
                    implementation = ConversationResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ), @ApiResponse(
            responseCode = "404",
            description = "해당 리소스 없음",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ConversationResponse findByWith(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(
            description = "상대 사용자 ID",
            required = true,
            schema = @Schema(implementation = UUID.class)
        ) @RequestParam UUID userId
    );

    /* =========================
     * 메시지 읽음 처리
     * ========================= */
    @Operation(
        summary = "메시지 읽음 처리",
        description = "특정 메시지를 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공"
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No Content"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    void readDirectMessage(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(description = "대화 ID", required = true) @PathVariable UUID conversationId,

        @Parameter(description = "메시지 ID", required = true) @PathVariable UUID directMessageId
    );

    /* =========================
     * 대화 단건 조회
     * ========================= */
    @Operation(
        summary = "대화 조회"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                schema = @Schema(implementation = ConversationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "해당 리소스 없음",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ConversationResponse findConversationById(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(required = true) @PathVariable UUID conversationId
    );

    /* =========================
     * 대화 생성
     * ========================= */
    @Operation(
        summary = "대화 생성"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                schema = @Schema(
                    implementation = ConversationResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    ConversationResponse createConversation(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Valid @RequestBody ConversationCreateRequest request
    );

}
