package com.mopl.batch.cleanup.orphanstorage.service;

import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupProperties;
import com.mopl.jpa.repository.content.JpaContentRepository;
import com.mopl.jpa.repository.user.JpaUserRepository;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrphanStorageCleanupService {

    private static final String CONTENT_THUMBNAIL_PREFIX = "contents/";
    private static final String USER_PROFILE_IMAGE_PREFIX = "users/";
    private static final int MAX_ITERATIONS = 10000;

    private final StorageProvider storageProvider;
    private final JpaContentRepository contentRepository;
    private final JpaUserRepository userRepository;
    private final OrphanStorageCleanupProperties props;
    private final OrphanStorageCleanupPolicyResolver policyResolver;

    public int cleanupContentThumbnails() {
        int totalDeleted = 0;
        int iterations = 0;
        int chunkSize = policyResolver.chunkSize(props.contentThumbnail());

        while (iterations < MAX_ITERATIONS) {
            List<String> storagePaths = storageProvider.listObjects(CONTENT_THUMBNAIL_PREFIX, chunkSize);

            if (storagePaths.isEmpty()) {
                break;
            }

            int deleted = 0;
            for (String path : storagePaths) {
                if (contentRepository.findOneByThumbnailPath(path) == null) {
                    storageProvider.delete(path);
                    deleted++;
                    log.info("[OrphanStorageCleanup] deleted orphan content thumbnail: {}", path);
                }
            }

            if (storagePaths.size() < chunkSize) {
                totalDeleted += deleted;
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[OrphanStorageCleanup] contentThumbnail reached max iterations={}, totalDeleted={}",
                MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }

    public int cleanupUserProfileImages() {
        int totalDeleted = 0;
        int iterations = 0;
        int chunkSize = policyResolver.chunkSize(props.userProfileImage());

        while (iterations < MAX_ITERATIONS) {
            List<String> storagePaths = storageProvider.listObjects(USER_PROFILE_IMAGE_PREFIX, chunkSize);

            if (storagePaths.isEmpty()) {
                break;
            }

            int deleted = 0;
            for (String path : storagePaths) {
                if (userRepository.findOneByProfileImagePath(path) == null) {
                    storageProvider.delete(path);
                    deleted++;
                    log.info("[OrphanStorageCleanup] deleted orphan user profile image: {}", path);
                }
            }

            if (storagePaths.size() < chunkSize) {
                totalDeleted += deleted;
                break;
            }

            totalDeleted += deleted;
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[OrphanStorageCleanup] userProfileImage reached max iterations={}, totalDeleted={}",
                MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }
}
