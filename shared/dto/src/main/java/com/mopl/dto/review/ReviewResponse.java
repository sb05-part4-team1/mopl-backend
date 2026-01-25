package com.mopl.dto.review;

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
