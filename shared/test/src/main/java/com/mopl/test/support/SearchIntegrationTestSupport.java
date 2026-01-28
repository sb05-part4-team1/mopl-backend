package com.mopl.test.support;

import com.mopl.test.container.ElasticsearchContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base annotation for Elasticsearch integration tests with Testcontainer.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @SearchIntegrationTestSupport
 * class ElasticsearchContentRepositoryTest {
 * 
 * @Autowired
 *            private ContentSearchRepository repository;
 *
 * @Test
 *       void testSearch() {
 *       // test with real Elasticsearch
 *       }
 *       }
 *       }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(ElasticsearchContainerConfig.class)
public @interface SearchIntegrationTestSupport {

}
