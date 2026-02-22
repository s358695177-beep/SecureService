package com.sunjintong.secureservice.common;

public enum ErrorCode {
    SUCCESS(0,"success"),
    PARAM_INVALID(1001,"parameter invalid"),
    USERNAME_EXISTS(1002, "username already exists"),
    UNAUTHORIZED(1003,"unauthorized"),
    FORBIDDEN(1004,"forbidden"),
    INTERNAL_ERROR(5000, "internal error");
    private final int code;
    private final String message;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
