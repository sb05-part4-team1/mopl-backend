package com.mopl.api.interfaces.api.storage;

import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileController.class)
@Import({
    ApiControllerAdvice.class,
    TestSecurityConfig.class
})
@DisplayName("FileController 슬라이스 테스트")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageProvider storageProvider;

    @Nested
    @DisplayName("GET /api/files/display - 파일 조회")
    class DisplayFileTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답과 이미지 반환")
        void withValidPath_returns200OKWithImage() throws Exception {
            // given
            String path = "images/test-image.png";
            byte[] imageBytes = new byte[]{0x00, 0x01, 0x02, 0x03};
            Resource resource = new ByteArrayResource(imageBytes);

            given(storageProvider.download(path)).willReturn(resource);

            // when & then
            mockMvc.perform(get("/api/files/display")
                    .param("path", path))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageBytes));

            then(storageProvider).should().download(path);
        }

        @Test
        @DisplayName("path 파라미터가 없으면 400 Bad Request 응답")
        void withoutPath_returns400BadRequest() throws Exception {
            // when & then
            mockMvc.perform(get("/api/files/display"))
                .andExpect(status().isBadRequest());
        }
    }
}
