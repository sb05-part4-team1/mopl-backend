package com.mopl.batch.cleanup.orphanstorage.service;

import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupProperties;
import com.mopl.batch.cleanup.orphanstorage.config.OrphanStorageCleanupProperties.PolicyProperties;
import com.mopl.jpa.repository.orphanstorage.JpaOrphanStorageCleanupRepository;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrphanStorageCleanupService 단위 테스트")
class OrphanStorageCleanupServiceTest {

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private JpaOrphanStorageCleanupRepository orphanStorageCleanupRepository;

    @Mock
    private OrphanStorageCleanupProperties props;

    @Mock
    private OrphanStorageCleanupPolicyResolver policyResolver;

    private OrphanStorageCleanupService service;

    private static final int CHUNK_SIZE = 100;

    @BeforeEach
    void setUp() {
        service = new OrphanStorageCleanupService(
            storageProvider,
            orphanStorageCleanupRepository,
            props,
            policyResolver
        );
    }

    @Nested
    @DisplayName("cleanupContentThumbnails()")
    class CleanupContentThumbnailsTest {

        @Test
        @DisplayName("스토리지에 파일이 없으면 0을 반환한다")
        void returnsZeroWhenNoFilesInStorage() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.contentThumbnail()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
            when(storageProvider.listObjects(eq("contents/"), any(), anyInt()))
                .thenReturn(Collections.emptyList());

            int result = service.cleanupContentThumbnails();

            assertThat(result).isZero();
            verify(storageProvider, never()).delete(anyString());
        }

        @Test
        @DisplayName("DB에 존재하는 파일은 삭제하지 않는다")
        void doesNotDeleteFilesExistingInDb() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.contentThumbnail()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<String> storagePaths = List.of("contents/thumb1.jpg", "contents/thumb2.jpg");
            when(storageProvider.listObjects(eq("contents/"), any(), anyInt()))
                .thenReturn(storagePaths)
                .thenReturn(Collections.emptyList());
            when(orphanStorageCleanupRepository.findOneByThumbnailPath("contents/thumb1.jpg"))
                .thenReturn(1);
            when(orphanStorageCleanupRepository.findOneByThumbnailPath("contents/thumb2.jpg"))
                .thenReturn(1);

            int result = service.cleanupContentThumbnails();

            assertThat(result).isZero();
            verify(storageProvider, never()).delete(anyString());
        }

        @Test
        @DisplayName("DB에 존재하지 않는 orphan 파일을 삭제한다")
        void deletesOrphanFiles() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.contentThumbnail()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<String> storagePaths = List.of("contents/orphan1.jpg", "contents/orphan2.jpg");
            when(storageProvider.listObjects(eq("contents/"), any(), anyInt()))
                .thenReturn(storagePaths)
                .thenReturn(Collections.emptyList());
            when(orphanStorageCleanupRepository.findOneByThumbnailPath(anyString()))
                .thenReturn(null);

            int result = service.cleanupContentThumbnails();

            assertThat(result).isEqualTo(2);
            verify(storageProvider).delete("contents/orphan1.jpg");
            verify(storageProvider).delete("contents/orphan2.jpg");
        }

        @Test
        @DisplayName("여러 페이지에 걸쳐 orphan 파일을 삭제한다")
        void deletesOrphanFilesAcrossMultiplePages() {
            PolicyProperties policy = new PolicyProperties(2);
            when(props.contentThumbnail()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(2);

            List<String> page1 = List.of("contents/orphan1.jpg", "contents/orphan2.jpg");
            List<String> page2 = List.of("contents/orphan3.jpg");
            when(storageProvider.listObjects(eq("contents/"), any(), eq(2)))
                .thenReturn(page1)
                .thenReturn(page2)
                .thenReturn(Collections.emptyList());
            when(orphanStorageCleanupRepository.findOneByThumbnailPath(anyString()))
                .thenReturn(null);

            int result = service.cleanupContentThumbnails();

            assertThat(result).isEqualTo(3);
            verify(storageProvider, times(3)).delete(anyString());
        }
    }

    @Nested
    @DisplayName("cleanupUserProfileImages()")
    class CleanupUserProfileImagesTest {

        @Test
        @DisplayName("스토리지에 파일이 없으면 0을 반환한다")
        void returnsZeroWhenNoFilesInStorage() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.userProfileImage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
            when(storageProvider.listObjects(eq("users/"), any(), anyInt()))
                .thenReturn(Collections.emptyList());

            int result = service.cleanupUserProfileImages();

            assertThat(result).isZero();
            verify(storageProvider, never()).delete(anyString());
        }

        @Test
        @DisplayName("DB에 존재하는 파일은 삭제하지 않는다")
        void doesNotDeleteFilesExistingInDb() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.userProfileImage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<String> storagePaths = List.of("users/profile1.jpg");
            when(storageProvider.listObjects(eq("users/"), any(), anyInt()))
                .thenReturn(storagePaths)
                .thenReturn(Collections.emptyList());
            when(orphanStorageCleanupRepository.findOneByProfileImagePath("users/profile1.jpg"))
                .thenReturn(1);

            int result = service.cleanupUserProfileImages();

            assertThat(result).isZero();
            verify(storageProvider, never()).delete(anyString());
        }

        @Test
        @DisplayName("DB에 존재하지 않는 orphan 프로필 이미지를 삭제한다")
        void deletesOrphanProfileImages() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
            when(props.userProfileImage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<String> storagePaths = List.of("users/orphan1.jpg", "users/orphan2.jpg");
            when(storageProvider.listObjects(eq("users/"), any(), anyInt()))
                .thenReturn(storagePaths)
                .thenReturn(Collections.emptyList());
            when(orphanStorageCleanupRepository.findOneByProfileImagePath(anyString()))
                .thenReturn(null);

            int result = service.cleanupUserProfileImages();

            assertThat(result).isEqualTo(2);
            verify(storageProvider).delete("users/orphan1.jpg");
            verify(storageProvider).delete("users/orphan2.jpg");
        }
    }
}
