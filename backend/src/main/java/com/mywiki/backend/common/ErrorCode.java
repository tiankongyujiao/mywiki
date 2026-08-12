package com.mywiki.backend.common;

public enum ErrorCode {

    ARTICLE_NOT_FOUND(10001, "文章不存在"),
    PARAM_INVALID(10002, "参数错误"),
    SYSTEM_ERROR(50000, "系统异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
