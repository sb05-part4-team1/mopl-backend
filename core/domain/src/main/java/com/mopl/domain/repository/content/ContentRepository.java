package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel;

import java.util.Optional;
import java.util.UUID;

public interface ContentRepository {

    Optional<ContentModel> findById(UUID contentId);

    ContentModel save(ContentModel contentModel);
}
