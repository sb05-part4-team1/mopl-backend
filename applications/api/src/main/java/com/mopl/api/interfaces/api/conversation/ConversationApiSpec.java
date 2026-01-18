package com.mopl.api.interfaces.api.conversation;

import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
     * 대화 목록 조회
     * ========================= */
    @Operation(
        summary = "대화 목록 조회",
        description = "API 요청자 본인의 대화 목록을 커서 기반으로 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "성공",
                content = @Content(
                    schema = @Schema(implementation = ConversationResponse.class)
                )
            )
        }
    )
    CursorResponse<ConversationResponse> getConversations(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @ParameterObject ConversationQueryRequest request
    );

    /* =========================
     * 대화 메시지 목록 조회
     * ========================= */
    @Operation(
        summary = "대화 메시지 목록 조회",
        description = "특정 대화방의 메시지를 커서 기반으로 조회합니다."
    )
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
        summary = "상대방 기준 대화 조회",
        description = "상대 사용자 ID로 1:1 대화를 조회합니다."
    )
    ConversationResponse findByWith(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(
            description = "상대 사용자 ID",
            required = true,
            schema = @Schema(format = "uuid")
        ) @RequestParam UUID userId
    );

    /* =========================
     * 메시지 읽음 처리
     * ========================= */
    @Operation(
        summary = "메시지 읽음 처리",
        description = "특정 메시지를 읽음 처리합니다."
    )
    void directMessageRead(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(description = "대화 ID", required = true) @PathVariable UUID conversationId,

        @Parameter(description = "메시지 ID", required = true) @PathVariable UUID directMessageId
    );

    /* =========================
     * 대화 단건 조회
     * ========================= */
    @Operation(
        summary = "대화 단건 조회",
        description = "대화 ID로 단건 조회합니다."
    )
    ConversationResponse findConversationById(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(description = "대화 ID", required = true) @PathVariable UUID conversationId
    );

    /* =========================
     * 대화 생성
     * ========================= */
    @Operation(
        summary = "대화 생성",
        description = "새로운 대화를 생성합니다."
    )
    ConversationResponse createConversation(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Valid @RequestBody ConversationCreateRequest request
    );

}

//@Operation(
//        summary = "대화 목록 조회",
//        description = "API 요청자 본인의 대화 목록만 조회할 수 있습니다.",
//        responses = {
//                @ApiResponse(
//                        responseCode = "200",
//                        description = "성공",
//                        content = @Content(
//                                mediaType = "*/*",
//                                schema = @Schema(implementation = ConversationResponse.class)
//                        )
//                ),
//                @ApiResponse(
//                        responseCode = "400",
//                        description = "잘못된 요청",
//                        content = @Content(
//                                mediaType = "*/*",
//                                schema = @Schema(implementation = ErrorResponse.class)
//                        )
//                ),
//                @ApiResponse(
//                        responseCode = "401",
//                        description = "인증 오류",
//                        content = @Content(
//                                mediaType = "*/*",
//                                schema = @Schema(implementation = ErrorResponse.class)
//                        )
//                ),
//                @ApiResponse(
//                        responseCode = "500",
//                        description = "서버 오류",
//                        content = @Content(
//                                mediaType = "*/*",
//                                schema = @Schema(implementation = ErrorResponse.class)
//                        )
//                )
//        }
//)
//@GetMapping("/api/conversations")
//ResponseEntity<?> getConversations(
//
//        @Parameter(
//                name = "keywordLike",
//                description = "검색 키워드",
//                in = ParameterIn.QUERY
//        )
//        @RequestParam(required = false)
//        String keywordLike,
//
//        @Parameter(
//                name = "cursor",
//                description = "커서",
//                in = ParameterIn.QUERY
//        )
//        @RequestParam(required = false)
//        String cursor,
//
//        @Parameter(
//                name = "idAfter",
//                description = "보조 커서",
//                in = ParameterIn.QUERY,
//                schema = @Schema(format = "uuid")
//        )
//        @RequestParam(required = false)
//        UUID idAfter,
//
//        @Parameter(
//                name = "limit",
//                description = "한 번에 가져올 개수",
//                required = true,
//                in = ParameterIn.QUERY,
//                schema = @Schema(type = "integer", format = "int32")
//        )
//        @RequestParam
//        Integer limit,
//
//        @Parameter(
//                name = "sortDirection",
//                description = "정렬 방향",
//                required = true,
//                in = ParameterIn.QUERY,
//                schema = @Schema(
//                        allowableValues = { "ASCENDING", "DESCENDING" }
//                )
//        )
//        @RequestParam
//        String sortDirection,
//
//        @Parameter(
//                name = "sortBy",
//                description = "정렬 기준",
//                required = true,
//                in = ParameterIn.QUERY,
//                schema = @Schema(
//                        allowableValues = { "createdAt" }
//                )
//        )
//        @RequestParam
//        String sortBy
//);
