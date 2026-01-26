package com.mopl.api.interfaces.api.follow;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowStatusResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Follow API")
public interface FollowApiSpec {

    @Operation(summary = "팔로우")
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = FollowResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Conflict
    FollowResponse follow(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        FollowRequest request
    );

    @Operation(summary = "팔로우 취소")
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    void unFollow(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(name = "followId", required = true) UUID followId
    );

    @Operation(summary = "특정 유저의 팔로워 수 조회")
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = Long.class))
    )
    @ApiErrorResponse.Default
    long getFollowCount(@Parameter(name = "followeeId", required = true) UUID followeeId);

    @Operation(summary = "특정 유저를 내가 팔로우하는지 여부 조회")
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = FollowStatusResponse.class))
    )
    @ApiErrorResponse.Default
    FollowStatusResponse getFollowStatus(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(name = "followeeId", required = true) UUID followeeId
    );
}
