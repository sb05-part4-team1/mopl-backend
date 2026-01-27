package com.mopl.jpa.repository.setting;

import com.mopl.jpa.entity.setting.SystemSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSystemConfigRepository extends JpaRepository<SystemSettingEntity, String> {
}
