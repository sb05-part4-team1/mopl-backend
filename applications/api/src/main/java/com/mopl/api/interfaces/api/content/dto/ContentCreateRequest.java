package com.mopl.api.interfaces.api.content.dto;

import com.mopl.domain.model.content.ContentModel.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;
import static com.mopl.domain.model.playlist.PlaylistModel.DESCRIPTION_MAX_LENGTH;

@Schema(example = """
    {
      "type": "movie",
      "title": "인셉션",
      "description": "꿈속의 꿈",
      "tags": ["SF", "액션"]
    }
    """)
public record ContentCreateRequest(
    @NotNull ContentType type,
    @NotBlank @Size(max = TITLE_MAX_LENGTH) String title,
    @NotBlank @Size(max = DESCRIPTION_MAX_LENGTH) String description,
    List<@NotBlank String> tags
) {
}
