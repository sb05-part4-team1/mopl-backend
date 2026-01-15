package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    PlaylistRepositoryImpl.class,
    PlaylistEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("PlaylistRepositoryImpl 슬라이스 테스트")
class PlaylistRepositoryImplTest {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel savedOwner;

    @BeforeEach
    void setUp() {
        savedOwner = userRepository.save(
            UserModel.create(
                UserModel.AuthProvider.EMAIL,
                "owner@example.com",
                "플레이리스트 소유자",
                "encodedPassword"
            )
        );
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 플레이리스트 ID로 조회하면 PlaylistModel을 반환한다")
        void withExistingId_returnsPlaylistModel() {
            // given
            PlaylistModel savedPlaylist = playlistRepository.save(
                PlaylistModel.create(
                    savedOwner,
                    "내 플레이리스트",
                    "좋아하는 영화 모음"
                )
            );

            // when
            Optional<PlaylistModel> foundPlaylist = playlistRepository.findById(savedPlaylist.getId());

            // then
            assertThat(foundPlaylist).isPresent();
            assertThat(foundPlaylist.get().getId()).isEqualTo(savedPlaylist.getId());
            assertThat(foundPlaylist.get().getTitle()).isEqualTo("내 플레이리스트");
            assertThat(foundPlaylist.get().getDescription()).isEqualTo("좋아하는 영화 모음");
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<PlaylistModel> foundPlaylist = playlistRepository.findById(nonExistingId);

            // then
            assertThat(foundPlaylist).isEmpty();
        }

        @Test
        @DisplayName("조회 결과에 Owner 정보가 포함된다")
        void withExistingId_includesOwnerInfo() {
            // given
            PlaylistModel savedPlaylist = playlistRepository.save(
                PlaylistModel.create(
                    savedOwner,
                    "내 플레이리스트",
                    "설명"
                )
            );

            // when
            Optional<PlaylistModel> foundPlaylist = playlistRepository.findById(savedPlaylist.getId());

            // then
            assertThat(foundPlaylist).isPresent();
            UserModel owner = foundPlaylist.get().getOwner();
            assertThat(owner).isNotNull();
            assertThat(owner.getId()).isEqualTo(savedOwner.getId());
            assertThat(owner.getName()).isEqualTo("플레이리스트 소유자");
            assertThat(owner.getEmail()).isEqualTo("owner@example.com");
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 플레이리스트를 저장하고 반환한다")
        void withNewPlaylist_savesAndReturnsPlaylist() {
            // given
            PlaylistModel playlistModel = PlaylistModel.create(
                savedOwner,
                "내 플레이리스트",
                "좋아하는 영화 모음"
            );

            // when
            PlaylistModel savedPlaylist = playlistRepository.save(playlistModel);

            // then
            assertThat(savedPlaylist.getId()).isNotNull();
            assertThat(savedPlaylist.getTitle()).isEqualTo("내 플레이리스트");
            assertThat(savedPlaylist.getDescription()).isEqualTo("좋아하는 영화 모음");
            assertThat(savedPlaylist.getOwner()).isNotNull();
            assertThat(savedPlaylist.getOwner().getId()).isEqualTo(savedOwner.getId());
            assertThat(savedPlaylist.getCreatedAt()).isNotNull();
            assertThat(savedPlaylist.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("기존 플레이리스트를 업데이트하고 반환한다")
        void withExistingPlaylist_updatesAndReturnsPlaylist() {
            // given
            PlaylistModel playlistModel = PlaylistModel.create(
                savedOwner,
                "원래 제목",
                "원래 설명"
            );
            PlaylistModel savedPlaylist = playlistRepository.save(playlistModel);

            // when
            savedPlaylist.update("수정된 제목", "수정된 설명");
            PlaylistModel updatedPlaylist = playlistRepository.save(savedPlaylist);

            // then
            assertThat(updatedPlaylist.getId()).isEqualTo(savedPlaylist.getId());
            assertThat(updatedPlaylist.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedPlaylist.getDescription()).isEqualTo("수정된 설명");
        }

        @Test
        @DisplayName("설명이 null인 플레이리스트도 저장 가능하다")
        void withNullDescription_savesSuccessfully() {
            // given
            PlaylistModel playlistModel = PlaylistModel.create(
                savedOwner,
                "설명 없는 플레이리스트",
                null
            );

            // when
            PlaylistModel savedPlaylist = playlistRepository.save(playlistModel);

            // then
            assertThat(savedPlaylist.getId()).isNotNull();
            assertThat(savedPlaylist.getTitle()).isEqualTo("설명 없는 플레이리스트");
            assertThat(savedPlaylist.getDescription()).isNull();
        }
    }
}
