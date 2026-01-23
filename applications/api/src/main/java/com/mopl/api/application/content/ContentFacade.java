package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.dto.ContentResponse;
import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.api.interfaces.api.content.dto.ContentUpdateRequest;
import com.mopl.api.interfaces.api.content.mapper.ContentResponseMapper;
import com.mopl.api.interfaces.api.content.mapper.ContentSummaryMapper;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewStats;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentFacade {

    private final ContentService contentService;
    private final ContentTagService contentTagService;
    private final ReviewService reviewService;
    private final WatchingSessionService watchingSessionService;
    private final StorageProvider storageProvider;
    private final ContentResponseMapper contentResponseMapper;
    private final ContentSummaryMapper contentSummaryMapper;

    public CursorResponse<ContentSummary> getContents(ContentQueryRequest request) {
        CursorResponse<ContentModel> response = contentService.getAll(request);

        List<UUID> contentIds = response.data().stream()
            .map(ContentModel::getId)
            .toList();

        Map<UUID, List<String>> tagsByContentId = contentTagService.getTagNamesByContentIds(contentIds);

        return response.map(model -> contentSummaryMapper.toSummary(
            model,
            tagsByContentId.getOrDefault(model.getId(), List.of())
        ));
    }

    public ContentResponse getContent(UUID contentId) {
        ContentModel content = contentService.getById(contentId);
        return toContentResponse(content);
    }

    @Transactional
    public ContentResponse upload(ContentCreateRequest request, MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw InvalidContentDataException.withDetailMessage("썸네일 파일은 필수입니다.");
        }

        String storedPath = uploadToStorage(thumbnail);

        ContentModel contentModel = ContentModel.create(
            request.type(),
            request.title(),
            request.description(),
            storedPath
        );

        ContentModel saved = contentService.create(contentModel);
        return toContentResponse(saved);
    }

    @Transactional
    public ContentResponse update(
        UUID contentId,
        ContentUpdateRequest request,
        MultipartFile thumbnail
    ) {
        String storedPath = (thumbnail != null && !thumbnail.isEmpty())
            ? uploadToStorage(thumbnail)
            : null;

        ContentModel updated = contentService.update(
            contentId,
            request.title(),
            request.description(),
            storedPath,
            request.tags()
        );
        return toContentResponse(updated);
    }

    public void delete(UUID contentId) {
        contentService.delete(contentId);
    }

    private String uploadToStorage(MultipartFile file) {
        try {
            String fileName = "contents/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            storageProvider.upload(file.getInputStream(), file.getSize(), fileName);
            return fileName;
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private ContentResponse toContentResponse(ContentModel content) {
        String thumbnailUrl = storageProvider.getUrl(content.getThumbnailUrl());
        List<TagModel> tags = contentTagService.getTagsByContentId(content.getId());
        ReviewStats reviewStats = reviewService.getStatsByContentId(content.getId());
        long watcherCount = watchingSessionService.countByContentId(content.getId());

        return contentResponseMapper.toResponse(content, thumbnailUrl, tags, reviewStats, watcherCount);
    }
}
