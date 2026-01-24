package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.interfaces.api.common.CommonApiResponse;
import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "WatchingSession API", description = "시청 세션 API")
public interface WatchingSessionApiSpec {

    @Operation(summary = "특정 사용자의 시청 세션 조회 (nullable)")
    @Parameter(name = "watcherId", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = WatchingSessionResponse.class))
    )
    @ApiResponse(responseCode = "204", description = "시청 중인 세션 없음")
    @CommonApiResponse.Default
    WatchingSessionResponse getWatchingSession(UUID watcherId);

    @Operation(summary = "특정 콘텐츠의 시청 세션 목록 조회 (커서 페이지네이션)")
    @Parameters({
        @Parameter(
            name = "contentId",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "watcherNameLike",
            description = "시청자 이름 부분일치 검색어",
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
            description = "한 번에 가져올 개수 (1~100)",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = Integer.class, minimum = "1", maximum = "100")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = SortDirection.class)
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = WatchingSessionSortField.class)
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @CommonApiResponse.Default
    ResponseEntity<WatchingSessionResponse> getWatchingSessions(
        @Parameter(hidden = true) UUID contentId,
        @Parameter(hidden = true) WatchingSessionQueryRequest request
    );
}
