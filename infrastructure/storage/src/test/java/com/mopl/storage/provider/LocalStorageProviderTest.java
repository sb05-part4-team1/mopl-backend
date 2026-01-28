package com.mopl.storage.provider;

import com.mopl.domain.exception.storage.FileDeleteException;
import com.mopl.domain.exception.storage.FileNotFoundException;
import com.mopl.domain.exception.storage.FileUploadException;
import com.mopl.storage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LocalStorageProvider 단위 테스트")
class LocalStorageProviderTest {

    private LocalStorageProvider storageProvider;
    private static final String BASE_URL = "http://localhost:8080";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorageProperties.Local localProperties = new StorageProperties.Local(tempDir, BASE_URL);
        storageProvider = new LocalStorageProvider(localProperties);
    }

    @Nested
    @DisplayName("init()")
    class InitTest {

        @Test
        @DisplayName("루트 디렉토리가 없으면 생성")
        void createsRootDirectoryIfNotExists() {
            // given
            Path newRootPath = tempDir.resolve("new-root");
            StorageProperties.Local localProperties = new StorageProperties.Local(newRootPath, BASE_URL);
            LocalStorageProvider provider = new LocalStorageProvider(localProperties);

            // when
            provider.init();

            // then
            assertThat(Files.exists(newRootPath)).isTrue();
        }

        @Test
        @DisplayName("루트 디렉토리가 이미 존재하면 유지")
        void keepsExistingRootDirectory() throws IOException {
            // given
            Path existingRoot = tempDir.resolve("existing-root");
            Files.createDirectories(existingRoot);
            StorageProperties.Local localProperties = new StorageProperties.Local(existingRoot, BASE_URL);
            LocalStorageProvider provider = new LocalStorageProvider(localProperties);

            // when
            provider.init();

            // then
            assertThat(Files.exists(existingRoot)).isTrue();
        }

        @Test
        @DisplayName("디렉토리 생성 실패 시 RuntimeException 발생")
        @DisabledOnOs(OS.WINDOWS)
        void throwsExceptionOnDirectoryCreationFailure() throws IOException {
            // given
            Path readOnlyDir = tempDir.resolve("readonly");
            Files.createDirectories(readOnlyDir);
            Files.setPosixFilePermissions(readOnlyDir, PosixFilePermissions.fromString("r-xr-xr-x"));

            Path targetPath = readOnlyDir.resolve("cannot-create");
            StorageProperties.Local localProperties = new StorageProperties.Local(targetPath, BASE_URL);
            LocalStorageProvider provider = new LocalStorageProvider(localProperties);

            // when & then
            try {
                assertThatThrownBy(provider::init)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("스토리지 초기화에 실패했습니다.");
            } finally {
                Files.setPosixFilePermissions(readOnlyDir, PosixFilePermissions.fromString("rwxr-xr-x"));
            }
        }
    }

    @Nested
    @DisplayName("upload()")
    class UploadTest {

        @Test
        @DisplayName("파일 업로드 성공")
        void uploadsFileSuccessfully() throws IOException {
            // given
            String content = "test content";
            String path = "sub/test.txt";

            // when
            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                storageProvider.upload(inputStream, content.length(), path);
            }

            // then
            Path uploadedFile = tempDir.resolve(path);
            assertThat(Files.exists(uploadedFile)).isTrue();
            assertThat(Files.readString(uploadedFile)).isEqualTo(content);
        }

        @Test
        @DisplayName("중첩 디렉토리 경로에 파일 업로드 성공")
        void uploadsToNestedDirectory() throws IOException {
            // given
            String content = "nested content";
            String path = "a/b/c/deep.txt";

            // when
            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                storageProvider.upload(inputStream, content.length(), path);
            }

            // then
            Path uploadedFile = tempDir.resolve(path);
            assertThat(Files.exists(uploadedFile)).isTrue();
        }

        @Test
        @DisplayName("스트림 읽기 실패 시 FileUploadException 발생")
        void throwsExceptionOnStreamError() throws IOException {
            // given & when & then
            try (InputStream exceptionStream = new InputStream() {

                @Override
                public int read() throws IOException {
                    throw new IOException("강제 에러");
                }
            }) {
                assertThatThrownBy(() -> storageProvider.upload(exceptionStream, 100, "fail.txt"))
                    .isInstanceOf(FileUploadException.class);
            }
        }

        @Test
        @DisplayName("경로 탈출 시도 시 예외 발생")
        void throwsExceptionOnPathTraversal() {
            // given
            String content = "malicious";

            // when & then
            assertThatThrownBy(() -> {
                try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                    storageProvider.upload(inputStream, content.length(), "../escape.txt");
                }
            })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 파일 경로");
        }
    }

    @Nested
    @DisplayName("getUrl()")
    class GetUrlTest {

        @Test
        @DisplayName("유효한 경로로 URL 생성")
        void generatesUrlForValidPath() {
            // given
            String path = "images/sample.png";

            // when
            String url = storageProvider.getUrl(path);

            // then
            assertThat(url).contains(BASE_URL);
            assertThat(url).contains("/api/files/display?path=");
            assertThat(url).contains("images%2Fsample.png");
        }

        @Test
        @DisplayName("null 경로면 null 반환")
        void returnsNullForNullPath() {
            assertThat(storageProvider.getUrl(null)).isNull();
        }

        @Test
        @DisplayName("빈 경로면 null 반환")
        void returnsNullForEmptyPath() {
            assertThat(storageProvider.getUrl("")).isNull();
            assertThat(storageProvider.getUrl("   ")).isNull();
        }
    }

    @Nested
    @DisplayName("download()")
    class DownloadTest {

        @Test
        @DisplayName("존재하는 파일 다운로드 성공")
        void downloadsExistingFile() throws IOException {
            // given
            String path = "download-test.txt";
            String content = "download content";
            Files.writeString(tempDir.resolve(path), content);

            // when
            Resource resource = storageProvider.download(path);

            // then
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 파일 다운로드 시 FileNotFoundException 발생")
        void throwsExceptionForNonExistingFile() {
            // when & then
            assertThatThrownBy(() -> storageProvider.download("non-existent.txt"))
                .isInstanceOf(FileNotFoundException.class);
        }

        @Test
        @DisplayName("읽을 수 없는 파일 다운로드 시 FileNotFoundException 발생")
        @DisabledOnOs(OS.WINDOWS)
        void throwsExceptionForUnreadableFile() throws IOException {
            // given
            String path = "unreadable-file.txt";
            Path filePath = tempDir.resolve(path);
            Files.writeString(filePath, "content");
            Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString("---------"));

            // when & then
            try {
                assertThatThrownBy(() -> storageProvider.download(path))
                    .isInstanceOf(FileNotFoundException.class);
            } finally {
                Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString("rw-r--r--"));
            }
        }

        @Test
        @DisplayName("경로 탈출 시도 시 예외 발생")
        void throwsExceptionOnPathTraversal() {
            // when & then
            assertThatThrownBy(() -> storageProvider.download("../escape.txt"))
                .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("존재하는 파일 삭제 성공")
        void deletesExistingFile() throws IOException {
            // given
            String path = "delete-test.txt";
            Files.createFile(tempDir.resolve(path));

            // when
            storageProvider.delete(path);

            // then
            assertThat(Files.exists(tempDir.resolve(path))).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 파일 삭제 시 예외 없이 완료")
        void handlesNonExistingFileGracefully() {
            // when & then - 예외 발생하지 않음
            storageProvider.delete("non-existent.txt");
        }

        @Test
        @DisplayName("경로 탈출 시도 시 예외 발생")
        void throwsExceptionOnPathTraversal() {
            // when & then
            assertThatThrownBy(() -> storageProvider.delete("../escape.txt"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("파일 삭제 중 IOException 발생 시 FileDeleteException 발생")
        @DisabledOnOs(OS.WINDOWS)
        void throwsExceptionOnDeleteFailure() throws IOException {
            // given
            Path subDir = tempDir.resolve("locked-dir");
            Files.createDirectories(subDir);
            Path file = subDir.resolve("locked-file.txt");
            Files.writeString(file, "content");

            // 디렉토리를 읽기 전용으로 설정하여 파일 삭제 불가하게 만듦
            Files.setPosixFilePermissions(subDir, PosixFilePermissions.fromString("r-xr-xr-x"));

            // when & then
            try {
                assertThatThrownBy(() -> storageProvider.delete("locked-dir/locked-file.txt"))
                    .isInstanceOf(FileDeleteException.class);
            } finally {
                Files.setPosixFilePermissions(subDir, PosixFilePermissions.fromString("rwxr-xr-x"));
                Files.deleteIfExists(file);
            }
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("존재하는 파일이면 true 반환")
        void returnsTrueForExistingFile() throws IOException {
            // given
            String path = "exists-test.txt";
            Files.createFile(tempDir.resolve(path));

            // when
            boolean result = storageProvider.exists(path);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 파일이면 false 반환")
        void returnsFalseForNonExistingFile() {
            // when
            boolean result = storageProvider.exists("non-existent.txt");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("listObjects()")
    class ListObjectsTest {

        @Test
        @DisplayName("prefix에 해당하는 파일 목록 반환")
        void listsFilesWithPrefix() throws IOException {
            // given
            Path subDir = tempDir.resolve("images");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("a.png"));
            Files.createFile(subDir.resolve("b.png"));
            Files.createFile(subDir.resolve("c.png"));

            // when
            var result = storageProvider.listObjects("images/", null, 10);

            // then
            assertThat(result).containsExactly("images/a.png", "images/b.png", "images/c.png");
        }

        @Test
        @DisplayName("startAfter 이후 파일만 반환")
        void listsFilesAfterStartAfter() throws IOException {
            // given
            Path subDir = tempDir.resolve("files");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("1.txt"));
            Files.createFile(subDir.resolve("2.txt"));
            Files.createFile(subDir.resolve("3.txt"));

            // when
            var result = storageProvider.listObjects("files/", "files/1.txt", 10);

            // then
            assertThat(result).containsExactly("files/2.txt", "files/3.txt");
        }

        @Test
        @DisplayName("maxKeys만큼만 반환")
        void limitsResultByMaxKeys() throws IOException {
            // given
            Path subDir = tempDir.resolve("limited");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("1.txt"));
            Files.createFile(subDir.resolve("2.txt"));
            Files.createFile(subDir.resolve("3.txt"));

            // when
            var result = storageProvider.listObjects("limited/", null, 2);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 디렉토리면 빈 리스트 반환")
        void returnsEmptyListForNonExistingDirectory() {
            // when
            var result = storageProvider.listObjects("non-existent/", null, 10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("파일 경로를 prefix로 사용하면 부모 디렉토리 기준으로 검색")
        void usesParentDirectoryWhenPrefixIsFile() throws IOException {
            // given
            Path subDir = tempDir.resolve("docs");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("readme.md"));

            // when
            var result = storageProvider.listObjects("docs/readme.md", null, 10);

            // then
            assertThat(result).containsExactly("docs/readme.md");
        }

        @Test
        @DisplayName("startAfter가 빈 문자열이면 무시")
        void ignoresEmptyStartAfter() throws IOException {
            // given
            Path subDir = tempDir.resolve("blank-test");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("1.txt"));
            Files.createFile(subDir.resolve("2.txt"));

            // when
            var result = storageProvider.listObjects("blank-test/", "", 10);

            // then
            assertThat(result).containsExactly("blank-test/1.txt", "blank-test/2.txt");
        }

        @Test
        @DisplayName("startAfter가 공백 문자열이면 무시")
        void ignoresBlankStartAfter() throws IOException {
            // given
            Path subDir = tempDir.resolve("whitespace-test");
            Files.createDirectories(subDir);
            Files.createFile(subDir.resolve("a.txt"));

            // when
            var result = storageProvider.listObjects("whitespace-test/", "   ", 10);

            // then
            assertThat(result).containsExactly("whitespace-test/a.txt");
        }

        @Test
        @DisplayName("prefix의 부모 디렉토리가 존재하지 않으면 빈 리스트 반환")
        void returnsEmptyListWhenParentDirectoryNotExists() {
            // when - 존재하지 않는 경로의 파일을 prefix로 사용
            var result = storageProvider.listObjects("non-existent-dir/file.txt", null, 10);

            // then
            assertThat(result).isEmpty();
        }
    }
}
