package com.mopl.jpa.repository.setting;

import com.mopl.domain.repository.setting.SystemConfigRepository;
import com.mopl.jpa.entity.setting.SystemSettingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SystemConfigRepositoryImpl implements SystemConfigRepository {

    private final JpaSystemConfigRepository jpaSystemConfigRepository;

    @Override
    public Optional<String> findValue(String key) {
        return jpaSystemConfigRepository.findById(key)
            .map(SystemSettingEntity::getValue);
    }
}
