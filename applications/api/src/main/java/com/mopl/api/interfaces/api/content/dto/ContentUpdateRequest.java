package com.mopl.api.interfaces.api.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;

@Schema(
    description = "콘텐츠 수정 요청 DTO",
    example = """
        {
          "title": "인셉션",
          "description": "꿈속의 꿈",
          "tags": ["SF", "액션"]
        }
        """
)
public record ContentUpdateRequest(

    @Schema(description = "콘텐츠 제목") @Size(max = TITLE_MAX_LENGTH) String title,
    @Schema(description = "콘텐츠 설명") String description,
    @Schema(description = "콘텐츠 태그 목록") List<@NotBlank String> tags
) {
}
