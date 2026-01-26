package com.mopl.jpa.entity.league;

import com.mopl.domain.model.league.LeagueModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LeagueEntityMapper 단위 테스트")
class LeagueEntityMapperTest {

    private final LeagueEntityMapper mapper = new LeagueEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("LeagueEntity를 LeagueModel로 변환")
        void withLeagueEntity_returnsLeagueModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            LeagueEntity entity = LeagueEntity.builder()
                .id(id)
                .leagueId(4328L)
                .name("Premier League")
                .sport("Soccer")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            LeagueModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getLeagueId()).isEqualTo(4328L);
            assertThat(result.getName()).isEqualTo("Premier League");
            assertThat(result.getSport()).isEqualTo("Soccer");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            LeagueModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("LeagueModel을 LeagueEntity로 변환")
        void withLeagueModel_returnsLeagueEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            LeagueModel model = LeagueModel.builder()
                .id(id)
                .leagueId(4387L)
                .name("NBA")
                .sport("Basketball")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            LeagueEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getLeagueId()).isEqualTo(4387L);
            assertThat(result.getName()).isEqualTo("NBA");
            assertThat(result.getSport()).isEqualTo("Basketball");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            LeagueEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            LeagueModel originalModel = LeagueModel.builder()
                .id(id)
                .leagueId(4424L)
                .name("KBO League")
                .sport("Baseball")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            LeagueEntity entity = mapper.toEntity(originalModel);
            LeagueModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getLeagueId()).isEqualTo(originalModel.getLeagueId());
            assertThat(resultModel.getName()).isEqualTo(originalModel.getName());
            assertThat(resultModel.getSport()).isEqualTo(originalModel.getSport());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getDeletedAt()).isNull();
        }
    }
}
