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

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> void batchInsert(
        String sql,
        List<T> items,
        Function<T, MapSqlParameterSource> parameterMapper
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }

        SqlParameterSource[] batchParams = items.stream()
            .map(parameterMapper)
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
