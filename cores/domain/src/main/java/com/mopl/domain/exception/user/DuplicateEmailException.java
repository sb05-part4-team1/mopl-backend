package com.mopl.domain.exception.user;

import java.util.Map;

public class DuplicateEmailException extends UserException {

    public DuplicateEmailException(String email) {
        super(UserErrorCode.DUPLICATE_EMAIL, Map.of("email", email));
    }
}
