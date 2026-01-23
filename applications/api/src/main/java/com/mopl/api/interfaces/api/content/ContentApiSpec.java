package com.mopl.api.interfaces.api.content;

import com.mopl.api.interfaces.api.common.CommonApiResponse;
import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.dto.ContentCursorResponse;
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

@Tag(name = "Content API", description = "콘텐츠 관리 API")
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
            description = "검색 키워드",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "tagsIn",
            description = "태그 목록",
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
            schema = @Schema(implementation = ContentSortField.class, defaultValue = "watcherCount")
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ContentCursorResponse.class))
    )
    @CommonApiResponse.Default
    CursorResponse<ContentResponse> getContents(@Parameter(hidden = true) ContentQueryRequest request);

    @Operation(summary = "콘텐츠 상세 조회")
    @Parameter(name = "contentId", description = "콘텐츠 UUID", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.NotFound
    ContentResponse getContent(UUID contentId);

    @Operation(summary = "[어드민] 콘텐츠 업로드", description = "콘텐츠 정보와 썸네일 이미지를 업로드합니다.")
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    ContentResponse upload(
        @Parameter(description = "콘텐츠 정보 (JSON)", required = true) ContentCreateRequest request,
        @Parameter(description = "썸네일 이미지 파일") MultipartFile thumbnail
    );

    @Operation(
        summary = "[어드민] 콘텐츠 수정",
        description = "콘텐츠 제목, 설명, 태그 및 썸네일 이미지를 수정합니다. 썸네일은 선택 사항입니다."
    )
    @Parameter(name = "contentId", description = "콘텐츠 UUID", required = true)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ContentResponse.class))
    )
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    @CommonApiResponse.NotFound
    ContentResponse update(
        UUID contentId,
        @Parameter(description = "수정할 콘텐츠 정보 (JSON)", required = true) ContentUpdateRequest request,
        @Parameter(description = "새 썸네일 이미지 파일 (선택)") MultipartFile thumbnail
    );

    @Operation(
        summary = "[어드민] 콘텐츠 삭제",
        description = """
            콘텐츠를 소프트 삭제합니다.
            - purge 시점에 연관 데이터와 함께 하드 삭제됩니다.
            - 연관 데이터: Review, ContentTag, PlaylistContent
            """
    )
    @Parameter(name = "contentId", description = "콘텐츠 UUID", required = true)
    @ApiResponse(responseCode = "204", description = "성공")
    @CommonApiResponse.Default
    @CommonApiResponse.Forbidden
    @CommonApiResponse.NotFound
    void delete(UUID contentId);
}
