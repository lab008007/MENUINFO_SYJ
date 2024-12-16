package com.aloha.menuinfo.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String errorCode;
    private final String message;
    
    public ApiException(String message, String errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public ApiException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.errorCode = errorCode;
    }
}
