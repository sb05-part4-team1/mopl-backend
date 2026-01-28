package com.mopl.storage.provider;

import com.mopl.domain.exception.storage.FileDeleteException;
import com.mopl.domain.exception.storage.FileNotFoundException;
import com.mopl.domain.exception.storage.FileUploadException;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LocalStorageProvider implements StorageProvider {

    private final StorageProperties.Local localProperties;

    @PostConstruct
    public void init() {
        try {
            if (Files.notExists(localProperties.rootPath())) {
                Files.createDirectories(localProperties.rootPath());
                LogContext.with("provider", "local").and("rootPath", localProperties.rootPath()).info("Root directory created");
            }
        } catch (IOException e) {
            LogContext.with("provider", "local").error("Storage initialization failed", e);
            throw new RuntimeException("스토리지 초기화에 실패했습니다.", e);
        }
    }

    @Override
    public void upload(InputStream inputStream, long contentLength, String path) {
        Path targetPath = resolveSafePath(path);
        try (inputStream) {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            LogContext.with("provider", "local").and("path", path).and("size", contentLength).info("File uploaded");
        } catch (IOException e) {
            LogContext.with("provider", "local").and("path", path).error("File upload failed", e);
            throw FileUploadException.withPathAndCause(path, e.getMessage());
        }
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        return String.format("%s/api/files/display?path=%s", localProperties.baseUrl(), encodedPath);
    }

    @Override
    public Resource download(String path) {
        try {
            Path filePath = resolveSafePath(path);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                LogContext.with("provider", "local").and("path", path).warn("File not found or not readable");
                throw FileNotFoundException.withPath(path);
            }
            return resource;
        } catch (MalformedURLException | IllegalArgumentException e) {
            LogContext.with("provider", "local").and("path", path).error("Invalid file path", e);
            throw FileNotFoundException.withPath(path);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path targetPath = resolveSafePath(path);
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                LogContext.with("provider", "local").and("path", path).info("File deleted");
            } else {
                LogContext.with("provider", "local").and("path", path).warn("File not found for deletion");
            }
        } catch (IOException e) {
            LogContext.with("provider", "local").and("path", path).error("File delete failed", e);
            throw FileDeleteException.withPathAndCause(path, e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        Path targetPath = resolveSafePath(path);
        return Files.exists(targetPath);
    }

    @Override
    public List<String> listObjects(String prefix, String startAfter, int maxKeys) {
        Path prefixPath = resolveSafePath(prefix);
        if (!Files.isDirectory(prefixPath)) {
            prefixPath = prefixPath.getParent();
        }
        if (prefixPath == null || !Files.exists(prefixPath)) {
            return List.of();
        }

        try (Stream<Path> walk = Files.walk(prefixPath)) {
            Stream<String> stream = walk
                .filter(Files::isRegularFile)
                .map(p -> localProperties.rootPath().relativize(p).toString().replace("\\", "/"))
                .filter(p -> p.startsWith(prefix))
                .sorted();

            if (startAfter != null && !startAfter.isBlank()) {
                stream = stream.filter(p -> p.compareTo(startAfter) > 0);
            }

            return stream.limit(maxKeys).toList();
        } catch (IOException e) {
            LogContext.with("provider", "local").and("prefix", prefix).error("File list failed", e);
            return List.of();
        }
    }

    private Path resolveSafePath(String path) {
        Path resolved = localProperties.rootPath().resolve(path).normalize();
        if (!resolved.startsWith(localProperties.rootPath())) {
            throw new IllegalArgumentException("잘못된 파일 경로: " + path);
        }
        return resolved;
    }
}
