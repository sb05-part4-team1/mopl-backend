package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.mopl.jpa.entity.playlist.QPlaylistEntity.playlistEntity;
import static com.mopl.jpa.entity.playlist.QPlaylistSubscriberEntity.playlistSubscriberEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class PlaylistQueryRepositoryImpl implements PlaylistQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaylistEntityMapper playlistEntityMapper;

    @Override
    public CursorResponse<PlaylistModel> findAll(PlaylistQueryRequest request) {
        PlaylistSortFieldJpa sortFieldJpa = PlaylistSortFieldJpa.from(request.sortBy());

        JPAQuery<PlaylistEntity> jpaQuery = queryFactory
            .selectFrom(playlistEntity)
            .leftJoin(playlistEntity.owner).fetchJoin()
            .where(
                keywordLike(request.keywordLike()),
                ownerIdEqual(request.ownerIdEqual()),
                subscriberIdEqual(request.subscriberIdEqual())
            );

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            playlistEntity.id
        );

        List<PlaylistEntity> rows = jpaQuery.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        long totalCount = countTotal(request);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            playlistEntityMapper::toModelWithOwner,
            sortFieldJpa::extractValue,
            PlaylistEntity::getId
        );
    }

    private long countTotal(PlaylistQueryRequest request) {
        Long total = queryFactory
            .select(playlistEntity.count())
            .from(playlistEntity)
            .where(
                keywordLike(request.keywordLike()),
                ownerIdEqual(request.ownerIdEqual()),
                subscriberIdEqual(request.subscriberIdEqual())
            )
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression keywordLike(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }
        return playlistEntity.title.containsIgnoreCase(keyword)
            .or(playlistEntity.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression ownerIdEqual(UUID ownerId) {
        return ownerId != null ? playlistEntity.owner.id.eq(ownerId) : null;
    }

    private BooleanExpression subscriberIdEqual(UUID subscriberId) {
        if (subscriberId == null) {
            return null;
        }
        return playlistEntity.id.in(
            JPAExpressions
                .select(playlistSubscriberEntity.playlist.id)
                .from(playlistSubscriberEntity)
                .where(playlistSubscriberEntity.subscriber.id.eq(subscriberId))
        );
    }
}
