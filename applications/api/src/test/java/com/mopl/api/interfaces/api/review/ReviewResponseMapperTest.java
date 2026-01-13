package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.fixture.ReviewModelFixture;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewResponseMapper 단위 테스트")
class ReviewResponseMapperTest {

    @InjectMocks
    private ReviewResponseMapper reviewResponseMapper;

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @Test
    @DisplayName("ReviewModel을 받아 ReviewResponse로 변환한다")
    void toResponse_mapsFieldsCorrectly() {
        // given
        ReviewModel reviewModel = ReviewModelFixture.create();

        // 어떤 UserModel이 들어오든(any) 무조건 이 Summary를 리턴해라 (유연한 Stubbing)
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
    void toResponse_checksAuthorMapping() {
        // given
        // 1. Fixture가 ReviewModel과 그 안의 UserModel(Author)까지 알아서 생성하게 둠
        ReviewModel reviewModel = ReviewModelFixture.create();

        // 2. 생성된 그 Author를 꺼냄 (이게 핵심!)
        UserModel generatedAuthor = reviewModel.getAuthor();

        UserSummary expectedSummary = new UserSummary(
            generatedAuthor.getId(), "테스터", "http://image.url"
        );

        // 3. "아까 그 Author가 들어오면 -> 이 Summary를 줘" 라고 설정
        given(userSummaryMapper.toSummary(generatedAuthor)).willReturn(expectedSummary);

        // when
        ReviewResponse response = reviewResponseMapper.toResponse(reviewModel);

        // then
        assertThat(response.author()).isNotNull();
        assertThat(response.author().userId()).isEqualTo(generatedAuthor.getId());
        assertThat(response.author().name()).isEqualTo("테스터");
    }
}
