package com.mopl.kafka.dlq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DlqConfig 단위 테스트")
class DlqConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DlqConfig.class));

    @Nested
    @DisplayName("dlqAlertPublisher()")
    class DlqAlertPublisherTest {

        @Test
        @DisplayName("DlqAlertPublisher 빈이 없을 때 LoggingDlqAlertPublisher 생성")
        void whenNoBeanExists_createsLoggingPublisher() {
            contextRunner.run(context -> {
                assertThat(context).hasSingleBean(DlqAlertPublisher.class);
                assertThat(context.getBean(DlqAlertPublisher.class))
                    .isInstanceOf(LoggingDlqAlertPublisher.class);
            });
        }

        @Test
        @DisplayName("DlqAlertPublisher 빈이 이미 있으면 새로 생성하지 않음")
        void whenBeanAlreadyExists_doesNotCreateNew() {
            contextRunner
                .withUserConfiguration(CustomPublisherConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(DlqAlertPublisher.class);
                    assertThat(context.getBean(DlqAlertPublisher.class))
                        .isInstanceOf(CustomDlqAlertPublisher.class);
                });
        }
    }

    @Configuration
    static class CustomPublisherConfig {

        @Bean
        DlqAlertPublisher customDlqAlertPublisher() {
            return new CustomDlqAlertPublisher();
        }
    }

    static class CustomDlqAlertPublisher implements DlqAlertPublisher {

        @Override
        public void publish(DlqEvent event) {
            // custom implementation
        }
    }
}
