package com.mopl.jpa.repository.conversation.query;

import static org.springframework.util.StringUtils.hasText;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.QConversationEntity;
import com.mopl.jpa.entity.conversation.QReadStatusEntity;
import com.mopl.jpa.entity.user.QUserEntity;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationQueryRepositoryImpl implements ConversationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ConversationEntityMapper conversationEntityMapper;

    @Override
    public CursorResponse<ConversationModel> findAllConversation(
        ConversationQueryRequest request,
        UUID userId
    ) {
        QConversationEntity conversation = QConversationEntity.conversationEntity;
        QReadStatusEntity readStatusMe = new QReadStatusEntity("readStatusMe");
        QReadStatusEntity readStatusOther = new QReadStatusEntity("readStatusOther");
        QUserEntity otherUser = QUserEntity.userEntity;

        ConversationSortFieldJpa sortFieldJpa = ConversationSortFieldJpa.from(request.sortBy());

        // base query
        JPAQuery<ConversationEntity> query = queryFactory
            .selectDistinct(conversation)
            .from(readStatusMe)
            .join(readStatusMe.conversation, conversation)
            .join(readStatusOther)
            .on(readStatusOther.conversation.eq(conversation))
            .join(readStatusOther.participant, otherUser)
            .where(
                // 내가 참여한 대화
                readStatusMe.participant.id.eq(userId),
                // 상대방만
                readStatusOther.participant.id.ne(userId),
                // soft delete
                conversation.deletedAt.isNull(),
                // keyword 검색
                hasText(request.keywordLike())
                    ? otherUser.name.containsIgnoreCase(request.keywordLike())
                    : null
            );

        // cursor pagination 적용
        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            query,
            conversation.id
        );

        List<ConversationEntity> rows = query.fetch();

        Long totalCountValue = queryFactory
            .select(conversation.countDistinct())
            .from(readStatusMe)
            .join(readStatusMe.conversation, conversation)
            .join(readStatusOther)
            .on(readStatusOther.conversation.eq(conversation))
            .join(readStatusOther.participant, otherUser)
            .where(
                readStatusMe.participant.id.eq(userId),
                readStatusOther.participant.id.ne(userId),
                conversation.deletedAt.isNull(),
                hasText(request.keywordLike())
                    ? otherUser.name.containsIgnoreCase(request.keywordLike())
                    : null
            )
            .fetchOne();

        long totalCount = totalCountValue != null ? totalCountValue : 0L;

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

}0
