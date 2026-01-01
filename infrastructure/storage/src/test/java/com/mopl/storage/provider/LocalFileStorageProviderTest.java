package com.mopl.storage.provider;

import com.mopl.storage.config.LocalStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("로컬 파일 스토리지 프로바이더 테스트")
class LocalFileStorageProviderTest {

    private LocalFileStorageProvider storageProvider;
    private final String baseUrl = "http://localhost:8080";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(tempDir, baseUrl);
        storageProvider = new LocalFileStorageProvider(properties);
        storageProvider.init();
    }

    @Nested
    @DisplayName("파일 업로드 테스트")
    class UploadTest {
        @Test
        @DisplayName("정상적인 파일과 경로가 주어지면 업로드에 성공한다")
        void upload_Success() {
            // given
            String content = "test content";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            String relativePath = "sub/test.txt";

            // when
            String resultPath = storageProvider.upload(inputStream, relativePath);

            // then
            assertThat(resultPath).isEqualTo(relativePath);
            assertThat(tempDir.resolve(relativePath)).exists();
        }

        @Test
        @DisplayName("잘못된 스트림이 주어지면 런타임 예외가 발생한다")
        void upload_Fail_InvalidStream() throws IOException {
            // given
            InputStream exceptionStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("강제 발생 에러");
                }
            };

            // when & then
            assertThatThrownBy(() -> storageProvider.upload(exceptionStream, "fail.txt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 저장 중 오류가 발생했습니다.");
        }
    }

    @Nested
    @DisplayName("파일 로드 테스트")
    class LoadTest {
        @Test
        @DisplayName("존재하는 파일을 로드하면 Resource 객체를 반환한다")
        void load_Success() throws IOException {
            // given
            String relativePath = "load-test.txt";
            Path file = tempDir.resolve(relativePath);
            Files.writeString(file, "hello");

            // when
            Resource resource = storageProvider.load(relativePath);

            // then
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
            assertThat(resource.getFilename()).isEqualTo(relativePath);
        }

        @Test
        @DisplayName("존재하지 않는 파일을 로드하면 예외가 발생한다")
        void load_Fail_NotFound() {
            // when & then
            assertThatThrownBy(() -> storageProvider.load("non-existent.jpg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("URL 생성 및 삭제 테스트")
    class UrlAndDeleteTest {
        @Test
        @DisplayName("상대 경로가 주어지면 전체 접근 URL을 생성한다")
        void getUrl_Success() {
            // given
            String relativePath = "images/sample.png";

            // when
            String url = storageProvider.getUrl(relativePath);

            // then
            assertThat(url).isEqualTo(baseUrl + "/api/v1/files/display?path=" + relativePath);
        }

        @Test
        @DisplayName("빈 경로가 주어지면 null을 반환한다")
        void getUrl_EmptyPath() {
            assertThat(storageProvider.getUrl("")).isNull();
            assertThat(storageProvider.getUrl(null)).isNull();
        }

        @Test
        @DisplayName("파일 삭제 요청 시 실제 파일이 제거된다")
        void delete_Success() throws IOException {
            // given
            String relativePath = "delete-test.txt";
            Files.createFile(tempDir.resolve(relativePath));

            // when
            storageProvider.delete(relativePath);

            // then
            assertThat(tempDir.resolve(relativePath)).doesNotExist();
        }
    }
}