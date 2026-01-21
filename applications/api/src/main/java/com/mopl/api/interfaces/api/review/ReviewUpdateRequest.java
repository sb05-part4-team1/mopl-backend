package com.mopl.api.interfaces.api.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.review.ReviewModel.TEXT_MAX_LENGTH;

@Schema(
    example = """
        {
          "text": "수정된 리뷰 내용입니다.",
          "rating": 4.0
        }
        """
)
public record ReviewUpdateRequest(
    @Schema(description = "리뷰 내용") @Size(max = TEXT_MAX_LENGTH) String text,

    @Schema(description = "평점(0.0~5.0)",
        format = "double") @DecimalMin("0.0") @DecimalMax("5.0") Double rating
) {
}
