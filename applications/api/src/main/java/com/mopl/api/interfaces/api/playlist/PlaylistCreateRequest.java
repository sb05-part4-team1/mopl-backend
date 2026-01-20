package com.mopl.api.interfaces.api.playlist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.playlist.PlaylistModel.DESCRIPTION_MAX_LENGTH;
import static com.mopl.domain.model.playlist.PlaylistModel.TITLE_MAX_LENGTH;

@Schema(
    example = """
        {
          "title": "내 플레이리스트",
          "description": "좋아하는 영화 모음"
        }
        """
)
public record PlaylistCreateRequest(
    @NotBlank @Size(max = TITLE_MAX_LENGTH) String title,
    @Size(max = DESCRIPTION_MAX_LENGTH) String description
) {
}
