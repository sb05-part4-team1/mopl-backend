package com.mopl.test.support;

import com.mopl.test.container.KafkaContainerConfig;
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
 * Base annotation for Kafka integration tests with Testcontainer.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @KafkaIntegrationTestSupport
 * class KafkaEventPublisherTest {
 * 
 * @Autowired
 *            private KafkaTemplate<String, Object> kafkaTemplate;
 *
 * @Test
 *       void testPublish() {
 *       // test with real Kafka
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
@Import(KafkaContainerConfig.class)
public @interface KafkaIntegrationTestSupport {

}
