package com.mopl.api.interfaces.api.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;

@Schema(example = """
    {
      "title": "인셉션",
      "description": "꿈속의 꿈",
      "tags": ["SF", "액션"]
    }
    """)
public record ContentUpdateRequest(
    @Size(max = TITLE_MAX_LENGTH) String title,
    String description,
    List<@NotBlank String> tags
) {
}
