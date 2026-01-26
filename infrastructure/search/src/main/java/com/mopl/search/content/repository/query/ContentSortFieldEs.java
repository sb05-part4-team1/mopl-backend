package com.mopl.search.content.repository.query;

import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.search.document.ContentDocument;
import java.time.Instant;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentSortFieldEs {

    WATCHER_COUNT(
        ContentSortField.watcherCount,
        "reviewCount",
        ContentDocument::getReviewCount,
        Object::toString,
        Integer::parseInt
    ),

    CREATED_AT(
        ContentSortField.createdAt,
        "createdAt",
        ContentDocument::getCreatedAt,
        Object::toString,
        Instant::parse
    ),

    RATE(
        ContentSortField.rate,
        "averageRating",
        ContentDocument::getAverageRating,
        Object::toString,
        Double::parseDouble
    );

    private final ContentSortField domainField;
    private final String esField;
    private final Function<ContentDocument, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Object> deserializer;

    public static ContentSortFieldEs from(ContentSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
            case watcherCount -> WATCHER_COUNT;
            case rate -> RATE;
        };
    }

    public String serialize(Object value) {
        return value == null ? "" : serializer.apply(value);
    }

    public Object deserialize(String cursor) {
        return deserializer.apply(cursor);
    }

    public Object extract(ContentDocument doc) {
        return valueExtractor.apply(doc);
    }

    public String fieldName() {
        return domainField.name();
    }
}
