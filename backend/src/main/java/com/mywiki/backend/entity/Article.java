package com.mywiki.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
