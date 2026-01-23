package com.mopl.api.interfaces.api.content;

import com.mopl.api.application.content.ContentFacade;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApiSpec {

    private final ContentFacade contentFacade;

    // TODO: ContentResponse로 다시 변경
    @GetMapping
    public CursorResponse<ContentSummary> getContents(ContentQueryRequest request) {
        return contentFacade.getContents(request);
    }

    @GetMapping("/{contentId}")
    public ContentResponse getContent(@PathVariable UUID contentId) {
        return contentFacade.getContent(contentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponse upload(
        @RequestPart(name = "request") @Valid ContentCreateRequest request,
        @RequestPart(name = "thumbnail") MultipartFile thumbnail
    ) {
        return contentFacade.upload(request, thumbnail);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContentResponse update(
        @PathVariable UUID contentId,
        @RequestPart(name = "request") @Valid ContentUpdateRequest request,
        @RequestPart(name = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return contentFacade.update(contentId, request, thumbnail);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable UUID contentId
    ) {
        contentFacade.delete(contentId);
    }
}
