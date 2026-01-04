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

    @Operation(summary = "콘텐츠 업로드", description = "콘텐츠 정보와 썸네일 이미지를 업로드합니다.")
    @ApiResponse(
        responseCode = "201",
        description = "콘텐츠가 성공적으로 업로드됨",
        content = @Content(
            mediaType = "application/json",
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

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 ID를 통해 상세 정보와 태그 목록을 조회합니다.")
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
        @Parameter(description = "콘텐츠 UUID", required = true,
            example = "550e8400-e29b-41d4-a716-446655440000") UUID contentId
    );
}
