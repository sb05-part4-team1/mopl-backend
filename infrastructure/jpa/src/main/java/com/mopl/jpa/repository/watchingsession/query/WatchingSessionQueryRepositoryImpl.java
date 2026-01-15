package com.mopl.jpa.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class WatchingSessionQueryRepositoryImpl implements WatchingSessionQueryRepository {

    private final WatchingSessionInMemoryStore watchingSessionInMemoryStore;

    @Override
    public CursorResponse<WatchingSessionModel> findByContentId(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        WatchingSessionSortFieldSupport sortField = WatchingSessionSortFieldSupport.from(request
            .sortBy());

        List<WatchingSessionModel> all = watchingSessionInMemoryStore.findAll();

        List<WatchingSessionModel> filtered = all.stream()
            .filter(session -> session != null)
            .filter(session -> session.getContent() != null && session.getContent().getId() != null)
            .filter(session -> session.getWatcher() != null)
            .filter(session -> contentId.equals(session.getContent().getId()))
            .filter(session -> watcherNameLike(session, request.watcherNameLike()))
            .toList();

        long totalCount = filtered.size();

        List<WatchingSessionModel> sorted = filtered.stream()
            .sorted(buildComparator(request.sortDirection()))
            .toList();

        List<WatchingSessionModel> afterCursor = applyCursor(sorted, request, sortField);

        int limit = request.limit();
        List<WatchingSessionModel> pagePlusOne = afterCursor.stream()
            .limit((long) limit + 1)
            .toList();

        boolean hasNext = pagePlusOne.size() > limit;
        List<WatchingSessionModel> result = hasNext ? pagePlusOne.subList(0, limit) : pagePlusOne;

        if (result.isEmpty()) {
            return CursorResponse.empty(sortField.getFieldName(), request.sortDirection());
        }

        if (!hasNext) {
            return CursorResponse.of(
                result,
                null,
                null,
                false,
                totalCount,
                sortField.getFieldName(),
                request.sortDirection()
            );
        }

        WatchingSessionModel last = result.get(result.size() - 1);
        String nextCursor = sortField.serializeCursor(sortField.extractValue(last));
        UUID nextIdAfter = last.getId();

        return CursorResponse.of(
            result,
            nextCursor,
            nextIdAfter,
            true,
            totalCount,
            sortField.getFieldName(),
            request.sortDirection()
        );
    }

    private boolean watcherNameLike(WatchingSessionModel session, String watcherNameLike) {
        if (!hasText(watcherNameLike)) {
            return true;
        }

        String keyword = watcherNameLike.trim();
        if (!hasText(keyword)) {
            return true;
        }

        String watcherName = session.getWatcher().getName();
        if (!hasText(watcherName)) {
            return false;
        }

        return watcherName.toLowerCase().contains(keyword.toLowerCase());
    }

    private Comparator<WatchingSessionModel> buildComparator(SortDirection sortDirection) {
        Comparator<WatchingSessionModel> comparator = Comparator
            .comparing(WatchingSessionModel::getCreatedAt, Comparator.nullsLast(Comparator
                .naturalOrder()))
            .thenComparing(WatchingSessionModel::getId, Comparator.nullsLast(Comparator
                .naturalOrder()));

        return sortDirection.isAscending() ? comparator : comparator.reversed();
    }

    private List<WatchingSessionModel> applyCursor(
        List<WatchingSessionModel> sorted,
        WatchingSessionQueryRequest request,
        WatchingSessionSortFieldSupport sortField
    ) {
        // 프로젝트 표준: cursor + idAfter 둘 다 있어야 커서 조건 적용
        if (request.idAfter() == null || !hasText(request.cursor())) {
            return sorted;
        }

        Comparable<?> cursorValue = sortField.deserializeCursor(request.cursor());
        UUID idAfter = request.idAfter();
        boolean isAscending = request.sortDirection().isAscending();

        return sorted.stream()
            .filter(session -> {
                Object extracted = sortField.extractValue(session);
                if (!(extracted instanceof Comparable<?> extractedComparable)) {
                    return false;
                }

                UUID id = session.getId();
                if (id == null) {
                    return false;
                }

                @SuppressWarnings("unchecked") int cmp = ((Comparable<Object>) extractedComparable)
                    .compareTo(cursorValue);

                if (isAscending) {
                    return (cmp > 0) || (cmp == 0 && id.compareTo(idAfter) > 0);
                } else {
                    return (cmp < 0) || (cmp == 0 && id.compareTo(idAfter) < 0);
                }
            })
            .toList();
    }
}
