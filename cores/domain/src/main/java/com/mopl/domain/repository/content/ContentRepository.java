package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel;

import java.util.UUID;

public interface ContentRepository {

    ContentModel save(ContentModel contentModel);

    boolean existsById(UUID contentId);
}
