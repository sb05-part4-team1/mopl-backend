package com.mopl.api.interfaces.api.content;

import com.mopl.domain.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Content API", description = "콘텐츠 관리 API")
public interface ContentApiSpec {

    @Operation(
        summary = "콘텐츠 업로드",
        description = "콘텐츠 정보와 썸네일 이미지를 업로드합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "콘텐츠가 성공적으로 업로드됨",
        content = @Content(
            mediaType = "application[wordSnippet](/snippet)",
            schema = @Schema(implementation = ContentResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터 또는 파일 형식",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ContentResponse upload(
        @Parameter(description = "콘텐츠 정보 (JSON)", required = true) ContentCreateRequest request,

        @Parameter(description = "썸네일 이미지 파일", required = true) MultipartFile thumbnail
    );

    @Operation(
        summary = "콘텐츠 상세 조회",
        description = "콘텐츠 ID를 통해 상세 정보와 태그 목록을 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 조회됨",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContentResponse.class)
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
    ContentResponse getDetail(
        @Parameter(
            description = "콘텐츠 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID contentId
    );

    @Operation(
        summary = "콘텐츠 수정",
        description = "콘텐츠 제목, 설명, 태그 및 썸네일 이미지를 수정합니다. 썸네일은 선택 사항입니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "콘텐츠가 성공적으로 수정됨",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContentResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터",
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
    ContentResponse update(
        @Parameter(
            description = "콘텐츠 UUID",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID contentId,

        @Parameter(description = "수정할 콘텐츠 정보 (JSON)", required = true) ContentUpdateRequest request,

        @Parameter(description = "새 썸네일 이미지 파일 (선택)") MultipartFile thumbnail
    );

    @Operation(
        summary = "콘텐츠 삭제 (관리자)",
        description = """
            콘텐츠를 소프트 삭제합니다.
            - 콘텐츠와 리뷰는 soft delete 처리됩니다.
            - 태그, 플레이리스트 등 연관관계는 유지됩니다.
            - purge 시점에 연관 데이터는 함께 하드 삭제됩니다.
            """
    )
    @ApiResponse(
        responseCode = "204",
        description = "콘텐츠가 성공적으로 삭제됨"
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 콘텐츠",
        content = @Content(
            mediaType = "application/json",
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
}
