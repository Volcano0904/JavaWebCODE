package com.campus.activityservice.exception;

/**
 * 活动服务自定义异常类
 */
public class ActivityException extends RuntimeException {
    public ActivityException(String message) {
        super(message);
    }

    public ActivityException(String message, Throwable cause) {
        super(message, cause);
    }
}