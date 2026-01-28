package com.mopl.test.support;

import com.mopl.test.container.MysqlContainerConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base annotation for JPA repository tests with MySQL Testcontainer.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @JpaIntegrationTestSupport
 * 
 * @Import(JpaUserRepository.class)
 *                                  class JpaUserRepositoryTest {
 * @Autowired
 *            private JpaUserRepository repository;
 *
 * @Autowired
 *            private TestEntityManager em;
 *
 * @Test
 *       void testSave() {
 *       // test with real MySQL
 *       }
 *       }
 *       }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@DataJpaTest(showSql = false)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(MysqlContainerConfig.class)
public @interface JpaIntegrationTestSupport {

}
