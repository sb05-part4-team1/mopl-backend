package com.mopl.search.content.repository;

import com.mopl.search.document.ContentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ContentDocumentRepository extends ElasticsearchRepository<ContentDocument, String> {
}
