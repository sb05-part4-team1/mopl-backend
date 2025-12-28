package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContentFacade {

    private final ContentService contentService;
    private final TagService tagService;

    @Transactional
    public ContentModel upload(ContentCreateRequest request, MultipartFile thumbnail) { // 반환 타입 변경
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new InvalidContentDataException("썸네일 파일은 필수입니다.");
        }

        // Todo: 이미지 업로드 서비스 로직 (현재는 임시 URL)
        String thumbnailUrl = "https://temp-storage.com/" + thumbnail.getOriginalFilename();

        List<TagModel> tags = tagService.findOrCreateTags(request.tags());

        ContentModel contentModel = ContentModel.create(
            request.type(),
            request.title(),
            request.description(),
            thumbnailUrl
        );

        return contentService.create(contentModel, tags); // 저장된 모델을 그대로 반환
    }
}
