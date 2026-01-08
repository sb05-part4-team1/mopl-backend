package com.mopl.jpa.repository.follow;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.jpa.entity.follow.FollowEntity;
import com.mopl.jpa.entity.follow.FollowEntityMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final JpaFollowRepository jpaFollowRepository;
    private final FollowEntityMapper followEntityMapper;

    @Override
    public FollowModel save(FollowModel followModel) {
        FollowEntity followEntity = followEntityMapper.toEntity(followModel);
        FollowEntity savedFollowEntity = jpaFollowRepository.save(followEntity);
        return followEntityMapper.toModel(savedFollowEntity);
    }

    @Override
    public Optional<FollowModel> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) {
        return jpaFollowRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
            .map(followEntityMapper::toModel);
    }

    @Override
    public boolean existsByFollowerIdAndFolloweeId(UUID followerId,
        UUID followeeId) {
        return jpaFollowRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    public Optional<FollowModel> findById(UUID followId) {
        return jpaFollowRepository.findById(followId)
            .map(followEntityMapper::toModel);
    }

    @Override
    public void delete(FollowModel followModel) {
        jpaFollowRepository.deleteById(followModel.getId());
    }

    @Override
    public long countByFolloweeId(UUID followeeId) {
        return jpaFollowRepository.countByFolloweeId(followeeId);
    }
}
