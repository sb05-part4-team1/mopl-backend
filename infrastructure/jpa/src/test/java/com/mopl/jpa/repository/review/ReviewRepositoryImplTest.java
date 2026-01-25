package com.mopl.jpa.repository.review;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.content.ContentRepositoryImpl;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    ReviewRepositoryImpl.class,
    ReviewEntityMapper.class,
    ContentRepositoryImpl.class,
    ContentEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("ReviewRepositoryImpl 슬라이스 테스트")
class ReviewRepositoryImplTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    private ContentModel savedContent;
    private UserModel savedAuthor;

    @BeforeEach
    void setUp() {
        savedContent = contentRepository.save(
            ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈을 다룬 SF 영화",
                "https://mopl.com/inception.png"
            )
        );

        savedAuthor = userRepository.save(
            UserModel.create(
                "reviewer@example.com",
                "리뷰어",
                "encodedPassword"
            )
        );
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 리뷰 ID로 조회하면 ReviewModel을 반환한다")
        void withExistingId_returnsReviewModel() {
            // given
            ReviewModel savedReview = reviewRepository.save(
                ReviewModel.create(
                    savedContent,
                    savedAuthor,
                    "정말 재미있는 영화입니다!",
                    4.5
                )
            );

            // when
            Optional<ReviewModel> foundReview = reviewRepository.findById(savedReview.getId());

            // then
            assertThat(foundReview).isPresent();
            assertThat(foundReview.get().getId()).isEqualTo(savedReview.getId());
            assertThat(foundReview.get().getText()).isEqualTo("정말 재미있는 영화입니다!");
            assertThat(foundReview.get().getRating()).isEqualTo(4.5);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<ReviewModel> foundReview = reviewRepository.findById(nonExistingId);

            // then
            assertThat(foundReview).isEmpty();
        }

        @Test
        @DisplayName("조회 결과에 Content ID 정보가 포함된다")
        void withExistingId_includesContentId() {
            // given
            ReviewModel savedReview = reviewRepository.save(
                ReviewModel.create(
                    savedContent,
                    savedAuthor,
                    "좋은 영화",
                    4.0
                )
            );

            // when
            Optional<ReviewModel> foundReview = reviewRepository.findById(savedReview.getId());

            // then
            assertThat(foundReview).isPresent();
            ContentModel content = foundReview.get().getContent();
            assertThat(content).isNotNull();
            assertThat(content.getId()).isEqualTo(savedContent.getId());
        }

        @Test
        @DisplayName("조회 결과에 Author ID 정보가 포함된다")
        void withExistingId_includesAuthorId() {
            // given
            ReviewModel savedReview = reviewRepository.save(
                ReviewModel.create(
                    savedContent,
                    savedAuthor,
                    "좋은 영화",
                    4.0
                )
            );

            // when
            Optional<ReviewModel> foundReview = reviewRepository.findById(savedReview.getId());

            // then
            assertThat(foundReview).isPresent();
            UserModel author = foundReview.get().getAuthor();
            assertThat(author).isNotNull();
            assertThat(author.getId()).isEqualTo(savedAuthor.getId());
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 리뷰를 저장하고 반환한다")
        void withNewReview_savesAndReturnsReview() {
            // given
            ReviewModel reviewModel = ReviewModel.create(
                savedContent,
                savedAuthor,
                "인셉션은 놀란 감독의 걸작입니다.",
                5.0
            );

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull();
            assertThat(savedReview.getText()).isEqualTo("인셉션은 놀란 감독의 걸작입니다.");
            assertThat(savedReview.getRating()).isEqualTo(5.0);
            assertThat(savedReview.getContent()).isNotNull();
            assertThat(savedReview.getContent().getId()).isEqualTo(savedContent.getId());
            assertThat(savedReview.getAuthor()).isNotNull();
            assertThat(savedReview.getAuthor().getId()).isEqualTo(savedAuthor.getId());
            assertThat(savedReview.getCreatedAt()).isNotNull();
            assertThat(savedReview.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("기존 리뷰를 업데이트하고 반환한다")
        void withExistingReview_updatesAndReturnsReview() {
            // given
            ReviewModel reviewModel = ReviewModel.create(
                savedContent,
                savedAuthor,
                "원래 리뷰 내용",
                3.0
            );
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // when
            ReviewModel modifiedReview = savedReview.update("수정된 리뷰 내용", 4.5);
            ReviewModel updatedReview = reviewRepository.save(modifiedReview);

            // then
            assertThat(updatedReview.getId()).isEqualTo(savedReview.getId());
            assertThat(updatedReview.getText()).isEqualTo("수정된 리뷰 내용");
            assertThat(updatedReview.getRating()).isEqualTo(4.5);
        }

        @Test
        @DisplayName("빈 텍스트 리뷰도 저장 가능하다")
        void withEmptyText_savesSuccessfully() {
            // given
            ReviewModel reviewModel = ReviewModel.create(
                savedContent,
                savedAuthor,
                "",
                4.0
            );

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull();
            assertThat(savedReview.getText()).isEmpty();
            assertThat(savedReview.getRating()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("최소 평점(0.0)으로 저장 가능하다")
        void withMinRating_savesSuccessfully() {
            // given
            ReviewModel reviewModel = ReviewModel.create(
                savedContent,
                savedAuthor,
                "별로였습니다",
                0.0
            );

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull();
            assertThat(savedReview.getRating()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("최대 평점(5.0)으로 저장 가능하다")
        void withMaxRating_savesSuccessfully() {
            // given
            ReviewModel reviewModel = ReviewModel.create(
                savedContent,
                savedAuthor,
                "최고의 영화!",
                5.0
            );

            // when
            ReviewModel savedReview = reviewRepository.save(reviewModel);

            // then
            assertThat(savedReview.getId()).isNotNull();
            assertThat(savedReview.getRating()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("existsByContentIdAndAuthorId()")
    class ExistsByContentIdAndAuthorIdTest {

        @Test
        @DisplayName("동일한 콘텐츠와 작성자로 리뷰가 존재하면 true를 반환한다")
        void withExistingReview_returnsTrue() {
            // given
            reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰 내용", 4.0)
            );

            // when
            boolean exists = reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("동일한 콘텐츠와 작성자로 리뷰가 없으면 false를 반환한다")
        void withNoReview_returnsFalse() {
            // when
            boolean exists = reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 작성자의 리뷰가 있어도 false를 반환한다")
        void withDifferentAuthor_returnsFalse() {
            // given
            UserModel anotherAuthor = userRepository.save(
                UserModel.create("another@example.com", "다른 리뷰어", "encodedPassword")
            );
            reviewRepository.save(
                ReviewModel.create(savedContent, anotherAuthor, "다른 리뷰", 3.0)
            );

            // when
            boolean exists = reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 리뷰는 존재하지 않는 것으로 처리한다")
        void withDeletedReview_returnsFalse() {
            // given
            ReviewModel review = reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰 내용", 4.0)
            );
            review.delete();
            reviewRepository.save(review);

            // when
            boolean exists = reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            );

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteByIdIn()")
    class DeleteByIdInTest {

        @Test
        @DisplayName("ID 목록에 해당하는 활성 리뷰를 삭제하고 삭제된 개수를 반환한다")
        void withActiveReviewIds_deletesAndReturnsCount() {
            // given
            ReviewModel review1 = reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰1", 4.0)
            );

            UserModel author2 = userRepository.save(
                UserModel.create("author2@example.com", "작성자2", "encodedPassword")
            );
            ReviewModel review2 = reviewRepository.save(
                ReviewModel.create(savedContent, author2, "리뷰2", 3.0)
            );

            List<UUID> idsToDelete = List.of(review1.getId(), review2.getId());

            // when
            int deletedCount = reviewRepository.deleteByIdIn(idsToDelete);

            // then
            assertThat(deletedCount).isEqualTo(2);
            assertThat(reviewRepository.findById(review1.getId())).isEmpty();
            assertThat(reviewRepository.findById(review2.getId())).isEmpty();
        }

        @Test
        @DisplayName("빈 목록이면 0을 반환한다")
        void withEmptyList_returnsZero() {
            // when
            int deletedCount = reviewRepository.deleteByIdIn(List.of());

            // then
            assertThat(deletedCount).isZero();
        }

        @Test
        @DisplayName("존재하지 않는 ID가 포함되어 있으면 존재하는 것만 삭제한다")
        void withNonExistingIds_deletesOnlyExisting() {
            // given
            ReviewModel review = reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰", 4.0)
            );

            List<UUID> idsToDelete = List.of(review.getId(), UUID.randomUUID());

            // when
            int deletedCount = reviewRepository.deleteByIdIn(idsToDelete);

            // then
            assertThat(deletedCount).isEqualTo(1);
            assertThat(reviewRepository.findById(review.getId())).isEmpty();
        }

        @Test
        @DisplayName("soft deleted된 리뷰를 hard delete한다 (통합 테스트 필요)")
        void withSoftDeletedReviews_hardDeletes() {
            // This test requires MySQL integration test
            // because @SQLRestriction prevents Spring Data JPA
            // from finding soft-deleted records
        }
    }

    @Nested
    @DisplayName("softDeleteByContentIdIn()")
    class SoftDeleteByContentIdInTest {

        @Test
        @DisplayName("콘텐츠 ID 목록에 해당하는 리뷰를 soft delete하고 개수를 반환한다")
        void withContentIds_softDeletesAndReturnsCount() {
            // given
            reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰1", 4.0)
            );

            UserModel author2 = userRepository.save(
                UserModel.create("author2@example.com", "작성자2", "encodedPassword")
            );
            reviewRepository.save(
                ReviewModel.create(savedContent, author2, "리뷰2", 3.0)
            );

            Instant now = Instant.now();

            // when
            int deletedCount = reviewRepository.softDeleteByContentIdIn(
                List.of(savedContent.getId()),
                now
            );

            // then
            assertThat(deletedCount).isEqualTo(2);
            assertThat(reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            )).isFalse();
        }

        @Test
        @DisplayName("이미 삭제된 리뷰는 영향받지 않는다")
        void withAlreadyDeletedReviews_doesNotAffect() {
            // given
            ReviewModel review = reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰", 4.0)
            );
            review.delete();
            reviewRepository.save(review);

            Instant now = Instant.now();

            // when
            int deletedCount = reviewRepository.softDeleteByContentIdIn(
                List.of(savedContent.getId()),
                now
            );

            // then
            assertThat(deletedCount).isZero();
        }

        @Test
        @DisplayName("빈 콘텐츠 ID 목록이면 0을 반환한다")
        void withEmptyContentIds_returnsZero() {
            // given
            reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰", 4.0)
            );

            Instant now = Instant.now();

            // when
            int deletedCount = reviewRepository.softDeleteByContentIdIn(List.of(), now);

            // then
            assertThat(deletedCount).isZero();
        }

        @Test
        @DisplayName("다른 콘텐츠의 리뷰는 영향받지 않는다")
        void withDifferentContent_doesNotAffect() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다른 영화",
                    "설명",
                    "thumbnail.png"
                )
            );

            reviewRepository.save(
                ReviewModel.create(savedContent, savedAuthor, "리뷰1", 4.0)
            );
            reviewRepository.save(
                ReviewModel.create(anotherContent, savedAuthor, "리뷰2", 3.0)
            );

            Instant now = Instant.now();

            // when
            int deletedCount = reviewRepository.softDeleteByContentIdIn(
                List.of(anotherContent.getId()),
                now
            );

            // then
            assertThat(deletedCount).isEqualTo(1);
            assertThat(reviewRepository.existsByContentIdAndAuthorId(
                savedContent.getId(),
                savedAuthor.getId()
            )).isTrue();
        }
    }
}
