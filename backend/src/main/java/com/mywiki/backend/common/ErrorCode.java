package com.mywiki.backend.common;

public enum ErrorCode {

    ARTICLE_NOT_FOUND(10001, "文章不存在", 404),
    PARAM_INVALID(10002, "参数错误", 400),
    USERNAME_EXIST(10003, "用户名已存在", 400),
    LOGIN_FAILED(10004, "用户名或密码错误", 401),
    SYSTEM_ERROR(50000, "系统异常", 500),
    NO_PERMISSION(10005, "无权操作该文章", 403);

    private final Integer code;
    private final String message;
    private final Integer httpStatus;

    ErrorCode(Integer code, String message, Integer httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
