package com.mopl.domain.repository.user;

import java.util.Optional;

public interface TemporaryPasswordRepository {

    void save(String email, String encodedPassword);

    Optional<String> findByEmail(String email);

    void deleteByEmail(String email);
}
