package com.mediguardian.core.exception;

import com.mediguardian.core.common.ErrorCodes;

public class BusinessException extends BaseException {
    public BusinessException(String message, ErrorCodes errorCode) {
        super(message, errorCode);
    }
    
    public BusinessException(ErrorCodes errorCode) {
        super(errorCode);
    }
}
