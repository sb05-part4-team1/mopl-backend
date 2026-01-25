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
import java.util.UUID;

import static com.mopl.jpa.entity.conversation.QConversationEntity.conversationEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ConversationQueryRepositoryImpl implements ConversationQueryRepository {

    private static final QReadStatusEntity myReadStatus = new QReadStatusEntity("myReadStatus");
    private static final QReadStatusEntity otherReadStatus = new QReadStatusEntity("otherReadStatus");
    private static final QUserEntity otherUser = new QUserEntity("otherUser");

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

    private JPAQuery<?> baseQuery(UUID userId, String keywordLike) {
        return queryFactory
            .from(conversationEntity)
            .join(myReadStatus)
            .on(myReadStatus.conversation.eq(conversationEntity)
                .and(myReadStatus.participant.id.eq(userId)))
            .join(otherReadStatus)
            .on(otherReadStatus.conversation.eq(conversationEntity)
                .and(otherReadStatus.participant.id.ne(userId)))
            .join(otherUser)
            .on(otherUser.id.eq(otherReadStatus.participant.id))
            .where(keywordLike(keywordLike));
    }

    private long countTotal(UUID userId, String keywordLike) {
        Long total = baseQuery(userId, keywordLike)
            .select(conversationEntity.count())
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression keywordLike(String keywordLike) {
        return hasText(keywordLike) ? otherUser.name.containsIgnoreCase(keywordLike) : null;
    }
}
