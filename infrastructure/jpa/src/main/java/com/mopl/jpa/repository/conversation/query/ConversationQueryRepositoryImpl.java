package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.mopl.jpa.entity.conversation.QConversationEntity.conversationEntity;

@Repository
@RequiredArgsConstructor
public class ConversationQueryRepositoryImpl implements ConversationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ConversationEntityMapper conversationEntityMapper;

    @Override
    public CursorResponse<ConversationModel> findAllConversation(ConversationQueryRequest request) {
        ConversationSortFieldJpa sortFieldJpa = ConversationSortFieldJpa.from(request.sortBy());

        JPAQuery<ConversationEntity> query = queryFactory.selectFrom(conversationEntity);

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            query,
            conversationEntity.id
        );

        List<ConversationEntity> rows = query.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        long totalCount = countTotal();

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

    private long countTotal() {
        Long total = queryFactory
            .select(conversationEntity.count())
            .from(conversationEntity)
            .fetchOne();
        return total != null ? total : 0;
    }
}
