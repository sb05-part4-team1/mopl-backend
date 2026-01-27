package com.mopl.domain.repository.setting;

import java.util.Optional;

public interface SystemConfigRepository {

    Optional<String> findValue(String key);
}
