package com.mopl.dto.playlist;

import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.dto.content.ContentSummary;
import com.mopl.dto.content.ContentSummaryMapper;
import com.mopl.dto.user.UserSummary;
import com.mopl.dto.user.UserSummaryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistResponseMapper 단위 테스트")
class PlaylistResponseMapperTest {

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @Mock
    private ContentSummaryMapper contentSummaryMapper;

    @InjectMocks
    private PlaylistResponseMapper mapper;

    @Nested
    @DisplayName("toResponse(PlaylistModel)")
    class ToResponseWithPlaylistModelOnlyTest {

        @Test
        @DisplayName("PlaylistModel만으로 PlaylistResponse 변환 (기본값 적용)")
        void withPlaylistModel_returnsPlaylistResponseWithDefaults() {
            // given
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            UserSummary ownerSummary = new UserSummary(
                playlistModel.getOwner().getId(),
                playlistModel.getOwner().getName(),
                "https://cdn.example.com/profile.jpg"
            );

            given(userSummaryMapper.toSummary(playlistModel.getOwner())).willReturn(ownerSummary);
            given(contentSummaryMapper.toSummaries(eq(Collections.emptyList()), any())).willReturn(List.of());

            // when
            PlaylistResponse result = mapper.toResponse(playlistModel);

            // then
            assertThat(result.id()).isEqualTo(playlistModel.getId());
            assertThat(result.owner()).isEqualTo(ownerSummary);
            assertThat(result.title()).isEqualTo(playlistModel.getTitle());
            assertThat(result.description()).isEqualTo(playlistModel.getDescription());
            assertThat(result.updatedAt()).isEqualTo(playlistModel.getUpdatedAt());
            assertThat(result.subscriberCount()).isEqualTo(playlistModel.getSubscriberCount());
            assertThat(result.subscribedByMe()).isFalse();
            assertThat(result.contents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toResponse(PlaylistModel, boolean, Collection, Map)")
    class ToResponseWithFullParametersTest {

        @Test
        @DisplayName("PlaylistModel과 연관 데이터를 PlaylistResponse로 변환")
        void withPlaylistModelAndRelatedData_returnsPlaylistResponse() {
            // given
            PlaylistModel playlistModel = PlaylistModelFixture.create();
            ContentModel content1 = ContentModelFixture.create();
            ContentModel content2 = ContentModelFixture.create();
            List<ContentModel> contentModels = List.of(content1, content2);
            Map<UUID, List<String>> tagsByContentId = Map.of(
                content1.getId(), List.of("SF", "Action"),
                content2.getId(), List.of("Drama")
            );
            boolean subscribedByMe = true;

            UserSummary ownerSummary = new UserSummary(
                playlistModel.getOwner().getId(),
                playlistModel.getOwner().getName(),
                "https://cdn.example.com/profile.jpg"
            );

            List<ContentSummary> contentSummaries = List.of(
                new ContentSummary(
                    content1.getId(),
                    content1.getType(),
                    content1.getTitle(),
                    content1.getDescription(),
                    "https://cdn.example.com/thumb1.jpg",
                    List.of("SF", "Action"),
                    content1.getAverageRating(),
                    content1.getReviewCount()
                ),
                new ContentSummary(
                    content2.getId(),
                    content2.getType(),
                    content2.getTitle(),
                    content2.getDescription(),
                    "https://cdn.example.com/thumb2.jpg",
                    List.of("Drama"),
                    content2.getAverageRating(),
                    content2.getReviewCount()
                )
            );

            given(userSummaryMapper.toSummary(playlistModel.getOwner())).willReturn(ownerSummary);
            given(contentSummaryMapper.toSummaries(contentModels, tagsByContentId)).willReturn(contentSummaries);

            // when
            PlaylistResponse result = mapper.toResponse(
                playlistModel,
                subscribedByMe,
                contentModels,
                tagsByContentId
            );

            // then
            assertThat(result.id()).isEqualTo(playlistModel.getId());
            assertThat(result.owner()).isEqualTo(ownerSummary);
            assertThat(result.title()).isEqualTo(playlistModel.getTitle());
            assertThat(result.description()).isEqualTo(playlistModel.getDescription());
            assertThat(result.updatedAt()).isEqualTo(playlistModel.getUpdatedAt());
            assertThat(result.subscriberCount()).isEqualTo(playlistModel.getSubscriberCount());
            assertThat(result.subscribedByMe()).isTrue();
            assertThat(result.contents()).hasSize(2);
            assertThat(result.contents()).isEqualTo(contentSummaries);
        }

        @Test
        @DisplayName("빈 콘텐츠 목록으로 변환")
        void withEmptyContents_returnsPlaylistResponseWithEmptyContents() {
            // given
            PlaylistModel playlistModel = PlaylistModelFixture.create();
            List<ContentModel> contentModels = Collections.emptyList();
            Map<UUID, List<String>> tagsByContentId = Map.of();
            boolean subscribedByMe = false;

            UserSummary ownerSummary = new UserSummary(
                playlistModel.getOwner().getId(),
                playlistModel.getOwner().getName(),
                "https://cdn.example.com/profile.jpg"
            );

            given(userSummaryMapper.toSummary(playlistModel.getOwner())).willReturn(ownerSummary);
            given(contentSummaryMapper.toSummaries(contentModels, tagsByContentId)).willReturn(List.of());

            // when
            PlaylistResponse result = mapper.toResponse(
                playlistModel,
                subscribedByMe,
                contentModels,
                tagsByContentId
            );

            // then
            assertThat(result.subscribedByMe()).isFalse();
            assertThat(result.contents()).isEmpty();
        }
    }
}
