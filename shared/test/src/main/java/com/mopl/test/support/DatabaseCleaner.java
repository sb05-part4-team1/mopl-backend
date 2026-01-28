package com.mopl.test.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Utility class to clean database tables between tests.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @Autowired
 * private DatabaseCleaner databaseCleaner;
 *
 * @BeforeEach
 * void setUp() {
 *     databaseCleaner.clear();
 * }
 * }</pre>
 */
@Component
public class DatabaseCleaner {

    private static final Set<String> EXCLUDED_TABLES = Set.of(
        "flyway_schema_history",
        "shedlock"
    );

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @Transactional
    public void clear() {
        entityManager.flush();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (String tableName : getTableNames()) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    private List<String> getTableNames() {
        if (tableNames == null) {
            tableNames = entityManager.getMetamodel().getEntities().stream()
                .map(this::extractTableName)
                .filter(tableName -> !EXCLUDED_TABLES.contains(tableName.toLowerCase()))
                .toList();
        }
        return tableNames;
    }

    private String extractTableName(EntityType<?> entity) {
        Table tableAnnotation = entity.getJavaType().getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isBlank()) {
            return tableAnnotation.name();
        }
        return toSnakeCase(entity.getName());
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
