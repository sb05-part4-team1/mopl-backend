package com.mopl.domain.service.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistModel create(
            UserModel owner,
            String title,
            String description
    ) {
        PlaylistModel playlistModel = PlaylistModel.create(
                owner.getId(),
                title,
                description
        );

        return playlistRepository.save(playlistModel);

    }
}
