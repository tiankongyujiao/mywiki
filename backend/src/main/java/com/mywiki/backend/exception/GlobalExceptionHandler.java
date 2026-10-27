package com.mywiki.backend.exception;

import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e, HttpServletResponse response){
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            response.setStatus(errorCode.getHttpStatus());
            return Result.error(
                    errorCode.getCode(),
                    errorCode.getMessage()
            );
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return Result.error(
                400,
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        log.error("未处理异常", e);
        return Result.error(
                50000,
                "系统异常"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String message =
                e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return Result.error(
                10002,
                message
        );
    }
}
