package com.mopl.storage.provider;

import com.mopl.domain.exception.storage.FileDeleteException;
import com.mopl.domain.exception.storage.FileNotFoundException;
import com.mopl.domain.exception.storage.FileUploadException;
import com.mopl.storage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LocalStorageProvider implements StorageProvider {

    private final StorageProperties.Local localProperties;

    @PostConstruct
    public void init() {
        try {
            if (Files.notExists(localProperties.rootPath())) {
                Files.createDirectories(localProperties.rootPath());
                log.info("로컬 스토리지 루트 디렉토리 생성: {}", localProperties.rootPath());
            }
        } catch (IOException e) {
            log.error("스토리지 초기화 실패", e);
            throw new RuntimeException("스토리지 초기화에 실패했습니다.", e);
        }
    }

    @Override
    public void upload(InputStream inputStream, long contentLength, String path) {
        Path targetPath = resolveSafePath(path);
        try (inputStream) {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 업로드 성공: path={}, size={}", targetPath, contentLength);
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", path, e);
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
                log.warn("파일을 찾을 수 없거나 읽을 수 없음: {}", filePath);
                throw FileNotFoundException.withPath(path);
            }
            return resource;
        } catch (MalformedURLException | IllegalArgumentException e) {
            log.error("잘못된 파일 경로: {}", path, e);
            throw FileNotFoundException.withPath(path);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path targetPath = resolveSafePath(path);
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", targetPath);
            } else {
                log.warn("삭제 대상 파일이 없음: {}", targetPath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", path, e);
            throw FileDeleteException.withPathAndCause(path, e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        Path targetPath = resolveSafePath(path);
        return Files.exists(targetPath);
    }

    @Override
    public List<String> listObjects(String prefix, int maxKeys) {
        Path prefixPath = resolveSafePath(prefix);
        if (!Files.isDirectory(prefixPath)) {
            prefixPath = prefixPath.getParent();
        }
        if (prefixPath == null || !Files.exists(prefixPath)) {
            return List.of();
        }

        try (Stream<Path> walk = Files.walk(prefixPath)) {
            return walk
                .filter(Files::isRegularFile)
                .map(p -> localProperties.rootPath().relativize(p).toString().replace("\\", "/"))
                .filter(p -> p.startsWith(prefix))
                .limit(maxKeys)
                .toList();
        } catch (IOException e) {
            log.error("파일 목록 조회 실패: prefix={}", prefix, e);
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
