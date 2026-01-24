package com.mopl.storage.config;

import com.mopl.storage.config.StorageProperties.Local;
import com.mopl.storage.config.StorageProperties.S3;
import com.mopl.storage.config.StorageProperties.StorageType;
import com.mopl.storage.provider.LocalStorageProvider;
import com.mopl.storage.provider.S3StorageProvider;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StorageAutoConfig 단위 테스트")
class StorageAutoConfigTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("localStorageProvider()")
    class LocalStorageProviderBeanTest {

        @Test
        @DisplayName("LocalStorageProvider 빈 생성")
        void createsLocalStorageProviderBean() {
            // given
            Local local = new Local(tempDir, "http://localhost:8080");
            StorageProperties properties = new StorageProperties(StorageType.LOCAL, local, null);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            // when
            StorageProvider provider = config.localStorageProvider();

            // then
            assertThat(provider).isInstanceOf(LocalStorageProvider.class);
        }
    }

    @Nested
    @DisplayName("s3StorageProvider()")
    class S3StorageProviderBeanTest {

        @Test
        @DisplayName("S3StorageProvider 빈 생성")
        void createsS3StorageProviderBean() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                "http://localhost:4566"
            );
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            S3Client s3Client = config.s3Client();
            S3Presigner s3Presigner = config.s3Presigner();

            // when
            StorageProvider provider = config.s3StorageProvider(s3Client, s3Presigner);

            // then
            assertThat(provider).isInstanceOf(S3StorageProvider.class);

            // cleanup
            s3Client.close();
            s3Presigner.close();
        }
    }

    @Nested
    @DisplayName("s3Client()")
    class S3ClientBeanTest {

        @Test
        @DisplayName("S3Client 빈 생성 (엔드포인트 없음)")
        void createsS3ClientWithoutEndpoint() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            // when
            S3Client client = config.s3Client();

            // then
            assertThat(client).isNotNull();

            // cleanup
            client.close();
        }

        @Test
        @DisplayName("S3Client 빈 생성 (커스텀 엔드포인트)")
        void createsS3ClientWithEndpoint() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                "http://localhost:4566"
            );
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            // when
            S3Client client = config.s3Client();

            // then
            assertThat(client).isNotNull();

            // cleanup
            client.close();
        }
    }

    @Nested
    @DisplayName("s3Presigner()")
    class S3PresignerBeanTest {

        @Test
        @DisplayName("S3Presigner 빈 생성 (엔드포인트 없음)")
        void createsS3PresignerWithoutEndpoint() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                null
            );
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            // when
            S3Presigner presigner = config.s3Presigner();

            // then
            assertThat(presigner).isNotNull();

            // cleanup
            presigner.close();
        }

        @Test
        @DisplayName("S3Presigner 빈 생성 (커스텀 엔드포인트)")
        void createsS3PresignerWithEndpoint() {
            // given
            S3 s3 = new S3(
                "accessKey",
                "secretKey",
                "ap-northeast-2",
                "test-bucket",
                Duration.ofMinutes(10),
                "http://localhost:4566"
            );
            StorageProperties properties = new StorageProperties(StorageType.S3, null, s3);
            StorageAutoConfig config = new StorageAutoConfig(properties);

            // when
            S3Presigner presigner = config.s3Presigner();

            // then
            assertThat(presigner).isNotNull();

            // cleanup
            presigner.close();
        }
    }
}
