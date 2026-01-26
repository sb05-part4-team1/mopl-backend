package com.mopl.batch.cleanup.service.playlist;

import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistCleanupTxService {

    private final PlaylistContentRepository playlistContentRepository;
    private final PlaylistSubscriberRepository playlistSubscriberRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional
    public int cleanupBatch(List<UUID> playlistIds) {
        int deletedPlaylistContents = playlistContentRepository.deleteAllByPlaylistIds(playlistIds);
        int deletedSubscribers = playlistSubscriberRepository.deleteAllByPlaylistIds(playlistIds);
        int deletedPlaylists = playlistRepository.deleteByIdIn(playlistIds);

        if (deletedPlaylists != playlistIds.size()) {
            log.warn(
                "playlist delete mismatch. requested={} deleted={}",
                playlistIds.size(),
                deletedPlaylists
            );
        }

        log.info(
            "playlist cleanup batch done. requested={} deletedPlaylists={} deletedPlaylistContents={} deletedSubscribers={}",
            playlistIds.size(),
            deletedPlaylists,
            deletedPlaylistContents,
            deletedSubscribers
        );

        return deletedPlaylists;
    }
}
