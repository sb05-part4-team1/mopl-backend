package com.mopl.api.interfaces.api.content;

import com.mopl.api.application.content.ContentFacade;
import com.mopl.domain.model.content.ContentModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApiSpec {

    private final ContentFacade contentFacade;
    private final ContentResponseMapper contentResponseMapper; // 매퍼를 여기서 사용

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponse upload(
        @RequestPart(name = "request") @Valid ContentCreateRequest request,
        @RequestPart(name = "thumbnail") MultipartFile thumbnail
    ) {
        ContentModel contentModel = contentFacade.upload(request, thumbnail);

        return contentResponseMapper.toResponse(
            contentModel
        );
    }
}
