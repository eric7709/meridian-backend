package com.app.meridian.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationFailure extends ApiException {

    public ValidationFailure(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
