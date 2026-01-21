package com.mopl.api.interfaces.api.conversation;

import com.mopl.domain.support.cursor.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "메시지 목록 커서 응답")
public record DirectMessageCursorResponse(
    @Schema(description = "대화 목록") List<DirectMessageResponse> data,

    @Schema(example = "string") String nextCursor,

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID nextIdAfter,

    @Schema(example = "true") boolean hasNext,

    @Schema(example = "0") long totalCount,

    @Schema(example = "string") String sortBy,

    @Schema(example = "ASCENDING") SortDirection sortDirection
) {

}
