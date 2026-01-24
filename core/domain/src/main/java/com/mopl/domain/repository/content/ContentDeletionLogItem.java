package com.mopl.domain.repository.content;

import java.util.UUID;

public record ContentDeletionLogItem(
    UUID logId,
    UUID contentId,
    String thumbnailPath
) {
}
