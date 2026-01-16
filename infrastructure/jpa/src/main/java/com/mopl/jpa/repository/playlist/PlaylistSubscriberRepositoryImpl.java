package com.mopl.jpa.repository.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.user.JpaUserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistSubscriberRepositoryImpl implements PlaylistSubscriberRepository {

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaUserRepository jpaUserRepository;
    private final EntityManager entityManager;

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
    public boolean save(UUID playlistId, UUID subscriberId) {
        PlaylistEntity playlistReference = jpaPlaylistRepository.getReferenceById(playlistId);
        UserEntity subscriberReference = jpaUserRepository.getReferenceById(subscriberId);

        PlaylistSubscriberEntity playlistSubscriberEntity = PlaylistSubscriberEntity.builder()
            .playlist(playlistReference)
            .subscriber(subscriberReference)
            .build();

        try {
            jpaPlaylistSubscriberRepository.saveAndFlush(playlistSubscriberEntity);
            return true;
        } catch (DataIntegrityViolationException e) {
            entityManager.clear();
            return false;
        }
    }

    @Override
    public boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        int deletedCount = jpaPlaylistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
        return deletedCount > 0;
    }

    @Override
    public boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        return jpaPlaylistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
    }

    @Override
    public long countByPlaylistId(UUID playlistId) {
        return jpaPlaylistSubscriberRepository.countByPlaylistId(playlistId);
    }
}
