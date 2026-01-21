package com.mopl.api.interfaces.api.playlist;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Playlist API", description = "플레이리스트 API")
public interface PlaylistApiSpec {

    @Operation(summary = "플레이리스트 목록 조회(커서 퍼이지네이션)")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @Parameters({
        @Parameter(
            name = "keywordLike",
            description = "검색 키워드",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "ownerIdEqual",
            description = "소유자 ID",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", format = "uuid")
        ),
        @Parameter(
            name = "subscriberIdEqual",
            description = "구독자 ID",
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
                allowableValues = {"updatedAt", "subscribeCount"}
            )
        )
    })
    CursorResponse<PlaylistResponse> getPlaylists(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(hidden = true) PlaylistQueryRequest request
    );

    @Operation(summary = "플레이리스트 상세 조회")
    @Parameter(
        name = "playlistId",
        description = "조회할 플레이리스트 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = PlaylistResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저 또는 플레이리스트를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    PlaylistResponse getPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(hidden = true) UUID playlistId
    );

    @Operation(
        summary = "플레이리스트 생성",
        description = "생성한 플레이리스트는 API 요청자 본인의 플레이리스트로 생성됩니다."
    )
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = PlaylistCreateRequest.class))
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = PlaylistResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "*/*",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    PlaylistResponse createPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        PlaylistCreateRequest request
    );

    @Operation(
        summary = "플레이리스트 수정",
        description = "플레이리스트 소유자만 수정할 수 있습니다."
    )
    @Parameter(
        name = "playlistId",
        description = "수정할 플레이리스트 ID",
        required = true
    )
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = PlaylistUpdateRequest.class))
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = PlaylistResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저 또는 플레이리스트를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    PlaylistResponse updatePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        PlaylistUpdateRequest request
    );

    @Operation(
        summary = "플레이리스트 삭제",
        description = "플레이리스트 소유자만 삭제할 수 있습니다."
    )
    @Parameter(
        name = "playlistId",
        description = "삭제할 플레이리스트 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저 또는 플레이리스트를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void deletePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );

    @Operation(
        summary = "플레이리스트에 콘텐츠 추가",
        description = "플레이리스트 소유자만 콘텐츠를 추가할 수 있습니다."
    )
    @Parameter(
        name = "playlistId",
        description = "플레이리스트 ID",
        required = true
    )
    @Parameter(
        name = "contentId",
        description = "추가할 콘텐츠 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저, 플레이리스트 또는 콘텐츠를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "이미 추가된 콘텐츠",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void addContentToPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        UUID contentId
    );

    @Operation(
        summary = "플레이리스트에서 콘텐츠 삭제",
        description = "플레이리스트 소유자만 콘텐츠를 삭제할 수 있습니다."
    )
    @Parameter(
        name = "playlistId",
        description = "플레이리스트 ID",
        required = true)
    @Parameter(
        name = "contentId",
        description = "삭제할 콘텐츠 ID",
        required = true)
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저, 플레이리스트 또는 플레이리스트 콘텐츠를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void deleteContentFromPlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId,
        UUID contentId
    );

    @Operation(summary = "플레이리스트 구독")
    @Parameter(
        name = "playlistId",
        description = "구독할 플레이리스트 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저 또는 플레이리스트를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "이미 구독 중인 플레이리스트",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void subscribePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );

    @Operation(summary = "플레이리스트 구독 취소")
    @Parameter(
        name = "playlistId",
        description = "구독 취소할 플레이리스트 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "204",
        description = " 성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "유저 또는 플레이리스트를 찾을 수 없음",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void unsubscribePlaylist(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID playlistId
    );
}
