package com.mopl.batch.cleanup.service.playlist;

import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistCleanupExecutor {

    private final PlaylistContentRepository playlistContentRepository;
    private final PlaylistSubscriberRepository playlistSubscriberRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional
    public int cleanupBatch(List<UUID> playlistIds) {
        playlistContentRepository.deleteAllByPlaylistIds(playlistIds);
        playlistSubscriberRepository.deleteAllByPlaylistIds(playlistIds);
        return playlistRepository.deleteAllByIds(playlistIds);
    }
}
