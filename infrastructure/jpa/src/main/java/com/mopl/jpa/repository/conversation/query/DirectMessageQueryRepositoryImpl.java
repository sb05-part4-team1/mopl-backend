package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import com.mopl.jpa.entity.conversation.QConversationEntity;
import com.mopl.jpa.entity.conversation.QDirectMessageEntity;
import com.mopl.jpa.entity.conversation.QReadStatusEntity;
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
        DirectMessageSortFieldJpa sortField = DirectMessageSortFieldJpa.from(request.sortBy());

        JPAQuery<DirectMessageEntity> query = queryFactory
            .select(directMessageEntity)
            .from(directMessageEntity)
            .join(directMessageEntity.conversation, conversationEntity)
            .join(readStatusEntity)
            .on(readStatusEntity.conversation.eq(conversationEntity))
            .where(
                conversationEntity.id.eq(conversationId),
                readStatusEntity.participant.id.eq(userId)
            );

        // cursor pagination 적용
        CursorPaginationHelper.applyCursorPagination(
            request,
            sortField,
            query,
            directMessageEntity.id
        );

        List<DirectMessageEntity> rows = query.fetch();

        // total count
        Long totalCountValue = queryFactory
            .select(directMessageEntity.count())
            .from(directMessageEntity)
            .join(directMessageEntity.conversation, conversationEntity)
            .join(readStatusEntity)
            .on(readStatusEntity.conversation.eq(conversationEntity))
            .where(
                conversationEntity.id.eq(conversationId),
                readStatusEntity.participant.id.eq(userId)
            )
            .fetchOne();

        long totalCount = totalCountValue != null ? totalCountValue : 0L;

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortField,
            totalCount,
            directMessageEntityMapper::toModelWithSender,
            sortField::extractValue,
            DirectMessageEntity::getId
        );
    }

    @Override
    public Map<UUID, DirectMessageModel> findLastMessagesWithSenderByConversationIdIn(
        Collection<UUID> conversationIds
    ) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }

        QDirectMessageEntity directMessage = directMessageEntity;
        QDirectMessageEntity subMessage = new QDirectMessageEntity("subMessage");

        List<DirectMessageEntity> lastMessages = queryFactory
            .selectFrom(directMessage)
            .join(directMessage.sender).fetchJoin()
            .where(
                directMessage.conversation.id.in(conversationIds),
                directMessage.id.eq(
                    JPAExpressions
                        .select(subMessage.id.max())
                        .from(subMessage)
                        .where(
                            subMessage.conversation.id.eq(directMessage.conversation.id)
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
