package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.dto.ContentUpdateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.dto.content.ContentResponse;
import com.mopl.dto.content.ContentResponseMapper;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final WatchingSessionService watchingSessionService;
    private final StorageProvider storageProvider;
    private final ContentResponseMapper contentResponseMapper;
    private final TransactionTemplate transactionTemplate;

    private final ContentSearchSyncPort contentSearchSyncPort;
    private final AfterCommitExecutor afterCommitExecutor;

    public CursorResponse<ContentResponse> getContents(ContentQueryRequest request) {
        CursorResponse<ContentModel> response = contentService.getAll(request);
        List<ContentModel> contents = response.data();

        if (contents.isEmpty()) {
            return CursorResponse.empty(
                response.sortBy(),
                response.sortDirection()
            );
        }

        List<UUID> contentIds = contents.stream()
            .map(ContentModel::getId)
            .toList();

        Map<UUID, List<TagModel>> tagsByContentId = contentTagService.getTagsByContentIdIn(contentIds);
        Map<UUID, Long> watcherCountByContentId = watchingSessionService.countByContentIdIn(contentIds);

        return response.map(content -> {
            List<String> tagNames = toTagNames(tagsByContentId.getOrDefault(content.getId(), List.of()));
            long watcherCount = watcherCountByContentId.getOrDefault(content.getId(), 0L);

            return contentResponseMapper.toResponse(content, tagNames, watcherCount);
        });
    }

    public ContentResponse getContent(UUID contentId) {
        ContentModel content = contentService.getById(contentId);
        return toContentResponse(content);
    }

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

        return transactionTemplate.execute(status -> {
            ContentModel saved = contentService.create(contentModel);
            contentTagService.applyTags(saved.getId(), request.tags());
            afterCommitExecutor.execute(() -> contentSearchSyncPort.upsert(saved));
            return toContentResponse(saved);
        });
    }

    public ContentResponse update(
        UUID contentId,
        ContentUpdateRequest request,
        MultipartFile thumbnail
    ) {
        ContentModel contentModel = contentService.getById(contentId);

        String storedPath = (thumbnail != null && !thumbnail.isEmpty())
            ? uploadToStorage(thumbnail)
            : null;

        ContentModel updatedContentModel = contentModel.update(
            request.title(),
            request.description(),
            storedPath
        );

        return transactionTemplate.execute(status -> {
            ContentModel saved = contentService.update(updatedContentModel);

            if (request.tags() != null) {
                contentTagService.deleteAllByContentId(saved.getId());
                contentTagService.applyTags(saved.getId(), request.tags());
            }

            afterCommitExecutor.execute(() -> contentSearchSyncPort.upsert(saved));
            return toContentResponse(saved);
        });
    }

    public void delete(UUID contentId) {
        transactionTemplate.executeWithoutResult(status -> {
            ContentModel contentModel = contentService.getById(contentId);
            contentModel.delete();
            contentService.delete(contentModel);
            afterCommitExecutor.execute(() -> contentSearchSyncPort.delete(contentId));
        });
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
        List<String> tagNames = toTagNames(contentTagService.getTagsByContentId(content.getId()));
        long watcherCount = watchingSessionService.countByContentId(content.getId());
        return contentResponseMapper.toResponse(content, tagNames, watcherCount);
    }

    private List<String> toTagNames(List<TagModel> tags) {
        return tags.stream()
            .map(TagModel::getName)
            .toList();
    }
}
