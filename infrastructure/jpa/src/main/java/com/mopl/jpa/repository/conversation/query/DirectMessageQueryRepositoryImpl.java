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

import static com.mopl.jpa.entity.conversation.QConversationEntity.conversationEntity;
import static com.mopl.jpa.entity.conversation.QDirectMessageEntity.directMessageEntity;
import static com.mopl.jpa.entity.conversation.QReadStatusEntity.readStatusEntity;

@Repository
@RequiredArgsConstructor
public class DirectMessageQueryRepositoryImpl implements DirectMessageQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public CursorResponse<DirectMessageModel> findAll(
        UUID userId,
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        DirectMessageSortFieldJpa sortFieldJpa = DirectMessageSortFieldJpa.from(request.sortBy());

        JPAQuery<DirectMessageEntity> jpaQuery = baseQuery(userId, conversationId)
            .select(directMessageEntity)
            .join(directMessageEntity.sender).fetchJoin();

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

        long totalCount = countTotal(userId, conversationId);

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

    private JPAQuery<?> baseQuery(UUID userId, UUID conversationId) {
        return queryFactory
            .from(directMessageEntity)
            .join(directMessageEntity.conversation, conversationEntity)
            .join(readStatusEntity)
            .on(readStatusEntity.conversation.eq(conversationEntity))
            .where(
                conversationEntity.id.eq(conversationId),
                readStatusEntity.participant.id.eq(userId)
            );
    }

    private long countTotal(UUID userId, UUID conversationId) {
        Long count = baseQuery(userId, conversationId)
            .select(directMessageEntity.count())
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Map<UUID, DirectMessageModel> findLastMessagesWithSenderByConversationIdIn(
        Collection<UUID> conversationIds
    ) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }

        QDirectMessageEntity subMessage = new QDirectMessageEntity("subMessage");

        List<DirectMessageEntity> lastMessages = queryFactory
            .selectFrom(directMessageEntity)
            .join(directMessageEntity.sender).fetchJoin()
            .where(
                directMessageEntity.conversation.id.in(conversationIds),
                directMessageEntity.id.eq(
                    JPAExpressions
                        .select(subMessage.id.max())
                        .from(subMessage)
                        .where(
                            subMessage.conversation.id.eq(directMessageEntity.conversation.id)
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
}
