package com.mopl.domain.exception.user;

import java.util.Map;

public class DuplicateEmailException extends UserException {

    public static final String MESSAGE = "해당 이메일로 가입된 사용자가 이미 존재합니다.";

    public DuplicateEmailException(String email) {
        super(MESSAGE, Map.of("email", email));
    }
}
