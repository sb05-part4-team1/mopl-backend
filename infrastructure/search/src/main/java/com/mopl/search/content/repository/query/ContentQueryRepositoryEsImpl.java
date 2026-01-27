package com.mopl.search.content.repository.query;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.query.ContentQueryRepository;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.search.content.mapper.ContentDocumentMapper;
import com.mopl.search.document.ContentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
public class ContentQueryRepositoryEsImpl implements ContentQueryRepository {

    private final ElasticsearchOperations operations;
    private final ContentDocumentMapper mapper;

    @Override
    public CursorResponse<ContentModel> findAll(ContentQueryRequest request) {
        ContentSortFieldEs sortField = ContentSortFieldEs.from(request.sortBy());
        SortOrder order = isAsc(request) ? SortOrder.Asc : SortOrder.Desc;

        int limit = request.limit();
        int fetchSize = limit + 1;

        NativeQueryBuilder builder = NativeQuery.builder();

        builder.withQuery(q -> q.bool(b -> {
            if (request.typeEqual() != null) {
                b.filter(f -> f.term(t -> t.field("type").value(request.typeEqual().name())));
            }

            String keyword = request.keywordLike();
            if (keyword != null) {
                b.must(m -> m.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("title", "description")
                    .type(TextQueryType.PhrasePrefix)
                ));
            }

            return b;
        }));

        builder.withSort(s -> s.field(f -> {
            if (sortField == ContentSortFieldEs.CREATED_AT) {
                return f.field(sortField.getEsField()).order(order).format("strict_date_optional_time");
            }
            return f.field(sortField.getEsField()).order(order);
        }));

        builder.withSort(s -> s.field(f -> f.field("contentId").order(order)));

        builder.withMaxResults(fetchSize);

        if (hasCursor(request)) {
            Object cursorValue = sortField.deserialize(request.cursor());
            builder.withSearchAfter(List.of(cursorValue, request.idAfter().toString()));
        }

        SearchHits<ContentDocument> hits = operations.search(builder.build(), ContentDocument.class);

        List<SearchHit<ContentDocument>> hitList = new ArrayList<>(hits.getSearchHits());
        boolean hasNext = hitList.size() > limit;

        if (hasNext) {
            hitList = hitList.subList(0, limit);
        }

        if (hitList.isEmpty()) {
            return CursorResponse.empty(sortField.fieldName(), request.sortDirection());
        }

        List<ContentModel> data = hitList.stream()
            .map(SearchHit::getContent)
            .map(mapper::toModel)
            .toList();

        SearchHit<ContentDocument> lastHit = hitList.getLast();
        List<Object> sortValues = lastHit.getSortValues();

        String nextCursor = !sortValues.isEmpty()
            ? String.valueOf(sortValues.getFirst())
            : sortField.serialize(sortField.extract(lastHit.getContent()));

        UUID nextIdAfter = sortValues.size() >= 2
            ? UUID.fromString(String.valueOf(sortValues.get(1)))
            : UUID.fromString(lastHit.getContent().getContentId());

        long totalCount = hits.getTotalHits();

        return CursorResponse.of(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortField.fieldName(),
            request.sortDirection()
        );
    }

    private boolean hasCursor(ContentQueryRequest request) {
        return request.cursor() != null
               && !request.cursor().isBlank()
               && request.idAfter() != null;
    }

    private boolean isAsc(ContentQueryRequest request) {
        return request.sortDirection() != null
               && request.sortDirection().name().equalsIgnoreCase("asc");
    }
}
