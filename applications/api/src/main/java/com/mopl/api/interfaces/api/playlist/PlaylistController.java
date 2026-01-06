package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.application.playlist.PlaylistFacade;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            ){
        // 인증된 객체에서 id만 가져옴
        UUID requesterId = userDetails.userId();

        return playlistFacade.createPlaylist(
                requesterId,
                request
        );
    }

}
