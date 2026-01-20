package com.mopl.api.interfaces.api.admin;

import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.scheduler.PlaylistSubscriberCountSyncScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AdminController 단위 테스트")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaylistSubscriberCountSyncScheduler playlistSubscriberCountSyncScheduler;

    @Nested
    @DisplayName("POST /api/admin/sync/playlist-subscriber-counts")
    class SyncAllPlaylistSubscriberCountsTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 권한으로 전체 동기화 요청 시 204 반환")
        void withAdminRole_returns204() throws Exception {
            // when & then
            mockMvc.perform(post("/api/admin/sync/playlist-subscriber-counts"))
                .andExpect(status().isNoContent());

            then(playlistSubscriberCountSyncScheduler).should().syncSubscriberCounts();
        }

    }

    @Nested
    @DisplayName("POST /api/admin/sync/playlist-subscriber-counts/{playlistId}")
    class SyncPlaylistSubscriberCountTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 권한으로 특정 플레이리스트 동기화 요청 시 204 반환")
        void withAdminRole_returns204() throws Exception {
            // given
            UUID playlistId = UUID.randomUUID();

            // when & then
            mockMvc.perform(post("/api/admin/sync/playlist-subscriber-counts/{playlistId}",
                playlistId))
                .andExpect(status().isNoContent());

            then(playlistSubscriberCountSyncScheduler).should().syncSubscriberCount(playlistId);
        }
    }
}
