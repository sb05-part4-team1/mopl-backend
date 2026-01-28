package com.mopl.storage.config;

import com.mopl.storage.config.StorageProperties.Local;
import com.mopl.storage.config.StorageProperties.S3;
import com.mopl.storage.config.StorageProperties.StorageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StorageProperties 단위 테스트")
class StoragePropertiesTest {

    @Nested
    @DisplayName("LOCAL 타입 검증")
    class LocalTypeValidationTest {

        @Test
        @DisplayName("유효한 LOCAL 설정으로 생성 성공")
        void createsWithValidLocalConfig() {
            // given
            Local local = new Local(Path.of("/tmp/storage"), "http://localhost:8080");

            // when
            StorageProperties properties = new StorageProperties(StorageType.LOCAL, local, null);

            // then
            assertThat(properties.type()).isEqualTo(StorageType.LOCAL);
            assertThat(properties.local()).isEqualTo(local);
        }

        @Test
        @DisplayName("LOCAL 타입인데 local이 null이면 예외 발생")
        void throwsExceptionWhenLocalIsNull() {
            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.LOCAL, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.local must not be null");
        }

        @Test
        @DisplayName("LOCAL 타입인데 rootPath가 null이면 예외 발생")
        void throwsExceptionWhenRootPathIsNull() {
            // given
            Local local = new Local(null, "http://localhost:8080");

            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.LOCAL, local, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.local.root-path must not be null");
        }

        @Test
        @DisplayName("LOCAL 타입인데 baseUrl이 비어있으면 예외 발생")
        void throwsExceptionWhenBaseUrlIsEmpty() {
            // given
            Local local = new Local(Path.of("/tmp/storage"), "");

            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.LOCAL, local, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.local.base-url must not be empty");
        }
    }

    @Nested
    @DisplayName("S3 타입 검증")
    class S3TypeValidationTest {

        private S3 validS3Config() {
            return new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );
        }

        @Test
        @DisplayName("유효한 S3 설정으로 생성 성공")
        void createsWithValidS3Config() {
            // given
            S3 s3 = validS3Config();

            // when
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);

            // then
            assertThat(properties.type()).isEqualTo(StorageType.S3);
            assertThat(properties.s3()).isEqualTo(s3);
        }

        @Test
        @DisplayName("S3 타입인데 s3가 null이면 예외 발생")
        void throwsExceptionWhenS3IsNull() {
            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.S3, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.s3 must not be null");
        }

        @Test
        @DisplayName("S3 타입에서 accessKey/secretKey는 선택적 (IAM Role 사용 시)")
        void allowsEmptyCredentialsForIamRole() {
            // given
            S3 s3 = new S3(
                "",
                "",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );

            // when
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);

            // then
            assertThat(properties.s3().accessKey()).isEmpty();
            assertThat(properties.s3().secretKey()).isEmpty();
        }

        @Test
        @DisplayName("S3 타입인데 region이 비어있으면 예외 발생")
        void throwsExceptionWhenRegionIsEmpty() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );

            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.S3, null, s3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.s3.region must not be empty");
        }

        @Test
        @DisplayName("S3 타입인데 bucket이 비어있으면 예외 발생")
        void throwsExceptionWhenBucketIsEmpty() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "",
                Duration.ofMinutes(10),
                null
            );

            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.S3, null, s3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.s3.bucket must not be empty");
        }

        @Test
        @DisplayName("S3 타입인데 presignedUrlExpiration이 null이면 예외 발생")
        void throwsExceptionWhenPresignedUrlExpirationIsNull() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                null,
                null
            );

            // when & then
            assertThatThrownBy(() -> new StorageProperties(StorageType.S3, null, s3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mopl.storage.s3.presigned-url-expiration must not be null");
        }

        @Test
        @DisplayName("S3 타입에서 endpoint는 선택적 (null 허용)")
        void allowsNullEndpoint() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );

            // when
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);

            // then
            assertThat(properties.s3().endpoint()).isNull();
        }
    }

    @Nested
    @DisplayName("StorageType 열거형")
    class StorageTypeTest {

        @Test
        @DisplayName("LOCAL 타입 존재")
        void hasLocalType() {
            assertThat(StorageType.valueOf("LOCAL")).isEqualTo(StorageType.LOCAL);
        }

        @Test
        @DisplayName("S3 타입 존재")
        void hasS3Type() {
            assertThat(StorageType.valueOf("S3")).isEqualTo(StorageType.S3);
        }
    }
}
