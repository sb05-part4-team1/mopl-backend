package com.mopl.jpa.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({JpaAutoConfig.class, JpaConfig.class, QuerydslConfig.class})
@DisplayName("JpaAutoConfig 테스트")
class JpaAutoConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("JpaAutoConfig가 빈으로 등록된다")
    void jpaAutoConfigBeanIsRegistered() {
        // when
        JpaAutoConfig jpaAutoConfig = applicationContext.getBean(JpaAutoConfig.class);

        // then
        assertThat(jpaAutoConfig).isNotNull();
    }

    @Test
    @DisplayName("JpaConfig가 Import된다")
    void importsJpaConfig() {
        // when/then
        assertThat(applicationContext.getBean(JpaConfig.class)).isNotNull();
    }

    @Test
    @DisplayName("QuerydslConfig가 Import된다")
    void importsQuerydslConfig() {
        // when/then
        assertThat(applicationContext.getBean(QuerydslConfig.class)).isNotNull();
    }
}
