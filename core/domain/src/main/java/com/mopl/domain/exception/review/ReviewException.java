package com.mopl.domain.exception.review;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public class ReviewException extends MoplException {

    protected ReviewException(String message) {
        super(message);
    }

    protected ReviewException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
