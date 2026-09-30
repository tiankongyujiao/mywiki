package com.mywiki.backend.common;

public enum ErrorCode {

    ARTICLE_NOT_FOUND(10001, "文章不存在", 404),
    PARAM_INVALID(10002, "参数错误", 400),
    USERNAME_EXIST(10003, "用户名已存在", 400),
    LOGIN_FAILED(10004, "用户名或密码错误", 401),
    SYSTEM_ERROR(50000, "系统异常", 500),
    NO_PERMISSION(10005, "无权操作该文章", 403),
    USER_NOT_FOUND(10006, "用户不存在", 404),
    COMMENT_NOT_FOUND(10007, "评论不存在", 404),
    FORBIDDEN(10008, "无权操作该评论", 403),
    CATEGORY_NAME_EXIST(10009, "分类名称已存在", 400),
    CATEGORY_ADMIN_ONLY(10010, "只有管理员才能创建分类", 403),
    CATEGORY_NOT_FOUND(10011, "分类不存在", 400),
    CATEGORY_IN_USE(10012, "该分类下还有文章，无法删除", 400);



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
