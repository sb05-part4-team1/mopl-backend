package com.mopl.domain.service.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlaylistSubscriptionService {

    private final PlaylistSubscriberRepository playlistSubscriberRepository;

    public void subscribe(
        UUID playlistId,
        UUID subscriberId
    ) {
        if (playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(playlistId,
            subscriberId)) {
            return;
        }
        playlistSubscriberRepository.save(playlistId, subscriberId);
    }

    public void unsubscribe(
        UUID playlistId,
        UUID subscriberId
    ) {
        playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(playlistId, subscriberId);
    }
}
