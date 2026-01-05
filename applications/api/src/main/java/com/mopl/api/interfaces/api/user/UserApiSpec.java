package com.mopl.api.interfaces.api.user;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.jpa.repository.user.query.UserQueryRequest;
import com.mopl.jpa.support.cursor.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "사용자 프로필 수정")
    @Parameter(
        name = "userId",
        description = "수정할 User ID"
    )
    @RequestBody(
        content = @Content(
            mediaType = "multipart/form-data",
            schema = @Schema(type = "string", format = "binary")
        )
    )
    @ApiResponse(
        responseCode = "200",
        description = "프로필 수정 성공",
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
    UserResponse updateProfile(UUID userId, MultipartFile image);

    @Operation(summary = "사용자 목록 조회", description = "커서 기반 페이지네이션으로 사용자 목록을 조회합니다.")
    @ApiResponse(
        responseCode = "200",
        description = "사용자 목록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CursorResponse.class)
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
    CursorResponse<UserResponse> getUsers(UserQueryRequest request);
}
