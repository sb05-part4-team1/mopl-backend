package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID contentId,
    UserSummary author,
    String text,
    double rating
) {
}
