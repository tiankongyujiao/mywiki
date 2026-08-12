package com.mywiki.backend.exception;

import com.mywiki.backend.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e){
        return Result.error(
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        return Result.error(
                50000,
                "系统异常"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e){
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
