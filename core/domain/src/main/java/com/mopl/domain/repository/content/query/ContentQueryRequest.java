package com.mopl.domain.repository.content.query;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;
import java.util.List;
import java.util.UUID;

public record ContentQueryRequest(
    ContentType typeEqual,
    String keywordLike,
    List<String> tagsIn,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    ContentSortField sortBy
) implements CursorRequest<ContentSortField> {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public ContentQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.DESCENDING;
        sortBy = sortBy != null ? sortBy : ContentSortField.watcherCount;

        keywordLike = normalizeKeyword(keywordLike);
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String k = keyword.trim();
        if (k.isEmpty()) {
            return null;
        }

        if (isHangulJamoOnly(k)) {
            return null;
        }

        boolean hasKoreanSyllable = containsKoreanSyllable(k);
        int minLength = hasKoreanSyllable ? 2 : 3;

        return k.length() >= minLength ? k : null;
    }

    private static boolean isHangulJamoOnly(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 'ㄱ' || c > 'ㅎ') {
                return false;
            }
        }
        return true;
    }

    private static boolean containsKoreanSyllable(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '가' && c <= '힣') {
                return true;
            }
        }
        return false;
    }
}
