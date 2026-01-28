package com.mopl.api.interfaces.api.follow;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowStatusResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
    @Parameter(name = "followId", description = "팔로우 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    void unFollow(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID followId
    );

    @Operation(summary = "특정 유저의 팔로워 수 조회")
    @Parameter(name = "followeeId", description = "팔로워 수를 조회할 사용자 ID", required = true, in = ParameterIn.QUERY)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = Long.class))
    )
    @ApiErrorResponse.Default
    long getFollowCount(UUID followeeId);

    @Operation(summary = "특정 유저를 내가 팔로우하는지 여부 조회")
    @Parameter(name = "followeeId", description = "팔로우 상태를 확인할 사용자 ID", required = true, in = ParameterIn.QUERY)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = FollowStatusResponse.class))
    )
    @ApiErrorResponse.Default
    FollowStatusResponse getFollowStatus(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID followeeId
    );
}
