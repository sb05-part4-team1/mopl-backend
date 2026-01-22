package com.mopl.domain.support.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CacheName 단위 테스트")
class CacheNameTest {

    @Test
    @DisplayName("all()에 모든 캐시 이름 상수를 포함")
    void all_containsAllDeclaredConstants() {
        Set<String> allSet = Set.of(CacheName.all());

        Arrays.stream(CacheName.class.getDeclaredFields())
            .filter(this::isStringConstant)
            .forEach(field -> {
                String value = getFieldValue(field);
                assertThat(allSet)
                    .as("all()에 %s 상수가 누락됨", field.getName())
                    .contains(value);
            });
    }

    private boolean isStringConstant(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers)
            && Modifier.isFinal(modifiers)
            && field.getType() == String.class;
    }

    private String getFieldValue(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field: " + field.getName(), e);
        }
    }
}
