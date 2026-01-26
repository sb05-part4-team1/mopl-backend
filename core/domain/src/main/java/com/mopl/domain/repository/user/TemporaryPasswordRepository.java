package com.mopl.domain.repository.user;

import java.util.Optional;

public interface TemporaryPasswordRepository {

    Optional<String> findByEmail(String email);

    void save(String email, String encodedPassword);

    void deleteByEmail(String email);
}
