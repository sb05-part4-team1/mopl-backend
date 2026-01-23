package com.mopl.api.interfaces.api.review;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
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

import java.util.UUID;

@Tag(name = "Review API", description = "리뷰 API")
public interface ReviewApiSpec {

    @Operation(summary = "리뷰 목록 조회(커서 페이지네이션)")
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
    @Parameters({
        @Parameter(
            name = "contentId",
            description = "콘텐츠 ID",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "cursor",
            description = "커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = String.class)
        ),
        @Parameter(
            name = "idAfter",
            description = "보조 커서",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = UUID.class)
        ),
        @Parameter(
            name = "limit",
            description = "한 번에 가져올 개수",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = Integer.class)
        ),
        @Parameter(
            name = "sortDirection",
            description = "정렬 방향",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = SortDirection.class)
        ),
        @Parameter(
            name = "sortBy",
            description = "정렬 기준",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ReviewSortField.class)
        )
    })
    CursorResponse<ReviewResponse> getReviews(
        @Parameter(hidden = true) ReviewQueryRequest request
    );

    @Operation(
        summary = "리뷰 생성",
        description = "생성한 리뷰는 API 요청자 본인의 리뷰로 생성됩니다."
    )
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = ReviewCreateRequest.class))
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공",
        content = @Content(
            schema = @Schema(implementation = ReviewResponse.class)
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
    ReviewResponse createReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        ReviewCreateRequest request
    );

    @Operation(
        summary = "리뷰 수정",
        description = "리뷰 작성자만 수정할 수 있습니다."
    )
    @Parameter(name = "reviewId", description = "수정할 리뷰 ID", required = true)
    @RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = ReviewUpdateRequest.class))
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공",
        content = @Content(
            schema = @Schema(implementation = ReviewResponse.class)
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
        responseCode = "403",
        description = "권한 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "리뷰를 찾을 수 없음",
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
    ReviewResponse updateReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID reviewId,
        ReviewUpdateRequest request
    );

    @Operation(
        summary = "리뷰 삭제",
        description = "리뷰 작성자만 삭제할 수 있습니다."
    )
    @Parameter(name = "reviewId", description = "삭제할 리뷰 ID", required = true)
    @ApiResponse(
        responseCode = "200",
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
        responseCode = "404",
        description = "리뷰를 찾을 수 없음",
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
    void deleteReview(
        @Parameter(hidden = true) MoplUserDetails userDetails,
        UUID reviewId
    );
}
