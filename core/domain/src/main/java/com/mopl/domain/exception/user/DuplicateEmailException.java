package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class DuplicateEmailException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.DUPLICATE_EMAIL;

    private DuplicateEmailException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static DuplicateEmailException withEmail(String email) {
        return new DuplicateEmailException(Map.of("email", email));
    }
}
