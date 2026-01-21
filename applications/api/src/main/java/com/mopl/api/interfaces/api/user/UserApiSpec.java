package com.mopl.api.interfaces.api.user;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.user.UserQueryRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "User API", description = "사용자 API")
public interface UserApiSpec {

    @Operation(summary = "사용자 등록(회원가입)")
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserCreateRequest.class)
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = UserResponse.class)
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
        responseCode = "409",
        description = "중복된 이메일",
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
    UserResponse signUp(UserCreateRequest request);

    @Operation(
        summary = "[어드민]사용자 목록 조회(커서 페이지네이션)",
        description = "커서 기반 페이지네이션으로 사용자 목록을 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "사용자 목록 조회 성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = UserCursorResponse.class)
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
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @Parameters({
        @Parameter(
            name = "emailLike",
            description = "이메일",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")
        ),
        @Parameter(
            name = "roleEqual",
            description = "권한",
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "string",
                allowableValues = {"USER", "ADMIN"}
            )
        ),
        @Parameter(
            name = "isLocked",
            description = "계정 잠금 상태",
            in = ParameterIn.QUERY,
            schema = @Schema(
                type = "boolean"
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
                allowableValues = {"name", "email", "createdAt", "isLocked", "role"}
            )
        )
    })
    CursorResponse<UserResponse> getUsers(
        @Parameter(hidden = true) UserQueryRequest request
    );

    @Operation(summary = "사용자 상세 조회")
    @Parameter(
        name = "userId",
        description = "조회할 사용자 ID",
        required = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = UserResponse.class)
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
        description = "잘못된 요청",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "사용자를 찾을 수 없음",
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
    UserResponse getUser(UUID userId);

    @Operation(
        summary = "사용자 프로필 변경",
        description = "본인의 프로필만 변경할 수 있습니다."
    )
    @Parameter(
        name = "userId",
        description = "수정할 User ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            mediaType = "*/*",
            schema = @Schema(implementation = UserResponse.class)
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
        description = "사용자를 찾을 수 없음",
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
    UserResponse updateProfile(UUID userId, UserUpdateRequest request, MultipartFile image);

    @Operation(
        summary = "[어드민]계정 잠금 상태 수정",
        description = "[어드민 기능] 계정 잠금 상태를 변경합니다."
    )
    @Parameter(
        name = "userId",
        description = "수정할 사용자 ID",
        required = true
    )
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserLockUpdateRequest.class)
        )
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
        description = "사용자를 찾을 수 없음",
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
    void updateLocked(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID userId,
        UserLockUpdateRequest request
    );

    @Operation(
        summary = "[어드민]사용자 역할 수정",
        description = "관리자 권한이 필요합니다."
    )
    @Parameter(
        name = "userId",
        description = "수정할 사용자 ID",
        required = true)
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserRoleUpdateRequest.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터",
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
        description = "사용자를 찾을 수 없음",
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
    void updateRole(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID userId,
        UserRoleUpdateRequest request
    );

    @Operation(
        summary = "비밀번호 변경",
        description = "본인의 비밀번호만 변경할 수 있습니다."
    )
    @Parameter(
        name = "userId",
        description = "사용자 ID",
        required = true
    )
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = ChangePasswordRequest.class)
        )
    )
    @ApiResponse(responseCode = "204", description = "성공")
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
        description = "사용자를 찾을 수 없음",
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
    void updatePassword(UUID userId, ChangePasswordRequest request);
}
