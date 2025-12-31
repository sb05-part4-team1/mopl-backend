package com.mopl.api.interfaces.api.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ReviewUpdateRequest(

    @NotNull(message = "리뷰 내용은 필수입니다.") @Size(max = 10_000,
        message = "리뷰 내용은 10000자를 초과할 수 없습니다.") String text,

    @NotNull(message = "평점은 필수입니다.") @DecimalMin(value = "0.0", inclusive = true,
        message = "평점은 0.0 이상이어야 합니다.") @DecimalMax(value = "5.0", inclusive = true,
            message = "평점은 5.0 이하여야 합니다.") BigDecimal rating
) {
}
