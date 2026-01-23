package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.ContentResponse;
import com.mopl.api.interfaces.api.content.ContentResponseMapper;
import com.mopl.api.interfaces.api.content.ContentUpdateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.storage.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentFacade {

    private final ContentService contentService;
    private final ContentResponseMapper contentResponseMapper;
    private final FileStorageProvider fileStorageProvider;

    @Transactional
    public ContentModel upload(ContentCreateRequest request, MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new InvalidContentDataException("썸네일 파일은 필수입니다.");
        }

        String thumbnailUrl = uploadToStorage(thumbnail);

        ContentModel contentModel = ContentModel.create(
            request.type(),
            request.title(),
            request.description(),
            thumbnailUrl
        );

        return contentService.create(contentModel, request.tags());
    }

    @Transactional(readOnly = true)
    public CursorResponse<ContentResponse> getContents(ContentQueryRequest request) {
        return contentService.getAll(request).map(contentResponseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContentModel getDetail(UUID contentId) {
        // TODO: 평균 평점(averageRating), 리뷰 수(reviewCount), 시청자 수(watcherCount) 로직 추가 필요
        return contentService.getById(contentId);
    }

    @Transactional
    public ContentModel update(
        UUID contentId,
        ContentUpdateRequest request,
        MultipartFile thumbnail
    ) {
        String thumbnailUrl = (thumbnail != null && !thumbnail.isEmpty())
            ? uploadToStorage(thumbnail)
            : null;

        return contentService.update(
            contentId,
            request.title(),
            request.description(),
            thumbnailUrl,
            request.tags()
        );
    }

    @Transactional
    public void delete(UUID contentId) {
        contentService.delete(contentId);
    }

    /**
     * 파일 업로드 로직 공통화
     */
    private String uploadToStorage(MultipartFile file) {
        try {
            String fileName = "contents/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            return fileStorageProvider.upload(file.getInputStream(), fileName);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
