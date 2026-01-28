package com.mopl.jpa.support.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JdbcBatchInsertHelper {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public <T> int[] batchInsert(
        String sql,
        List<T> items,
        Function<T, MapSqlParameterSource> parameterMapper
    ) {
        if (items == null || items.isEmpty()) {
            return new int[0];
        }

        SqlParameterSource[] batchParams = items.stream()
            .map(parameterMapper)
            .toArray(SqlParameterSource[]::new);

        return jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
