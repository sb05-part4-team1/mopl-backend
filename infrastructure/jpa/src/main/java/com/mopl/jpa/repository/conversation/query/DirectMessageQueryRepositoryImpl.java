package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import com.mopl.jpa.entity.conversation.QDirectMessageEntity;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mopl.jpa.entity.conversation.QDirectMessageEntity.directMessageEntity;

@Repository
@RequiredArgsConstructor
public class DirectMessageQueryRepositoryImpl implements DirectMessageQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public CursorResponse<DirectMessageModel> findAll(
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        DirectMessageSortFieldJpa sortFieldJpa = DirectMessageSortFieldJpa.from(request.sortBy());

        JPAQuery<DirectMessageEntity> jpaQuery = baseQuery(conversationId)
            .select(directMessageEntity)
            .leftJoin(directMessageEntity.sender).fetchJoin();

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            directMessageEntity.id
        );

        List<DirectMessageEntity> rows = jpaQuery.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        long totalCount = countTotal(conversationId);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            directMessageEntityMapper::toModelWithSender,
            sortFieldJpa::extractValue,
            DirectMessageEntity::getId
        );
    }

    @Override
    public Map<UUID, DirectMessageModel> findLastDirectMessagesWithSenderByConversationIdIn(
        Collection<UUID> conversationIds
    ) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }

        QDirectMessageEntity subDirectMessageEntity = new QDirectMessageEntity("subDirectMessageEntity");

        List<DirectMessageEntity> lastMessages = queryFactory
            .selectFrom(directMessageEntity)
            .leftJoin(directMessageEntity.sender).fetchJoin()
            .where(
                directMessageEntity.conversation.id.in(conversationIds),
                directMessageEntity.createdAt.eq(
                    JPAExpressions
                        .select(subDirectMessageEntity.createdAt.max())
                        .from(subDirectMessageEntity)
                        .where(
                            subDirectMessageEntity.conversation.id.eq(directMessageEntity.conversation.id)
                        )
                )
            )
            .fetch();

        return lastMessages.stream()
            .collect(Collectors.toMap(
                entity -> entity.getConversation().getId(),
                directMessageEntityMapper::toModelWithSender
            ));
    }

    private JPAQuery<?> baseQuery(UUID conversationId) {
        return queryFactory
            .from(directMessageEntity)
            .where(directMessageEntity.conversation.id.eq(conversationId));
    }

    private long countTotal(UUID conversationId) {
        Long count = baseQuery(conversationId)
            .select(directMessageEntity.count())
            .fetchOne();
        return count != null ? count : 0L;
    }
}
