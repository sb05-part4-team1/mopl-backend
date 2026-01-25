package com.mopl.jpa.repository.content.batch;

import java.util.UUID;

public interface ContentDeletionLogRow {

    UUID getLogId();

    UUID getContentId();

    String getThumbnailPath();
}
