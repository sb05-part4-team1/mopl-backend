package com.mopl.search.content.service;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.search.content.mapper.ContentDocumentMapper;
import com.mopl.search.content.repository.ContentDocumentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ContentIndexService {

    private final ContentDocumentRepository repository;
    private final ContentDocumentMapper mapper;

    public void upsert(ContentModel model) {
        if (model == null) {
            log.debug("content index upsert skipped. reason=model is null");
            return;
        }

        try {
            repository.save(mapper.toDocument(model));
            log.debug("content index upsert done. contentId={}", model.getId());
        } catch (RuntimeException e) {
            log.error("content index upsert failed. contentId={}", model.getId(), e);
            throw e;
        }
    }

    public void upsertAll(List<ContentModel> models) {
        if (models == null || models.isEmpty()) {
            log.debug("content index bulk upsert skipped. reason=empty models");
            return;
        }

        try {
            repository.saveAll(
                models.stream()
                    .map(mapper::toDocument)
                    .toList()
            );

            log.info("content index bulk upsert done. count={}", models.size());
        } catch (RuntimeException e) {
            log.error("content index bulk upsert failed. count={}", models.size(), e);
            throw e;
        }
    }

    public void delete(UUID contentId) {
        if (contentId == null) {
            log.debug("content index delete skipped. reason=contentId is null");
            return;
        }

        try {
            repository.deleteById(contentId.toString());
            log.debug("content index delete done. contentId={}", contentId);
        } catch (RuntimeException e) {
            log.error("content index delete failed. contentId={}", contentId, e);
            throw e;
        }
    }

    public void deleteAll(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            log.debug("content index bulk delete skipped. reason=empty contentIds");
            return;
        }

        try {
            repository.deleteAllById(
                contentIds.stream()
                    .map(UUID::toString)
                    .toList()
            );

            log.info("content index bulk delete done. count={}", contentIds.size());
        } catch (RuntimeException e) {
            log.error("content index bulk delete failed. count={}", contentIds.size(), e);
            throw e;
        }
    }
}
