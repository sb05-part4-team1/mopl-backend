package com.mopl.storage.provider;

import com.mopl.storage.config.LocalStorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.storage.type", havingValue = "local")
public class LocalFileStorageProvider implements FileStorageProvider {

    private final LocalStorageProperties properties;

    @PostConstruct
    public void init() {
        try {
            if (Files.notExists(properties.rootPath())) {
                Files.createDirectories(properties.rootPath());
                log.info("로컬 스토리지 루트 디렉토리 생성: {}", properties.rootPath());
            }
        } catch (IOException e) {
            log.error("스토리지 초기화 실패", e);
            throw new RuntimeException("스토리지 초기화에 실패했습니다.", e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String relativePath) {
        Path targetPath = properties.rootPath().resolve(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 업로드 성공: {}", targetPath);
            return relativePath;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", relativePath, e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String getUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        return String.format("%s/api/v1/files/display?path=%s", properties.baseUrl(), relativePath);
    }

    @Override
    public Resource load(String relativePath) {
        try {
            Path filePath = properties.rootPath().resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없거나 읽을 수 없음: {}", filePath);
                throw new RuntimeException("파일을 찾을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            log.error("잘못된 파일 경로: {}", relativePath, e);
            throw new RuntimeException("잘못된 파일 경로입니다.", e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path targetPath = properties.rootPath().resolve(relativePath);
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", targetPath);
            } else {
                log.warn("삭제 대상 파일이 없음: {}", targetPath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", relativePath, e);
            throw new RuntimeException("파일 삭제 실패: " + relativePath, e);
        }
    }

}
