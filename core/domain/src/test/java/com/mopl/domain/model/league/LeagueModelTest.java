package com.mopl.domain.model.league;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LeagueModel 단위 테스트")
class LeagueModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 LeagueModel 생성")
        void withValidValues_createsLeagueModel() {
            // when
            LeagueModel league = LeagueModel.create(39L, "Premier League", "Football");

            // then
            assertThat(league.getLeagueId()).isEqualTo(39L);
            assertThat(league.getName()).isEqualTo("Premier League");
            assertThat(league.getSport()).isEqualTo("Football");
        }

        @Test
        @DisplayName("이름과 스포츠의 앞뒤 공백이 제거된다")
        void withWhitespace_stripsWhitespace() {
            // when
            LeagueModel league = LeagueModel.create(140L, "  La Liga  ", "  Football  ");

            // then
            assertThat(league.getName()).isEqualTo("La Liga");
            assertThat(league.getSport()).isEqualTo("Football");
        }
    }
}
