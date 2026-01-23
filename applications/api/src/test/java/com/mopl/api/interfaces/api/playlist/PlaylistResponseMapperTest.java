// package com.mopl.api.interfaces.api.playlist;
//
// import com.mopl.api.interfaces.api.content.ContentSummaryMapper;
// import com.mopl.api.interfaces.api.user.UserSummaryMapper;
// import com.mopl.domain.fixture.ContentModelFixture;
// import com.mopl.domain.fixture.PlaylistModelFixture;
// import com.mopl.domain.model.content.ContentModel;
// import com.mopl.domain.model.playlist.PlaylistModel;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
//
// import java.util.List;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// @DisplayName("PlaylistResponseMapper 단위 테스트")
// class PlaylistResponseMapperTest {
//
//     private final UserSummaryMapper userSummaryMapper = new UserSummaryMapper();
//     private final ContentSummaryMapper contentSummaryMapper = new ContentSummaryMapper();
//     private final PlaylistResponseMapper mapper = new PlaylistResponseMapper(
//         userSummaryMapper,
//         contentSummaryMapper
//     );
//
//     @Nested
//     @DisplayName("toResponse(PlaylistModel)")
//     class ToResponseSimpleTest {
//
//         @Test
//         @DisplayName("PlaylistModel을 기본값으로 PlaylistResponse로 변환")
//         void withPlaylistModel_returnsPlaylistResponseWithDefaults() {
//             // given
//             PlaylistModel model = PlaylistModelFixture.create();
//
//             // when
//             PlaylistResponse result = mapper.toResponse(model);
//
//             // then
//             assertThat(result.id()).isEqualTo(model.getId());
//             assertThat(result.owner().userId()).isEqualTo(model.getOwner().getId());
//             assertThat(result.owner().name()).isEqualTo(model.getOwner().getName());
//             assertThat(result.title()).isEqualTo(model.getTitle());
//             assertThat(result.description()).isEqualTo(model.getDescription());
//             assertThat(result.updatedAt()).isEqualTo(model.getUpdatedAt());
//             assertThat(result.subscriberCount()).isZero();
//             assertThat(result.subscribedByMe()).isFalse();
//             assertThat(result.contents()).isEmpty();
//         }
//     }
//
//     @Nested
//     @DisplayName("toResponse(PlaylistModel, subscriberCount, subscribedByMe, contentModels)")
//     class ToResponseFullTest {
//
//         @Test
//         @DisplayName("모든 파라미터를 포함하여 PlaylistResponse로 변환")
//         void withAllParams_returnsFullPlaylistResponse() {
//             // given
//             PlaylistModel model = PlaylistModelFixture.create();
//             long subscriberCount = 100L;
//             boolean subscribedByMe = true;
//             List<ContentModel> contentModels = List.of(
//                 ContentModelFixture.create(),
//                 ContentModelFixture.create()
//             );
//
//             // when
//             PlaylistResponse result = mapper.toResponse(
//                 model,
//                 subscriberCount,
//                 subscribedByMe,
//                 contentModels
//             );
//
//             // then
//             assertThat(result.id()).isEqualTo(model.getId());
//             assertThat(result.owner().userId()).isEqualTo(model.getOwner().getId());
//             assertThat(result.title()).isEqualTo(model.getTitle());
//             assertThat(result.description()).isEqualTo(model.getDescription());
//             assertThat(result.updatedAt()).isEqualTo(model.getUpdatedAt());
//             assertThat(result.subscriberCount()).isEqualTo(subscriberCount);
//             assertThat(result.subscribedByMe()).isTrue();
//             assertThat(result.contents()).hasSize(2);
//         }
//
//         @Test
//         @DisplayName("콘텐츠 목록이 null이면 빈 리스트로 변환")
//         void withNullContents_returnsEmptyContentsList() {
//             // given
//             PlaylistModel model = PlaylistModelFixture.create();
//
//             // when
//             PlaylistResponse result = mapper.toResponse(
//                 model,
//                 10L,
//                 true,
//                 null
//             );
//
//             // then
//             assertThat(result.contents()).isEmpty();
//         }
//     }
// }
