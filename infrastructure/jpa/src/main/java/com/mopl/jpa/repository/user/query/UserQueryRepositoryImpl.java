package com.mopl.jpa.repository.user.query;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mopl.jpa.entity.user.QUserEntity.userEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final UserEntityMapper userEntityMapper;

    @Override
    public CursorResponse<UserModel> findAll(UserQueryRequest request) {
        UserCursorRequestJpa jpaRequest = UserCursorRequestJpa.from(request);

        JPAQuery<UserEntity> jpaQuery = queryFactory
            .selectFrom(userEntity)
            .where(
                emailLike(request.emailLike()),
                roleEqual(request.roleEqual()),
                isLocked(request.isLocked())
            );

        CursorPaginationHelper.applyCursorPagination(jpaRequest, jpaQuery, userEntity.id);

        List<UserEntity> rows = jpaQuery.fetch();

        Long total = queryFactory
            .select(userEntity.count())
            .from(userEntity)
            .where(
                emailLike(request.emailLike()),
                roleEqual(request.roleEqual()),
                isLocked(request.isLocked())
            )
            .fetchOne();

        long totalCount = total != null ? total : 0;

        return CursorPaginationHelper.buildResponse(
            rows,
            jpaRequest,
            totalCount,
            userEntityMapper::toModel,
            entity -> jpaRequest.sortBy().extractValue(entity),
            UserEntity::getId
        );
    }

    private BooleanExpression emailLike(String emailLike) {
        return hasText(emailLike) ? userEntity.email.containsIgnoreCase(emailLike) : null;
    }

    private BooleanExpression roleEqual(UserModel.Role roleEqual) {
        return roleEqual != null ? userEntity.role.eq(roleEqual) : null;
    }

    private BooleanExpression isLocked(Boolean isLocked) {
        return isLocked != null ? userEntity.locked.eq(isLocked) : null;
    }
}
