package com.mopl.api.interfaces.api.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import static com.mopl.domain.model.playlist.PlaylistModel.DESCRIPTION_MAX_LENGTH;
import static com.mopl.domain.model.playlist.PlaylistModel.TITLE_MAX_LENGTH;

@Schema(example = """
    {
      "title": "수정된 플레이리스트",
      "description": "수정된 설명"
    }
    """)
public record PlaylistUpdateRequest(
    @Size(max = TITLE_MAX_LENGTH) String title,
    @Size(max = DESCRIPTION_MAX_LENGTH) String description
) {
}
