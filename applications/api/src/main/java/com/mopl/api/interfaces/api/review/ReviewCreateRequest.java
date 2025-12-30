package com.mopl.api.interfaces.api.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ReviewCreateRequest(
    @NotNull(message = "콘텐츠 ID는 필수입니다.") UUID contentId,

    @NotBlank(message = "리뷰 내용은 필수입니다.") @Size(max = 10000) String text,

    @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating
) {
}
