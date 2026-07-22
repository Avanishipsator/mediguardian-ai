package com.mediguardian.core.common;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    UNAUTHORIZED("ERR_401", "Unauthorized access"),
    FORBIDDEN("ERR_403", "Access denied"),
    NOT_FOUND("ERR_404", "Resource not found"),
    VALIDATION_ERROR("ERR_400", "Validation failed"),
    INTERNAL_SERVER_ERROR("ERR_500", "An unexpected error occurred"),
    CONCURRENT_MODIFICATION("ERR_409", "Data was modified by another user. Please refresh and try again.");

    private final String code;
    private final String defaultMessage;

    ErrorCodes(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
