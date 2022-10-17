package com.mj.epayement.core.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mj.epayement.core.exception.RestException;


@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorDto> handlerException(RestException exception, WebRequest webRequest) {
        final HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        final var errordto = ErrorDto.builder()
                .code(exception.getErrorCodes())
                .httpCode(badRequest.value())
                .message(exception.getMessage())
                .build();
        return new ResponseEntity<>(errordto, badRequest);
    }

}
