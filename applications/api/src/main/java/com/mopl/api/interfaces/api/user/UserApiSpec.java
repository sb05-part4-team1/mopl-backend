package com.mopl.api.interfaces.api.user;

import com.mopl.api.interfaces.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
        description = "User가 성공적으로 생성됨",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserDto.class)
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
        description = "관련 리소스를 찾을 수 없음",
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
    UserDto signUp(UserCreateRequest request);
}
