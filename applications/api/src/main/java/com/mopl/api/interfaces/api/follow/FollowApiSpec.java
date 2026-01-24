package com.mopl.api.interfaces.api.follow;

import java.util.UUID;

import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.api.interfaces.api.follow.dto.FollowResponse;
import org.springframework.http.ResponseEntity;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.security.userdetails.MoplUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Follow API", description = "팔로우 API")
public interface FollowApiSpec {

    @Operation(summary = "팔로우")
    @RequestBody(
        required = true,
        content = @Content(
            schema = @Schema(implementation = FollowRequest.class)
        )
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            schema = @Schema(implementation = FollowResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            schema = @Schema(implementation = FollowResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ResponseEntity<FollowResponse> follow(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        FollowRequest request
    );

    @Operation(summary = "팔로우 취소")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ResponseEntity<Void> unFollow(@Parameter(hidden = true) MoplUserDetails userDetails, @Parameter(
        name = "followId", required = true) UUID followId);

    @Operation(summary = "특정 유저의 팔로워 수 조회")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        ))
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ResponseEntity<Long> getFollowCount(@Parameter(name = "followeeId",
        required = true) UUID followeeId);

    @Operation(summary = "특정 유저를 내가 팔로우하는지 여부 조회")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        ))
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ResponseEntity<Boolean> getFollowStatus(@Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(name = "followeeId",
            required = true) UUID followeeId);
}
