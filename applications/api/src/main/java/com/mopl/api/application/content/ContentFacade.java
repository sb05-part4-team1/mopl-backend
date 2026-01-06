package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.tag.TagService;
import com.mopl.storage.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentFacade {

    private final ContentService contentService;
    private final TagService tagService;
    private final FileStorageProvider fileStorageProvider;

    @Transactional
    public ContentModel upload(ContentCreateRequest request, MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new InvalidContentDataException("썸네일 파일은 필수입니다.");
        }

        try {
            String fileName = "contents/" + java.util.UUID.randomUUID() + "_" + thumbnail
                .getOriginalFilename();
            String storedPath = fileStorageProvider.upload(thumbnail.getInputStream(), fileName);
            String thumbnailUrl = fileStorageProvider.getUrl(storedPath);

            List<TagModel> tags = tagService.findOrCreateTags(request.tags());
            ContentModel contentModel = ContentModel.create(
                request.type(),
                request.title(),
                request.description(),
                thumbnailUrl
            );

            return contentService.create(contentModel, tags);

        } catch (IOException e) {
            throw new RuntimeException("파일 스트림 읽기 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public ContentModel getDetail(UUID contentId) {
        // TODO: 평균 평점(averageRating), 리뷰 수(reviewCount), 시청자 수(watcherCount) 로직 추가 필요
        return contentService.getById(contentId);
    }
}
