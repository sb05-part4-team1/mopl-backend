package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.QReadStatusEntity;
import com.mopl.jpa.entity.user.QUserEntity;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mopl.jpa.entity.conversation.QConversationEntity.conversationEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ConversationQueryRepositoryImpl implements ConversationQueryRepository {

    private static final QReadStatusEntity readStatusEntity = new QReadStatusEntity("readStatusEntity");
    private static final QReadStatusEntity otherReadStatusEntity = new QReadStatusEntity("otherReadStatusEntity");
    private static final QUserEntity otherUserEntity = new QUserEntity("otherUserEntity");

    private final JPAQueryFactory queryFactory;
    private final ConversationEntityMapper conversationEntityMapper;

    @Override
    public CursorResponse<ConversationModel> findAll(UUID userId, ConversationQueryRequest request) {
        ConversationSortFieldJpa sortFieldJpa = ConversationSortFieldJpa.from(request.sortBy());

        JPAQuery<ConversationEntity> jpaQuery = baseQuery(userId, request.keywordLike())
            .select(conversationEntity);

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            conversationEntity.id
        );

        List<ConversationEntity> rows = jpaQuery.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        long totalCount = countTotal(userId, request.keywordLike());

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            conversationEntityMapper::toModel,
            sortFieldJpa::extractValue,
            ConversationEntity::getId
        );
    }

    @Override
    public Optional<ConversationModel> findByParticipants(UUID userId, UUID withId) {
        QReadStatusEntity readStatusEntity = new QReadStatusEntity("readStatusEntity");
        QReadStatusEntity withReadStatusEntity = new QReadStatusEntity("withReadStatusEntity");

        ConversationEntity result = queryFactory
            .select(readStatusEntity.conversation)
            .from(readStatusEntity)
            .join(withReadStatusEntity).on(withReadStatusEntity.conversation.eq(readStatusEntity.conversation))
            .where(
                readStatusEntity.participant.id.eq(userId),
                withReadStatusEntity.participant.id.eq(withId)
            )
            .fetchOne();

        return Optional.ofNullable(result)
            .map(conversationEntityMapper::toModel);
    }

    private JPAQuery<?> baseQuery(UUID userId, String keywordLike) {
        return queryFactory
            .from(conversationEntity)
            .join(readStatusEntity)
            .on(readStatusEntity.conversation.eq(conversationEntity)
                .and(readStatusEntity.participant.id.eq(userId)))
            .join(otherReadStatusEntity)
            .on(otherReadStatusEntity.conversation.eq(conversationEntity)
                .and(otherReadStatusEntity.participant.id.ne(userId)))
            .join(otherUserEntity)
            .on(otherUserEntity.id.eq(otherReadStatusEntity.participant.id))
            .where(keywordLike(keywordLike));
    }

    private long countTotal(UUID userId, String keywordLike) {
        Long total = baseQuery(userId, keywordLike)
            .select(conversationEntity.count())
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression keywordLike(String keywordLike) {
        return hasText(keywordLike) ? otherUserEntity.name.containsIgnoreCase(keywordLike) : null;
    }
}
