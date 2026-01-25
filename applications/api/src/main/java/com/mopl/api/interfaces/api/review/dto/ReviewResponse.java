package com.mopl.api.interfaces.api.review.dto;

import com.mopl.dto.user.UserSummary;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID contentId,
    UserSummary author,
    String text,
    double rating
) {
}
