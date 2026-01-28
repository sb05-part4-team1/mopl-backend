package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.playlist.dto.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.dto.PlaylistUpdateRequest;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.dto.playlist.PlaylistResponse;
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

@Tag(name = "Playlist API")
public interface PlaylistApiSpec {

    @Operation(summary = "플레이리스트 목록 조회 (커서 페이지네이션)")
    @Parameters({
        @Parameter(
            name = "keywordLike",
            description = "검색 키워드",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "ownerIdEqual",
            description = "소유자 ID",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "subscriberIdEqual",
            description = "구독자 ID",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
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
            schema = @Schema(implementation = PlaylistSortField.class)
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @ApiErrorResponse.Default
    CursorResponse<PlaylistResponse> getPlaylists(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(hidden = true) PlaylistQueryRequest request
    );

    @Operation(summary = "플레이리스트 상세 조회")
    @Parameter(name = "playlistId", description = "플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    PlaylistResponse getPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );

    @Operation(
        summary = "플레이리스트 생성",
        description = "생성한 플레이리스트는 API 요청자 본인의 플레이리스트로 생성됩니다."
    )
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
    )
    @ApiErrorResponse.Default
    PlaylistResponse createPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(required = true) PlaylistCreateRequest request
    );

    @Operation(
        summary = "플레이리스트 수정",
        description = "플레이리스트 소유자만 수정할 수 있습니다."
    )
    @Parameter(name = "playlistId", description = "플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    PlaylistResponse updatePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        @Parameter(required = true) PlaylistUpdateRequest request
    );

    @Operation(
        summary = "플레이리스트 삭제",
        description = "플레이리스트 소유자만 삭제할 수 있습니다."
    )
    @Parameter(name = "playlistId", description = "플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void deletePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );

    @Operation(
        summary = "플레이리스트에 콘텐츠 추가",
        description = "플레이리스트 소유자만 콘텐츠를 추가할 수 있습니다."
    )
    @Parameter(name = "playlistId", description = "플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @Parameter(name = "contentId", description = "추가할 콘텐츠 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    @ApiErrorResponse.Conflict
    void addContentToPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        UUID contentId
    );

    @Operation(
        summary = "플레이리스트에서 콘텐츠 삭제",
        description = "플레이리스트 소유자만 콘텐츠를 삭제할 수 있습니다."
    )
    @Parameter(name = "playlistId", description = "플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @Parameter(name = "contentId", description = "삭제할 콘텐츠 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void deleteContentFromPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        UUID contentId
    );

    @Operation(summary = "플레이리스트 구독")
    @Parameter(name = "playlistId", description = "구독할 플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    @ApiErrorResponse.Conflict
    void subscribePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );

    @Operation(summary = "플레이리스트 구독 취소")
    @Parameter(name = "playlistId", description = "구독 취소할 플레이리스트 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    void unsubscribePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );
}
