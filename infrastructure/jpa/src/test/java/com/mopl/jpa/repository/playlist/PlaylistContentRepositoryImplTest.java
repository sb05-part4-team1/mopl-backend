package com.mopl.jpa.repository.playlist;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.content.ContentRepositoryImpl;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    PlaylistContentRepositoryImpl.class,
    PlaylistRepositoryImpl.class,
    PlaylistEntityMapper.class,
    ContentRepositoryImpl.class,
    ContentEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("PlaylistContentRepositoryImpl 슬라이스 테스트")
class PlaylistContentRepositoryImplTest {

    @Autowired
    private PlaylistContentRepository playlistContentRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaPlaylistContentRepository jpaPlaylistContentRepository;

    private PlaylistModel playlist;
    private ContentModel content;

    @BeforeEach
    void setUp() {
        UserModel owner = userRepository.save(
            UserModel.create(
                "owner@example.com",
                "소유자",
                "encodedPassword"
            )
        );

        playlist = playlistRepository.save(
            PlaylistModel.create(
                "테스트 플레이리스트",
                "설명",
                owner
            )
        );

        content = contentRepository.save(
            ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            )
        );
    }

    @Nested
    @DisplayName("findContentsByPlaylistId()")
    class FindContentsByPlaylistIdTest {

        @Test
        @DisplayName("플레이리스트의 콘텐츠 목록을 반환한다")
        void returnsContentList() {
            // given
            playlistContentRepository.save(playlist.getId(), content.getId());

            // when
            List<ContentModel> contents = playlistContentRepository.findContentsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(contents).hasSize(1);
            assertThat(contents.getFirst().getId()).isEqualTo(content.getId());
        }

        @Test
        @DisplayName("콘텐츠가 없으면 빈 목록을 반환한다")
        void whenNoContents_returnsEmptyList() {
            // when
            List<ContentModel> contents = playlistContentRepository.findContentsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(contents).isEmpty();
        }

        @Test
        @DisplayName("여러 콘텐츠가 있으면 모두 반환한다")
        void whenMultipleContents_returnsAll() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(playlist.getId(), anotherContent.getId());

            // when
            List<ContentModel> contents = playlistContentRepository.findContentsByPlaylistId(
                playlist.getId()
            );

            // then
            assertThat(contents).hasSize(2);
            assertThat(contents).extracting(ContentModel::getId)
                .containsExactlyInAnyOrder(content.getId(), anotherContent.getId());
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 빈 목록을 반환한다")
        void whenPlaylistNotExists_returnsEmptyList() {
            // given
            UUID nonExistentPlaylistId = UUID.randomUUID();

            // when
            List<ContentModel> contents = playlistContentRepository.findContentsByPlaylistId(
                nonExistentPlaylistId
            );

            // then
            assertThat(contents).isEmpty();
        }
    }

    @Nested
    @DisplayName("findContentsByPlaylistIdIn()")
    class FindContentsByPlaylistIdInTest {

        @Test
        @DisplayName("여러 플레이리스트의 콘텐츠를 Map으로 반환한다")
        void returnsContentsAsMap() {
            // given
            UserModel anotherOwner = userRepository.save(
                UserModel.create("another@example.com", "다른 소유자", "encodedPassword")
            );
            PlaylistModel anotherPlaylist = playlistRepository.save(
                PlaylistModel.create("다른 플레이리스트", "설명", anotherOwner)
            );
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(anotherPlaylist.getId(), anotherContent.getId());

            // when
            Map<UUID, List<ContentModel>> result = playlistContentRepository.findContentsByPlaylistIdIn(
                List.of(playlist.getId(), anotherPlaylist.getId())
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(playlist.getId())).hasSize(1);
            assertThat(result.get(playlist.getId()).getFirst().getId()).isEqualTo(content.getId());
            assertThat(result.get(anotherPlaylist.getId())).hasSize(1);
            assertThat(result.get(anotherPlaylist.getId()).getFirst().getId()).isEqualTo(anotherContent.getId());
        }

        @Test
        @DisplayName("빈 플레이리스트 ID 목록이 주어지면 빈 Map을 반환한다")
        void whenEmptyPlaylistIds_returnsEmptyMap() {
            // when
            Map<UUID, List<ContentModel>> result = playlistContentRepository.findContentsByPlaylistIdIn(
                List.of()
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("콘텐츠가 없는 플레이리스트는 결과에 포함되지 않는다")
        void whenNoContents_playlistNotInResult() {
            // when
            Map<UUID, List<ContentModel>> result = playlistContentRepository.findContentsByPlaylistIdIn(
                List.of(playlist.getId())
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("한 플레이리스트에 여러 콘텐츠가 있으면 모두 포함된다")
        void whenMultipleContentsInPlaylist_allIncluded() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(playlist.getId(), anotherContent.getId());

            // when
            Map<UUID, List<ContentModel>> result = playlistContentRepository.findContentsByPlaylistIdIn(
                List.of(playlist.getId())
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(playlist.getId())).hasSize(2);
            assertThat(result.get(playlist.getId())).extracting(ContentModel::getId)
                .containsExactlyInAnyOrder(content.getId(), anotherContent.getId());
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("플레이리스트에 콘텐츠가 존재하면 true를 반환한다")
        void whenContentExists_returnsTrue() {
            // given
            playlistContentRepository.save(playlist.getId(), content.getId());

            // when
            boolean exists = playlistContentRepository.exists(playlist.getId(), content.getId());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("플레이리스트에 콘텐츠가 존재하지 않으면 false를 반환한다")
        void whenContentNotExists_returnsFalse() {
            // when
            boolean exists = playlistContentRepository.exists(playlist.getId(), content.getId());

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 플레이리스트 ID로 조회하면 false를 반환한다")
        void whenDifferentPlaylistId_returnsFalse() {
            // given
            playlistContentRepository.save(playlist.getId(), content.getId());
            UUID differentPlaylistId = UUID.randomUUID();

            // when
            boolean exists = playlistContentRepository.exists(differentPlaylistId, content.getId());

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 콘텐츠 ID로 조회하면 false를 반환한다")
        void whenDifferentContentId_returnsFalse() {
            // given
            playlistContentRepository.save(playlist.getId(), content.getId());
            UUID differentContentId = UUID.randomUUID();

            // when
            boolean exists = playlistContentRepository.exists(playlist.getId(), differentContentId);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("플레이리스트에 콘텐츠를 추가한다")
        void save_addsContentToPlaylist() {
            // when
            playlistContentRepository.save(playlist.getId(), content.getId());

            // then
            List<PlaylistContentEntity> playlistContents = jpaPlaylistContentRepository.findAll();
            assertThat(playlistContents).hasSize(1);
            assertThat(playlistContents.getFirst().getPlaylist().getId()).isEqualTo(playlist.getId());
            assertThat(playlistContents.getFirst().getContent().getId()).isEqualTo(content.getId());
        }

        @Test
        @DisplayName("같은 플레이리스트에 여러 콘텐츠를 추가할 수 있다")
        void save_multipleContentsToSamePlaylist() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );

            // when
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(playlist.getId(), anotherContent.getId());

            // then
            List<PlaylistContentEntity> playlistContents = jpaPlaylistContentRepository.findAll();
            assertThat(playlistContents).hasSize(2);
        }

        @Test
        @DisplayName("같은 콘텐츠를 여러 플레이리스트에 추가할 수 있다")
        void save_sameContentToMultiplePlaylists() {
            // given
            UserModel anotherOwner = userRepository.save(
                UserModel.create(
                    "another@example.com",
                    "다른 소유자",
                    "encodedPassword"
                )
            );

            PlaylistModel anotherPlaylist = playlistRepository.save(
                PlaylistModel.create(
                    "다른 플레이리스트",
                    "설명",
                    anotherOwner
                )
            );

            // when
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(anotherPlaylist.getId(), content.getId());

            // then
            List<PlaylistContentEntity> playlistContents = jpaPlaylistContentRepository.findAll();
            assertThat(playlistContents).hasSize(2);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("플레이리스트에서 특정 콘텐츠를 삭제하면 true를 반환한다")
        void delete_removesContentFromPlaylist_returnsTrue() {
            // given
            playlistContentRepository.save(playlist.getId(), content.getId());
            assertThat(jpaPlaylistContentRepository.findAll()).hasSize(1);

            // when
            boolean deleted = playlistContentRepository.deleteByPlaylistIdAndContentId(playlist.getId(), content.getId());

            // then
            assertThat(deleted).isTrue();
            assertThat(jpaPlaylistContentRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("다른 콘텐츠는 유지된다")
        void delete_keepsOtherContents() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(playlist.getId(), anotherContent.getId());
            assertThat(jpaPlaylistContentRepository.findAll()).hasSize(2);

            // when
            boolean deleted = playlistContentRepository.deleteByPlaylistIdAndContentId(playlist.getId(), content.getId());

            // then
            assertThat(deleted).isTrue();
            List<PlaylistContentEntity> remaining = jpaPlaylistContentRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getContent().getId()).isEqualTo(anotherContent.getId());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 삭제 시 false를 반환한다")
        void delete_nonExistentContent_returnsFalse() {
            // when
            boolean deleted = playlistContentRepository.deleteByPlaylistIdAndContentId(playlist.getId(), content.getId());

            // then
            assertThat(deleted).isFalse();
            assertThat(jpaPlaylistContentRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("다른 플레이리스트의 동일 콘텐츠는 유지된다")
        void delete_keepsContentInOtherPlaylist() {
            // given
            UserModel anotherOwner = userRepository.save(
                UserModel.create(
                    "another@example.com",
                    "다른 소유자",
                    "encodedPassword"
                )
            );

            PlaylistModel anotherPlaylist = playlistRepository.save(
                PlaylistModel.create(
                    "다른 플레이리스트",
                    "설명",
                    anotherOwner
                )
            );

            playlistContentRepository.save(playlist.getId(), content.getId());
            playlistContentRepository.save(anotherPlaylist.getId(), content.getId());
            assertThat(jpaPlaylistContentRepository.findAll()).hasSize(2);

            // when
            boolean deleted = playlistContentRepository.deleteByPlaylistIdAndContentId(playlist.getId(), content.getId());

            // then
            assertThat(deleted).isTrue();
            List<PlaylistContentEntity> remaining = jpaPlaylistContentRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getPlaylist().getId()).isEqualTo(anotherPlaylist.getId());
        }
    }
}
