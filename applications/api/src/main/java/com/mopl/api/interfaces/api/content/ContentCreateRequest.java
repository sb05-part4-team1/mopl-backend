package com.mopl.api.interfaces.api.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TYPE_MAX_LENGTH;

@Schema(
    description = "콘텐츠 생성 요청 DTO",
    example = """
        {
          "type": "영화",
          "title": "인셉션",
          "description": "꿈속의 꿈",
          "tags": ["SF", "액션"]
        }
        """
)
public record ContentCreateRequest(
    @NotBlank @Size(max = TYPE_MAX_LENGTH) String type,
    @NotBlank @Size(max = TITLE_MAX_LENGTH) String title,
    @NotBlank String description,
    @NotEmpty List<@NotBlank String> tags
) {
}
