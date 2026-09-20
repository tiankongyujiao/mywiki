package com.mywiki.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private Long userId;

    private String username;

    private String content;

    private LocalDateTime createdAt;
}
