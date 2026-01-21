package com.mopl.api.interfaces.api.user;

import com.mopl.domain.support.cursor.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record UserCursorResponse(
    @Schema(description = "유저 목록") List<UserResponse> data,

    @Schema(example = "string") String nextCursor,

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID nextIdAfter,

    @Schema(example = "true") boolean hasNext,

    @Schema(example = "0") long totalCount,

    @Schema(example = "string") String sortBy,

    @Schema(example = "ASCENDING") SortDirection sortDirection
) {

}
