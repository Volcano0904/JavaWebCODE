package com.campus.userservice.exception;

/**
 * 用户服务自定义异常类
 */
public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}