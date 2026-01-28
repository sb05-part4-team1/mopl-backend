package com.mopl.storage.provider;

import com.mopl.domain.exception.storage.FileDeleteException;
import com.mopl.domain.exception.storage.FileNotFoundException;
import com.mopl.domain.exception.storage.FileUploadException;
import com.mopl.storage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3StorageProvider 단위 테스트")
class S3StorageProviderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3StorageProvider storageProvider;

    private static final String BUCKET = "test-bucket";
    private static final String REGION = "ap-northeast-2";
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        StorageProperties.S3 s3Properties = new StorageProperties.S3(
            "accessKey",
            "secretKey",
            REGION,
            BUCKET,
            PRESIGNED_URL_EXPIRATION,
            null
        );
        storageProvider = new S3StorageProvider(s3Properties, s3Client, s3Presigner);
    }

    @Nested
    @DisplayName("upload()")
    class UploadTest {

        @Test
        @DisplayName("파일 업로드 성공")
        void uploadsFileSuccessfully() {
            // given
            String content = "test content";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            String path = "images/test.png";

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

            // when
            storageProvider.upload(inputStream, content.length(), path);

            // then
            then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("S3 업로드 실패 시 FileUploadException 발생")
        void throwsExceptionOnS3Error() {
            // given
            String content = "test content";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            String path = "images/test.png";

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(S3Exception.builder().message("S3 error").build());

            // when & then
            assertThatThrownBy(() -> storageProvider.upload(inputStream, content.length(), path))
                .isInstanceOf(FileUploadException.class);
        }
    }

    @Nested
    @DisplayName("getUrl()")
    class GetUrlTest {

        @Test
        @DisplayName("Presigned URL 생성 성공")
        void generatesPresignedUrl() throws Exception {
            // given
            String path = "images/test.png";
            String expectedUrl = "https://test-bucket.s3.amazonaws.com/images/test.png?signed=true";

            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            given(presignedRequest.url()).willReturn(URI.create(expectedUrl).toURL());
            given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

            // when
            String url = storageProvider.getUrl(path);

            // then
            assertThat(url).isEqualTo(expectedUrl);
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
        @DisplayName("파일 다운로드 성공")
        void downloadsFileSuccessfully() {
            // given
            String path = "images/test.png";

            @SuppressWarnings("unchecked") ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);

            given(s3Client.getObject(any(GetObjectRequest.class)))
                .willReturn(responseInputStream);

            // when
            Resource resource = storageProvider.download(path);

            // then
            assertThat(resource).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 파일 다운로드 시 FileNotFoundException 발생")
        void throwsExceptionForNonExistingFile() {
            // given
            String path = "non-existent.png";

            given(s3Client.getObject(any(GetObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("Key not found").build());

            // when & then
            assertThatThrownBy(() -> storageProvider.download(path))
                .isInstanceOf(FileNotFoundException.class);
        }

        @Test
        @DisplayName("S3 오류 발생 시 FileNotFoundException 발생")
        void throwsExceptionOnS3Error() {
            // given
            String path = "error.png";

            given(s3Client.getObject(any(GetObjectRequest.class)))
                .willThrow(S3Exception.builder().message("S3 error").build());

            // when & then
            assertThatThrownBy(() -> storageProvider.download(path))
                .isInstanceOf(FileNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("파일 삭제 성공")
        void deletesFileSuccessfully() {
            // given
            String path = "images/test.png";

            // when
            storageProvider.delete(path);

            // then
            then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("S3 삭제 오류 시 FileDeleteException 발생")
        void throwsExceptionOnS3Error() {
            // given
            String path = "images/test.png";

            willThrow(S3Exception.builder().message("S3 error").build())
                .given(s3Client).deleteObject(any(DeleteObjectRequest.class));

            // when & then
            assertThatThrownBy(() -> storageProvider.delete(path))
                .isInstanceOf(FileDeleteException.class);
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("존재하는 파일이면 true 반환")
        void returnsTrueForExistingFile() {
            // given
            String path = "images/test.png";

            given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build());

            // when
            boolean result = storageProvider.exists(path);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 파일이면 false 반환")
        void returnsFalseForNonExistingFile() {
            // given
            String path = "non-existent.png";

            given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("Key not found").build());

            // when
            boolean result = storageProvider.exists(path);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("listObjects()")
    class ListObjectsTest {

        @Test
        @DisplayName("prefix에 해당하는 파일 목록 반환")
        void listsObjectsWithPrefix() {
            // given
            String prefix = "images/";
            ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(
                    S3Object.builder().key("images/a.png").build(),
                    S3Object.builder().key("images/b.png").build()
                )
                .build();

            given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .willReturn(response);

            // when
            var result = storageProvider.listObjects(prefix, null, 10);

            // then
            assertThat(result).containsExactly("images/a.png", "images/b.png");
        }

        @Test
        @DisplayName("startAfter 파라미터가 전달됨")
        void passesStartAfterParameter() {
            // given
            ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("files/2.txt").build())
                .build();

            given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .willReturn(response);

            // when
            var result = storageProvider.listObjects("files/", "files/1.txt", 10);

            // then
            assertThat(result).containsExactly("files/2.txt");
            then(s3Client).should().listObjectsV2(any(ListObjectsV2Request.class));
        }

        @Test
        @DisplayName("빈 결과 반환")
        void returnsEmptyListWhenNoObjects() {
            // given
            ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(List.of())
                .build();

            given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .willReturn(response);

            // when
            var result = storageProvider.listObjects("non-existent/", null, 10);

            // then
            assertThat(result).isEmpty();
        }
    }
}
