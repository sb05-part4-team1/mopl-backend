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
        UserSortFieldJpa sortFieldJpa = UserSortFieldJpa.from(request.sortBy());

        JPAQuery<UserEntity> jpaQuery = queryFactory
                .selectFrom(userEntity)
                .where(
                        emailLike(request.emailLike()),
                        roleEqual(request.roleEqual()),
                        isLocked(request.isLocked())
                );

        CursorPaginationHelper.applyCursorPagination(
                request,
                sortFieldJpa,
                jpaQuery,
                userEntity.id
        );

        List<UserEntity> rows = jpaQuery.fetch();
        long totalCount = countTotal(request);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            userEntityMapper::toModel,
            sortFieldJpa::extractValue,
            UserEntity::getId
        );
    }

    private long countTotal(UserQueryRequest request) {
        Long total = queryFactory
            .select(userEntity.count())
            .from(userEntity)
            .where(
                emailLike(request.emailLike()),
                roleEqual(request.roleEqual()),
                isLocked(request.isLocked())
            )
            .fetchOne();
        return total != null ? total : 0;
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
