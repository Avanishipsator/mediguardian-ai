package com.mediguardian.core.exception;

import com.mediguardian.core.common.ErrorCodes;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorCodes errorCode;

    public BaseException(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCodes errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
}
