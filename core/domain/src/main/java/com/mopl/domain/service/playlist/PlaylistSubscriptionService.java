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
        // 이미 구독 중이면 그냥 성공(멱등)
        if (playlistSubscriberRepository.existsByPlaylistIdAndSubscriberId(playlistId,
            subscriberId)) {
            return;
        }

        // 아니면 구독 관계 저장
        playlistSubscriberRepository.save(playlistId, subscriberId);
    }

    public void unsubscribe(
        UUID playlistId,
        UUID subscriberId
    ) {
        // 구독이 없으면 그냥 성공(멱등)
        playlistSubscriberRepository.deleteByPlaylistIdAndSubscriberId(playlistId, subscriberId);
    }

}
