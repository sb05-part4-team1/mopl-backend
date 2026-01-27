package com.mopl.search.content.repository.query;

import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.search.document.ContentDocument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum ContentSortFieldEs {

    POPULARITY(
        ContentSortField.POPULARITY,
        "popularityScore",
        ContentDocument::getPopularityScore,
        Object::toString,
        Double::parseDouble
    ),

    CREATED_AT(
        ContentSortField.CREATED_AT,
        "createdAt",
        ContentDocument::getCreatedAt,
        Object::toString,
        Instant::parse
    ),

    RATE(
        ContentSortField.RATE,
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
            case CREATED_AT -> CREATED_AT;
            case POPULARITY -> POPULARITY;
            case RATE -> RATE;
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
