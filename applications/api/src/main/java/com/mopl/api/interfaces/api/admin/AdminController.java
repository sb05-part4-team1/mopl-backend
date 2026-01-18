package com.mopl.api.interfaces.api.admin;

import com.mopl.api.scheduler.PlaylistSubscriberCountSyncScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// TODO: batch로 이동
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PlaylistSubscriberCountSyncScheduler playlistSubscriberCountSyncScheduler;

    @PostMapping("/sync/playlist-subscriber-counts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncAllPlaylistSubscriberCounts() {
        playlistSubscriberCountSyncScheduler.syncSubscriberCounts();
    }

    @PostMapping("/sync/playlist-subscriber-counts/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncPlaylistSubscriberCount(@PathVariable UUID playlistId) {
        playlistSubscriberCountSyncScheduler.syncSubscriberCount(playlistId);
    }
}
