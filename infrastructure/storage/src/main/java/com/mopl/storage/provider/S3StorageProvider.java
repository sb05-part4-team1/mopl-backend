package com.mopl.storage.provider;

import com.mopl.domain.exception.storage.FileDeleteException;
import com.mopl.domain.exception.storage.FileNotFoundException;
import com.mopl.domain.exception.storage.FileUploadException;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
public class S3StorageProvider implements StorageProvider {

    private final StorageProperties.S3 s3Properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    @CacheEvict(cacheNames = CacheName.PRESIGNED_URLS, key = "#path")
    public void upload(InputStream inputStream, long contentLength, String path) {
        try (inputStream) {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(path)
                .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            LogContext.with("provider", "s3").and("path", path).and("size", contentLength).info("File uploaded");
        } catch (Exception e) {
            LogContext.with("provider", "s3").and("path", path).error("File upload failed", e);
            throw FileUploadException.withPathAndCause(path, e.getMessage());
        }
    }

    @Override
    @Cacheable(
        cacheNames = CacheName.PRESIGNED_URLS,
        key = "#path",
        condition = "#path != null && !#path.isBlank()"
    )
    public String getUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Properties.bucket())
            .key(path)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(s3Properties.presignedUrlExpiration())
            .getObjectRequest(getObjectRequest)
            .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public Resource download(String path) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(path)
                .build();

            InputStream inputStream = s3Client.getObject(request);
            return new InputStreamResource(inputStream);
        } catch (NoSuchKeyException e) {
            LogContext.with("provider", "s3").and("path", path).warn("File not found");
            throw FileNotFoundException.withPath(path);
        } catch (S3Exception e) {
            LogContext.with("provider", "s3").and("path", path).error("File download failed", e);
            throw FileNotFoundException.withPath(path);
        }
    }

    @Override
    @CacheEvict(cacheNames = CacheName.PRESIGNED_URLS, key = "#path")
    public void delete(String path) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(path)
                .build();

            s3Client.deleteObject(request);
            LogContext.with("provider", "s3").and("path", path).info("File deleted");
        } catch (S3Exception e) {
            LogContext.with("provider", "s3").and("path", path).error("File delete failed", e);
            throw FileDeleteException.withPathAndCause(path, e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(path)
                .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public List<String> listObjects(String prefix, int maxKeys) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(s3Properties.bucket())
            .prefix(prefix)
            .maxKeys(maxKeys)
            .build();

        return s3Client.listObjectsV2(request).contents().stream()
            .map(S3Object::key)
            .toList();
    }
}
