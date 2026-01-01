package com.mopl.domain.exception.auth;

public class UnauthorizedException extends AuthException {

    private static final String MESSAGE = "인증에 실패했습니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }
}
