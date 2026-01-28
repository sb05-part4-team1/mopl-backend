package com.mopl.jpa.support.batch;

import com.mopl.jpa.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({JpaConfig.class, JdbcBatchInsertHelper.class})
@DisplayName("JdbcBatchInsertHelper 테스트")
class JdbcBatchInsertHelperTest {

    @Autowired
    private JdbcBatchInsertHelper jdbcBatchInsertHelper;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("batchInsert()")
    class BatchInsertTest {

        @Test
        @DisplayName("여러 항목을 일괄 삽입한다")
        void withMultipleItems_insertsAll() {
            // given
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS test_table (
                    id INT PRIMARY KEY,
                    name VARCHAR(100)
                )
                """;
            jdbcTemplate.getJdbcTemplate().execute(createTableSql);

            List<TestItem> items = List.of(
                new TestItem(1, "Item1"),
                new TestItem(2, "Item2"),
                new TestItem(3, "Item3")
            );

            String insertSql = "INSERT INTO test_table (id, name) VALUES (:id, :name)";

            // when
            jdbcBatchInsertHelper.batchInsert(
                insertSql,
                items,
                item -> new MapSqlParameterSource()
                    .addValue("id", item.id())
                    .addValue("name", item.name())
            );

            // then
            Integer count = jdbcTemplate.getJdbcTemplate()
                .queryForObject("SELECT COUNT(*) FROM test_table", Integer.class);
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("빈 목록을 삽입하면 아무 작업도 수행하지 않는다")
        void withEmptyList_doesNothing() {
            // given
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS test_table2 (
                    id INT PRIMARY KEY,
                    name VARCHAR(100)
                )
                """;
            jdbcTemplate.getJdbcTemplate().execute(createTableSql);

            List<TestItem> emptyList = List.of();
            String insertSql = "INSERT INTO test_table2 (id, name) VALUES (:id, :name)";

            // when
            jdbcBatchInsertHelper.batchInsert(
                insertSql,
                emptyList,
                item -> new MapSqlParameterSource()
                    .addValue("id", item.id())
                    .addValue("name", item.name())
            );

            // then
            Integer count = jdbcTemplate.getJdbcTemplate()
                .queryForObject("SELECT COUNT(*) FROM test_table2", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("null 목록을 삽입하면 아무 작업도 수행하지 않는다")
        void withNullList_doesNothing() {
            // given
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS test_table3 (
                    id INT PRIMARY KEY,
                    name VARCHAR(100)
                )
                """;
            jdbcTemplate.getJdbcTemplate().execute(createTableSql);

            String insertSql = "INSERT INTO test_table3 (id, name) VALUES (:id, :name)";

            // when
            jdbcBatchInsertHelper.batchInsert(
                insertSql,
                null,
                item -> new MapSqlParameterSource()
                    .addValue("id", ((TestItem) item).id())
                    .addValue("name", ((TestItem) item).name())
            );

            // then
            Integer count = jdbcTemplate.getJdbcTemplate()
                .queryForObject("SELECT COUNT(*) FROM test_table3", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("단일 항목도 삽입할 수 있다")
        void withSingleItem_inserts() {
            // given
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS test_table4 (
                    id INT PRIMARY KEY,
                    name VARCHAR(100)
                )
                """;
            jdbcTemplate.getJdbcTemplate().execute(createTableSql);

            List<TestItem> items = List.of(new TestItem(1, "SingleItem"));
            String insertSql = "INSERT INTO test_table4 (id, name) VALUES (:id, :name)";

            // when
            jdbcBatchInsertHelper.batchInsert(
                insertSql,
                items,
                item -> new MapSqlParameterSource()
                    .addValue("id", item.id())
                    .addValue("name", item.name())
            );

            // then
            Integer count = jdbcTemplate.getJdbcTemplate()
                .queryForObject("SELECT COUNT(*) FROM test_table4", Integer.class);
            assertThat(count).isEqualTo(1);
        }
    }

    private record TestItem(int id, String name) {
    }
}
