package com.mopl.api.interfaces.api.user;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.user.dto.ChangePasswordRequest;
import com.mopl.api.interfaces.api.user.dto.UserCreateRequest;
import com.mopl.api.interfaces.api.user.dto.UserLockUpdateRequest;
import com.mopl.dto.user.UserResponse;
import com.mopl.api.interfaces.api.user.dto.UserRoleUpdateRequest;
import com.mopl.api.interfaces.api.user.dto.UserUpdateRequest;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.repository.user.UserSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
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

@Tag(name = "User API")
public interface UserApiSpec {

    @Operation(summary = "[어드민] 사용자 목록 조회 (커서 페이지네이션)")
    @Parameters({
        @Parameter(
            name = "emailLike",
            description = "이메일 부분일치 검색어",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "roleEqual",
            description = "권한",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UserModel.Role.class)
        ),
        @Parameter(
            name = "isLocked",
            description = "계정 잠금 상태",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Boolean.class)
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
            schema = @Schema(implementation = UserSortField.class, defaultValue = "name")
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    CursorResponse<UserResponse> getUsers(@Parameter(hidden = true) UserQueryRequest request);

    @Operation(summary = "사용자 상세 조회")
    @Parameter(name = "userId", description = "조회할 사용자 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.NotFound
    UserResponse getUser(UUID userId);

    @Operation(summary = "사용자 등록(회원가입)")
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Conflict
    UserResponse signUp(UserCreateRequest request);

    @Operation(summary = "비밀번호 변경", description = "본인의 비밀번호만 변경할 수 있습니다.")
    @Parameter(name = "userId", description = "사용자 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void updatePassword(UUID userId, ChangePasswordRequest request);

    @Operation(
        summary = "사용자 프로필 변경",
        description = "본인의 프로필만 변경할 수 있습니다. multipart/form-data 형식으로 요청합니다."
    )
    @Parameters({
        @Parameter(
            name = "userId",
            description = "수정할 User ID",
            required = true,
            in = ParameterIn.PATH
        ),
        @Parameter(
            name = "request",
            description = "프로필 수정 요청 데이터",
            required = false
        ),
        @Parameter(
            name = "image",
            description = "프로필 이미지 파일 (PNG, JPEG 지원)",
            required = false
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    UserResponse updateProfile(UUID userId, UserUpdateRequest request, MultipartFile image);

    @Operation(summary = "[어드민] 사용자 역할 수정")
    @Parameter(name = "userId", description = "수정할 사용자 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void updateRole(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID userId,
        UserRoleUpdateRequest request
    );

    @Operation(summary = "[어드민] 계정 잠금 상태 수정")
    @Parameter(name = "userId", description = "수정할 사용자 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void updateLocked(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID userId,
        UserLockUpdateRequest request
    );
}
