package com.ibanlookup.exception;


import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected void handle(Exception e) {
        log.error("Unhandled exception", e);
    }

    @ExceptionHandler(IBANLookupException.class)
    @ResponseBody
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    protected ResponseEntity<PafServiceBusinessExceptionModel> handleIbanLookupException(IBANLookupException e) {
        log.error(e.getMessage(), e);
        return buildExceptionResponse(ResponseEntity.status(HttpStatus.SC_UNPROCESSABLE_ENTITY), HttpStatus.SC_UNPROCESSABLE_ENTITY, e.getErrorCode());
    }
}
