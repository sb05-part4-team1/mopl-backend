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
        try {
            return playlistSubscriberCountRepository.getCount(playlistId);
        } catch (Exception e) {
            return playlistSubscriberRepository.countByPlaylistId(playlistId);
        }
    }

    public Map<UUID, Long> getSubscriberCounts(Collection<UUID> playlistIds) {
        try {
            return playlistSubscriberCountRepository.getCounts(playlistIds);
        } catch (Exception e) {
            return playlistSubscriberRepository.countByPlaylistIdIn(playlistIds);
        }
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
        tryIncrementCount(playlistId);
    }

    public void unsubscribe(UUID playlistId, UUID subscriberId) {
        boolean deleted = playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(
            playlistId,
            subscriberId
        );
        if (!deleted) {
            throw PlaylistSubscriptionNotFoundException.withPlaylistIdAndSubscriberId(playlistId, subscriberId);
        }
        tryDecrementCount(playlistId);
    }

    private void tryIncrementCount(UUID playlistId) {
        try {
            playlistSubscriberCountRepository.increment(playlistId);
        } catch (Exception ignored) {
            // Redis 장애 시 무시, sync scheduler가 보정
        }
    }

    private void tryDecrementCount(UUID playlistId) {
        try {
            playlistSubscriberCountRepository.decrement(playlistId);
        } catch (Exception ignored) {
            // Redis 장애 시 무시, sync scheduler가 보정
        }
    }
}
