package com.mopl.api.interfaces.api.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import static com.mopl.domain.model.review.ReviewModel.TEXT_MAX_LENGTH;

@Schema(
    example = """
        {
          "contentId": "550e8400-e29b-41d4-a716-446655440000",
          "text": "정말 재미있는 영화였습니다!",
          "rating": 4.0
        }
        """
)
public record ReviewCreateRequest(

    @Schema(description = "콘텐츠 ID", format = "uuid") @NotNull UUID contentId,

    @Schema(description = "리뷰 내용") @NotBlank @Size(max = TEXT_MAX_LENGTH) String text,

    @Schema(description = "평점(0.0~5.0)",
        format = "double") @NotNull @DecimalMin("0.0") @DecimalMax("5.0") Double rating
) {
}
