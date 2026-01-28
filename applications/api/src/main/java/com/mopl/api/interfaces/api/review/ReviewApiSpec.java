package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.ApiErrorResponse;
import com.mopl.api.interfaces.api.review.dto.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.dto.ReviewUpdateRequest;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.dto.review.ReviewResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Review API")
public interface ReviewApiSpec {

    @Operation(summary = "리뷰 목록 조회 (커서 페이지네이션)")
    @Parameters({
        @Parameter(
            name = "contentId",
            description = "콘텐츠 ID",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
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
            description = "한 번에 가져올 개수 (1~100)",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = Integer.class, minimum = "1", maximum = "100")
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = SortDirection.class)
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = ReviewSortField.class)
        )
    })
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))
    )
    @ApiErrorResponse.Default
    CursorResponse<ReviewResponse> getReviews(@Parameter(hidden = true) ReviewQueryRequest request);

    @Operation(
        summary = "리뷰 생성",
        description = "생성한 리뷰는 API 요청자 본인의 리뷰로 생성됩니다."
    )
    @ApiResponse(
        responseCode = "201",
        content = @Content(schema = @Schema(implementation = ReviewResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Conflict
    ReviewResponse createReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        @Parameter(required = true) ReviewCreateRequest request
    );

    @Operation(
        summary = "리뷰 수정",
        description = "리뷰 작성자만 수정할 수 있습니다."
    )
    @Parameter(name = "reviewId", description = "리뷰 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = ReviewResponse.class))
    )
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    ReviewResponse updateReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID reviewId,
        @Parameter(required = true) ReviewUpdateRequest request
    );

    @Operation(
        summary = "리뷰 삭제",
        description = "리뷰 작성자만 삭제할 수 있습니다."
    )
    @Parameter(name = "reviewId", description = "리뷰 ID", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "204")
    @ApiErrorResponse.Default
    @ApiErrorResponse.Forbidden
    @ApiErrorResponse.NotFound
    void deleteReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID reviewId
    );
}
