package com.mopl.api.interfaces.api.user;

import com.mopl.domain.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "User API", description = "사용자 API")
public interface UserApiSpec {

    @Operation(summary = "회원가입")
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserCreateRequest.class)
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "회원가입 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserResponse.class)
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
        responseCode = "409",
        description = "중복된 이메일",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    UserResponse signUp(UserCreateRequest request);

    @Operation(summary = "사용자 상세 조회")
    @Parameter(name = "userId", description = "조회할 사용자 ID", required = true)
    @ApiResponse(
        responseCode = "200",
        description = "사용자 상세 조회 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserResponse.class)
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
        description = "사용자를 찾을 수 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    UserResponse getUser(UUID userId);
}
