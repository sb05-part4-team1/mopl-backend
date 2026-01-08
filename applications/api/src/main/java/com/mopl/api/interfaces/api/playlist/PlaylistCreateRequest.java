package com.mopl.api.interfaces.api.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistCreateRequest(

    @NotBlank @Size(max = 255) String title,

    @Size(max = 10_000) String description
) {
}
