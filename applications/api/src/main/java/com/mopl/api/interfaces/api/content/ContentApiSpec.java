package com.mopl.api.interfaces.api.content;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Content API", description = "콘텐츠 관리 API")
public interface ContentApiSpec {

    @Operation(
        summary = "[어드민]콘텐츠 업로드",
        description = "콘텐츠 정보와 썸네일 이미지를 업로드합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ContentResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터 또는 파일 형식",
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
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ContentResponse upload(
        @Parameter(description = "콘텐츠 정보 (JSON)", required = true) ContentCreateRequest request,

        @Parameter(description = "썸네일 이미지 파일") MultipartFile thumbnail
    );

    @Operation(
        summary = "콘텐츠 상세 조회",
        description = "콘텐츠 ID를 통해 상세 정보와 태그 목록을 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContentResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 콘텐츠",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ContentResponse getDetail(
        @Parameter(
            description = "콘텐츠 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID contentId
    );

    @Operation(
        summary = "[어드민]콘텐츠 수정",
        description = "콘텐츠 제목, 설명, 태그 및 썸네일 이미지를 수정합니다. 썸네일은 선택 사항입니다."
    )
    @PutMapping(
        value = "/{contentId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "콘텐츠가 성공적으로 수정됨",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ContentResponse.class)
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
        description = "존재하지 않는 콘텐츠",
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
    ContentResponse update(
        @Parameter(
            description = "콘텐츠 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID contentId,

        @Parameter(
            description = "수정할 콘텐츠 정보 (JSON)",
            required = true
        ) ContentUpdateRequest request,

        @Parameter(
            description = "새 썸네일 이미지 파일 (선택)"
        ) MultipartFile thumbnail
    );

    @Operation(
        summary = "[어드민]콘텐츠 삭제",
        description = """
            콘텐츠를 소프트 삭제합니다.
            - 콘텐츠와 리뷰는 soft delete 처리됩니다.
            - 태그, 플레이리스트 등 연관관계는 유지됩니다.
            - purge 시점에 연관 데이터는 함께 하드 삭제됩니다.
            """
    )
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
        responseCode = "403",
        description = "권한 오류",
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
    void delete(
        @Parameter(
            description = "콘텐츠 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID contentId
    );

    @Operation(
        summary = "콘텐츠 목록 조회(커서 페이지네이션)",
        description = "커서 기반 페이지네이션을 사용하여 콘텐츠 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(
                    implementation = ContentCursorResponse.class
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
    @Parameters({
        @Parameter(
            name = "typeEqual",
            description = "콘텐츠 타입",
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "string",
                allowableValues = {"movie", "tvSeries", "sport"}
            )
        ),
        @Parameter(
            name = "keywordLike",
            description = "검색 키워드",
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "string"
            )
        ),
        @Parameter(
            name = "tagsIn",
            description = "태그 목록",
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "array<string>"
            )
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
                allowableValues = {"createdAt", "watcherCount", "rate"}
            )
        )
    })
    CursorResponse<ContentResponse> getContents(
        @Parameter(hidden = true) ContentQueryRequest request
    );
}
