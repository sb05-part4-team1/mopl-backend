package com.mopl.dto.review;

import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.dto.user.UserSummary;
import com.mopl.dto.user.UserSummaryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewResponseMapper 단위 테스트")
class ReviewResponseMapperTest {

    @InjectMocks
    private ReviewResponseMapper reviewResponseMapper;

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("ReviewModel을 받아 ReviewResponse로 변환한다")
        void withValidModel_mapsFieldsCorrectly() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.create();

            UserSummary expectedUserSummary = new UserSummary(
                UUID.randomUUID(), "테스터", "http://image.com"
            );
            given(userSummaryMapper.toSummary(any(UserModel.class))).willReturn(expectedUserSummary);

            // when
            ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

            // then
            assertThat(response.id()).isEqualTo(reviewModel.getId());
            assertThat(response.author()).isEqualTo(expectedUserSummary);

            verify(userSummaryMapper).toSummary(reviewModel.getAuthor());
        }

        @Test
        @DisplayName("작성자(Author) 정보가 정확히 매핑되는지 확인한다")
        void withValidModel_checksAuthorMapping() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.create();
            UserModel generatedAuthor = reviewModel.getAuthor();

            UserSummary expectedSummary = new UserSummary(
                generatedAuthor.getId(), "테스터", "http://image.url"
            );

            given(userSummaryMapper.toSummary(generatedAuthor)).willReturn(expectedSummary);

            // when
            ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

            // then
            assertThat(response.author()).isNotNull();
            assertThat(response.author().userId()).isEqualTo(generatedAuthor.getId());
            assertThat(response.author().name()).isEqualTo("테스터");
        }

        @Test
        @DisplayName("null ReviewModel이 입력되면 NullPointerException이 발생한다")
        @SuppressWarnings("ConstantConditions")
        void withNullModel_throwsNullPointerException() {
            // when & then
            assertThatThrownBy(() -> reviewResponseMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Author가 null이면 userSummaryMapper에 null이 전달된다")
        void withNullAuthor_passesNullToMapper() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.builder()
                .setNull("author")
                .sample();

            given(userSummaryMapper.toSummary(isNull())).willReturn(null);

            // when
            ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

            // then
            assertThat(response.author()).isNull();
            verify(userSummaryMapper).toSummary(isNull());
        }

        @Test
        @DisplayName("Content가 null이면 NullPointerException이 발생한다")
        void withNullContent_throwsNullPointerException() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.builder()
                .setNull("content")
                .sample();

            // when & then
            assertThatThrownBy(() -> reviewResponseMapper.toResponse(reviewModel))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("text와 rating이 정확히 매핑된다")
        void withValidModel_mapsTextAndRating() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.create();

            given(userSummaryMapper.toSummary(any(UserModel.class))).willReturn(null);

            // when
            ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

            // then
            assertThat(response.text()).isEqualTo(reviewModel.getText());
            assertThat(response.rating()).isEqualTo(reviewModel.getRating());
        }

        @Test
        @DisplayName("contentId가 정확히 매핑된다")
        void withValidModel_mapsContentId() {
            // given
            ReviewModel reviewModel = ReviewModelFixture.create();

            given(userSummaryMapper.toSummary(any(UserModel.class))).willReturn(null);

            // when
            ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

            // then
            assertThat(response.contentId()).isEqualTo(reviewModel.getContent().getId());
        }
    }
}
