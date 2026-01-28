package com.mopl.jpa.repository.setting;

import com.mopl.domain.repository.setting.SystemConfigRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.setting.SystemSettingEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    SystemConfigRepositoryImpl.class
})
@DisplayName("SystemConfigRepositoryImpl 슬라이스 테스트")
class SystemConfigRepositoryImplTest {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        SystemSettingEntity setting1 = new SystemSettingEntity();
        java.lang.reflect.Field keyField;
        java.lang.reflect.Field valueField;
        try {
            keyField = SystemSettingEntity.class.getDeclaredField("key");
            valueField = SystemSettingEntity.class.getDeclaredField("value");
            keyField.setAccessible(true);
            valueField.setAccessible(true);
            keyField.set(setting1, "max.upload.size");
            valueField.set(setting1, "10485760");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entityManager.persist(setting1);

        SystemSettingEntity setting2 = new SystemSettingEntity();
        try {
            keyField = SystemSettingEntity.class.getDeclaredField("key");
            valueField = SystemSettingEntity.class.getDeclaredField("value");
            keyField.setAccessible(true);
            valueField.setAccessible(true);
            keyField.set(setting2, "api.version");
            valueField.set(setting2, "v1.0.0");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entityManager.persist(setting2);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findValue()")
    class FindValueTest {

        @Test
        @DisplayName("존재하는 설정 키로 조회하면 값을 반환한다")
        void withExistingKey_returnsValue() {
            // when
            Optional<String> value = systemConfigRepository.findValue("max.upload.size");

            // then
            assertThat(value).isPresent();
            assertThat(value.get()).isEqualTo("10485760");
        }

        @Test
        @DisplayName("존재하지 않는 설정 키로 조회하면 빈 Optional을 반환한다")
        void withNonExistingKey_returnsEmpty() {
            // when
            Optional<String> value = systemConfigRepository.findValue("non.existing.key");

            // then
            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("다른 설정 키도 정상적으로 조회된다")
        void withDifferentKey_returnsValue() {
            // when
            Optional<String> value = systemConfigRepository.findValue("api.version");

            // then
            assertThat(value).isPresent();
            assertThat(value.get()).isEqualTo("v1.0.0");
        }
    }
}
