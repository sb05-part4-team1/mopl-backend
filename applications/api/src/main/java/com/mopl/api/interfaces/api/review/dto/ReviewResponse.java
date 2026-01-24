package com.mopl.api.interfaces.api.review.dto;

import com.mopl.api.interfaces.api.user.dto.UserSummary;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID contentId,
    UserSummary author,
    String text,
    double rating
) {
}
