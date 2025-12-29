package com.mopl.jpa.repository.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.domain.model.user.FollowModel;
import com.mopl.domain.repository.user.FollowRepository;
import com.mopl.jpa.entity.user.FollowEntity;
import com.mopl.jpa.entity.user.FollowEntityMapper;
import com.mopl.jpa.entity.user.QFollowEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final JpaFollowRepository jpaFollowRepository;
    private final FollowEntityMapper followEntityMapper;

    private final JPAQueryFactory queryFactory;
    private final QFollowEntity follow = QFollowEntity.followEntity;

    @Override
    public FollowModel save(FollowModel followModel) {
        FollowEntity followEntity = followEntityMapper.toEntity(followModel);
        FollowEntity savedFollowEntity = jpaFollowRepository.save(followEntity);
        return followEntityMapper.toModel(savedFollowEntity);
    }

    @Override
    public Optional<FollowModel> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) {
        FollowEntity result = queryFactory
            .selectFrom(follow)
            .where(
                follow.follower.id.eq(followerId),
                follow.followee.id.eq(followeeId)
            )
            .fetchOne();

        return Optional.ofNullable(followEntityMapper.toModel(result));
    }

    @Override
    public boolean existsByFollowerIdAndFolloweeIdAndDeletedAtIsNull(UUID followerId,
        UUID followeeId) {
        return queryFactory
            .selectOne()
            .from(follow)
            .where(
                follow.follower.id.eq(followerId),
                follow.followee.id.eq(followeeId),
                follow.deletedAt.isNull()
            )
            .fetchFirst() != null;
    }
}
