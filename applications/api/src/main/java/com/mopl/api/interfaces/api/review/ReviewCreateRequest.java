package com.mopl.api.interfaces.api.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import static com.mopl.domain.model.review.ReviewModel.TEXT_MAX_LENGTH;

public record ReviewCreateRequest(
    @NotNull UUID contentId,
    @NotBlank @Size(max = TEXT_MAX_LENGTH) String text,
    @NotNull @DecimalMin("0.0") @DecimalMax("5.0") Double rating
) {
}
