package com.mopl.jpa.entity.tag;

import com.mopl.domain.model.tag.TagModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TagEntityMapper 단위 테스트")
class TagEntityMapperTest {

    private final TagEntityMapper mapper = new TagEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("TagEntity를 TagModel로 변환")
        void withTagEntity_returnsTagModel() {
            // given
            UUID id = UUID.randomUUID();
            TagEntity entity = TagEntity.builder()
                .id(id)
                .name("SF")
                .build();

            // when
            TagModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("SF");
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            TagModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("TagModel을 TagEntity로 변환")
        void withTagModel_returnsTagEntity() {
            // given
            UUID id = UUID.randomUUID();
            TagModel model = TagModel.builder()
                .id(id)
                .name("액션")
                .build();

            // when
            TagEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("액션");
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            TagModel originalModel = TagModel.builder()
                .id(UUID.randomUUID())
                .name("액션")
                .build();

            // when
            TagEntity entity = mapper.toEntity(originalModel);
            TagModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getName()).isEqualTo(originalModel.getName());
        }
    }
}
