package com.mopl.dto.follow;

import com.mopl.domain.fixture.FollowModelFixture;
import com.mopl.domain.model.follow.FollowModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FollowResponseMapper 단위 테스트")
class FollowResponseMapperTest {

    private final FollowResponseMapper mapper = new FollowResponseMapper();

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("FollowModel을 FollowResponse로 변환")
        void withFollowModel_returnsFollowResponse() {
            // given
            FollowModel followModel = FollowModelFixture.create();

            // when
            FollowResponse result = mapper.toResponse(followModel);

            // then
            assertThat(result.id()).isEqualTo(followModel.getId());
            assertThat(result.followeeId()).isEqualTo(followModel.getFolloweeId());
            assertThat(result.followerId()).isEqualTo(followModel.getFollowerId());
        }
    }
}
