package com.campus.registrationservice.exception;

/**
 * 报名服务自定义异常类
 */
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}