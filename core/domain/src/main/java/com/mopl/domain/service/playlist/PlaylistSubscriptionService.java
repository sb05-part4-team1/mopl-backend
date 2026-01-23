package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistSubscriptionAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistSubscriptionNotFoundException;
import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistSubscriptionService {

    private final PlaylistSubscriberRepository playlistSubscriberRepository;
    private final PlaylistSubscriberCountRepository playlistSubscriberCountRepository;

    public long getSubscriberCount(UUID playlistId) {
        return playlistSubscriberCountRepository.getCount(playlistId);
    }

    public Map<UUID, Long> getSubscriberCounts(Collection<UUID> playlistIds) {
        return playlistSubscriberCountRepository.getCounts(playlistIds);
    }

    public boolean isSubscribedByPlaylistIdAndSubscriberId(
        UUID playlistId,
        UUID subscriberId
    ) {
        return playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
    }

    public Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds) {
        return playlistSubscriberRepository.findSubscribedPlaylistIds(
            subscriberId,
            playlistIds
        );
    }

    public List<UUID> getSubscriberIds(UUID playlistId) {
        return playlistSubscriberRepository.findSubscriberIdsByPlaylistId(playlistId);
    }

    public void subscribe(UUID playlistId, UUID subscriberId) {
        if (playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId)
        ) {
            throw PlaylistSubscriptionAlreadyExistsException.withPlaylistIdAndSubscriberId(playlistId, subscriberId);
        }

        playlistSubscriberRepository.save(playlistId, subscriberId);
        playlistSubscriberCountRepository.increment(playlistId);
    }

    public void unsubscribe(UUID playlistId, UUID subscriberId) {
        boolean deleted = playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
        if (!deleted) {
            throw PlaylistSubscriptionNotFoundException.withPlaylistIdAndSubscriberId(playlistId, subscriberId);
        }
        playlistSubscriberCountRepository.decrement(playlistId);
    }
}
