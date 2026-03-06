package com.sunjintong.secureservice.common;

import java.time.Instant;

public class Result<T> {
    private final int code;
    private final String message;
    private final T data;
    //private String traceId;
    private final String timestamp;
    private Result(int code, String message, T data, String timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        //this.traceId = traceId;
        this.timestamp = timestamp;
    }
    public static <T> Result<T> ok(T data){
        return new Result<T>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(),data, Instant.now().toString());
    }
    public static <T> Result<T> fail(ErrorCode errorCode){
        return new Result<T>(errorCode.getCode(), errorCode.getMessage(),null, Instant.now().toString());
    }
    public static <T> Result<T> fail(ErrorCode errorCode, String message){
        return new Result<T>(errorCode.getCode(), message,null, Instant.now().toString());
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
    /*public String getTraceId() {
        return traceId;
    }*/
    public String getTimestamp() {
        return timestamp;
    }
}
