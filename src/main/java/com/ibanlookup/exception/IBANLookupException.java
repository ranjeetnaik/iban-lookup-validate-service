package com.ibanlookup.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IBANLookupException extends RuntimeException {
    private final ErrorCode errorCode;

    public IBANLookupException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}