package com.mopl.domain.service.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistSubscriptionService {

    private final PlaylistSubscriberRepository playlistSubscriberRepository;
    private final PlaylistSubscriberCountRepository playlistSubscriberCountRepository;

    public long getSubscriberCount(UUID playlistId) {
        return playlistSubscriberCountRepository.getCount(playlistId);
    }

    public boolean isSubscribedByPlaylistIdAndSubscriberId(UUID playlistId, UUID userId) {
        return playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(playlistId, userId);
    }

    public void subscribe(UUID playlistId, UUID subscriberId) {
        if (playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId)) {
            return;
        }
        playlistSubscriberRepository.save(playlistId, subscriberId);
        playlistSubscriberCountRepository.increment(playlistId);
    }

    public void unsubscribe(UUID playlistId, UUID subscriberId) {
        if (!playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId)) {
            return;
        }
        playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(playlistId, subscriberId);
        playlistSubscriberCountRepository.decrement(playlistId);
    }
}
