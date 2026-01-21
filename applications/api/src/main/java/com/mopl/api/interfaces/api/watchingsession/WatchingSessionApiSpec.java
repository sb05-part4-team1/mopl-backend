package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
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
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "WatchingSession API", description = "시청 세션 API")
public interface WatchingSessionApiSpec {

    @Operation(
        summary = "특정 사용자의 시청 세션 조회(nullable)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(
                    implementation = WatchingSessionDto.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<WatchingSessionDto> getWatchingSession(
        @Parameter(hidden = true) MoplUserDetails userDetails,

        @Parameter(
            required = true
        ) @PathVariable UUID watcherId
    );

    @Operation(
        summary = "특정 콘텐츠의 시청 세션 목록 조회(커서 페이지네이션)"
    )
    @Parameters({
        @Parameter(
            name = "contentId",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "watcherNameLike",
            description = "시청자 이름",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", format = "int32")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "string",
                allowableValues = {"ASCENDING", "DESCENDING"}
            )
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "string",
                allowableValues = {"createdAt"}
            )
        )
    })
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public CursorResponse<WatchingSessionDto> getWatchingSessions(
        @Parameter(hidden = true) @AuthenticationPrincipal MoplUserDetails userDetails,
        @Parameter(hidden = true) @PathVariable UUID contentId,
        @Parameter(hidden = true) WatchingSessionQueryRequest request
    );

}
