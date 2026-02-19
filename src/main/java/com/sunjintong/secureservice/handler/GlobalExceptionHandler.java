package com.sunjintong.secureservice.handler;
import com.sunjintong.secureservice.common.BizException;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1) 业务异常：你主动抛出的（如 USERNAME_EXISTS）
    @ExceptionHandler(BizException.class)
    public Result<Void> handleException(BizException e) {
        return Result.fail(e.getErrorCode(), e.getMessage());
    }
    // 2) @Valid 校验失败（RequestBody DTO校验）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? ErrorCode.PARAM_INVALID.getMessage()
                : e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return Result.fail(ErrorCode.PARAM_INVALID, msg);
    }
    // 3) @Validated 校验失败（如@PathVariable/@RequestParam校验）
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        return Result.fail(ErrorCode.PARAM_INVALID, e.getMessage());
    }
    // 4) 兜底异常（先简单处理，后面我们再加 INTERNAL_ERROR）
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.fail(ErrorCode.PARAM_INVALID, "invalid json");
    }
}
