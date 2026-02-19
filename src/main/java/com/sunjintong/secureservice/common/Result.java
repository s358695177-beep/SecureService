package com.sunjintong.secureservice.common;

import ch.qos.logback.core.spi.ErrorCodes;

public class Result<T> {
    private final int code;
    private final String message;
    private final T data;
    Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public static <T> Result<T> ok(T data){
        return new Result<T>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(),data);
    }
    public static <T> Result<T> fail(ErrorCode errorCode){
        return new Result<T>(errorCode.getCode(), errorCode.getMessage(),null);
    }
    public static <T> Result<T> fail(ErrorCode errorCode, String message){
        return new Result<T>(errorCode.getCode(), message,null);
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public T getData() {
        return data;
    }
}
