package com.mopl.jpa.repository.follow;

import com.mopl.domain.model.follow.FollowModel;
import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.jpa.entity.follow.FollowEntity;
import com.mopl.jpa.entity.follow.FollowEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final JpaFollowRepository jpaFollowRepository;
    private final FollowEntityMapper followEntityMapper;

    @Override
    public Optional<FollowModel> findById(UUID followId) {
        return jpaFollowRepository.findById(followId)
            .map(followEntityMapper::toModel);
    }

    @Override
    public Optional<FollowModel> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) {
        return jpaFollowRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
            .map(followEntityMapper::toModel);
    }

    @Override
    public List<UUID> findFollowerIdsByFolloweeId(UUID followeeId) {
        return jpaFollowRepository.findFollowerIdsByFolloweeId(followeeId);
    }

    @Override
    public long countByFolloweeId(UUID followeeId) {
        return jpaFollowRepository.countByFolloweeId(followeeId);
    }

    @Override
    public boolean existsByFollowerIdAndFolloweeId(
        UUID followerId,
        UUID followeeId
    ) {
        return jpaFollowRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    public FollowModel save(FollowModel followModel) {
        FollowEntity followEntity = followEntityMapper.toEntity(followModel);
        FollowEntity savedFollowEntity = jpaFollowRepository.save(followEntity);
        return followEntityMapper.toModel(savedFollowEntity);
    }

    @Override
    public void delete(FollowModel followModel) {
        jpaFollowRepository.deleteById(followModel.getId());
    }
}
