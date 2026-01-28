package com.mopl.batch.cleanup.orphanstorage.service;

import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupProperties;
import com.mopl.jpa.repository.orphanstorage.JpaOrphanStorageCleanupRepository;
import com.mopl.logging.context.LogContext;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrphanStorageCleanupService {

    private static final String CONTENT_THUMBNAIL_PREFIX = "contents/";
    private static final String USER_PROFILE_IMAGE_PREFIX = "users/";
    private static final int MAX_ITERATIONS = 10000;

    private final StorageProvider storageProvider;
    private final JpaOrphanStorageCleanupRepository orphanStorageCleanupRepository;
    private final OrphanStorageCleanupProperties props;
    private final OrphanStorageCleanupPolicyResolver policyResolver;

    public int cleanupContentThumbnails() {
        int totalDeleted = 0;
        int iterations = 0;
        int chunkSize = policyResolver.chunkSize(props.contentThumbnail());
        String startAfter = null;

        while (iterations < MAX_ITERATIONS) {
            List<String> storagePaths = storageProvider.listObjects(CONTENT_THUMBNAIL_PREFIX, startAfter, chunkSize);

            if (storagePaths.isEmpty()) {
                break;
            }

            for (String path : storagePaths) {
                if (orphanStorageCleanupRepository.findOneByThumbnailPath(path) == null) {
                    storageProvider.delete(path);
                    totalDeleted++;
                    LogContext.with("service", "orphanStorageCleanup")
                        .and("type", "contentThumbnail")
                        .and("path", path)
                        .debug("Deleted orphan file");
                }
            }

            if (storagePaths.size() < chunkSize) {
                break;
            }

            startAfter = storagePaths.getLast();
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "orphanStorageCleanup")
                .and("type", "contentThumbnail")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalDeleted", totalDeleted)
                .warn("Reached max iterations");
        }

        return totalDeleted;
    }

    public int cleanupUserProfileImages() {
        int totalDeleted = 0;
        int iterations = 0;
        int chunkSize = policyResolver.chunkSize(props.userProfileImage());
        String startAfter = null;

        while (iterations < MAX_ITERATIONS) {
            List<String> storagePaths = storageProvider.listObjects(USER_PROFILE_IMAGE_PREFIX, startAfter, chunkSize);

            if (storagePaths.isEmpty()) {
                break;
            }

            for (String path : storagePaths) {
                if (orphanStorageCleanupRepository.findOneByProfileImagePath(path) == null) {
                    storageProvider.delete(path);
                    totalDeleted++;
                    LogContext.with("service", "orphanStorageCleanup")
                        .and("type", "userProfileImage")
                        .and("path", path)
                        .debug("Deleted orphan file");
                }
            }

            if (storagePaths.size() < chunkSize) {
                break;
            }

            startAfter = storagePaths.getLast();
            iterations++;
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "orphanStorageCleanup")
                .and("type", "userProfileImage")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalDeleted", totalDeleted)
                .warn("Reached max iterations");
        }

        return totalDeleted;
    }
}
