package com.mj.epayement.core.exception;

import lombok.Getter;


public class RestException extends RuntimeException {

    @Getter
    private final ErrorCodes errorCodes;

    public RestException(String message, ErrorCodes errorCodes) {
        super(message);
        this.errorCodes = errorCodes;
    }
}
