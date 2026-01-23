package com.mopl.jpa.repository.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.user.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistSubscriberRepositoryImpl implements PlaylistSubscriberRepository {

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Set<UUID> findAllPlaylistIds() {
        return jpaPlaylistSubscriberRepository.findAllPlaylistIds();
    }

    @Override
    public Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds) {
        if (playlistIds.isEmpty()) {
            return Set.of();
        }
        return jpaPlaylistSubscriberRepository.findPlaylistIdsBySubscriberIdAndPlaylistIdIn(
            subscriberId,
            playlistIds
        );
    }

    @Override
    public List<UUID> findSubscriberIdsByPlaylistId(UUID playlistId) {
        return jpaPlaylistSubscriberRepository.findSubscriberIdsByPlaylistId(playlistId);
    }

    @Override
    public long countByPlaylistId(UUID playlistId) {
        return jpaPlaylistSubscriberRepository.countByPlaylistId(playlistId);
    }

    @Override
    public Map<UUID, Long> countByPlaylistIdIn(Collection<UUID> playlistIds) {
        if (playlistIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> results = jpaPlaylistSubscriberRepository.countByPlaylistIdIn(playlistIds);
        Map<UUID, Long> countMap = new HashMap<>();

        for (UUID playlistId : playlistIds) {
            countMap.put(playlistId, 0L);
        }

        for (Object[] row : results) {
            UUID playlistId = (UUID) row[0];
            Long count = (Long) row[1];
            countMap.put(playlistId, count);
        }

        return countMap;
    }

    @Override
    public boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        return jpaPlaylistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
    }

    @Override
    public void save(UUID playlistId, UUID subscriberId) {
        PlaylistEntity playlistReference = jpaPlaylistRepository.getReferenceById(playlistId);
        UserEntity subscriberReference = jpaUserRepository.getReferenceById(subscriberId);

        PlaylistSubscriberEntity playlistSubscriberEntity = PlaylistSubscriberEntity.builder()
            .playlist(playlistReference)
            .subscriber(subscriberReference)
            .build();

        jpaPlaylistSubscriberRepository.save(playlistSubscriberEntity);
    }

    @Override
    public boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        int deletedCount = jpaPlaylistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
        return deletedCount > 0;
    }
}
