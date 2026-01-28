package com.mopl.search.content.service;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.logging.context.LogContext;
import com.mopl.search.content.mapper.ContentDocumentMapper;
import com.mopl.search.content.repository.ContentDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ContentIndexService {

    private final ContentDocumentRepository repository;
    private final ContentDocumentMapper mapper;

    public void upsert(ContentModel model) {
        if (model == null) {
            return;
        }

        try {
            repository.save(mapper.toDocument(model));
        } catch (RuntimeException e) {
            LogContext.with("contentId", model.getId()).error("Content index upsert failed", e);
            throw e;
        }
    }

    public void upsertAll(List<ContentModel> models) {
        if (models == null || models.isEmpty()) {
            return;
        }

        try {
            repository.saveAll(
                models.stream()
                    .map(mapper::toDocument)
                    .toList()
            );

            LogContext.with("count", models.size()).info("Content index bulk upsert completed");
        } catch (RuntimeException e) {
            LogContext.with("count", models.size()).error("Content index bulk upsert failed", e);
            throw e;
        }
    }

    public void delete(UUID contentId) {
        if (contentId == null) {
            return;
        }

        try {
            repository.deleteById(contentId.toString());
        } catch (RuntimeException e) {
            LogContext.with("contentId", contentId).error("Content index delete failed", e);
            throw e;
        }
    }

    public void deleteAll(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return;
        }

        try {
            repository.deleteAllById(
                contentIds.stream()
                    .map(UUID::toString)
                    .toList()
            );

            LogContext.with("count", contentIds.size()).info("Content index bulk delete completed");
        } catch (RuntimeException e) {
            LogContext.with("count", contentIds.size()).error("Content index bulk delete failed", e);
            throw e;
        }
    }
}
