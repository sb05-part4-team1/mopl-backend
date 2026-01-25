package com.mopl.dto.user;

import java.util.UUID;

public record UserSummary(
    UUID userId,
    String name,
    String profileImageUrl
) {
}
