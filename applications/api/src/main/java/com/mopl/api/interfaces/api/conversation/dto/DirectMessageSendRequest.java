package com.mopl.api.interfaces.api.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(example = """
    {
      "content": "안녕하세요!"
    }
    """)
public record DirectMessageSendRequest(
    @NotBlank String content
) {}
