package com.sunjintong.secureservice.handler;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1) 业务异常：你主动抛出的（如 USERNAME_EXISTS）
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>>  handleException(BizException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = switch (errorCode){
            case PARAM_INVALID ->  HttpStatus.BAD_REQUEST;
            case INTERNAL_ERROR ->   HttpStatus.INTERNAL_SERVER_ERROR;
            case USERNAME_EXISTS ->   HttpStatus.CONFLICT;
            default ->  HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Result.fail(e.getErrorCode(), e.getMessage()));
    }
    // 2) @Valid 校验失败（RequestBody DTO校验）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? ErrorCode.PARAM_INVALID.getMessage()
                : e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ErrorCode.PARAM_INVALID, msg));
    }
    // 3) @Validated 校验失败（如@PathVariable/@RequestParam校验）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ErrorCode.PARAM_INVALID, e.getMessage())) ;
    }
    // 4) 兜底异常（先简单处理，后面我们再加 INTERNAL_ERROR）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAny(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.INTERNAL_ERROR));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ErrorCode.PARAM_INVALID, "invalid json"));
    }
    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<Result<Void>> handleException(JWTVerificationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.fail(ErrorCode.UNAUTHORIZED, e.getMessage()));
    }
}
