package com.mywiki.backend.exception;

import com.mywiki.backend.common.ErrorCode;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
