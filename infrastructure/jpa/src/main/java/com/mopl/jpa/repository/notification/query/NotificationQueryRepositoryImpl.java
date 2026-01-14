package com.mopl.jpa.repository.notification.query;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.mopl.jpa.entity.notification.QNotificationEntity.notificationEntity;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final NotificationEntityMapper notificationEntityMapper;

    @Override
    public CursorResponse<NotificationModel> findAll(UUID receiverId,
        NotificationQueryRequest request) {
        NotificationSortFieldJpa sortFieldJpa = NotificationSortFieldJpa.from(request.sortBy());

        JPAQuery<NotificationEntity> jpaQuery = queryFactory
            .selectFrom(notificationEntity)
            .where(
                receiverIdEqual(receiverId)
            );

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            notificationEntity.id
        );

        List<NotificationEntity> rows = jpaQuery.fetch();
        long totalCount = countTotal(receiverId);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            notificationEntityMapper::toModel,
            sortFieldJpa::extractValue,
            NotificationEntity::getId
        );
    }

    private long countTotal(UUID receiverId) {
        Long total = queryFactory
            .select(notificationEntity.count())
            .from(notificationEntity)
            .where(
                receiverIdEqual(receiverId)
            )
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression receiverIdEqual(UUID receiverId) {
        return receiverId != null ? notificationEntity.receiver.id.eq(receiverId) : null;
    }
}
