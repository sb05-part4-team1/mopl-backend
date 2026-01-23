package com.mopl.jpa.repository.content.projection;

import java.util.UUID;

public interface ContentDeletionLogRow {

    UUID getLogId();

    UUID getContentId();

    String getThumbnailPath();
}
