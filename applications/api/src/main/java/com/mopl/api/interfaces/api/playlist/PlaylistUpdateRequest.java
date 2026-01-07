package com.mopl.api.interfaces.api.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.playlist.PlaylistModel.DESCRIPTION_MAX_LENGTH;
import static com.mopl.domain.model.playlist.PlaylistModel.TITLE_MAX_LENGTH;

public record PlaylistUpdateRequest(

    @NotBlank @Size(max = TITLE_MAX_LENGTH) String title,

    @Size(max = DESCRIPTION_MAX_LENGTH) String description

) {
}
