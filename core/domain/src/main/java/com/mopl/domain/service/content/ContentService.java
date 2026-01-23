package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ContentService {

    private final ContentQueryRepository contentQueryRepository;
    private final ContentRepository contentRepository;
    private final ContentTagService contentTagService;

    public CursorResponse<ContentModel> getAll(ContentQueryRequest request) {
        return contentQueryRepository.findAll(request);
    }

    public ContentModel getById(UUID contentId) {
        return contentRepository.findById(contentId)
            .orElseThrow(() -> ContentNotFoundException.withId(contentId));
    }

    public ContentModel create(ContentModel content, List<String> tagNames) {
        ContentModel savedContent = contentRepository.save(content);
        contentTagService.applyTags(savedContent.getId(), tagNames);
        return savedContent;
    }

    public ContentModel update(
        UUID contentId,
        String title,
        String description,
        String thumbnailUrl,
        List<String> tagNames
    ) {
        ContentModel content = getById(contentId);

        ContentModel updated = content.update(title, description, thumbnailUrl);
        ContentModel saved = contentRepository.save(updated);

        if (tagNames != null) {
            contentTagService.deleteAllByContentId(saved.getId());
            contentTagService.applyTags(saved.getId(), tagNames);
        }

        return saved;
    }

    public void delete(ContentModel contentModel) {
        contentModel.delete();
        contentRepository.save(contentModel);
    }
}
