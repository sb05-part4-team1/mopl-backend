package com.mopl.domain.repository.content.batch;

import java.util.UUID;

public record ContentDeletionLogItem(
    UUID logId,
    UUID contentId,
    String thumbnailPath
) {
}
