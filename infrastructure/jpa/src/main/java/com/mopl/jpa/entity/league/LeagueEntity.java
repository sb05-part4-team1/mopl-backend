package com.mopl.jpa.entity.league;

import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "leagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class LeagueEntity extends BaseEntity {

    @Column(name = "league_id", nullable = false, unique = true)
    private Long leagueId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sport;
}
