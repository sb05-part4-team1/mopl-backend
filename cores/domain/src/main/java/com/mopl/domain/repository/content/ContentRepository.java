package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import java.util.List;
import java.util.UUID; 

public interface ContentRepository {

    
    ContentModel save(ContentModel contentModel, List<TagModel> tags);

    
    boolean existsById(UUID contentId);
}