package com.mopl.api.interfaces.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Admin API", description = "관리자 API")
public interface AdminApiSpec {

    @Operation(
        summary = "[관리자]모든 플레이리스트 구독자 수 동기화 (전체)"
    )
    public void syncAllPlaylistSubscriberCounts();

    @Operation(
        summary = "[관리자]특정 플레이리스트 구독자 수 동기화"
    )
    public void syncPlaylistSubscriberCount(@PathVariable UUID playlistId);

}
