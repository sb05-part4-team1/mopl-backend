package com.mopl.test.support;

import com.mopl.test.container.MysqlContainerConfig;
import com.mopl.test.container.RedisContainerConfig;
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
 * Base annotation for integration tests that require MySQL and Redis containers.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @IntegrationTestSupport
 * class MyIntegrationTest {
 * 
 * @Autowired
 *            private MyRepository repository;
 *
 * @Test
 *       void testSomething() {
 *       // test with real database
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
@Import({MysqlContainerConfig.class, RedisContainerConfig.class})
public @interface IntegrationTestSupport {

}
