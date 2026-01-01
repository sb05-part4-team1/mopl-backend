package com.mopl.api.interfaces.api.user;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.mopl.domain.exception.ErrorResponse;

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
            mediaType = "application/json",
            schema = @Schema(implementation = FollowResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FollowResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ResponseEntity<FollowResponse> follow(@Parameter(hidden = true) UUID followerId,
        FollowRequest request);

}
