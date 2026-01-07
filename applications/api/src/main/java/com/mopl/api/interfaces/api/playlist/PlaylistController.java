package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.application.playlist.PlaylistFacade;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistFacade playlistFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse createPlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid PlaylistCreateRequest request
    ) {
        // 인증된 객체에서 id만 가져옴
        UUID requesterId = userDetails.userId();

        return playlistFacade.createPlaylist(
            requesterId,
            request
        );
    }

    @PatchMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.OK)
    public PlaylistResponse updatePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId,
        @RequestBody @Valid PlaylistUpdateRequest request
    ) {
        UUID requesterId = userDetails.userId();

        return playlistFacade.updatePlaylist(
            requesterId,
            playlistId,
            request
        );
    }

    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.OK)
    public void deletePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId
    ) {
        UUID requesterId = userDetails.userId();

        playlistFacade.deletePlaylist(requesterId, playlistId);
    }

}
