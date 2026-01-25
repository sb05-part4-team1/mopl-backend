package com.mopl.api.interfaces.api.content;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.dto.ContentResponse;
import com.mopl.api.interfaces.api.content.dto.ContentUpdateRequest;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentSortField;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Content API")
public interface ContentApiSpec {

    @Operation(summary = "콘텐츠 목록 조회 (커서 페이지네이션)")
    @Parameters({
        @Parameter(
            name = "typeEqual",
            description = "콘텐츠 타입",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ContentModel.ContentType.class)
        ),
        @Parameter(
            name = "keywordLike",
            description = "제목 또는 설명 부분일치 검색어",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "tagsIn",
            description = "태그 목록 (포함 필터)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String[].class)
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
            schema = @Schema(implementation = ContentSortField.class)
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @ApiErrorResponse.Default
    CursorResponse<ContentResponse> getContents(@Parameter(hidden = true) ContentQueryRequest request);

    @Operation(summary = "콘텐츠 상세 조회")
    @Parameter(name = "contentId", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    ContentResponse getContent(UUID contentId);

    @Operation(summary = "[어드민] 콘텐츠 생성")
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    ContentResponse upload(
        @Parameter(required = true) ContentCreateRequest request,
        @Parameter(description = "썸네일 이미지 파일", required = true) MultipartFile thumbnail
    );

    @Operation(summary = "[어드민] 콘텐츠 수정")
    @Parameter(name = "contentId", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    ContentResponse update(
        UUID contentId,
        @Parameter(required = true) ContentUpdateRequest request,
        @Parameter(description = "새 썸네일 이미지 파일") MultipartFile thumbnail
    );

    @Operation(
        summary = "[어드민] 콘텐츠 삭제",
        description = """
            콘텐츠를 소프트 삭제합니다.
            - purge 시점에 연관 데이터와 함께 하드 삭제됩니다.
            - 연관 데이터: Review, ContentTag, PlaylistContent
            """
    )
    @Parameter(name = "contentId", required = true)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void delete(UUID contentId);
}
