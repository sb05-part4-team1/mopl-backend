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
    public CursorResponse<NotificationModel> findAll(
        UUID receiverId,
        NotificationQueryRequest request
    ) {
        NotificationSortFieldJpa sortFieldJpa = NotificationSortFieldJpa.from(request.sortBy());

        JPAQuery<NotificationEntity> jpaQuery = baseQuery(receiverId)
            .select(notificationEntity);

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            notificationEntity.id
        );

        List<NotificationEntity> rows = jpaQuery.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

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

    private JPAQuery<?> baseQuery(UUID receiverId) {
        return queryFactory
            .from(notificationEntity)
            .where(receiverIdEqual(receiverId));
    }

    private long countTotal(UUID receiverId) {
        Long total = baseQuery(receiverId)
            .select(notificationEntity.count())
            .fetchOne();
        return total != null ? total : 0;
    }

    @Override
    public List<NotificationModel> findByReceiverIdAndIdAfter(UUID receiverId, UUID idAfter) {
        List<NotificationEntity> entities = queryFactory
            .selectFrom(notificationEntity)
            .where(
                receiverIdEqual(receiverId),
                notificationEntity.id.gt(idAfter)
            )
            .orderBy(notificationEntity.id.asc())
            .fetch();

        return entities.stream()
            .map(notificationEntityMapper::toModel)
            .toList();
    }

    private BooleanExpression receiverIdEqual(UUID receiverId) {
        return receiverId != null ? notificationEntity.receiverId.eq(receiverId) : null;
    }
}
