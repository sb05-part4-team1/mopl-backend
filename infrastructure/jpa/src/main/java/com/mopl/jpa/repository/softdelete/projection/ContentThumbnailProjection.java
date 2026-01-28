package com.mopl.jpa.repository.softdelete.projection;

import java.util.UUID;

public interface ContentThumbnailProjection {

    UUID getId();

    String getThumbnailPath();
}
