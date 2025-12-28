package com.mopl.api.interfaces.api.review;

import java.math.BigDecimal;
import java.util.UUID;

public record ReviewCreateRequest(
    UUID contentId,
    String text,
    BigDecimal rating
) {
}
