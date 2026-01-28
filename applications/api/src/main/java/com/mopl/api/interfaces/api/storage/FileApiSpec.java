package com.mopl.api.interfaces.api.storage;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

@Tag(name = "File API", description = "파일 조회 API")
public interface FileApiSpec {

    @Operation(
        summary = "파일 조회",
        description = "저장된 파일(이미지)을 조회하여 반환합니다. 로컬 스토리지 사용 시에만 활성화됩니다."
    )
    @Parameter(
        name = "path",
        description = "파일 경로",
        required = true,
        in = ParameterIn.QUERY,
        example = "thumbnails/content-123.png",
        schema = @Schema(implementation = String.class)
    )
    @ApiResponse(
        responseCode = "200",
        description = "파일 조회 성공",
        content = @Content(mediaType = "image/*")
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    ResponseEntity<Resource> display(String path);
}
