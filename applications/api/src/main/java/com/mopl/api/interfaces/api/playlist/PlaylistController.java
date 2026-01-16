package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.application.playlist.PlaylistDetail;
import com.mopl.api.application.playlist.PlaylistFacade;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
public class PlaylistController implements PlaylistApiSpec {

    private final PlaylistFacade playlistFacade;
    private final PlaylistResponseMapper playlistResponseMapper;

    @GetMapping
    public CursorResponse<PlaylistResponse> getPlaylists(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @ModelAttribute PlaylistQueryRequest request
    ) {
        return playlistFacade.getPlaylists(userDetails.userId(), request);
    }

    @GetMapping("/{playlistId}")
    public PlaylistResponse getPlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId
    ) {
        PlaylistDetail playlistDetail = playlistFacade.getPlaylist(userDetails.userId(), playlistId);
        return playlistResponseMapper.toResponse(
            playlistDetail.playlist(),
            playlistDetail.subscriberCount(),
            playlistDetail.subscribedByMe(),
            playlistDetail.contents()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse createPlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid PlaylistCreateRequest request
    ) {
        PlaylistModel playlistModel = playlistFacade.createPlaylist(userDetails.userId(), request);
        return playlistResponseMapper.toResponse(playlistModel);
    }

    @PatchMapping("/{playlistId}")
    public PlaylistResponse updatePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId,
        @RequestBody @Valid PlaylistUpdateRequest request
    ) {
        PlaylistModel playlistModel = playlistFacade.updatePlaylist(
            userDetails.userId(),
            playlistId,
            request
        );
        return playlistResponseMapper.toResponse(playlistModel);
    }

    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId
    ) {
        playlistFacade.deletePlaylist(userDetails.userId(), playlistId);
    }

    @PostMapping("/{playlistId}/contents/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addContentToPlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId,
        @PathVariable UUID contentId
    ) {
        playlistFacade.addContentToPlaylist(userDetails.userId(), playlistId, contentId);
    }

    @DeleteMapping("/{playlistId}/contents/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContentFromPlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId,
        @PathVariable UUID contentId
    ) {
        playlistFacade.deleteContentFromPlaylist(userDetails.userId(), playlistId, contentId);
    }

    @PostMapping("/{playlistId}/subscription")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId
    ) {
        playlistFacade.subscribePlaylist(userDetails.userId(), playlistId);
    }

    @DeleteMapping("/{playlistId}/subscription")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribePlaylist(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID playlistId
    ) {
        playlistFacade.unsubscribePlaylist(userDetails.userId(), playlistId);
    }
}
