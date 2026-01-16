package com.mopl.api.interfaces.api.review;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Review API", description = "리뷰 API")
public interface ReviewApiSpec {

    @Operation(summary = "리뷰 목록 조회")
    @ApiResponse(
        responseCode = "200",
        description = "성공"
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
    CursorResponse<ReviewResponse> getReviews(ReviewQueryRequest request);

    @Operation(summary = "리뷰 생성")
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = ReviewCreateRequest.class))
    )
    @ApiResponse(
        responseCode = "201",
        description = "생성 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ReviewResponse.class)
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
        responseCode = "404",
        description = "콘텐츠를 찾을 수 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ReviewResponse createReview(MoplUserDetails userDetails, ReviewCreateRequest request);

    @Operation(summary = "리뷰 수정")
    @Parameter(name = "reviewId", description = "수정할 리뷰 ID", required = true)
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = ReviewUpdateRequest.class))
    )
    @ApiResponse(
        responseCode = "200",
        description = "수정 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ReviewResponse.class)
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
        responseCode = "403",
        description = "권한 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "리뷰를 찾을 수 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    ReviewResponse updateReview(
        MoplUserDetails userDetails,
        UUID reviewId,
        ReviewUpdateRequest request
    );

    @Operation(summary = "리뷰 삭제")
    @Parameter(name = "reviewId", description = "삭제할 리뷰 ID", required = true)
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(
        responseCode = "401",
        description = "인증 오류",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "리뷰를 찾을 수 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    void deleteReview(MoplUserDetails userDetails, UUID reviewId);
}
